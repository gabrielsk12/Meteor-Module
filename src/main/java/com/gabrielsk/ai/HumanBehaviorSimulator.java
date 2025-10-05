package com.gabrielsk.ai;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;

import java.util.*;

/**
 * Advanced Human Behavior Simulator
 * Makes AI indistinguishable from real players by simulating:
 * - Reaction times with variance
 * - Attention management (focus/distraction)
 * - Fatigue over time
 * - Learning curves
 * - Emotional states affecting performance
 * - Muscle memory simulation
 * - Prediction errors
 * - Decision hesitation
 */
public class HumanBehaviorSimulator {
    
    private static final MinecraftClient mc = MinecraftClient.getInstance();
    private static final Random random = new Random();
    
    // Human state variables
    private static double fatigueLevel = 0.0; // 0.0 = fresh, 1.0 = exhausted
    private static double focusLevel = 1.0; // 0.0 = distracted, 1.0 = focused
    private static double stressLevel = 0.0; // 0.0 = calm, 1.0 = panicking
    private static EmotionalState emotionalState = EmotionalState.NEUTRAL;
    
    // Session tracking
    private static long sessionStartTime = System.currentTimeMillis();
    private static int actionsPerformed = 0;
    private static Map<String, SkillLevel> skillLevels = new HashMap<>();
    private static Queue<Long> recentActionTimes = new LinkedList<>();
    
    // Attention system
    private static Entity focusedEntity = null;
    private static long lastFocusChange = 0;
    private static double attentionSpan = 5000; // ms before attention drifts
    
    // Muscle memory
    private static Map<String, Integer> actionRepetitions = new HashMap<>();
    private static Map<String, Double> actionAccuracy = new HashMap<>();
    
    /**
     * Gets realistic human reaction time based on current state
     * Real human reaction times: 200-300ms average
     * With fatigue/stress: 300-500ms
     * With focus/practice: 150-250ms
     */
    public static long getReactionTime() {
        double baseReactionTime = 250.0; // ms
        
        // Fatigue increases reaction time (up to +100ms)
        baseReactionTime += fatigueLevel * 100;
        
        // Focus decreases reaction time (up to -50ms)
        baseReactionTime -= (focusLevel - 0.5) * 100;
        
        // Stress increases reaction time (up to +150ms)
        baseReactionTime += stressLevel * 150;
        
        // Emotional state affects reaction time
        switch (emotionalState) {
            case EXCITED -> baseReactionTime -= 30;
            case ANXIOUS -> baseReactionTime += 50;
            case TIRED -> baseReactionTime += 80;
            case ANGRY -> baseReactionTime += 40;
        }
        
        // Add Gaussian noise for realism
        double noise = random.nextGaussian() * 30;
        baseReactionTime += noise;
        
        // Clamp to realistic values (100-600ms)
        baseReactionTime = Math.max(100, Math.min(600, baseReactionTime));
        
        return (long) baseReactionTime;
    }
    
    /**
     * Simulates human aiming with imperfection
     * Returns accuracy multiplier (0.0 = completely off, 1.0 = perfect)
     */
    public static double getAimAccuracy(String actionType) {
        double baseAccuracy = 0.85; // 85% base accuracy
        
        // Skill level improves accuracy
        SkillLevel skill = skillLevels.getOrDefault(actionType, SkillLevel.BEGINNER);
        baseAccuracy += skill.accuracyBonus;
        
        // Fatigue reduces accuracy
        baseAccuracy -= fatigueLevel * 0.2;
        
        // Focus improves accuracy
        baseAccuracy += (focusLevel - 0.5) * 0.3;
        
        // Stress reduces accuracy significantly
        baseAccuracy -= stressLevel * 0.3;
        
        // Emotional state affects accuracy
        switch (emotionalState) {
            case FOCUSED -> baseAccuracy += 0.1;
            case ANXIOUS -> baseAccuracy -= 0.15;
            case TIRED -> baseAccuracy -= 0.2;
            case ANGRY -> baseAccuracy -= 0.1;
        }
        
        // Muscle memory improves accuracy over time
        int repetitions = actionRepetitions.getOrDefault(actionType, 0);
        double muscleMemoryBonus = Math.min(0.15, repetitions * 0.001);
        baseAccuracy += muscleMemoryBonus;
        
        // Add small random variation
        baseAccuracy += (random.nextDouble() - 0.5) * 0.1;
        
        // Clamp to realistic values
        return Math.max(0.3, Math.min(1.0, baseAccuracy));
    }
    
    /**
     * Determines if player should hesitate before action
     * Humans don't always act instantly - they think, doubt, reconsider
     */
    public static boolean shouldHesitate() {
        // Higher chance when stressed or unfocused
        double hesitationChance = 0.05; // 5% base chance
        
        hesitationChance += stressLevel * 0.15; // Up to +15% when stressed
        hesitationChance += (1.0 - focusLevel) * 0.10; // Up to +10% when unfocused
        
        // Low skill = more hesitation
        hesitationChance += (1.0 - getAverageSkillLevel()) * 0.08;
        
        return random.nextDouble() < hesitationChance;
    }
    
    /**
     * Gets hesitation duration in milliseconds
     */
    public static long getHesitationDuration() {
        double baseDuration = 150.0; // ms
        
        baseDuration += stressLevel * 200; // More stress = longer hesitation
        baseDuration += (1.0 - focusLevel) * 100;
        baseDuration += random.nextGaussian() * 50;
        
        return (long) Math.max(50, Math.min(500, baseDuration));
    }
    
    /**
     * Simulates human prediction error
     * Humans can't perfectly predict - they overshoot, undershoot, misjudge
     */
    public static Vec3d addPredictionError(Vec3d predicted) {
        double errorMagnitude = 0.1; // Base 10cm error
        
        // Skill reduces error
        errorMagnitude *= (1.0 - getAverageSkillLevel() * 0.5);
        
        // Fatigue increases error
        errorMagnitude *= (1.0 + fatigueLevel);
        
        // Stress increases error
        errorMagnitude *= (1.0 + stressLevel);
        
        // Focus reduces error
        errorMagnitude *= (2.0 - focusLevel);
        
        // Add random directional error
        double errorX = (random.nextDouble() - 0.5) * errorMagnitude;
        double errorY = (random.nextDouble() - 0.5) * errorMagnitude * 0.5; // Less Y error
        double errorZ = (random.nextDouble() - 0.5) * errorMagnitude;
        
        return predicted.add(errorX, errorY, errorZ);
    }
    
    /**
     * Determines if player makes a mistake
     * Real players occasionally click wrong, miss, fat finger, etc.
     */
    public static boolean shouldMakeMistake() {
        double mistakeChance = 0.02; // 2% base mistake rate
        
        mistakeChance += fatigueLevel * 0.05; // Tired = more mistakes
        mistakeChance += stressLevel * 0.08; // Stressed = more mistakes
        mistakeChance += (1.0 - focusLevel) * 0.06; // Unfocused = more mistakes
        
        // Skill reduces mistakes
        mistakeChance *= (1.0 - getAverageSkillLevel() * 0.3);
        
        return random.nextDouble() < mistakeChance;
    }
    
    /**
     * Simulates attention span and focus changes
     * Humans don't maintain perfect focus - they get distracted
     */
    public static boolean shouldChangeAttention() {
        long timeSinceFocusChange = System.currentTimeMillis() - lastFocusChange;
        
        // Attention span varies with focus level
        double effectiveAttentionSpan = attentionSpan * focusLevel;
        
        if (timeSinceFocusChange > effectiveAttentionSpan) {
            // Natural attention drift
            if (random.nextDouble() < 0.3) { // 30% chance to drift when time is up
                return true;
            }
        }
        
        // Random sudden distractions (rare)
        if (random.nextDouble() < 0.001) { // 0.1% per check
            return true;
        }
        
        return false;
    }
    
    /**
     * Updates human state based on session time and activity
     * Simulates getting tired, stressed, or entering flow state
     */
    public static void updateHumanState() {
        long sessionDuration = System.currentTimeMillis() - sessionStartTime;
        
        // Fatigue increases over time
        double sessionMinutes = sessionDuration / 60000.0;
        fatigueLevel = Math.min(1.0, sessionMinutes / 120.0); // Max fatigue after 2 hours
        
        // Calculate recent action rate
        recentActionTimes.removeIf(time -> System.currentTimeMillis() - time > 10000);
        double actionsPerSecond = recentActionTimes.size() / 10.0;
        
        // High activity increases stress
        if (actionsPerSecond > 5) {
            stressLevel = Math.min(1.0, stressLevel + 0.01);
        } else {
            stressLevel = Math.max(0.0, stressLevel - 0.005);
        }
        
        // Focus decreases with fatigue
        focusLevel = Math.max(0.3, 1.0 - (fatigueLevel * 0.5));
        
        // Moderate activity with low stress = flow state
        if (actionsPerSecond > 2 && actionsPerSecond < 6 && stressLevel < 0.3) {
            focusLevel = Math.min(1.0, focusLevel + 0.02);
            emotionalState = EmotionalState.FOCUSED;
        }
        
        // High stress = anxiety
        if (stressLevel > 0.7) {
            emotionalState = EmotionalState.ANXIOUS;
        }
        
        // High fatigue = tired
        if (fatigueLevel > 0.7) {
            emotionalState = EmotionalState.TIRED;
        }
        
        // Update emotional state periodically
        if (random.nextDouble() < 0.001) {
            emotionalState = EmotionalState.values()[random.nextInt(EmotionalState.values().length)];
        }
    }
    
    /**
     * Records an action for learning and state tracking
     */
    public static void recordAction(String actionType, boolean successful) {
        actionsPerformed++;
        recentActionTimes.add(System.currentTimeMillis());
        
        // Update skill level
        SkillLevel currentSkill = skillLevels.getOrDefault(actionType, SkillLevel.BEGINNER);
        int repetitions = actionRepetitions.getOrDefault(actionType, 0) + 1;
        actionRepetitions.put(actionType, repetitions);
        
        // Update accuracy tracking
        double currentAccuracy = actionAccuracy.getOrDefault(actionType, 0.5);
        double newAccuracy = currentAccuracy * 0.95 + (successful ? 1.0 : 0.0) * 0.05;
        actionAccuracy.put(actionType, newAccuracy);
        
        // Skill improvement over time
        if (repetitions > 50 && currentSkill == SkillLevel.BEGINNER) {
            skillLevels.put(actionType, SkillLevel.INTERMEDIATE);
        } else if (repetitions > 200 && currentSkill == SkillLevel.INTERMEDIATE) {
            skillLevels.put(actionType, SkillLevel.ADVANCED);
        } else if (repetitions > 500 && currentSkill == SkillLevel.ADVANCED) {
            skillLevels.put(actionType, SkillLevel.EXPERT);
        }
        
        // Keep queue size manageable
        if (recentActionTimes.size() > 100) {
            recentActionTimes.poll();
        }
    }
    
    /**
     * Gets natural mouse movement speed
     * Humans don't move mouse at constant speed
     */
    public static double getMouseSpeed() {
        double baseSpeed = 0.3; // Base rotation speed
        
        // Skill increases speed
        baseSpeed += getAverageSkillLevel() * 0.2;
        
        // Fatigue decreases speed
        baseSpeed -= fatigueLevel * 0.1;
        
        // Add natural variation
        baseSpeed += (random.nextDouble() - 0.5) * 0.1;
        
        return Math.max(0.1, Math.min(0.8, baseSpeed));
    }
    
    /**
     * Determines if player should do a "double take"
     * Humans sometimes look away and look back to confirm
     */
    public static boolean shouldDoubleTake() {
        return random.nextDouble() < 0.03; // 3% chance
    }
    
    /**
     * Simulates finger slip / typo equivalent
     * Mouse moves wrong direction momentarily
     */
    public static boolean shouldFingerSlip() {
        double slipChance = 0.01; // 1% base
        slipChance += fatigueLevel * 0.02;
        slipChance += stressLevel * 0.03;
        
        return random.nextDouble() < slipChance;
    }
    
    /**
     * Gets overshoot amount for target acquisition
     * Humans often overshoot then correct
     */
    public static double getOvershootAmount() {
        if (random.nextDouble() < 0.4) { // 40% chance to overshoot
            double overshoot = 0.1 + random.nextDouble() * 0.2; // 10-30% overshoot
            overshoot *= (1.0 - getAverageSkillLevel() * 0.5); // Skilled players overshoot less
            return overshoot;
        }
        return 0.0;
    }
    
    /**
     * Simulates micro-adjustments
     * After reaching target, humans make tiny corrections
     */
    public static boolean shouldMicroAdjust() {
        return random.nextDouble() < 0.6; // 60% chance
    }
    
    /**
     * Gets micro-adjustment amount
     */
    public static double getMicroAdjustmentAmount() {
        return (random.nextDouble() - 0.5) * 0.05; // ±2.5 degrees
    }
    
    /**
     * Simulates checking surroundings
     * Players periodically look around for threats/items
     */
    public static boolean shouldCheckSurroundings() {
        double checkChance = 0.05; // 5% base chance per tick
        
        // More checking when anxious
        checkChance += stressLevel * 0.1;
        
        // Less checking when focused on target
        if (focusedEntity != null && focusedEntity.isAlive()) {
            checkChance *= 0.3;
        }
        
        return random.nextDouble() < checkChance;
    }
    
    /**
     * Gets direction to look when checking surroundings
     */
    public static Vec3d getSurroundingCheckDirection() {
        // Random direction with preference for horizontal sweep
        double yaw = random.nextDouble() * 360 - 180;
        double pitch = (random.nextDouble() - 0.5) * 30; // -15 to +15 degrees
        
        return new Vec3d(
            Math.cos(Math.toRadians(yaw)),
            Math.sin(Math.toRadians(pitch)),
            Math.sin(Math.toRadians(yaw))
        );
    }
    
    /**
     * Determines if player should pause to "think"
     * Moments of inactivity while planning next move
     */
    public static boolean shouldPauseToThink() {
        double pauseChance = 0.02; // 2% base chance
        
        // More pausing when stressed or unsure
        pauseChance += stressLevel * 0.05;
        pauseChance += (1.0 - focusLevel) * 0.03;
        
        return random.nextDouble() < pauseChance;
    }
    
    /**
     * Gets think pause duration
     */
    public static long getThinkPauseDuration() {
        double baseDuration = 300.0; // ms
        baseDuration += random.nextGaussian() * 100;
        baseDuration += stressLevel * 200;
        
        return (long) Math.max(100, Math.min(1000, baseDuration));
    }
    
    private static double getAverageSkillLevel() {
        if (skillLevels.isEmpty()) return 0.3; // Beginner default
        
        double sum = 0;
        for (SkillLevel skill : skillLevels.values()) {
            sum += skill.level;
        }
        return sum / skillLevels.size();
    }
    
    public static void setFocusedEntity(Entity entity) {
        if (entity != focusedEntity) {
            focusedEntity = entity;
            lastFocusChange = System.currentTimeMillis();
        }
    }
    
    public static Entity getFocusedEntity() {
        return focusedEntity;
    }
    
    public static void resetSession() {
        sessionStartTime = System.currentTimeMillis();
        actionsPerformed = 0;
        fatigueLevel = 0.0;
        focusLevel = 1.0;
        stressLevel = 0.0;
        emotionalState = EmotionalState.NEUTRAL;
        recentActionTimes.clear();
    }
    
    public enum SkillLevel {
        BEGINNER(0.3, 0.0),
        INTERMEDIATE(0.5, 0.05),
        ADVANCED(0.7, 0.10),
        EXPERT(0.9, 0.15);
        
        final double level;
        final double accuracyBonus;
        
        SkillLevel(double level, double accuracyBonus) {
            this.level = level;
            this.accuracyBonus = accuracyBonus;
        }
    }
    
    public enum EmotionalState {
        NEUTRAL,
        FOCUSED,
        EXCITED,
        ANXIOUS,
        TIRED,
        ANGRY,
        CALM
    }
    
    // Getters for monitoring
    public static double getFatigueLevel() { return fatigueLevel; }
    public static double getFocusLevel() { return focusLevel; }
    public static double getStressLevel() { return stressLevel; }
    public static EmotionalState getEmotionalState() { return emotionalState; }
    public static int getActionsPerformed() { return actionsPerformed; }
    public static long getSessionDuration() { return System.currentTimeMillis() - sessionStartTime; }
}
