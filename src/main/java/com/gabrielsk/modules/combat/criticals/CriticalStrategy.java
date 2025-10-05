package com.gabrielsk.modules.combat.criticals;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;

/**
 * Interface for critical hit exploitation strategies
 * Each strategy implements a different method to trigger critical hits
 */
public interface CriticalStrategy {
    
    /**
     * Execute the critical hit exploit
     * @param mc Minecraft client instance
     * @param target The entity being attacked
     * @return true if critical was successfully executed
     */
    boolean execute(MinecraftClient mc, Entity target);
    
    /**
     * Check if this strategy can be used in current conditions
     * @param mc Minecraft client instance
     * @return true if strategy is viable
     */
    boolean canExecute(MinecraftClient mc);
    
    /**
     * Get the name of this strategy
     * @return strategy name
     */
    String getName();
    
    /**
     * Get detection risk level (0.0 = undetectable, 1.0 = very obvious)
     * @return risk level
     */
    double getDetectionRisk();
    
    /**
     * Get damage multiplier this strategy provides
     * @return damage multiplier (1.5 = 150% damage)
     */
    double getDamageMultiplier();
    
    /**
     * Get execution speed (lower = faster)
     * @return execution time in milliseconds
     */
    int getExecutionSpeed();
}
