package com.gabrielsk.modules.automation;

import com.gabrielsk.GabrielSKAddon;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.PillarBlock;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import java.util.*;

/**
 * TreeChopper: Automatically chops down entire trees.
 */
public class TreeChopper extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Integer> range = sgGeneral.add(new IntSetting.Builder()
        .name("range")
        .description("Maximum tree height to chop.")
        .defaultValue(32)
        .min(8)
        .max(64)
        .sliderMax(64)
        .build());

    private final Setting<Boolean> autoTool = sgGeneral.add(new BoolSetting.Builder()
        .name("auto-tool")
        .description("Automatically switch to axe.")
        .defaultValue(true)
        .build());

    private final Setting<Boolean> leavesAlso = sgGeneral.add(new BoolSetting.Builder()
        .name("break-leaves")
        .description("Also break leaves attached to tree.")
        .defaultValue(false)
        .build());

    private final Set<BlockPos> treeBlocks = new HashSet<>();
    private BlockPos startPos = null;
    private int currentIndex = 0;

    public TreeChopper() {
        super(GabrielSKAddon.CATEGORY, "tree-chopper", "Automatically chops entire trees.");
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;

        // Check if we're breaking a log
        if (startPos == null || treeBlocks.isEmpty()) {
            BlockPos looking = getLookingAtLog();
            if (looking != null) {
                startPos = looking;
                scanTree(startPos);
            }
            return;
        }

        // Break next block in tree
        if (currentIndex < treeBlocks.size()) {
            BlockPos target = new ArrayList<>(treeBlocks).get(currentIndex);
            
            if (!isLog(target) && !isLeaf(target)) {
                currentIndex++;
                return;
            }

            if (autoTool.get()) {
                FindItemResult axe = InvUtils.findInHotbar(itemStack -> 
                    itemStack.getItem().toString().contains("axe")
                );
                if (axe.found()) {
                    InvUtils.swap(axe.slot(), false);
                }
            }

            mc.interactionManager.updateBlockBreakingProgress(target, Direction.UP);
            mc.player.swingHand(Hand.MAIN_HAND);
            currentIndex++;
        } else {
            reset();
        }
    }

    private void scanTree(BlockPos start) {
        treeBlocks.clear();
        Queue<BlockPos> queue = new LinkedList<>();
        Set<BlockPos> visited = new HashSet<>();
        
        queue.add(start);
        visited.add(start);

        while (!queue.isEmpty() && treeBlocks.size() < range.get()) {
            BlockPos pos = queue.poll();
            if (!isLog(pos)) continue;

            treeBlocks.add(pos);

            // Check surrounding blocks
            for (int x = -1; x <= 1; x++) {
                for (int y = -1; y <= 1; y++) {
                    for (int z = -1; z <= 1; z++) {
                        BlockPos neighbor = pos.add(x, y, z);
                        if (!visited.contains(neighbor)) {
                            visited.add(neighbor);
                            if (isLog(neighbor) || (leavesAlso.get() && isLeaf(neighbor))) {
                                queue.add(neighbor);
                            }
                        }
                    }
                }
            }
        }
    }

    private BlockPos getLookingAtLog() {
        if (mc.crosshairTarget == null) return null;
        if (mc.crosshairTarget instanceof net.minecraft.util.hit.BlockHitResult hit) {
            BlockPos pos = hit.getBlockPos();
            if (isLog(pos)) return pos;
        }
        return null;
    }

    private boolean isLog(BlockPos pos) {
        if (mc.world == null) return false;
        BlockState state = mc.world.getBlockState(pos);
        Block block = state.getBlock();
        
        return block == Blocks.OAK_LOG || block == Blocks.SPRUCE_LOG || 
               block == Blocks.BIRCH_LOG || block == Blocks.JUNGLE_LOG ||
               block == Blocks.ACACIA_LOG || block == Blocks.DARK_OAK_LOG ||
               block == Blocks.MANGROVE_LOG || block == Blocks.CHERRY_LOG ||
               block == Blocks.CRIMSON_STEM || block == Blocks.WARPED_STEM ||
               block == Blocks.OAK_WOOD || block == Blocks.SPRUCE_WOOD ||
               block == Blocks.BIRCH_WOOD || block == Blocks.JUNGLE_WOOD ||
               block == Blocks.ACACIA_WOOD || block == Blocks.DARK_OAK_WOOD ||
               block == Blocks.MANGROVE_WOOD || block == Blocks.CHERRY_WOOD;
    }

    private boolean isLeaf(BlockPos pos) {
        if (mc.world == null) return false;
        Block block = mc.world.getBlockState(pos).getBlock();
        return block.toString().contains("leaves");
    }

    private void reset() {
        treeBlocks.clear();
        startPos = null;
        currentIndex = 0;
    }

    @Override
    public void onDeactivate() {
        reset();
    }
}
