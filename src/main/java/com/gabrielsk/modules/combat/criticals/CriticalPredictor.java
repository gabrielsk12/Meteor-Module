package com.gabrielsk.modules.combat.criticals;

import com.gabrielsk.GabrielSKAddon;
import com.gabrielsk.ai.ml.PlayerBehaviorLearner;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;

/**
 * Machine Learning predictor for optimal critical timing
 * Learns from player behavior to predict best moments for criticals
 */
public class CriticalPredictor {
    
    private final MinecraftClient mc;
    private final PlayerBehaviorLearner mlLearner;
    
    private long lastPrediction = 0;
    private static final long PREDICTION_COOLDOWN = 100; // 100ms between predictions
    
    public CriticalPredictor(MinecraftClient mc) {
        this.mc = mc;
        this.mlLearner = GabrielSKAddon.getMLLearner();
    }
    
    /**
     * Predict optimal timing for critical hit using ML
     * @param target The entity to attack
     * @return confidence score (0.0 to 1.0) that now is a good time for critical
     */
    public double predictOptimalTiming(Entity target) {
        if (mc.player == null || !(target instanceof LivingEntity living)) {
            return 0.5; // Neutral confidence
        }
        
        // Rate limit predictions
        long now = System.currentTimeMillis();
        if (now - lastPrediction < PREDICTION_COOLDOWN) {
            return 0.5;
        }
        lastPrediction = now;
        
        // Get ML prediction from combat network
        if (mlLearner != null) {
            double[] prediction = mlLearner.predictCombatAction(living);
            if (prediction != null && prediction.length >= 4) {
                // prediction[0] = attack confidence
                // prediction[1] = defend confidence
                // prediction[2] = retreat confidence
                // prediction[3] = strafe confidence
                
                // High attack confidence + low defend confidence = good critical timing
                double attackConf = prediction[0];
                double defendConf = prediction[1];
                double retreatConf = prediction[2];
                
                // Calculate optimal timing score
                double timingScore = attackConf * 0.6 - defendConf * 0.2 - retreatConf * 0.2;
                
                // Normalize to 0.0-1.0 range
                return Math.max(0.0, Math.min(1.0, (timingScore + 0.5) / 1.5));
            }
        }
        
        // Fallback: use heuristic prediction
        return predictHeuristic(living);
    }
    
    /**
     * Heuristic-based prediction when ML is not available
     */
    private double predictHeuristic(LivingEntity target) {
        double confidence = 0.5;
        
        // Distance factor (optimal at 3-4 blocks)
        double distance = mc.player.distanceTo(target);
        if (distance >= 3.0 && distance <= 4.0) {
            confidence += 0.2;
        } else if (distance < 2.0) {
            confidence -= 0.1; // Too close
        }
        
        // Health factor (safer when we have more health)
        float healthRatio = mc.player.getHealth() / mc.player.getMaxHealth();
        if (healthRatio > 0.7) {
            confidence += 0.15;
        } else if (healthRatio < 0.3) {
            confidence -= 0.2; // Risky when low health
        }
        
        // Target health factor (better against low health targets)
        float targetHealthRatio = target.getHealth() / target.getMaxHealth();
        if (targetHealthRatio < 0.3) {
            confidence += 0.1; // Finish off low health targets
        }
        
        // Ground check (must be on ground for most strategies)
        if (!mc.player.isOnGround()) {
            confidence -= 0.3;
        }
        
        // Movement factor (better when not moving much)
        double velocity = mc.player.getVelocity().length();
        if (velocity < 0.1) {
            confidence += 0.1; // Standing still
        }
        
        return Math.max(0.0, Math.min(1.0, confidence));
    }
    
    /**
     * Record critical attempt for ML learning
     */
    public void recordCriticalAttempt(Entity target, boolean success, String strategyName) {
        if (mlLearner != null && target instanceof LivingEntity living) {
            // Record this as a combat action for learning
            int action = success ? 0 : 1; // 0 = attack (success), 1 = defend (failed)
            mlLearner.recordCombatAction(living, action);
        }
    }
    
    /**
     * Check if ML system recommends using critical now
     * @param target The entity to attack
     * @param threshold Confidence threshold (0.0 to 1.0)
     * @return true if ML recommends critical hit
     */
    public boolean shouldUseCritical(Entity target, double threshold) {
        double confidence = predictOptimalTiming(target);
        return confidence >= threshold;
    }
    
    /**
     * Get recommended strategy based on ML predictions
     */
    public String getRecommendedStrategy(Entity target) {
        if (!(target instanceof LivingEntity living)) {
            return "Packet"; // Default
        }
        
        double confidence = predictOptimalTiming(target);
        double distance = mc.player.distanceTo(target);
        float healthRatio = mc.player.getHealth() / mc.player.getMaxHealth();
        
        // High confidence + close range = fast strategy
        if (confidence > 0.8 && distance < 3.0) {
            return "Multi-Packet";
        }
        
        // Medium confidence + low health = safe strategy
        if (confidence > 0.6 && healthRatio < 0.5) {
            return "Velocity";
        }
        
        // High confidence + good conditions = balanced strategy
        if (confidence > 0.7) {
            return "Jump Reset";
        }
        
        // Default to packet
        return "Packet";
    }
}
