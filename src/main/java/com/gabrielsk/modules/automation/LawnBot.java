package com.gabrielsk.modules.automation;

import com.gabrielsk.GabrielSKAddon;
import com.gabrielsk.ai.legit.Humanizer;
import com.gabrielsk.ai.legit.Visibility;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.*;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;

/**
 * Lawnbot: clears grass/flowers/snow layers and optionally leaves/vines around the player.
 */
public class LawnBot extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgTargets = settings.createGroup("Targets");
    private final SettingGroup sgLegit = settings.createGroup("Legit");

    private final Setting<Double> radius = sgGeneral.add(new DoubleSetting.Builder()
        .name("radius").description("Scan radius in blocks.").defaultValue(6.0).min(2.0).max(12.0).sliderMax(16.0).build());

    private final Setting<Boolean> autoSwitch = sgGeneral.add(new BoolSetting.Builder()
        .name("auto-switch").description("Auto switch to best tool (shears/pickaxe).")
        .defaultValue(true).build());

    // Targets
    private final Setting<Boolean> grass = sgTargets.add(new BoolSetting.Builder()
        .name("tall-grass").description("Break tall grass.").defaultValue(true).build());
    private final Setting<Boolean> flowers = sgTargets.add(new BoolSetting.Builder()
        .name("flowers").description("Break flowers.").defaultValue(true).build());
    private final Setting<Boolean> snow = sgTargets.add(new BoolSetting.Builder()
        .name("snow-layer").description("Break snow layers.").defaultValue(true).build());
    private final Setting<Boolean> leaves = sgTargets.add(new BoolSetting.Builder()
        .name("leaves").description("Break leaves (uses shears).").defaultValue(false).build());
    private final Setting<Boolean> vines = sgTargets.add(new BoolSetting.Builder()
        .name("vines").description("Break vines (uses shears).").defaultValue(false).build());

    // Legit
    private final Setting<Boolean> legitMode = sgLegit.add(new BoolSetting.Builder()
        .name("enable").description("Enable human-like LOS and pacing.").defaultValue(true).build());
    private final Setting<Boolean> requireLos = sgLegit.add(new BoolSetting.Builder()
        .name("require-los").description("Require line of sight to target block.").defaultValue(true).visible(legitMode::get).build());
    private final Setting<Integer> actionDelayMs = sgLegit.add(new IntSetting.Builder()
        .name("action-delay-ms").description("Delay between breaks (ms).")
        .defaultValue(85).min(0).max(400).sliderMax(400).visible(legitMode::get).build());
    private final Setting<Double> actionJitter = sgLegit.add(new DoubleSetting.Builder()
        .name("action-jitter").description("Randomization (0-1)")
        .defaultValue(0.2).min(0.0).max(1.0).sliderMax(1.0).visible(legitMode::get).build());

    private long lastActionAt = 0L;

    public LawnBot() {
        super(GabrielSKAddon.CATEGORY, "lawn-bot", "Clears grass/flowers/leaves around you.");
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;

        BlockPos playerPos = mc.player.getBlockPos();
        List<BlockPos> candidates = new ArrayList<>();
        int r = (int) Math.floor(radius.get());

        for (int dx = -r; dx <= r; dx++) {
            for (int dy = -2; dy <= 2; dy++) {
                for (int dz = -r; dz <= r; dz++) {
                    BlockPos pos = playerPos.add(dx, dy, dz);
                    if (isTarget(pos)) candidates.add(pos);
                }
            }
        }

        if (candidates.isEmpty()) return;

        // Choose nearest
        BlockPos best = null;
        double bestDist = Double.MAX_VALUE;
        for (BlockPos pos : candidates) {
            double d = mc.player.squaredDistanceTo(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
            if (d < bestDist) { bestDist = d; best = pos; }
        }
        if (best == null) return;

        // Legit gating
        if (legitMode.get()) {
            long now = System.currentTimeMillis();
            long delay = Humanizer.reactionMs(actionDelayMs.get(), actionJitter.get());
            if (now - lastActionAt < delay) return;
            if (requireLos.get()) {
                if (!Visibility.hasLineOfSight(mc, Vec3d.ofCenter(best))) return;
            }
        }

        // Switch tool
        if (autoSwitch.get()) selectToolFor(best);

        // Break block
        Direction side = pickBestSide(best);
        mc.interactionManager.attackBlock(best, side);
        mc.player.swingHand(Hand.MAIN_HAND);
        lastActionAt = System.currentTimeMillis();
    }

    private boolean isTarget(BlockPos pos) {
        BlockState state = mc.world.getBlockState(pos);
        Block block = state.getBlock();
        if (block == Blocks.AIR) return false;

        if (grass.get() && (block instanceof TallPlantBlock || block == Blocks.GRASS || block == Blocks.FERN || block == Blocks.TALL_GRASS)) return true;
        if (flowers.get() && (block instanceof FlowerBlock || block instanceof FlowerbedBlock || block == Blocks.DANDELION || block == Blocks.POPPY)) return true;
        if (snow.get() && block == Blocks.SNOW) return true;
        if (leaves.get() && block instanceof LeavesBlock) return true;
        if (vines.get() && (block == Blocks.VINE || block == Blocks.CAVE_VINES || block == Blocks.CAVE_VINES_PLANT)) return true;
        return false;
    }

    private void selectToolFor(BlockPos pos) {
        Block block = mc.world.getBlockState(pos).getBlock();
        if (block instanceof LeavesBlock || block == Blocks.VINE || block == Blocks.COBWEB) {
            FindItemResult shears = InvUtils.findInHotbar(Items.SHEARS);
            if (shears.found()) InvUtils.swap(shears.slot(), false);
        }
    }

    private Direction pickBestSide(BlockPos pos) {
        // Prefer the side facing the player
        Vec3d eye = mc.player.getCameraPosVec(1.0f);
        Vec3d center = Vec3d.ofCenter(pos);
        Vec3d diff = center.subtract(eye);
        double ax = Math.abs(diff.x);
        double ay = Math.abs(diff.y);
        double az = Math.abs(diff.z);
        if (ax >= ay && ax >= az) return diff.x > 0 ? Direction.WEST : Direction.EAST;
        if (az >= ax && az >= ay) return diff.z > 0 ? Direction.NORTH : Direction.SOUTH;
        return diff.y > 0 ? Direction.DOWN : Direction.UP;
    }
}
