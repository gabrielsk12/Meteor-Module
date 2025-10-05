package com.gabrielsk.modules.combat.criticals;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;

/**
 * Multi-packet critical exploit
 * Sends multiple carefully crafted packets to maximize critical chance
 * Most effective but slightly higher detection risk
 */
public class MultiPacketCritical implements CriticalStrategy {
    
    private long lastExecution = 0;
    private static final long MIN_DELAY = 60;
    
    // Packet sequences for advanced exploitation
    private static final double[] OFFSETS = {0.0625, 0.0433, 0.0247, 0.0};
    
    @Override
    public boolean execute(MinecraftClient mc, Entity target) {
        if (mc.player == null || mc.getNetworkHandler() == null) return false;
        
        try {
            double x = mc.player.getX();
            double y = mc.player.getY();
            double z = mc.player.getZ();
            
            // Send sequence of packets with decreasing offsets
            // This simulates a more realistic jump arc
            for (double offset : OFFSETS) {
                double adaptiveOffset = offset + (Math.random() * 0.002 - 0.001); // Micro-variation
                mc.getNetworkHandler().sendPacket(
                    new PlayerMoveC2SPacket.PositionAndOnGround(
                        x, y + adaptiveOffset, z, false
                    )
                );
            }
            
            lastExecution = System.currentTimeMillis();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    @Override
    public boolean canExecute(MinecraftClient mc) {
        if (mc.player == null) return false;
        
        // Check basic conditions
        if (!mc.player.isOnGround()) return false;
        if (mc.player.isTouchingWater()) return false;
        if (mc.player.isInLava()) return false;
        if (mc.player.hasVehicle()) return false;
        if (mc.player.isClimbing()) return false;
        if (mc.player.isFallFlying()) return false;
        
        // Anti-spam delay
        if (System.currentTimeMillis() - lastExecution < MIN_DELAY) return false;
        
        return true;
    }
    
    @Override
    public String getName() {
        return "Multi-Packet";
    }
    
    @Override
    public double getDetectionRisk() {
        return 0.5; // Medium-high risk due to packet spam
    }
    
    @Override
    public double getDamageMultiplier() {
        return 1.5; // Standard critical
    }
    
    @Override
    public int getExecutionSpeed() {
        return 3; // Very fast
    }
}
