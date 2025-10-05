package com.gabrielsk.modules.movement;

import com.gabrielsk.GabrielSKAddon;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;

public class Jesus extends Module {
    public Jesus() {
        super(GabrielSKAddon.CATEGORY, "jesus", "Walk on water.");
    }
    
    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (mc.player == null || mc.world == null) return;
        
        BlockPos pos = mc.player.getBlockPos();
        
        if (mc.world.getBlockState(pos).getBlock() == Blocks.WATER && 
            mc.world.getBlockState(pos.up()).getBlock() == Blocks.AIR) {
            
            if (mc.player.input.sneaking) {
                mc.player.setVelocity(mc.player.getVelocity().x, -0.4, mc.player.getVelocity().z);
            } else {
                mc.player.setVelocity(mc.player.getVelocity().x, 0.1, mc.player.getVelocity().z);
            }
            
            mc.player.setOnGround(true);
        }
    }
}
