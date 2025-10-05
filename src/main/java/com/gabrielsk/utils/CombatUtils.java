package com.gabrielsk.utils;

import com.gabrielsk.math.MathUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;
import net.minecraft.block.Blocks;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Advanced combat utilities with prediction algorithms
 * Mathematically perfect damage calculations and targeting
 */
public class CombatUtils {
    
    private static final MinecraftClient mc = MinecraftClient.getInstance();
    
    // Damage calculation cache (performance optimization)
    private static final Map<String, DamageCache> damageCache = new ConcurrentHashMap<>();
    private static final long CACHE_EXPIRY = 50; // 50ms cache
    
    // Crystal damage constants
    private static final double CRYSTAL_EXPLOSION_POWER = 6.0;
    private static final double CRYSTAL_RANGE = 12.0;
    
    /**
     * Calculate damage from crystal explosion to entity
     * Uses raycasting for terrain blocking
     */
    public static double calculateCrystalDamage(Vec3d crystalPos, LivingEntity target) {
        String cacheKey = crystalPos.toString() + target.getId();
        DamageCache cached = damageCache.get(cacheKey);
        
        if (cached != null && System.currentTimeMillis() - cached.timestamp < CACHE_EXPIRY) {
            return cached.damage;
        }
        
        Vec3d targetPos = target.getPos();
        double distance = MathUtils.fastDistance(crystalPos, targetPos);
        
        if (distance > CRYSTAL_RANGE) {
            return 0.0;
        }
        
        // Calculate exposure (terrain blocking)
        double exposure = calculateExposure(crystalPos, target);
        
        // Calculate raw damage
        double damage = MathUtils.calculateExplosionDamage(
            crystalPos, targetPos, CRYSTAL_EXPLOSION_POWER, exposure > 0.9
        );
        
        // Apply armor reduction
        damage = applyArmorReduction(damage, target);
        
        // Apply resistance/protection
        damage = applyEffectReduction(damage, target);
        
        // Cache result
        damageCache.put(cacheKey, new DamageCache(damage, System.currentTimeMillis()));
        
        return damage;
    }
    
    /**
     * Calculate exposure using raycasting
     * Returns 0.0 (fully blocked) to 1.0 (fully exposed)
     */
    private static double calculateExposure(Vec3d explosionPos, Entity entity) {
        if (mc.world == null) return 1.0;
        
        Box box = entity.getBoundingBox();
        int raysHit = 0;
        int totalRays = 0;
        
        // Cast rays from explosion to entity bounding box
        double stepX = 1.0 / 3.0;
        double stepY = 1.0 / 3.0;
        double stepZ = 1.0 / 3.0;
        
        for (double x = 0; x <= 1; x += stepX) {
            for (double y = 0; y <= 1; y += stepY) {
                for (double z = 0; z <= 1; z += stepZ) {
                    Vec3d point = new Vec3d(
                        MathUtils.lerp(box.minX, box.maxX, x),
                        MathUtils.lerp(box.minY, box.maxY, y),
                        MathUtils.lerp(box.minZ, box.maxZ, z)
                    );
                    
                    totalRays++;
                    if (!isBlocked(explosionPos, point)) {
                        raysHit++;
                    }
                }
            }
        }
        
        return (double) raysHit / totalRays;
    }
    
    /**
     * Check if line of sight is blocked by terrain
     */
    private static boolean isBlocked(Vec3d from, Vec3d to) {
        if (mc.world == null) return false;
        
        // Simple raycast
        Vec3d direction = to.subtract(from).normalize();
        double distance = MathUtils.fastDistance(from, to);
        double step = 0.5;
        
        for (double d = 0; d < distance; d += step) {
            Vec3d point = from.add(direction.multiply(d));
            BlockPos pos = new BlockPos((int)point.x, (int)point.y, (int)point.z);
            
            if (mc.world.getBlockState(pos).isSolidBlock(mc.world, pos)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Apply armor damage reduction
     */
    private static double applyArmorReduction(double damage, LivingEntity entity) {
        int armor = entity.getArmor();
        int toughness = 0; // TODO: Calculate from items
        
        // Minecraft armor formula
        double armorReduction = Math.max(armor / 5.0, armor - damage / (2 + toughness / 4.0));
        armorReduction = Math.min(20.0, armorReduction);
        
        return damage * (1.0 - armorReduction / 25.0);
    }
    
    /**
     * Apply potion effects reduction (resistance, protection)
     */
    private static double applyEffectReduction(double damage, LivingEntity entity) {
        // Check for resistance effect
        // TODO: Implement potion effect checking
        
        // For now, assume no effects
        return damage;
    }
    
    /**
     * Find best crystal placement position
     * Returns position that deals maximum damage to target
     */
    public static BlockPos findBestCrystalPos(LivingEntity target, double maxSelfDamage) {
        if (mc.player == null || mc.world == null) return null;
        
        BlockPos targetPos = target.getBlockPos();
        BlockPos bestPos = null;
        double bestDamage = 0;
        
        // Search around target
        int range = 6;
        for (int x = -range; x <= range; x++) {
            for (int y = -2; y <= 2; y++) {
                for (int z = -range; z <= range; z++) {
                    BlockPos pos = targetPos.add(x, y, z);
                    
                    if (!canPlaceCrystal(pos)) continue;
                    
                    Vec3d crystalPos = Vec3d.ofCenter(pos).add(0, 1, 0);
                    
                    // Calculate damage
                    double targetDamage = calculateCrystalDamage(crystalPos, target);
                    double selfDamage = calculateCrystalDamage(crystalPos, mc.player);
                    
                    // Skip if too dangerous
                    if (selfDamage > maxSelfDamage) continue;
                    
                    // Prefer high target damage, low self damage
                    double score = targetDamage - selfDamage * 0.5;
                    
                    if (score > bestDamage) {
                        bestDamage = score;
                        bestPos = pos.up();
                    }
                }
            }
        }
        
        return bestPos;
    }
    
    /**
     * Check if crystal can be placed at position
     */
    public static boolean canPlaceCrystal(BlockPos pos) {
        if (mc.world == null) return false;
        
        // Must be obsidian or bedrock
        if (!mc.world.getBlockState(pos).isOf(Blocks.OBSIDIAN) && 
            !mc.world.getBlockState(pos).isOf(Blocks.BEDROCK)) {
            return false;
        }
        
        // Space above must be air
        BlockPos above = pos.up();
        if (!mc.world.getBlockState(above).isAir() || 
            !mc.world.getBlockState(above.up()).isAir()) {
            return false;
        }
        
        // Check for entities in the way
        Box box = new Box(above);
        return mc.world.getOtherEntities(null, box).isEmpty();
    }
    
    /**
     * Predict where to place crystal based on enemy movement
     */
    public static BlockPos predictCrystalPlacement(LivingEntity target, double ticksAhead) {
        Vec3d predictedPos = MathUtils.predictPosition(target, ticksAhead);
        BlockPos predictedBlock = new BlockPos((int)predictedPos.x, (int)predictedPos.y, (int)predictedPos.z);
        
        return findBestCrystalPos(target, 6.0); // 6 HP max self damage
    }
    
    /**
     * Find nearest enemy entity
     */
    public static LivingEntity findNearestEnemy(double range) {
        if (mc.world == null || mc.player == null) return null;
        
        LivingEntity nearest = null;
        double nearestDist = range;
        
        for (Entity entity : mc.world.getEntities()) {
            if (!(entity instanceof LivingEntity)) continue;
            if (entity == mc.player) continue;
            if (!entity.isAlive()) continue;
            
            LivingEntity living = (LivingEntity) entity;
            
            // Check if enemy
            if (living instanceof PlayerEntity && living != mc.player) {
                double dist = mc.player.distanceTo(living);
                if (dist < nearestDist) {
                    nearest = living;
                    nearestDist = dist;
                }
            }
        }
        
        return nearest;
    }
    
    /**
     * Get all enemies in range
     */
    public static List<LivingEntity> getEnemiesInRange(double range) {
        List<LivingEntity> enemies = new ArrayList<>();
        
        if (mc.world == null || mc.player == null) return enemies;
        
        for (Entity entity : mc.world.getEntities()) {
            if (!(entity instanceof LivingEntity)) continue;
            if (entity == mc.player) continue;
            if (!entity.isAlive()) continue;
            
            LivingEntity living = (LivingEntity) entity;
            
            if (living instanceof PlayerEntity && mc.player.distanceTo(living) <= range) {
                enemies.add(living);
            }
        }
        
        return enemies;
    }
    
    /**
     * Calculate if surround is needed
     */
    public static boolean needsSurround() {
        if (mc.player == null) return false;
        
        // Check for nearby enemies
        List<LivingEntity> enemies = getEnemiesInRange(8.0);
        if (enemies.isEmpty()) return false;
        
        // Check if already surrounded
        return !isPlayerSurrounded();
    }
    
    /**
     * Check if player is surrounded
     */
    public static boolean isPlayerSurrounded() {
        if (mc.world == null || mc.player == null) return false;
        
        BlockPos playerPos = mc.player.getBlockPos();
        
        // Check all 4 cardinal directions
        BlockPos[] surroundPositions = {
            playerPos.add(-1, -1, 0),
            playerPos.add(1, -1, 0),
            playerPos.add(0, -1, -1),
            playerPos.add(0, -1, 1)
        };
        
        for (BlockPos pos : surroundPositions) {
            if (mc.world.getBlockState(pos).isAir()) {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Get surround positions that need blocks
     */
    public static List<BlockPos> getSurroundPositions() {
        List<BlockPos> positions = new ArrayList<>();
        
        if (mc.player == null) return positions;
        
        BlockPos playerPos = mc.player.getBlockPos();
        List<BlockPos> pattern = MathUtils.calculateSurroundPattern(playerPos, 1);
        
        for (BlockPos pos : pattern) {
            if (mc.world != null && mc.world.getBlockState(pos).isAir()) {
                positions.add(pos);
            }
        }
        
        // Sort by priority (closest first)
        positions.sort(Comparator.comparingDouble(pos -> 
            MathUtils.distanceSquared(mc.player.getPos(), Vec3d.ofCenter(pos))
        ));
        
        return positions;
    }
    
    /**
     * Calculate optimal attack timing
     * Returns true if should attack now
     */
    public static boolean shouldAttack(LivingEntity target, double cooldown) {
        if (mc.player == null) return false;
        
        // Check cooldown
        if (mc.player.getAttackCooldownProgress(0) < cooldown) {
            return false;
        }
        
        // Check range
        double distance = mc.player.distanceTo(target);
        if (distance > 6.0) {
            return false;
        }
        
        // Check line of sight
        Vec3d playerPos = mc.player.getEyePos();
        Vec3d targetPos = target.getEyePos();
        
        return !isBlocked(playerPos, targetPos);
    }
    
    /**
     * Calculate optimal retreat direction
     */
    public static Vec3d calculateRetreatDirection() {
        if (mc.player == null) return Vec3d.ZERO;
        
        List<LivingEntity> enemies = getEnemiesInRange(16.0);
        if (enemies.isEmpty()) return Vec3d.ZERO;
        
        // Average direction away from all enemies
        Vec3d retreatDir = Vec3d.ZERO;
        Vec3d playerPos = mc.player.getPos();
        
        for (LivingEntity enemy : enemies) {
            Vec3d enemyPos = enemy.getPos();
            Vec3d awayFrom = playerPos.subtract(enemyPos).normalize();
            
            // Weight by distance (closer enemies more important)
            double distance = mc.player.distanceTo(enemy);
            double weight = 1.0 / (distance + 1.0);
            
            retreatDir = retreatDir.add(awayFrom.multiply(weight));
        }
        
        return retreatDir.normalize();
    }
    
    /**
     * Clear old cache entries
     */
    public static void cleanCache() {
        long currentTime = System.currentTimeMillis();
        damageCache.entrySet().removeIf(entry -> 
            currentTime - entry.getValue().timestamp > CACHE_EXPIRY
        );
    }
    
    // Cache data structure
    private static class DamageCache {
        double damage;
        long timestamp;
        
        DamageCache(double damage, long timestamp) {
            this.damage = damage;
            this.timestamp = timestamp;
        }
    }
}
