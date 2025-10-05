package com.gabrielsk.modules.combat.criticals;

import com.gabrielsk.ai.BehaviorTree;
import com.gabrielsk.ai.BehaviorTree.*;
import com.gabrielsk.ai.ThreatAssessment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * AI system for intelligent critical hit decision making
 * Uses Behavior Trees to decide when and how to use criticals
 */
public class CriticalAI {
    
    private final MinecraftClient mc;
    private final List<CriticalStrategy> strategies;
    private final BehaviorTree decisionTree;
    private final ThreatAssessment threatAssessment;
    
    private CriticalStrategy lastStrategy;
    private long lastStrategySwitch = 0;
    private static final long STRATEGY_SWITCH_COOLDOWN = 5000; // 5 seconds
    
    private int successCount = 0;
    private int failCount = 0;
    private double detectionLevel = 0.0;
    
    public CriticalAI(MinecraftClient mc) {
        this.mc = mc;
        this.strategies = new ArrayList<>();
        this.threatAssessment = new ThreatAssessment();
        
        // Initialize all strategies
        strategies.add(new PacketCritical());
        strategies.add(new VelocityCritical());
        strategies.add(new JumpResetCritical());
        strategies.add(new MultiPacketCritical());
        
        // Build behavior tree for decision making
        this.decisionTree = buildDecisionTree();
    }
    
    /**
     * Build behavior tree for intelligent critical decision making
     */
    private BehaviorTree buildDecisionTree() {
        BehaviorTree tree = new BehaviorTree();
        
        // Root: Selector (try strategies in priority order)
        Node root = tree.new Selector();
        
        // High threat: Use fastest strategy
        Node highThreat = tree.new Sequence();
        highThreat.addChild(tree.new Condition(() -> isHighThreat()));
        highThreat.addChild(tree.new Action(() -> useFastestStrategy()));
        
        // Low detection risk: Use safest strategy
        Node lowDetection = tree.new Sequence();
        lowDetection.addChild(tree.new Condition(() -> isLowDetectionRisk()));
        lowDetection.addChild(tree.new Action(() -> useSafestStrategy()));
        
        // Default: Use balanced strategy
        Node balanced = tree.new Action(() -> useBalancedStrategy());
        
        root.addChild(highThreat);
        root.addChild(lowDetection);
        root.addChild(balanced);
        
        tree.setRoot(root);
        return tree;
    }
    
    /**
     * Execute critical with AI decision making
     */
    public boolean executeCritical(Entity target) {
        if (mc.player == null || !(target instanceof LivingEntity)) return false;
        
        // Run behavior tree to select strategy
        decisionTree.tick();
        
        if (lastStrategy == null) {
            lastStrategy = useBalancedStrategy();
        }
        
        // Check if strategy can execute
        if (!lastStrategy.canExecute(mc)) {
            return false;
        }
        
        // Execute strategy
        boolean success = lastStrategy.execute(mc, target);
        
        // Update statistics
        if (success) {
            successCount++;
            updateDetectionLevel(-0.01); // Decrease detection level on success
        } else {
            failCount++;
            updateDetectionLevel(0.02); // Increase detection level on failure
        }
        
        return success;
    }
    
    /**
     * Get optimal strategy based on ML predictions
     */
    public CriticalStrategy getOptimalStrategy(Entity target) {
        if (!(target instanceof LivingEntity living)) {
            return strategies.get(0); // Default to packet
        }
        
        double threat = threatAssessment.calculateThreat(living);
        double distance = mc.player.distanceTo(target);
        
        // High threat + close range = fastest strategy
        if (threat > 0.7 && distance < 3.0) {
            return getFastestStrategy();
        }
        
        // Low threat + far range = safest strategy
        if (threat < 0.3 && distance > 4.0) {
            return getSafestStrategy();
        }
        
        // Medium threat = balanced strategy
        return getBalancedStrategy();
    }
    
    /**
     * Check if target poses high threat
     */
    private boolean isHighThreat() {
        if (mc.player == null || mc.world == null) return false;
        
        // Check nearby threats
        List<Entity> nearbyEntities = mc.world.getOtherEntities(mc.player, 
            mc.player.getBoundingBox().expand(10.0));
        
        int hostileCount = 0;
        for (Entity entity : nearbyEntities) {
            if (entity instanceof LivingEntity living) {
                double threat = threatAssessment.calculateThreat(living);
                if (threat > 0.5) hostileCount++;
            }
        }
        
        return hostileCount >= 3 || mc.player.getHealth() < 8.0f;
    }
    
    /**
     * Check if detection risk is low
     */
    private boolean isLowDetectionRisk() {
        return detectionLevel < 0.3 && failCount < 3;
    }
    
    /**
     * Use fastest strategy (highest DPS)
     */
    private CriticalStrategy useFastestStrategy() {
        if (canSwitchStrategy()) {
            lastStrategy = getFastestStrategy();
            lastStrategySwitch = System.currentTimeMillis();
        }
        return lastStrategy;
    }
    
    /**
     * Use safest strategy (lowest detection risk)
     */
    private CriticalStrategy useSafestStrategy() {
        if (canSwitchStrategy()) {
            lastStrategy = getSafestStrategy();
            lastStrategySwitch = System.currentTimeMillis();
        }
        return lastStrategy;
    }
    
    /**
     * Use balanced strategy
     */
    private CriticalStrategy useBalancedStrategy() {
        if (canSwitchStrategy()) {
            lastStrategy = getBalancedStrategy();
            lastStrategySwitch = System.currentTimeMillis();
        }
        return lastStrategy;
    }
    
    private boolean canSwitchStrategy() {
        return System.currentTimeMillis() - lastStrategySwitch > STRATEGY_SWITCH_COOLDOWN;
    }
    
    private CriticalStrategy getFastestStrategy() {
        return strategies.stream()
            .min((a, b) -> Integer.compare(a.getExecutionSpeed(), b.getExecutionSpeed()))
            .orElse(strategies.get(0));
    }
    
    private CriticalStrategy getSafestStrategy() {
        return strategies.stream()
            .min((a, b) -> Double.compare(a.getDetectionRisk(), b.getDetectionRisk()))
            .orElse(strategies.get(1));
    }
    
    private CriticalStrategy getBalancedStrategy() {
        return strategies.stream()
            .min((a, b) -> {
                double scoreA = a.getDetectionRisk() * 0.5 + (a.getExecutionSpeed() / 100.0) * 0.5;
                double scoreB = b.getDetectionRisk() * 0.5 + (b.getExecutionSpeed() / 100.0) * 0.5;
                return Double.compare(scoreA, scoreB);
            })
            .orElse(strategies.get(2));
    }
    
    private void updateDetectionLevel(double delta) {
        detectionLevel = Math.max(0.0, Math.min(1.0, detectionLevel + delta));
    }
    
    // Getters for statistics
    public int getSuccessCount() { return successCount; }
    public int getFailCount() { return failCount; }
    public double getDetectionLevel() { return detectionLevel; }
    public double getSuccessRate() { 
        int total = successCount + failCount;
        return total > 0 ? (double) successCount / total : 0.0;
    }
    public CriticalStrategy getCurrentStrategy() { return lastStrategy; }
}
