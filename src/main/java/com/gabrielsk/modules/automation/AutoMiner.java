package com.gabrielsk.modules.automation;

import com.gabrielsk.GabrielSKAddon;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.*;

/**
 * AutoMiner: Intelligently mines valuable ores in range.
 * Focuses on diamonds, ancient debris, emeralds, gold, iron, copper, coal, redstone, lapis.
 */
public class AutoMiner extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgOres = settings.createGroup("Ores");

    private final Setting<Double> range = sgGeneral.add(new DoubleSetting.Builder()
        .name("range")
        .description("Mining range.")
        .defaultValue(4.5)
        .min(1.0)
        .max(6.0)
        .sliderMax(6.0)
        .build());

    private final Setting<Integer> delay = sgGeneral.add(new IntSetting.Builder()
        .name("delay")
        .description("Delay between mining blocks (ticks).")
        .defaultValue(2)
        .min(0)
        .max(20)
        .sliderMax(20)
        .build());

    private final Setting<Boolean> autoTool = sgGeneral.add(new BoolSetting.Builder()
        .name("auto-tool")
        .description("Automatically switch to best tool.")
        .defaultValue(true)
        .build());

    private final Setting<Boolean> diamond = sgOres.add(new BoolSetting.Builder()
        .name("diamond")
        .description("Mine diamond ore.")
        .defaultValue(true)
        .build());

    private final Setting<Boolean> ancientDebris = sgOres.add(new BoolSetting.Builder()
        .name("ancient-debris")
        .description("Mine ancient debris.")
        .defaultValue(true)
        .build());

    private final Setting<Boolean> emerald = sgOres.add(new BoolSetting.Builder()
        .name("emerald")
        .description("Mine emerald ore.")
        .defaultValue(true)
        .build());

    private final Setting<Boolean> gold = sgOres.add(new BoolSetting.Builder()
        .name("gold")
        .description("Mine gold ore.")
        .defaultValue(true)
        .build());

    private final Setting<Boolean> iron = sgOres.add(new BoolSetting.Builder()
        .name("iron")
        .description("Mine iron ore.")
        .defaultValue(true)
        .build());

    private final Setting<Boolean> copper = sgOres.add(new BoolSetting.Builder()
        .name("copper")
        .description("Mine copper ore.")
        .defaultValue(false)
        .build());

    private final Setting<Boolean> coal = sgOres.add(new BoolSetting.Builder()
        .name("coal")
        .description("Mine coal ore.")
        .defaultValue(false)
        .build());

    private final Setting<Boolean> redstone = sgOres.add(new BoolSetting.Builder()
        .name("redstone")
        .description("Mine redstone ore.")
        .defaultValue(false)
        .build());

    private final Setting<Boolean> lapis = sgOres.add(new BoolSetting.Builder()
        .name("lapis")
        .description("Mine lapis ore.")
        .defaultValue(false)
        .build());

    private BlockPos currentTarget = null;
    private int tickCounter = 0;

    public AutoMiner() {
        super(GabrielSKAddon.CATEGORY, "auto-miner", "Automatically mines valuable ores nearby.");
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;

        if (tickCounter > 0) {
            tickCounter--;
            return;
        }

        // If we have a target and it's still valid, keep mining it
        if (currentTarget != null) {
            if (!isValidOre(currentTarget) || mc.player.getPos().distanceTo(Vec3d.ofCenter(currentTarget)) > range.get()) {
                currentTarget = null;
            } else {
                mineBlock(currentTarget);
                tickCounter = delay.get();
                return;
            }
        }

        // Find new target
        currentTarget = findNearestOre();
        if (currentTarget != null) {
            mineBlock(currentTarget);
            tickCounter = delay.get();
        }
    }

    private BlockPos findNearestOre() {
        BlockPos playerPos = mc.player.getBlockPos();
        int r = (int) Math.ceil(range.get());
        BlockPos best = null;
        double bestDist = Double.MAX_VALUE;

        for (int x = -r; x <= r; x++) {
            for (int y = -r; y <= r; y++) {
                for (int z = -r; z <= r; z++) {
                    BlockPos pos = playerPos.add(x, y, z);
                    double dist = mc.player.getPos().distanceTo(Vec3d.ofCenter(pos));
                    if (dist > range.get()) continue;

                    if (isValidOre(pos) && dist < bestDist) {
                        bestDist = dist;
                        best = pos;
                    }
                }
            }
        }

        return best;
    }

    private boolean isValidOre(BlockPos pos) {
        Block block = mc.world.getBlockState(pos).getBlock();
        
        if (diamond.get() && (block == Blocks.DIAMOND_ORE || block == Blocks.DEEPSLATE_DIAMOND_ORE)) return true;
        if (ancientDebris.get() && block == Blocks.ANCIENT_DEBRIS) return true;
        if (emerald.get() && (block == Blocks.EMERALD_ORE || block == Blocks.DEEPSLATE_EMERALD_ORE)) return true;
        if (gold.get() && (block == Blocks.GOLD_ORE || block == Blocks.DEEPSLATE_GOLD_ORE || block == Blocks.NETHER_GOLD_ORE)) return true;
        if (iron.get() && (block == Blocks.IRON_ORE || block == Blocks.DEEPSLATE_IRON_ORE)) return true;
        if (copper.get() && (block == Blocks.COPPER_ORE || block == Blocks.DEEPSLATE_COPPER_ORE)) return true;
        if (coal.get() && (block == Blocks.COAL_ORE || block == Blocks.DEEPSLATE_COAL_ORE)) return true;
        if (redstone.get() && (block == Blocks.REDSTONE_ORE || block == Blocks.DEEPSLATE_REDSTONE_ORE)) return true;
        if (lapis.get() && (block == Blocks.LAPIS_ORE || block == Blocks.DEEPSLATE_LAPIS_ORE)) return true;

        return false;
    }

    private void mineBlock(BlockPos pos) {
        if (autoTool.get()) {
            meteordevelopment.meteorclient.utils.player.InvUtils.swap(
                meteordevelopment.meteorclient.utils.player.InvUtils.findFastestTool(mc.world.getBlockState(pos)).slot(),
                false
            );
        }

        mc.interactionManager.updateBlockBreakingProgress(pos, net.minecraft.util.math.Direction.UP);
        mc.player.swingHand(Hand.MAIN_HAND);
    }
}
