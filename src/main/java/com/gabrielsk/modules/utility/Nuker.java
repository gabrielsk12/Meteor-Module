package com.gabrielsk.modules.utility;

import com.gabrielsk.GabrielSKAddon;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class Nuker extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    
    private final Setting<Integer> range = sgGeneral.add(new IntSetting.Builder()
        .name("range").description("Mining range.").defaultValue(4).min(1).max(6).sliderMax(6).build());
    
    private final Setting<Integer> delay = sgGeneral.add(new IntSetting.Builder()
        .name("delay").description("Delay between mining (ticks).").defaultValue(0).min(0).max(20).sliderMax(20).build());
    
    private final Setting<Boolean> flatten = sgGeneral.add(new BoolSetting.Builder()
        .name("flatten").description("Only mine blocks at feet level.").defaultValue(false).build());
    
    private int timer = 0;
    
    public Nuker() {
        super(GabrielSKAddon.CATEGORY, "nuker", "Automatically mines blocks around you.");
    }
    
    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (mc.player == null || mc.world == null) return;
        
        if (timer > 0) {
            timer--;
            return;
        }
        
        BlockPos playerPos = mc.player.getBlockPos();
        BlockPos closestPos = null;
        double closestDist = Double.MAX_VALUE;
        
        for (int x = -range.get(); x <= range.get(); x++) {
            for (int y = flatten.get() ? 0 : -range.get(); y <= (flatten.get() ? 0 : range.get()); y++) {
                for (int z = -range.get(); z <= range.get(); z++) {
                    BlockPos pos = playerPos.add(x, y, z);
                    
                    if (mc.world.getBlockState(pos).getBlock() == Blocks.AIR) continue;
                    if (mc.world.getBlockState(pos).getBlock() == Blocks.BEDROCK) continue;
                    
                    double dist = mc.player.squaredDistanceTo(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
                    
                    if (dist < closestDist) {
                        closestDist = dist;
                        closestPos = pos;
                    }
                }
            }
        }
        
        if (closestPos == null) return;
        
        Direction dir = Direction.UP;
        Vec3d hitVec = new Vec3d(closestPos.getX() + 0.5, closestPos.getY() + 0.5, closestPos.getZ() + 0.5);
        
        mc.interactionManager.updateBlockBreakingProgress(closestPos, dir);
        timer = delay.get();
    }
}
