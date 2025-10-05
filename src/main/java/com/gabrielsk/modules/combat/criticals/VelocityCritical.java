package com.gabrielsk.modules.combat.criticals;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;

/**
 * Velocity-based critical exploit
 * Manipulates player velocity to trigger critical hit
 * More human-like than packet methods
 */
public class VelocityCritical implements CriticalStrategy {
    
    private long lastExecution = 0;
    private static final long MIN_DELAY = 100;
    
    @Override
    public boolean execute(MinecraftClient mc, Entity target) {
        if (mc.player == null) return false;
        
        try {
            Vec3d currentVel = mc.player.getVelocity();
            
            // Mini jump with adaptive velocity
            double jumpVelocity = getAdaptiveJumpVelocity();
            mc.player.setVelocity(currentVel.x, jumpVelocity, currentVel.z);
            mc.player.velocityModified = true;
            
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
    
    /**
     * Adaptive jump velocity to appear more human-like
     * Varies between 0.08 and 0.12 to simulate natural variation
     */
    private double getAdaptiveJumpVelocity() {
        return 0.08 + (Math.random() * 0.04);
    }
    
    @Override
    public String getName() {
        return "Velocity";
    }
    
    @Override
    public double getDetectionRisk() {
        return 0.2; // Lower risk, more human-like
    }
    
    @Override
    public double getDamageMultiplier() {
        return 1.5; // Standard critical
    }
    
    @Override
    public int getExecutionSpeed() {
        return 15; // Slower but safer
    }
}
