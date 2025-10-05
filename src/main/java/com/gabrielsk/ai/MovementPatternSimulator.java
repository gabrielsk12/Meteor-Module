package com.gabrielsk.ai;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.util.*;

/**
 * Advanced Movement Pattern Simulator
 * Replicates real player movement including:
 * - Natural acceleration/deceleration
 * - Strafing patterns
 * - Jump timing variations
 * - Landing adjustments
 * - Momentum conservation
 * - Direction changes with inertia
 * - Sprint patterns
 * - Crouch timing
 */
public class MovementPatternSimulator {
    
    private static final MinecraftClient mc = MinecraftClient.getInstance();
    private static final Random random = new Random();
    
    // Movement state
    private static Vec3d currentVelocity = Vec3d.ZERO;
    private static Vec3d targetVelocity = Vec3d.ZERO;
    private static double currentSpeed = 0.0;
    private static long lastJumpTime = 0;
    private static boolean wasOnGround = true;
    
    // Movement patterns
    private static MovementPattern currentPattern = MovementPattern.NORMAL;
    private static long patternStartTime = 0;
    private static Queue<MovementAction> actionQueue = new LinkedList<>();
    
    // Natural variations
    private static double strafeVariation = 0.0;
    private static long lastStrafeChange = 0;
    private static double sprintThreshold = 0.8;
    
    /**
     * Gets natural movement input with human-like variations
     * Players don't hold W perfectly - they vary slightly
     */
    public static MovementInput getNaturalMovementInput(Vec3d targetDirection) {
        MovementInput input = new MovementInput();
        
        // Base movement toward target
        if (targetDirection.length() > 0.1) {
            Vec3d normalized = targetDirection.normalize();
            
            // Add natural variation to input (humans don't move perfectly straight)
            double forwardVariation = 0.95 + random.nextDouble() * 0.1; // 95-105%
            double strafeVariation = (random.nextDouble() - 0.5) * 0.15; // ±7.5%
            
            input.forward = (float) (normalized.z * forwardVariation);
            input.strafe = (float) (normalized.x + strafeVariation);
            
            // Occasional micro-corrections
            if (random.nextDouble() < 0.1) {
                input.forward += (random.nextDouble() - 0.5) * 0.2;
                input.strafe += (random.nextDouble() - 0.5) * 0.2;
            }
        }
        
        // Natural sprint behavior
        input.sprint = shouldSprint(input);
        
        // Natural jump behavior
        input.jump = shouldJump();
        
        // Natural sneak behavior
        input.sneak = shouldSneak();
        
        // Clamp values to realistic range
        input.forward = MathHelper.clamp(input.forward, -1.0f, 1.0f);
        input.strafe = MathHelper.clamp(input.strafe, -1.0f, 1.0f);
        
        return input;
    }
    
    /**
     * Determines if player should sprint
     * Players don't spam sprint - they have patterns
     */
    private static boolean shouldSprint(MovementInput input) {
        if (mc.player == null) return false;
        
        // Don't sprint if moving backward
        if (input.forward < 0) return false;
        
        // Don't sprint if hungry
        if (mc.player.getHungerManager().getFoodLevel() <= 6) return false;
        
        // Natural sprint threshold
        if (Math.abs(input.forward) < sprintThreshold) return false;
        
        // Fatigue affects sprinting
        if (HumanBehaviorSimulator.getFatigueLevel() > 0.7) {
            // Tired players sprint less
            return random.nextDouble() > 0.3;
        }
        
        // High stress = more sprinting
        if (HumanBehaviorSimulator.getStressLevel() > 0.6) {
            return true;
        }
        
        // Natural sprint variation - sometimes players forget to sprint
        if (random.nextDouble() < 0.05) {
            return false;
        }
        
        return true;
    }
    
    /**
     * Determines if player should jump
     * Players have timing patterns and don't spam jump
     */
    private static boolean shouldJump() {
        if (mc.player == null) return false;
        
        long timeSinceLastJump = System.currentTimeMillis() - lastJumpTime;
        
        // Minimum jump delay (humans can't spam instantly)
        if (timeSinceLastJump < 200) return false;
        
        // Just landed? Might jump again (bhop)
        boolean justLanded = !wasOnGround && mc.player.isOnGround();
        wasOnGround = mc.player.isOnGround();
        
        if (justLanded) {
            // Skill-based bunny hop timing
            double bhopChance = 0.3 * HumanBehaviorSimulator.getFocusLevel();
            
            if (random.nextDouble() < bhopChance) {
                // Imperfect timing (50-150ms after landing)
                if (timeSinceLastJump > 450 && timeSinceLastJump < 550) {
                    lastJumpTime = System.currentTimeMillis();
                    return true;
                }
            }
        }
        
        // Random occasional jumps (player testing, boredom)
        if (random.nextDouble() < 0.001) {
            lastJumpTime = System.currentTimeMillis();
            return true;
        }
        
        // Obstacle detection - jump to clear
        if (currentPattern == MovementPattern.OBSTACLE_CLEAR) {
            if (timeSinceLastJump > 500) {
                lastJumpTime = System.currentTimeMillis();
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Determines if player should sneak
     * Sneaking has specific use cases and timing
     */
    private static boolean shouldSneak() {
        if (mc.player == null) return false;
        
        // Sneak when placing blocks near edge
        if (currentPattern == MovementPattern.EDGE_PLACEMENT) {
            return true;
        }
        
        // Sneak to reduce fall damage sometimes
        if (!mc.player.isOnGround() && mc.player.getVelocity().y < -0.5) {
            return random.nextDouble() < 0.7; // 70% of the time
        }
        
        // Occasional accidental sneak (fat finger)
        if (random.nextDouble() < 0.001) {
            return true;
        }
        
        return false;
    }
    
    /**
     * Simulates natural strafing pattern
     * Players strafe in patterns, not randomly
     */
    public static StrafePattern getNaturalStrafePattern() {
        long timeSinceChange = System.currentTimeMillis() - lastStrafeChange;
        
        // Change strafe direction periodically
        if (timeSinceChange > 500 + random.nextInt(1000)) {
            strafeVariation = (random.nextDouble() - 0.5) * 2.0; // -1 to 1
            lastStrafeChange = System.currentTimeMillis();
        }
        
        return new StrafePattern(strafeVariation, getStrafeIntensity());
    }
    
    /**
     * Gets strafe intensity (how much to strafe)
     */
    private static double getStrafeIntensity() {
        double intensity = 0.5;
        
        // Combat = more strafing
        if (HumanBehaviorSimulator.getStressLevel() > 0.5) {
            intensity = 0.8 + random.nextDouble() * 0.2;
        }
        
        // Tired = less strafing
        intensity *= (1.0 - HumanBehaviorSimulator.getFatigueLevel() * 0.3);
        
        return intensity;
    }
    
    /**
     * Calculates realistic acceleration
     * Players don't instantly reach max speed
     */
    public static double getRealisticAcceleration(double currentSpeed, double targetSpeed) {
        double difference = targetSpeed - currentSpeed;
        
        // Natural acceleration curve (not instant, not linear)
        double acceleration = difference * 0.3; // 30% per tick toward target
        
        // Add micro-variations
        acceleration += (random.nextDouble() - 0.5) * 0.02;
        
        // Fatigue reduces acceleration
        acceleration *= (1.0 - HumanBehaviorSimulator.getFatigueLevel() * 0.2);
        
        return acceleration;
    }
    
    /**
     * Simulates natural direction changes
     * Players don't snap 180° instantly
     */
    public static float getNaturalDirectionChange(float currentYaw, float targetYaw) {
        float difference = MathHelper.wrapDegrees(targetYaw - currentYaw);
        
        // Maximum turn rate per tick (realistic mouse movement)
        float maxTurnRate = 15.0f + (float)(random.nextDouble() * 5.0); // 15-20° per tick
        
        // Skill affects turn rate
        maxTurnRate *= (0.7f + (float)HumanBehaviorSimulator.getFocusLevel() * 0.6f);
        
        // Clamp to max turn rate
        float turnAmount = MathHelper.clamp(difference, -maxTurnRate, maxTurnRate);
        
        // Add natural smoothing (ease in/out)
        if (Math.abs(difference) < 30) {
            // Slow down near target
            turnAmount *= 0.6f;
        }
        
        // Occasional overshoot then correct
        if (random.nextDouble() < 0.1) {
            turnAmount *= 1.2f; // 20% overshoot
        }
        
        return turnAmount;
    }
    
    /**
     * Simulates landing adjustment
     * Players adjust position slightly after landing
     */
    public static boolean shouldAdjustAfterLanding() {
        boolean justLanded = !wasOnGround && mc.player.isOnGround();
        
        if (justLanded) {
            return random.nextDouble() < 0.4; // 40% chance
        }
        
        return false;
    }
    
    /**
     * Gets landing adjustment vector
     */
    public static Vec3d getLandingAdjustment() {
        double x = (random.nextDouble() - 0.5) * 0.3;
        double z = (random.nextDouble() - 0.5) * 0.3;
        return new Vec3d(x, 0, z);
    }
    
    /**
     * Simulates W-tapping (releasing W momentarily for combos)
     * Advanced PvP technique
     */
    public static boolean shouldWTap() {
        if (HumanBehaviorSimulator.getStressLevel() > 0.5) {
            // Only in combat
            double wTapChance = 0.15; // 15% chance
            
            // Skill affects W-tap frequency
            wTapChance *= HumanBehaviorSimulator.getFocusLevel();
            
            return random.nextDouble() < wTapChance;
        }
        return false;
    }
    
    /**
     * Gets W-tap duration (how long to release W)
     */
    public static long getWTapDuration() {
        // Typically 50-100ms
        return 50 + random.nextLong(50);
    }
    
    /**
     * Simulates circle strafing pattern
     * Common PvP movement
     */
    public static CircleStrafeData getCircleStrafePattern(Entity target) {
        if (target == null) return null;
        
        CircleStrafeData data = new CircleStrafeData();
        
        // Determine strafe direction (clockwise or counter-clockwise)
        data.direction = random.nextBoolean() ? 1 : -1;
        
        // Vary circle radius (2-4 blocks)
        data.radius = 2.0 + random.nextDouble() * 2.0;
        
        // Speed around circle
        data.angularVelocity = 0.1 + random.nextDouble() * 0.1; // radians per tick
        
        // Add imperfection - not perfect circle
        data.radiusVariation = (random.nextDouble() - 0.5) * 0.5;
        
        return data;
    }
    
    /**
     * Simulates block clutch timing
     * Emergency block placement when falling
     */
    public static boolean shouldAttemptClutch() {
        if (mc.player == null) return false;
        
        // Only when falling
        if (mc.player.getVelocity().y > -0.5) return false;
        
        // Distance from ground
        double groundDistance = getDistanceToGround();
        
        if (groundDistance < 10 && groundDistance > 3) {
            // Skill-based clutch timing
            double clutchWindow = 0.3 * HumanBehaviorSimulator.getFocusLevel();
            
            // Stressed = worse timing
            clutchWindow *= (1.0 - HumanBehaviorSimulator.getStressLevel() * 0.5);
            
            return random.nextDouble() < clutchWindow;
        }
        
        return false;
    }
    
    /**
     * Gets distance to ground
     */
    private static double getDistanceToGround() {
        if (mc.player == null || mc.world == null) return 0;
        
        for (int i = 0; i < 50; i++) {
            if (!mc.world.getBlockState(mc.player.getBlockPos().down(i)).isAir()) {
                return i;
            }
        }
        return 50;
    }
    
    /**
     * Simulates parkour momentum
     * Players maintain momentum differently based on skill
     */
    public static double getParkourMomentumMultiplier() {
        double multiplier = 1.0;
        
        // Skilled players maintain momentum better
        multiplier += HumanBehaviorSimulator.getFocusLevel() * 0.2;
        
        // Fatigue reduces momentum control
        multiplier -= HumanBehaviorSimulator.getFatigueLevel() * 0.15;
        
        // Natural variation
        multiplier += (random.nextDouble() - 0.5) * 0.1;
        
        return Math.max(0.8, Math.min(1.2, multiplier));
    }
    
    /**
     * Simulates stop-and-go pattern
     * Players sometimes stop moving to think/look
     */
    public static boolean shouldStopMoving() {
        if (HumanBehaviorSimulator.shouldPauseToThink()) {
            return true;
        }
        
        // Random hesitation
        return random.nextDouble() < 0.01; // 1% chance
    }
    
    /**
     * Gets movement pattern for current situation
     */
    public static MovementPattern determineMovementPattern() {
        if (mc.player == null) return MovementPattern.NORMAL;
        
        // Check if in combat
        if (HumanBehaviorSimulator.getStressLevel() > 0.6) {
            return MovementPattern.COMBAT;
        }
        
        // Check if building
        if (mc.player.isSneaking()) {
            return MovementPattern.EDGE_PLACEMENT;
        }
        
        // Check if parkour
        if (!mc.player.isOnGround() && mc.player.getVelocity().y < -0.1) {
            return MovementPattern.PARKOUR;
        }
        
        return MovementPattern.NORMAL;
    }
    
    public static class MovementInput {
        public float forward = 0.0f;
        public float strafe = 0.0f;
        public boolean jump = false;
        public boolean sprint = false;
        public boolean sneak = false;
    }
    
    public static class StrafePattern {
        public double direction; // -1 to 1
        public double intensity; // 0 to 1
        
        public StrafePattern(double direction, double intensity) {
            this.direction = direction;
            this.intensity = intensity;
        }
    }
    
    public static class CircleStrafeData {
        public int direction; // 1 or -1
        public double radius;
        public double angularVelocity;
        public double radiusVariation;
    }
    
    public enum MovementPattern {
        NORMAL,
        COMBAT,
        PARKOUR,
        EDGE_PLACEMENT,
        OBSTACLE_CLEAR
    }
    
    private static class MovementAction {
        String type;
        long duration;
        long startTime;
        
        MovementAction(String type, long duration) {
            this.type = type;
            this.duration = duration;
            this.startTime = System.currentTimeMillis();
        }
        
        boolean isComplete() {
            return System.currentTimeMillis() - startTime >= duration;
        }
    }
}
