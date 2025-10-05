package com.gabrielsk.modules.combat.criticals;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;

/**
 * Classic packet-based critical exploit
 * Sends position packets to simulate jump for critical hit
 */
public class PacketCritical implements CriticalStrategy {
    
    private double lastOffset = 0.0625;
    private long lastExecution = 0;
    private static final long MIN_DELAY = 50; // Anti-pattern delay
    
    @Override
    public boolean execute(MinecraftClient mc, Entity target) {
        if (mc.player == null || mc.getNetworkHandler() == null) return false;
        
        // Adaptive offset to avoid detection patterns
        double offset = getAdaptiveOffset();
        double x = mc.player.getX();
        double y = mc.player.getY();
        double z = mc.player.getZ();
        
        try {
            // Send position packets to trigger critical
            mc.getNetworkHandler().sendPacket(
                new PlayerMoveC2SPacket.PositionAndOnGround(x, y + offset, z, false)
            );
            mc.getNetworkHandler().sendPacket(
                new PlayerMoveC2SPacket.PositionAndOnGround(x, y, z, false)
            );
            
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
        
        // Anti-spam delay
        if (System.currentTimeMillis() - lastExecution < MIN_DELAY) return false;
        
        return true;
    }
    
    /**
     * Adaptive offset to avoid detection patterns
     * Varies offset slightly each time
     */
    private double getAdaptiveOffset() {
        // Vary offset between 0.05 and 0.08 to avoid patterns
        lastOffset = 0.05 + (Math.random() * 0.03);
        return lastOffset;
    }
    
    @Override
    public String getName() {
        return "Packet";
    }
    
    @Override
    public double getDetectionRisk() {
        return 0.4; // Medium risk
    }
    
    @Override
    public double getDamageMultiplier() {
        return 1.5; // Standard critical
    }
    
    @Override
    public int getExecutionSpeed() {
        return 5; // Very fast
    }
}
