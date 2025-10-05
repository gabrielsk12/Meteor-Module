package com.gabrielsk.modules.automation;

import com.gabrielsk.GabrielSKAddon;
import com.gabrielsk.ai.legit.Humanizer;
import com.gabrielsk.ai.legit.Visibility;
import com.gabrielsk.math.MathUtils;
import com.gabrielsk.pathfinding.AStarPathfinder;
import com.gabrielsk.pathfinding.PathfindingOptions;
import com.gabrielsk.utils.PlayerMovement;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Blocks;
import net.minecraft.block.CropBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import java.util.*;

/**
 * GardenBot: Walks between farm tiles using A* and harvests/replants mature crops.
 */
public class GardenBot extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgLegit = settings.createGroup("Legit");

    private final Setting<Double> radius = sgGeneral.add(new DoubleSetting.Builder()
        .name("radius").description("Garden radius to operate in.")
        .defaultValue(12.0).min(6.0).max(32.0).sliderMax(48.0).build());

    private final Setting<Integer> gridStep = sgGeneral.add(new IntSetting.Builder()
        .name("grid-step").description("Grid spacing when scanning garden.")
        .defaultValue(3).min(2).max(6).sliderMax(8).build());

    private final Setting<Boolean> replant = sgGeneral.add(new BoolSetting.Builder()
        .name("replant").description("Replant seeds after harvest.")
        .defaultValue(true).build());

    // Legit settings
    private final Setting<Boolean> legitMode = sgLegit.add(new BoolSetting.Builder()
        .name("enable").description("Enable human-like LOS, pacing, and movement.")
        .defaultValue(true).build());

    private final Setting<Boolean> requireLos = sgLegit.add(new BoolSetting.Builder()
        .name("require-los").description("Require LOS to crop before acting.")
        .defaultValue(true).visible(legitMode::get).build());

    private final Setting<Integer> actionDelayMs = sgLegit.add(new IntSetting.Builder()
        .name("action-delay-ms").description("Delay between actions (ms).")
        .defaultValue(120).min(40).max(600).sliderMax(600).visible(legitMode::get).build());

    private final Setting<Double> actionJitter = sgLegit.add(new DoubleSetting.Builder()
        .name("action-jitter").description("Randomization (0-1).")
        .defaultValue(0.25).min(0.0).max(1.0).sliderMax(1.0).visible(legitMode::get).build());

    private final Setting<Double> walkSpeed = sgLegit.add(new DoubleSetting.Builder()
        .name("walk-speed").description("Walking speed towards path nodes.")
        .defaultValue(0.14).min(0.05).max(0.3).sliderMax(0.3).visible(legitMode::get).build());

    private List<BlockPos> currentPath = Collections.emptyList();
    private int pathIndex = 0;
    private BlockPos currentTarget = null;
    private long lastActionAt = 0L;

    public GardenBot() {
        super(GabrielSKAddon.CATEGORY, "garden-bot", "Pathfinds through your farm to harvest/replant.");
    }

    @Override
    public void onActivate() {
        resetPath();
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;

        // If no target, find next mature crop in grid order
        if (currentTarget == null || !isValidCrop(currentTarget)) {
            currentTarget = findNextTarget();
            if (currentTarget != null) buildPathToAdjacent(currentTarget);
        }

        // Walk path if exists
        if (!currentPath.isEmpty() && pathIndex < currentPath.size()) {
            walkTowardsNode(currentPath.get(pathIndex));
            if (isNear(currentPath.get(pathIndex), 0.75)) pathIndex++;
            return;
        }

        if (currentTarget == null || !isValidCrop(currentTarget)) return;

        // At target: perform action with legit pacing
        if (legitMode.get()) {
            long now = System.currentTimeMillis();
            long delay = Humanizer.reactionMs(actionDelayMs.get(), actionJitter.get());
            if (now - lastActionAt < delay) return;
            if (requireLos.get() && !Visibility.hasLineOfSight(mc, Vec3d.ofCenter(currentTarget))) return;
        }

        // Face target and break
        PlayerMovement.humanRotate(mc.player, Vec3d.ofCenter(currentTarget));
        mc.interactionManager.attackBlock(currentTarget, Direction.UP);
        mc.player.swingHand(Hand.MAIN_HAND);

        // Replant
        if (replant.get() && mc.world.getBlockState(currentTarget.down()).getBlock() == Blocks.FARMLAND) {
            var seeds = meteordevelopment.meteorclient.utils.player.InvUtils.findInHotbar(Items.WHEAT_SEEDS, Items.CARROT, Items.POTATO);
            if (seeds.found()) {
                meteordevelopment.meteorclient.utils.player.InvUtils.swap(seeds.slot(), false);
                BlockHitResult bhr = new BlockHitResult(Vec3d.ofCenter(currentTarget), Direction.UP, currentTarget, false);
                mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, bhr);
                meteordevelopment.meteorclient.utils.player.InvUtils.swapBack();
            }
        }

        lastActionAt = System.currentTimeMillis();
        // Move on to find next
        currentTarget = null;
        resetPath();
    }

    private void resetPath() {
        currentPath = Collections.emptyList();
        pathIndex = 0;
    }

    private void buildPathToAdjacent(BlockPos target) {
        // Choose adjacent walkable spot
        List<BlockPos> adj = List.of(target.north(), target.south(), target.east(), target.west());
        BlockPos start = mc.player.getBlockPos();
        PathfindingOptions opts = PathfindingOptions.safe();
        List<BlockPos> best = Collections.emptyList();
        for (BlockPos a : adj) {
            List<BlockPos> p = AStarPathfinder.findPath(start, a, opts);
            if (!p.isEmpty() && (best.isEmpty() || p.size() < best.size())) best = p;
        }
        currentPath = best;
        pathIndex = 0;
    }

    private BlockPos findNextTarget() {
        BlockPos origin = mc.player.getBlockPos();
        int r = (int) Math.floor(radius.get());
        int step = Math.max(2, gridStep.get());
        BlockPos best = null;
        double bestDist = Double.MAX_VALUE;
        for (int dx = -r; dx <= r; dx += step) {
            for (int dz = -r; dz <= r; dz += step) {
                // Scan a small column around this grid point to find a mature crop
                for (int dy = -1; dy <= 2; dy++) {
                    BlockPos p = origin.add(dx, dy, dz);
                    if (!isValidCrop(p)) continue;
                    double d = mc.player.squaredDistanceTo(p.getX() + 0.5, p.getY() + 0.5, p.getZ() + 0.5);
                    if (d < bestDist) { best = p; bestDist = d; }
                }
            }
        }
        return best;
    }

    private boolean isValidCrop(BlockPos pos) {
        var state = mc.world.getBlockState(pos);
        var block = state.getBlock();
        if (!(block instanceof CropBlock crop)) return false;
        Integer age = state.get(CropBlock.AGE);
        return age != null && age >= crop.getMaxAge();
    }

    private void walkTowardsNode(BlockPos node) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;
        Vec3d target = Vec3d.ofCenter(node);
        // Rotate towards target
        PlayerMovement.humanRotate(mc.player, target);
        // Apply gentle velocity towards node when on ground
        Vec3d diff = target.subtract(mc.player.getPos());
        Vec3d dir = new Vec3d(diff.x, 0, diff.z).normalize();
        if (Double.isFinite(dir.x) && Double.isFinite(dir.z)) {
            double speed = walkSpeed.get();
            Vec3d vel = mc.player.getVelocity();
            // preserve vertical motion
            mc.player.setVelocity(dir.x * speed, vel.y, dir.z * speed);
            mc.player.velocityModified = true;
        }
    }

    private boolean isNear(BlockPos node, double radius) {
        Vec3d c = Vec3d.ofCenter(node);
        return mc.player.getPos().squaredDistanceTo(c) <= radius * radius;
    }
}
