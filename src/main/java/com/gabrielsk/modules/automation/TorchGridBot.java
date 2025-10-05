package com.gabrielsk.modules.automation;

import com.gabrielsk.GabrielSKAddon;
import com.gabrielsk.ai.legit.Humanizer;
import com.gabrielsk.ai.legit.Visibility;
import com.gabrielsk.pathfinding.AStarPathfinder;
import com.gabrielsk.pathfinding.PathfindingOptions;
import com.gabrielsk.utils.PlayerMovement;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Blocks;
import net.minecraft.block.LeavesBlock;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

/**
 * TorchGridBot: Evaluates block+sky light on a configurable grid, places torches where needed.
 */
public class TorchGridBot extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgLegit = settings.createGroup("Legit");

    private final Setting<Double> radius = sgGeneral.add(new DoubleSetting.Builder()
        .name("radius").description("Scan radius.")
        .defaultValue(16.0).min(8.0).max(48.0).sliderMax(64.0).build());

    private final Setting<Integer> gridStep = sgGeneral.add(new IntSetting.Builder()
        .name("grid-step").description("Grid spacing.")
        .defaultValue(6).min(3).max(12).sliderMax(16).build());

    private final Setting<Integer> minLight = sgGeneral.add(new IntSetting.Builder()
        .name("min-light").description("Place torch if combined light < this.")
        .defaultValue(8).min(0).max(15).sliderMax(15).build());

    // Movement
    private final Setting<Boolean> usePathfinding = sgGeneral.add(new BoolSetting.Builder()
        .name("use-pathfinding").description("Walk to dark grid points before placing.")
        .defaultValue(true).build());
    private final Setting<Double> walkSpeed = sgGeneral.add(new DoubleSetting.Builder()
        .name("walk-speed").description("Walking speed towards path nodes.")
        .defaultValue(0.14).min(0.05).max(0.3).sliderMax(0.3).visible(usePathfinding::get).build());

    // Legit
    private final Setting<Boolean> legitMode = sgLegit.add(new BoolSetting.Builder()
        .name("enable").description("Enable LOS and pacing.").defaultValue(true).build());
    private final Setting<Boolean> requireLos = sgLegit.add(new BoolSetting.Builder()
        .name("require-los").description("Require line of sight.")
        .defaultValue(true).visible(legitMode::get).build());
    private final Setting<Integer> placeDelayMs = sgLegit.add(new IntSetting.Builder()
        .name("place-delay-ms").description("Delay between placements.")
        .defaultValue(140).min(40).max(600).sliderMax(600).visible(legitMode::get).build());
    private final Setting<Double> placeJitter = sgLegit.add(new DoubleSetting.Builder()
        .name("place-jitter").description("Randomization (0-1)")
        .defaultValue(0.25).min(0.0).max(1.0).sliderMax(1.0).visible(legitMode::get).build());

    private long lastPlaceAt = 0L;
    private BlockPos currentTarget = null; // solid top where torch will be placed
    private java.util.List<BlockPos> path = java.util.Collections.emptyList();
    private int pathIndex = 0;

    public TorchGridBot() {
        super(GabrielSKAddon.CATEGORY, "torch-grid-bot", "Places torches on a grid based on light levels.");
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;

        FindItemResult torch = InvUtils.findInHotbar(Items.TORCH);
        if (!torch.found()) return;

        BlockPos origin = mc.player.getBlockPos();
        int r = (int) Math.floor(radius.get());
        int step = Math.max(3, gridStep.get());

        // If we have a walking path, continue walking
        if (usePathfinding.get() && !path.isEmpty() && pathIndex < path.size()) {
            walkTowardsNode(path.get(pathIndex));
            if (isNear(path.get(pathIndex), 0.75)) pathIndex++;
            return;
        }

        // If we have a target, check if we can place now
        if (currentTarget != null) {
            if (canPlaceHere(currentTarget)) {
                if (legitMode.get()) {
                    long now = System.currentTimeMillis();
                    long delay = Humanizer.reactionMs(placeDelayMs.get(), placeJitter.get());
                    if (now - lastPlaceAt < delay) return;
                    if (requireLos.get() && !Visibility.hasLineOfSight(mc, Vec3d.ofCenter(currentTarget.up()))) return;
                }
                // Place torch
                InvUtils.swap(torch.slot(), false);
                BlockHitResult bhr = new BlockHitResult(Vec3d.ofCenter(currentTarget), Direction.UP, currentTarget, false);
                mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, bhr);
                InvUtils.swapBack();
                lastPlaceAt = System.currentTimeMillis();
                currentTarget = null;
                path = java.util.Collections.emptyList();
                pathIndex = 0;
                return;
            }
            // If cannot place due to distance and we use pathfinding, attempt to build a path
            if (usePathfinding.get()) {
                buildPathToAdjacent(currentTarget);
                return;
            }
        }

        // Scan grid for next dark spot
        for (int dx = -r; dx <= r; dx += step) {
            for (int dz = -r; dz <= r; dz += step) {
                BlockPos p = origin.add(dx, 0, dz);
                BlockPos place = findSolidTop(p);
                if (place == null) continue;
                int blockLight = mc.world.getLightLevel(net.minecraft.world.LightType.BLOCK, place.up());
                int skyLight = mc.world.getLightLevel(net.minecraft.world.LightType.SKY, place.up());
                int combined = Math.max(blockLight, skyLight);
                if (combined >= minLight.get()) continue;
                if (!mc.world.getBlockState(place.up()).isAir()) continue;

                currentTarget = place;
                // If close enough, try to place immediately; otherwise pathfind
                if (!usePathfinding.get() || isNear(place, 4.5)) {
                    // Will be attempted next tick in the placement branch
                    return;
                } else {
                    buildPathToAdjacent(place);
                    return;
                }
            }
        }
    }

    private BlockPos findSolidTop(BlockPos start) {
        // Probe up and down a bit to find ground surface
        for (int dy = 3; dy >= -3; dy--) {
            BlockPos p = start.up(dy);
            if (!mc.world.getBlockState(p).isAir() && mc.world.getBlockState(p.up()).isAir()) {
                // Must be solid
                if (mc.world.getBlockState(p).isSolidBlock(mc.world, p) && !(mc.world.getBlockState(p).getBlock() instanceof LeavesBlock)) {
                    return p;
                }
            }
        }
        return null;
    }

    private boolean canPlaceHere(BlockPos place) {
        if (mc.player == null || mc.world == null) return false;
        if (!mc.world.getBlockState(place.up()).isAir()) return false;
        // Distance gate (reach about 4.5 blocks from eye to target center)
        return isNear(place, 4.5);
    }

    private void buildPathToAdjacent(BlockPos target) {
        java.util.List<BlockPos> adj = java.util.List.of(target.north(), target.south(), target.east(), target.west());
        BlockPos start = mc.player.getBlockPos();
        PathfindingOptions opts = PathfindingOptions.safe();
        java.util.List<BlockPos> best = java.util.Collections.emptyList();
        for (BlockPos a : adj) {
            java.util.List<BlockPos> p = AStarPathfinder.findPath(start, a, opts);
            if (!p.isEmpty() && (best.isEmpty() || p.size() < best.size())) best = p;
        }
        path = best;
        pathIndex = 0;
    }

    private void walkTowardsNode(BlockPos node) {
        if (mc.player == null) return;
        Vec3d target = Vec3d.ofCenter(node);
        // Rotate and walk
        PlayerMovement.humanRotate(mc.player, target);
        Vec3d diff = target.subtract(mc.player.getPos());
        Vec3d dir = new Vec3d(diff.x, 0, diff.z).normalize();
        if (Double.isFinite(dir.x) && Double.isFinite(dir.z)) {
            double speed = walkSpeed.get();
            Vec3d vel = mc.player.getVelocity();
            mc.player.setVelocity(dir.x * speed, vel.y, dir.z * speed);
            mc.player.velocityModified = true;
        }
    }

    private boolean isNear(BlockPos node, double radius) {
        Vec3d c = Vec3d.ofCenter(node);
        return mc.player.getPos().squaredDistanceTo(c) <= radius * radius;
    }
}
