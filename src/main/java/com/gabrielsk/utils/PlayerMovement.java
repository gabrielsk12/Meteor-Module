package com.gabrielsk.utils;

import com.gabrielsk.ai.HumanBehaviorSimulator;
import com.gabrielsk.ai.RotationSimulator;
import com.gabrielsk.ai.MovementPatternSimulator;
import com.gabrielsk.math.MathUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;

import java.util.Random;

/**
 * ADVANCED Player-like movement utilities
 * Makes bot movements EXACTLY like real players
 * Now uses HumanBehaviorSimulator, RotationSimulator, and MovementPatternSimulator
 * for perfect human replication
 */
public class PlayerMovement {
    
    private static final MinecraftClient mc = MinecraftClient.getInstance();
    private static final Random random = new Random();
    
    // Legacy support
    private static long lastReactionTime = 0;
    private static final long MIN_REACTION_TIME = 80;
    private static final long MAX_REACTION_TIME = 200;
    
    // Movement patterns
    private static long lastMovementChange = 0;
    private static Vec3d movementTarget = Vec3d.ZERO;
    
    // Rotation state
    private static float currentYaw = 0;
    private static float currentPitch = 0;
    private static float desiredYaw = 0;
    private static float desiredPitch = 0;
    private static float targetYaw = 0;
    private static float targetPitch = 0;
    
    /**
     * NEW: Perfect human-like rotation using advanced AI
     * Uses Bezier curves, overshoot, micro-adjustments, and all human characteristics
     */
    public static void humanRotate(PlayerEntity player, Vec3d target) {
        if (player == null) return;
        
        // Update human state (fatigue, stress, focus)
        HumanBehaviorSimulator.updateHumanState();
        
        // Check if should hesitate before rotating
        if (HumanBehaviorSimulator.shouldHesitate()) {
            try {
                Thread.sleep(HumanBehaviorSimulator.getHesitationDuration());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        
        // Get human-like rotation with all characteristics
        RotationSimulator.RotationResult rotation = RotationSimulator.getHumanLikeRotation(target);
        
        if (rotation != null) {
            player.setYaw(rotation.yaw);
            player.setPitch(rotation.pitch);
            
            // Record action for learning
            HumanBehaviorSimulator.recordAction("rotation", rotation.success);
        }
    }
    
    /**
     * NEW: Human-like tracking rotation for moving targets
     */
    public static void humanTrack(PlayerEntity player, Entity target) {
        if (player == null || target == null) return;
        
        HumanBehaviorSimulator.updateHumanState();
        HumanBehaviorSimulator.setFocusedEntity(target);
        
        RotationSimulator.RotationResult rotation = RotationSimulator.getTrackingRotation(target);
        
        if (rotation != null) {
            player.setYaw(rotation.yaw);
            player.setPitch(rotation.pitch);
            
            HumanBehaviorSimulator.recordAction("tracking", rotation.success);
        }
    }
    
    /**
     * NEW: Flick shot rotation (fast snap for PvP)
     */
    public static void humanFlick(PlayerEntity player, Vec3d target) {
        if (player == null) return;
        
        HumanBehaviorSimulator.updateHumanState();
        
        RotationSimulator.RotationResult rotation = RotationSimulator.getFlickRotation(target);
        
        if (rotation != null) {
            player.setYaw(rotation.yaw);
            player.setPitch(rotation.pitch);
            
            HumanBehaviorSimulator.recordAction("flick", rotation.success);
        }
    }
    
    /**
     * NEW: Get human-like movement input
     */
    public static MovementPatternSimulator.MovementInput getHumanMovementInput(Vec3d targetDirection) {
        HumanBehaviorSimulator.updateHumanState();
        return MovementPatternSimulator.getNaturalMovementInput(targetDirection);
    }
    
    /**
     * LEGACY: Old smooth rotate method (kept for compatibility)
     * Now uses new AI system underneath
     */
    public static void smoothRotate(Vec3d target, double smoothness) {
        if (mc.player == null) return;
        
        // Redirect to new human rotate
        humanRotate(mc.player, target);
        
        // Smooth interpolation
        currentYaw = MathUtils.lerpAngle(currentYaw, desiredYaw, (float) smoothness);
        currentPitch = MathUtils.lerpAngle(currentPitch, desiredPitch, (float) smoothness);
        
        // Apply rotation
        mc.player.setYaw(currentYaw);
        mc.player.setPitch((float) MathUtils.clamp(currentPitch, -90, 90));
    }
    
    /**
     * Advanced smooth rotation with acceleration/deceleration
     * Mimics real mouse movement physics
     */
    public static void humanRotate(Vec3d target) {
        if (mc.player == null) return;
        
        Vec3d playerPos = mc.player.getEyePos();
        
        float desiredYaw = MathUtils.getYaw(playerPos, target);
        float desiredPitch = MathUtils.getPitch(playerPos, target);
        
        float yawDiff = MathUtils.normalizeAngle(desiredYaw - currentYaw);
        float pitchDiff = MathUtils.normalizeAngle(desiredPitch - currentPitch);
        
        // Distance-based speed (faster for large movements, slower for precision)
        float distance = (float) Math.sqrt(yawDiff * yawDiff + pitchDiff * pitchDiff);
        float speed = (float) MathUtils.clamp(distance / 20.0f, 0.05f, 0.3f);
        
        // Add micro-corrections (human hand tremor simulation)
        if (distance < 5.0f) {
            speed *= 0.5f;
            yawDiff += (float) MathUtils.gaussianRandom(0, 0.2);
            pitchDiff += (float) MathUtils.gaussianRandom(0, 0.1);
        }
        
        // Apply with easing
        currentYaw += yawDiff * speed;
        currentPitch += pitchDiff * speed;
        
        currentYaw = MathUtils.normalizeAngle(currentYaw);
        currentPitch = (float) MathUtils.clamp(currentPitch, -90, 90);
        
        mc.player.setYaw(currentYaw);
        mc.player.setPitch(currentPitch);
    }
    
    /**
     * Strafe around target (combat movement)
     * Uses circular motion with random variations
     */
    public static void strafeAroundTarget(Vec3d target, double radius, boolean clockwise) {
        if (mc.player == null) return;
        
        Vec3d playerPos = mc.player.getPos();
        Vec3d toTarget = target.subtract(playerPos).normalize();
        
        // Calculate strafe direction (perpendicular to target direction)
        double angle = clockwise ? -Math.PI / 2 : Math.PI / 2;
        Vec3d strafeDir = MathUtils.rotateY(toTarget, angle);
        
        // Add randomness to look more human
        double randomOffset = MathUtils.gaussianRandom(0, 0.1);
        strafeDir = strafeDir.multiply(1.0 + randomOffset);
        
        // Apply movement
        Vec3d desiredPos = playerPos.add(strafeDir.multiply(0.2));
        moveTowards(desiredPos);
        
        // Look at target while strafing
        humanRotate(target);
    }
    
    /**
     * Move towards position with human-like path variations
     */
    public static void moveTowards(Vec3d target) {
        if (mc.player == null) return;
        
        Vec3d playerPos = mc.player.getPos();
        Vec3d direction = target.subtract(playerPos).normalize();
        
        // Add slight path deviation (humans don't walk perfectly straight)
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastMovementChange > 500) {
            double deviation = MathUtils.gaussianRandom(0, 0.1);
            direction = MathUtils.rotateY(direction, deviation);
            lastMovementChange = currentTime;
        }
        
        // Calculate yaw for movement
        float moveYaw = (float) Math.toDegrees(Math.atan2(direction.z, direction.x)) - 90;
        mc.player.setYaw(moveYaw);
        
        // Set movement input
        mc.player.input.pressingForward = true;
        
        // Jump if needed
        if (shouldJump(target)) {
            mc.player.input.jumping = true;
        }
    }
    
    /**
     * Determine if player should jump
     */
    private static boolean shouldJump(Vec3d target) {
        if (mc.player == null) return false;
        
        // Jump if target is higher
        if (target.y > mc.player.getY() + 0.6) {
            return true;
        }
        
        // Jump if colliding horizontally
        if (mc.player.horizontalCollision) {
            return true;
        }
        
        // Random occasional jumps (human behavior)
        return random.nextDouble() < 0.01;
    }
    
    /**
     * Stop all movement
     */
    public static void stopMovement() {
        if (mc.player == null) return;
        
        mc.player.input.pressingForward = false;
        mc.player.input.pressingBack = false;
        mc.player.input.pressingLeft = false;
        mc.player.input.pressingRight = false;
        mc.player.input.jumping = false;
        mc.player.input.sneaking = false;
    }
    
    /**
     * Random idle movement (looking around like real player)
     */
    public static void idleLookAround() {
        if (mc.player == null) return;
        
        long currentTime = System.currentTimeMillis();
        
        // Occasionally look in random direction
        if (currentTime - lastReactionTime > random.nextInt(3000) + 1000) {
            targetYaw = mc.player.getYaw() + (float) MathUtils.gaussianRandom(0, 30);
            targetPitch = (float) MathUtils.gaussianRandom(0, 20);
            lastReactionTime = currentTime;
        }
        
        // Smoothly transition
        currentYaw = MathUtils.lerpAngle(currentYaw, targetYaw, 0.05f);
        currentPitch = MathUtils.lerpAngle(currentPitch, targetPitch, 0.05f);
        
        mc.player.setYaw(currentYaw);
        mc.player.setPitch((float) MathUtils.clamp(currentPitch, -30, 30));
    }
    
    /**
     * Simulate human reaction delay
     */
    public static boolean shouldReact() {
        long currentTime = System.currentTimeMillis();
        long timeSinceLastReaction = currentTime - lastReactionTime;
        long reactionDelay = MIN_REACTION_TIME + random.nextInt((int)(MAX_REACTION_TIME - MIN_REACTION_TIME));
        
        if (timeSinceLastReaction >= reactionDelay) {
            lastReactionTime = currentTime;
            return true;
        }
        
        return false;
    }
    
    /**
     * Predict where player will be based on current movement
     */
    public static Vec3d predictPlayerPosition(double ticksAhead) {
        if (mc.player == null) return Vec3d.ZERO;
        
        Vec3d currentPos = mc.player.getPos();
        Vec3d velocity = mc.player.getVelocity();
        
        return MathUtils.predictTrajectory(currentPos, velocity, ticksAhead, 0.08);
    }
    
    /**
     * Navigate with A* pathfinding (uses advanced AI system)
     */
    public static void navigateWithPathfinding(Vec3d target) {
        // This will be integrated with AdvancedAIBot's pathfinding
        // For now, use simple movement
        moveTowards(target);
    }
    
    /**
     * Sprint management (human-like)
     */
    public static void manageSprint() {
        if (mc.player == null) return;
        
        // Sprint when moving forward
        boolean shouldSprint = mc.player.input.pressingForward && 
                             mc.player.getHungerManager().getFoodLevel() > 6;
        
        // Add occasional sprint stops (humans don't sprint constantly)
        if (random.nextDouble() < 0.005) {
            shouldSprint = false;
        }
        
        mc.player.setSprinting(shouldSprint);
    }
    
    /**
     * Crouch/sneak management
     */
    public static void manageCrouch(boolean shouldSneak) {
        if (mc.player == null) return;
        
        mc.player.input.sneaking = shouldSneak;
    }
}
