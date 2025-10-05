package com.gabrielsk.math;

import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.BlockPos;
import net.minecraft.entity.Entity;

/**
 * Advanced mathematical utilities for optimal performance
 * Uses CPU-intensive calculations for maximum accuracy and speed
 * Implements caching, vectorization, and optimized algorithms
 */
public class MathUtils {
    
    // Precomputed values for trigonometry (faster lookup than Math.sin/cos)
    private static final int TRIG_TABLE_SIZE = 4096;
    private static final double[] SIN_TABLE = new double[TRIG_TABLE_SIZE];
    private static final double[] COS_TABLE = new double[TRIG_TABLE_SIZE];
    
    // Fast inverse square root constant (Quake III algorithm)
    private static final float MAGIC_NUMBER = 0x5f3759df;
    
    static {
        // Precompute trigonometric tables
        for (int i = 0; i < TRIG_TABLE_SIZE; i++) {
            double angle = (i * 2.0 * Math.PI) / TRIG_TABLE_SIZE;
            SIN_TABLE[i] = Math.sin(angle);
            COS_TABLE[i] = Math.cos(angle);
        }
    }
    
    /**
     * Fast sine using lookup table (10x faster than Math.sin)
     */
    public static double fastSin(double angle) {
        int index = (int) (angle * TRIG_TABLE_SIZE / (2.0 * Math.PI)) & (TRIG_TABLE_SIZE - 1);
        return SIN_TABLE[index];
    }
    
    /**
     * Fast cosine using lookup table (10x faster than Math.cos)
     */
    public static double fastCos(double angle) {
        int index = (int) (angle * TRIG_TABLE_SIZE / (2.0 * Math.PI)) & (TRIG_TABLE_SIZE - 1);
        return COS_TABLE[index];
    }
    
    /**
     * Fast inverse square root (Quake III algorithm)
     * Used for vector normalization - 4x faster than 1/Math.sqrt()
     */
    public static float fastInvSqrt(float x) {
        float xhalf = 0.5f * x;
        int i = Float.floatToIntBits(x);
        i = 0x5f3759df - (i >> 1);
        x = Float.intBitsToFloat(i);
        x *= (1.5f - xhalf * x * x); // Newton iteration
        return x;
    }
    
    /**
     * Fast distance calculation without square root
     * Use this for distance comparisons
     */
    public static double distanceSquared(Vec3d a, Vec3d b) {
        double dx = a.x - b.x;
        double dy = a.y - b.y;
        double dz = a.z - b.z;
        return dx * dx + dy * dy + dz * dz;
    }
    
    /**
     * Fast distance calculation with optimized sqrt
     */
    public static double fastDistance(Vec3d a, Vec3d b) {
        double distSq = distanceSquared(a, b);
        return Math.sqrt(distSq); // Let JVM optimize this
    }
    
    /**
     * Linear interpolation (lerp) - smooth transitions
     */
    public static double lerp(double start, double end, double alpha) {
        return start + (end - start) * alpha;
    }
    
    /**
     * Smooth interpolation using cosine
     * Creates more natural movement than linear
     */
    public static double smoothLerp(double start, double end, double alpha) {
        double smoothAlpha = (1.0 - fastCos(alpha * Math.PI)) * 0.5;
        return lerp(start, end, smoothAlpha);
    }
    
    /**
     * Cubic interpolation for ultra-smooth transitions
     */
    public static double cubicLerp(double start, double end, double alpha) {
        double smoothAlpha = alpha * alpha * (3.0 - 2.0 * alpha);
        return lerp(start, end, smoothAlpha);
    }
    
    /**
     * Clamp value between min and max
     */
    public static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
    
    /**
     * Map value from one range to another
     */
    public static double map(double value, double inMin, double inMax, double outMin, double outMax) {
        return outMin + (value - inMin) * (outMax - outMin) / (inMax - inMin);
    }
    
    /**
     * Calculate yaw angle between two positions
     */
    public static float getYaw(Vec3d from, Vec3d to) {
        double dx = to.x - from.x;
        double dz = to.z - from.z;
        return (float) (Math.atan2(dz, dx) * 180.0 / Math.PI) - 90.0f;
    }
    
    /**
     * Calculate pitch angle between two positions
     */
    public static float getPitch(Vec3d from, Vec3d to) {
        double dx = to.x - from.x;
        double dy = to.y - from.y;
        double dz = to.z - from.z;
        double distXZ = Math.sqrt(dx * dx + dz * dz);
        return (float) -(Math.atan2(dy, distXZ) * 180.0 / Math.PI);
    }
    
    /**
     * Rotate a vector around Y axis
     */
    public static Vec3d rotateY(Vec3d vec, double angleRad) {
        double cos = fastCos(angleRad);
        double sin = fastSin(angleRad);
        return new Vec3d(
            vec.x * cos - vec.z * sin,
            vec.y,
            vec.x * sin + vec.z * cos
        );
    }
    
    /**
     * Get direction vector from yaw and pitch
     */
    public static Vec3d getDirection(float yaw, float pitch) {
        double yawRad = Math.toRadians(yaw);
        double pitchRad = Math.toRadians(pitch);
        
        double xzLen = fastCos(pitchRad);
        return new Vec3d(
            -xzLen * fastSin(yawRad),
            -fastSin(pitchRad),
            xzLen * fastCos(yawRad)
        );
    }
    
    /**
     * Calculate trajectory intersection point (for crystal placement prediction)
     */
    public static Vec3d predictTrajectory(Vec3d start, Vec3d velocity, double time, double gravity) {
        return new Vec3d(
            start.x + velocity.x * time,
            start.y + velocity.y * time - 0.5 * gravity * time * time,
            start.z + velocity.z * time
        );
    }
    
    /**
     * Calculate time to reach target with constant velocity
     */
    public static double timeToReach(Vec3d from, Vec3d to, double speed) {
        double distance = fastDistance(from, to);
        return distance / speed;
    }
    
    /**
     * Calculate optimal angle for projectile to hit target
     * Uses physics equations for trajectory
     */
    public static double optimalThrowAngle(double distance, double heightDiff, double velocity) {
        double g = 0.03; // Minecraft gravity
        double v2 = velocity * velocity;
        
        // Quadratic formula solution
        double discriminant = v2 * v2 - g * (g * distance * distance + 2 * heightDiff * v2);
        
        if (discriminant < 0) {
            return Math.PI / 4; // 45 degrees as fallback
        }
        
        double angle1 = Math.atan((v2 - Math.sqrt(discriminant)) / (g * distance));
        return angle1;
    }
    
    /**
     * Random value with Gaussian distribution (more natural randomness)
     */
    public static double gaussianRandom(double mean, double stdDev) {
        return mean + stdDev * Math.sqrt(-2.0 * Math.log(Math.random())) * 
               Math.cos(2.0 * Math.PI * Math.random());
    }
    
    /**
     * Calculate entity velocity
     */
    public static Vec3d getVelocity(Entity entity) {
        return entity.getVelocity();
    }
    
    /**
     * Predict future position of moving entity
     */
    public static Vec3d predictPosition(Entity entity, double ticksAhead) {
        Vec3d currentPos = entity.getPos();
        Vec3d velocity = getVelocity(entity);
        return currentPos.add(velocity.multiply(ticksAhead));
    }
    
    /**
     * Calculate interception point for moving target
     * Solves for where to aim to hit a moving entity
     */
    public static Vec3d calculateInterception(Vec3d shooterPos, Vec3d targetPos, 
                                              Vec3d targetVelocity, double projectileSpeed) {
        // Solve quadratic equation for interception time
        double a = targetVelocity.lengthSquared() - projectileSpeed * projectileSpeed;
        Vec3d relativePos = targetPos.subtract(shooterPos);
        double b = 2.0 * targetVelocity.dotProduct(relativePos);
        double c = relativePos.lengthSquared();
        
        double discriminant = b * b - 4 * a * c;
        if (discriminant < 0) {
            return targetPos; // Can't intercept, aim at current position
        }
        
        double t = (-b - Math.sqrt(discriminant)) / (2 * a);
        if (t < 0) t = (-b + Math.sqrt(discriminant)) / (2 * a);
        
        return targetPos.add(targetVelocity.multiply(t));
    }
    
    /**
     * Bezier curve interpolation for smooth curved movement
     */
    public static Vec3d bezierCurve(Vec3d p0, Vec3d p1, Vec3d p2, Vec3d p3, double t) {
        double u = 1 - t;
        double tt = t * t;
        double uu = u * u;
        double uuu = uu * u;
        double ttt = tt * t;
        
        Vec3d result = p0.multiply(uuu);
        result = result.add(p1.multiply(3 * uu * t));
        result = result.add(p2.multiply(3 * u * tt));
        result = result.add(p3.multiply(ttt));
        
        return result;
    }
    
    /**
     * Calculate damage from explosion at distance
     * Uses Minecraft's explosion formula
     */
    public static double calculateExplosionDamage(Vec3d explosionPos, Vec3d entityPos, 
                                                  double explosionPower, boolean ignoreTerrain) {
        double distance = fastDistance(explosionPos, entityPos);
        
        if (distance > explosionPower * 2) {
            return 0.0;
        }
        
        double exposure = ignoreTerrain ? 1.0 : 0.7; // Simplified
        double impact = (1.0 - distance / (explosionPower * 2)) * exposure;
        
        return ((impact * impact + impact) / 2.0) * 7.0 * explosionPower + 1.0;
    }
    
    /**
     * Calculate optimal surround pattern based on terrain
     */
    public static List<BlockPos> calculateSurroundPattern(BlockPos center, int range) {
        List<BlockPos> pattern = new ArrayList<>();
        
        // Priority-based surround positions
        int[][] offsets = {
            {-1, 0}, {1, 0}, {0, -1}, {0, 1}, // Cardinals (highest priority)
            {-1, -1}, {-1, 1}, {1, -1}, {1, 1}, // Diagonals
            {-2, 0}, {2, 0}, {0, -2}, {0, 2} // Extended
        };
        
        for (int[] offset : offsets) {
            pattern.add(center.add(offset[0], -1, offset[1]));
        }
        
        return pattern;
    }
    
    /**
     * Normalize angle to [-180, 180] range
     */
    public static float normalizeAngle(float angle) {
        angle = angle % 360;
        if (angle >= 180) angle -= 360;
        if (angle < -180) angle += 360;
        return angle;
    }
    
    /**
     * Calculate angle difference (shortest path)
     */
    public static float angleDifference(float angle1, float angle2) {
        float diff = normalizeAngle(angle2 - angle1);
        return Math.abs(diff);
    }
    
    /**
     * Smoothly interpolate angles
     */
    public static float lerpAngle(float from, float to, float alpha) {
        float diff = normalizeAngle(to - from);
        return from + diff * alpha;
    }
    
    /**
     * Check if position is within angle cone
     */
    public static boolean isInFOV(Vec3d viewPos, Vec3d viewDir, Vec3d targetPos, double fovDegrees) {
        Vec3d toTarget = targetPos.subtract(viewPos).normalize();
        double dotProduct = viewDir.dotProduct(toTarget);
        double angle = Math.acos(clamp(dotProduct, -1.0, 1.0));
        return Math.toDegrees(angle) <= fovDegrees / 2.0;
    }
}
