package com.gabrielsk.ai;

import com.gabrielsk.math.MathUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.util.*;

/**
 * Advanced Rotation Simulator
 * Perfectly mimics human mouse movement:
 * - Bezier curves for smooth transitions
 * - Overshoot and correction
 * - Micro-adjustments
 * - Tracking delays
 * - Prediction errors
 * - Natural jitter
 * - Mouse acceleration/deceleration
 * - Eye tracking patterns
 */
public class RotationSimulator {
    
    private static final MinecraftClient mc = MinecraftClient.getInstance();
    private static final Random random = new Random();
    
    // Rotation state
    private static float currentYaw = 0;
    private static float currentPitch = 0;
    private static float targetYaw = 0;
    private static float targetPitch = 0;
    private static long rotationStartTime = 0;
    private static boolean isRotating = false;
    
    // Smooth rotation data
    private static List<Vec3d> bezierControlPoints = new ArrayList<>();
    private static double bezierProgress = 0.0;
    private static boolean hasOvershot = false;
    
    // Micro-adjustment tracking
    private static int microAdjustments = 0;
    private static long lastMicroAdjust = 0;
    
    /**
     * Calculates human-like rotation toward target
     * Uses Bezier curves, overshoot, and micro-adjustments
     */
    public static RotationResult getHumanLikeRotation(Vec3d targetPos) {
        if (mc.player == null) return null;
        
        RotationResult result = new RotationResult();
        result.success = false;
        
        // Calculate target angles
        Vec3d eyePos = mc.player.getEyePos();
        Vec3d direction = targetPos.subtract(eyePos).normalize();
        
        float newTargetYaw = (float) Math.toDegrees(Math.atan2(direction.z, direction.x)) - 90;
        float newTargetPitch = (float) -Math.toDegrees(Math.asin(direction.y));
        
        // Add prediction error (humans can't calculate perfectly)
        if (HumanBehaviorSimulator.shouldMakeMistake()) {
            newTargetYaw += (random.nextFloat() - 0.5f) * 5.0f;
            newTargetPitch += (random.nextFloat() - 0.5f) * 3.0f;
        }
        
        // Start new rotation if target changed significantly
        float yawDiff = Math.abs(MathHelper.wrapDegrees(newTargetYaw - targetYaw));
        float pitchDiff = Math.abs(newTargetPitch - targetPitch);
        
        if (yawDiff > 5.0f || pitchDiff > 3.0f || !isRotating) {
            startNewRotation(newTargetYaw, newTargetPitch);
        }
        
        // Apply rotation with human characteristics
        applyHumanRotation(result);
        
        return result;
    }
    
    /**
     * Starts a new rotation sequence
     */
    private static void startNewRotation(float targetYaw, float targetPitch) {
        if (mc.player == null) return;
        
        currentYaw = mc.player.getYaw();
        currentPitch = mc.player.getPitch();
        RotationSimulator.targetYaw = targetYaw;
        RotationSimulator.targetPitch = targetPitch;
        
        rotationStartTime = System.currentTimeMillis();
        isRotating = true;
        hasOvershot = false;
        microAdjustments = 0;
        
        // Generate Bezier curve control points
        generateBezierPath();
    }
    
    /**
     * Generates Bezier curve for smooth rotation
     */
    private static void generateBezierPath() {
        bezierControlPoints.clear();
        
        // Start point
        Vec3d start = new Vec3d(currentYaw, currentPitch, 0);
        
        // End point
        Vec3d end = new Vec3d(targetYaw, targetPitch, 0);
        
        // Control points for natural curve
        float yawDiff = MathHelper.wrapDegrees(targetYaw - currentYaw);
        float pitchDiff = targetPitch - currentPitch;
        
        // Add overshoot to control points (humans overshoot then correct)
        double overshootAmount = HumanBehaviorSimulator.getOvershootAmount();
        
        Vec3d control1 = new Vec3d(
            currentYaw + yawDiff * 0.3,
            currentPitch + pitchDiff * 0.3,
            0
        );
        
        Vec3d control2 = new Vec3d(
            targetYaw + yawDiff * overshootAmount,
            targetPitch + pitchDiff * overshootAmount * 0.5,
            0
        );
        
        bezierControlPoints.add(start);
        bezierControlPoints.add(control1);
        bezierControlPoints.add(control2);
        bezierControlPoints.add(end);
        
        bezierProgress = 0.0;
    }
    
    /**
     * Applies human-like rotation with all characteristics
     */
    private static void applyHumanRotation(RotationResult result) {
        if (mc.player == null) return;
        
        // Calculate rotation speed based on human factors
        double rotationSpeed = calculateRotationSpeed();
        
        // Progress along Bezier curve
        bezierProgress += rotationSpeed;
        
        if (bezierProgress < 1.0) {
            // Still rotating - interpolate along curve
            Vec3d point = calculateBezierPoint(bezierProgress);
            
            result.yaw = (float) point.x;
            result.pitch = (float) point.y;
            
            // Add natural jitter (hand isn't perfectly steady)
            if (random.nextDouble() < 0.3) {
                result.yaw += (random.nextFloat() - 0.5f) * 0.3f;
                result.pitch += (random.nextFloat() - 0.5f) * 0.2f;
            }
            
            result.success = false; // Still rotating
            
        } else {
            // Reached target - apply micro-adjustments
            if (!hasOvershot && HumanBehaviorSimulator.shouldMicroAdjust()) {
                applyMicroAdjustment(result);
            } else {
                result.yaw = targetYaw;
                result.pitch = targetPitch;
                result.success = true;
                isRotating = false;
            }
        }
        
        // Apply finger slip (occasional wrong direction)
        if (HumanBehaviorSimulator.shouldFingerSlip()) {
            result.yaw += (random.nextFloat() - 0.5f) * 10.0f;
            result.pitch += (random.nextFloat() - 0.5f) * 5.0f;
        }
        
        // Clamp pitch to valid range
        result.pitch = MathHelper.clamp(result.pitch, -90.0f, 90.0f);
    }
    
    /**
     * Calculates rotation speed based on human factors
     */
    private static double calculateRotationSpeed() {
        double baseSpeed = HumanBehaviorSimulator.getMouseSpeed();
        
        // Distance affects speed (large movements are faster initially)
        float yawDiff = Math.abs(MathHelper.wrapDegrees(targetYaw - currentYaw));
        float pitchDiff = Math.abs(targetPitch - currentPitch);
        double totalDistance = Math.sqrt(yawDiff * yawDiff + pitchDiff * pitchDiff);
        
        if (totalDistance > 90) {
            baseSpeed *= 1.5; // Faster for large movements
        }
        
        // Ease in/out - start slow, speed up, slow down near end
        double easing = calculateEasing(bezierProgress);
        baseSpeed *= easing;
        
        // Add natural variation
        baseSpeed += (random.nextDouble() - 0.5) * 0.05;
        
        // Reaction time delay at start
        long timeSinceStart = System.currentTimeMillis() - rotationStartTime;
        if (timeSinceStart < HumanBehaviorSimulator.getReactionTime()) {
            baseSpeed *= 0.3; // Slow start (reaction delay)
        }
        
        return Math.max(0.01, Math.min(0.15, baseSpeed));
    }
    
    /**
     * Calculates easing function for natural acceleration
     */
    private static double calculateEasing(double progress) {
        // Ease in-out cubic
        if (progress < 0.5) {
            return 4 * progress * progress * progress;
        } else {
            double f = ((2 * progress) - 2);
            return 1 - (f * f * f) / 2;
        }
    }
    
    /**
     * Calculates point on Bezier curve
     */
    private static Vec3d calculateBezierPoint(double t) {
        if (bezierControlPoints.size() != 4) return Vec3d.ZERO;
        
        double u = 1 - t;
        double tt = t * t;
        double uu = u * u;
        double uuu = uu * u;
        double ttt = tt * t;
        
        Vec3d p = bezierControlPoints.get(0).multiply(uuu);
        p = p.add(bezierControlPoints.get(1).multiply(3 * uu * t));
        p = p.add(bezierControlPoints.get(2).multiply(3 * u * tt));
        p = p.add(bezierControlPoints.get(3).multiply(ttt));
        
        return p;
    }
    
    /**
     * Applies micro-adjustment after reaching target
     * Humans make tiny corrections at the end
     */
    private static void applyMicroAdjustment(RotationResult result) {
        long timeSinceLastAdjust = System.currentTimeMillis() - lastMicroAdjust;
        
        if (timeSinceLastAdjust > 100 && microAdjustments < 3) {
            double adjustAmount = HumanBehaviorSimulator.getMicroAdjustmentAmount();
            
            result.yaw = targetYaw + (float) (adjustAmount * 2);
            result.pitch = targetPitch + (float) (adjustAmount);
            
            microAdjustments++;
            lastMicroAdjust = System.currentTimeMillis();
            result.success = false;
        } else {
            result.yaw = targetYaw;
            result.pitch = targetPitch;
            result.success = true;
            isRotating = false;
        }
    }
    
    /**
     * Simulates tracking a moving target
     * Players track ahead of target when it's moving
     */
    public static RotationResult getTrackingRotation(Entity target) {
        if (mc.player == null || target == null) return null;
        
        Vec3d targetPos = target.getPos();
        
        // Predict target position
        double predictionTime = 0.1 + (1.0 - HumanBehaviorSimulator.getAimAccuracy("tracking")) * 0.1;
        Vec3d predictedPos = MathUtils.predictEntityPosition(target, predictionTime);
        
        // Add tracking error (humans don't track perfectly)
        predictedPos = HumanBehaviorSimulator.addPredictionError(predictedPos);
        
        // Apply tracking lag (humans react with delay)
        long trackingDelay = (long) (50 + (1.0 - HumanBehaviorSimulator.getFocusLevel()) * 100);
        
        // Simulate delay by using slightly old target position
        Vec3d delayedPos = Vec3d.lerp(
            (double) trackingDelay / 200.0,
            target.getPos(),
            predictedPos
        );
        
        return getHumanLikeRotation(delayedPos);
    }
    
    /**
     * Simulates flick shot rotation
     * Fast, imperfect snap to target
     */
    public static RotationResult getFlickRotation(Vec3d targetPos) {
        // Flick shots are faster but less accurate
        double accuracy = HumanBehaviorSimulator.getAimAccuracy("flick") * 0.8;
        
        // Add larger error margin for flicks
        Vec3d adjustedTarget = targetPos.add(
            (random.nextDouble() - 0.5) * (1.0 - accuracy) * 2.0,
            (random.nextDouble() - 0.5) * (1.0 - accuracy),
            (random.nextDouble() - 0.5) * (1.0 - accuracy) * 2.0
        );
        
        // Force faster rotation
        double oldProgress = bezierProgress;
        RotationResult result = getHumanLikeRotation(adjustedTarget);
        bezierProgress = Math.min(1.0, oldProgress + 0.3); // 3x faster
        
        return result;
    }
    
    /**
     * Simulates "checking corners" rotation pattern
     * Players sweep their view when exploring
     */
    public static RotationResult getCornerCheckRotation() {
        if (mc.player == null) return null;
        
        RotationResult result = new RotationResult();
        
        // Random horizontal sweep
        float sweepAngle = random.nextFloat() * 60 - 30; // ±30 degrees
        float sweepPitch = random.nextFloat() * 20 - 10; // ±10 degrees
        
        result.yaw = mc.player.getYaw() + sweepAngle;
        result.pitch = mc.player.getPitch() + sweepPitch;
        result.success = false;
        
        return result;
    }
    
    /**
     * Simulates looking around when idle
     * Players naturally look around when not focused
     */
    public static RotationResult getIdleLookAround() {
        if (mc.player == null) return null;
        
        // Slow, wandering camera movement
        RotationResult result = new RotationResult();
        
        long time = System.currentTimeMillis();
        double noise1 = Math.sin(time * 0.0003) * 15;
        double noise2 = Math.cos(time * 0.0002) * 8;
        
        result.yaw = mc.player.getYaw() + (float) noise1;
        result.pitch = mc.player.getPitch() + (float) noise2;
        result.success = false;
        
        return result;
    }
    
    /**
     * Simulates double-take (look away and look back)
     */
    public static boolean isDoubleTaking() {
        return HumanBehaviorSimulator.shouldDoubleTake();
    }
    
    /**
     * Gets rotation for looking at specific point with human characteristics
     */
    public static RotationResult lookAtWithHumanError(Vec3d pos, double maxError) {
        // Add position error
        Vec3d errorVec = new Vec3d(
            (random.nextDouble() - 0.5) * maxError,
            (random.nextDouble() - 0.5) * maxError * 0.5,
            (random.nextDouble() - 0.5) * maxError
        );
        
        Vec3d adjustedPos = pos.add(errorVec);
        return getHumanLikeRotation(adjustedPos);
    }
    
    /**
     * Calculates if current rotation is close enough to target
     */
    public static boolean isOnTarget(float tolerance) {
        float yawDiff = Math.abs(MathHelper.wrapDegrees(currentYaw - targetYaw));
        float pitchDiff = Math.abs(currentPitch - targetPitch);
        
        return yawDiff < tolerance && pitchDiff < tolerance;
    }
    
    /**
     * Simulates panic rotation (fast, erratic when stressed)
     */
    public static RotationResult getPanicRotation(Vec3d targetPos) {
        if (HumanBehaviorSimulator.getStressLevel() < 0.7) {
            return getHumanLikeRotation(targetPos);
        }
        
        // Panic = fast, shaky, inaccurate
        RotationResult result = getHumanLikeRotation(targetPos);
        
        // Add shake
        if (result != null) {
            result.yaw += (random.nextFloat() - 0.5f) * 5.0f;
            result.pitch += (random.nextFloat() - 0.5f) * 3.0f;
        }
        
        // Force faster (less smooth)
        bezierProgress = Math.min(1.0, bezierProgress + 0.2);
        
        return result;
    }
    
    /**
     * Resets rotation state
     */
    public static void reset() {
        isRotating = false;
        bezierProgress = 0.0;
        hasOvershot = false;
        microAdjustments = 0;
        bezierControlPoints.clear();
    }
    
    public static class RotationResult {
        public float yaw;
        public float pitch;
        public boolean success; // True if target reached
        
        public RotationResult() {
            this.yaw = 0;
            this.pitch = 0;
            this.success = false;
        }
        
        public RotationResult(float yaw, float pitch, boolean success) {
            this.yaw = yaw;
            this.pitch = pitch;
            this.success = success;
        }
    }
    
    // Getters for debugging
    public static boolean isCurrentlyRotating() { return isRotating; }
    public static double getRotationProgress() { return bezierProgress; }
    public static int getMicroAdjustmentCount() { return microAdjustments; }
}
