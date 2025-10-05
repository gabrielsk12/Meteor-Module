package com.gabrielsk.utils;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Safety utility class to prevent common crashes
 * Provides null-safe access to game objects and bounds checking
 */
public class SafetyChecks {
    
    /**
     * Check if the game is in a safe state for module operations
     */
    public static boolean isGameSafe(MinecraftClient mc) {
        return mc != null && mc.player != null && mc.world != null && !mc.player.isRemoved();
    }
    
    /**
     * Safely get an ItemStack from inventory with bounds checking
     */
    public static ItemStack safeGetStack(MinecraftClient mc, int slot) {
        if (!isGameSafe(mc)) return ItemStack.EMPTY;
        if (slot < 0 || slot >= mc.player.getInventory().size()) return ItemStack.EMPTY;
        
        try {
            ItemStack stack = mc.player.getInventory().getStack(slot);
            return stack != null ? stack : ItemStack.EMPTY;
        } catch (Exception e) {
            return ItemStack.EMPTY;
        }
    }
    
    /**
     * Safely check if entity is valid and not removed
     */
    public static boolean isEntityValid(Entity entity) {
        return entity != null && entity.isAlive() && !entity.isRemoved();
    }
    
    /**
     * Safely check if position is loaded in world
     */
    public static boolean isPositionLoaded(World world, BlockPos pos) {
        if (world == null || pos == null) return false;
        try {
            return world.isChunkLoaded(pos);
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Safely get entity by checking validity
     */
    public static <T extends Entity> boolean isValidTarget(T entity, MinecraftClient mc, double maxRange) {
        if (!isGameSafe(mc) || !isEntityValid(entity)) return false;
        if (entity == mc.player) return false;
        
        try {
            double distance = mc.player.distanceTo(entity);
            return distance <= maxRange;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Safely execute an action with try-catch
     */
    public static void safeExecute(Runnable action) {
        try {
            action.run();
        } catch (Exception e) {
            // Silently fail to prevent crashes
        }
    }
    
    /**
     * Check if inventory operation is safe
     */
    public static boolean canAccessInventory(MinecraftClient mc) {
        if (!isGameSafe(mc)) return false;
        try {
            return mc.player.getInventory() != null && mc.player.currentScreenHandler != null;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Safely check if slot is valid
     */
    public static boolean isSlotValid(MinecraftClient mc, int slot) {
        if (!isGameSafe(mc)) return false;
        return slot >= 0 && slot < mc.player.getInventory().size();
    }
    
    /**
     * Safely get player health
     */
    public static float getSafeHealth(MinecraftClient mc) {
        if (!isGameSafe(mc)) return 20.0f;
        try {
            return mc.player.getHealth() + mc.player.getAbsorptionAmount();
        } catch (Exception e) {
            return 20.0f;
        }
    }
}
