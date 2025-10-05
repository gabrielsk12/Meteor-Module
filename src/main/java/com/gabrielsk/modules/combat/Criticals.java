package com.gabrielsk.modules.combat;

import com.gabrielsk.GabrielSKAddon;
import com.gabrielsk.modules.combat.criticals.*;
import meteordevelopment.meteorclient.events.entity.player.AttackEntityEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;

/**
 * Advanced Critical Hits Module
 * Features:
 * - 4 Different exploitation strategies
 * - AI-powered decision making with Behavior Trees
 * - Machine Learning timing prediction
 * - Adaptive pattern variation to avoid detection
 * - Real-time learning from player behavior
 * - Compatible with Minecraft 1.20+ and 1.21+
 */
public class Criticals extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgAI = settings.createGroup("AI");
    private final SettingGroup sgStats = settings.createGroup("Statistics");
    
    // General Settings
    private final Setting<Mode> mode = sgGeneral.add(new EnumSetting.Builder<Mode>()
        .name("mode")
        .description("Critical hit exploitation mode.")
        .defaultValue(Mode.AI)
        .build());
    
    private final Setting<StrategyType> strategy = sgGeneral.add(new EnumSetting.Builder<StrategyType>()
        .name("strategy")
        .description("Critical hit strategy (when not in AI mode).")
        .defaultValue(StrategyType.Packet)
        .visible(() -> mode.get() != Mode.AI)
        .build());
    
    // AI Settings
    private final Setting<Boolean> useML = sgAI.add(new BoolSetting.Builder()
        .name("use-ml-prediction")
        .description("Use Machine Learning to predict optimal timing.")
        .defaultValue(true)
        .build());
    
    private final Setting<Double> mlThreshold = sgAI.add(new DoubleSetting.Builder()
        .name("ml-threshold")
        .description("Confidence threshold for ML predictions (0.0-1.0).")
        .defaultValue(0.6)
        .min(0.0)
        .max(1.0)
        .sliderMax(1.0)
        .visible(() -> useML.get())
        .build());
    
    private final Setting<Boolean> adaptiveStrategy = sgAI.add(new BoolSetting.Builder()
        .name("adaptive-strategy")
        .description("Automatically switch strategies based on AI analysis.")
        .defaultValue(true)
        .build());
    
    private final Setting<Boolean> threatBased = sgAI.add(new BoolSetting.Builder()
        .name("threat-based")
        .description("Adjust behavior based on threat assessment.")
        .defaultValue(true)
        .build());
    
    // Statistics Settings
    private final Setting<Boolean> showStats = sgStats.add(new BoolSetting.Builder()
        .name("show-stats")
        .description("Show critical hit statistics in chat.")
        .defaultValue(false)
        .build());
    
    private final Setting<Integer> statsInterval = sgStats.add(new IntSetting.Builder()
        .name("stats-interval")
        .description("How often to show stats (in seconds).")
        .defaultValue(60)
        .min(10)
        .max(300)
        .sliderMax(300)
        .visible(() -> showStats.get())
        .build());
    
    // AI Components
    private CriticalAI criticalAI;
    private CriticalPredictor mlPredictor;
    
    // Manual strategies
    private PacketCritical packetCritical;
    private VelocityCritical velocityCritical;
    private JumpResetCritical jumpResetCritical;
    private MultiPacketCritical multiPacketCritical;
    
    // Statistics
    private long lastStatsDisplay = 0;
    private int totalAttempts = 0;
    private int successfulCrits = 0;
    
    public Criticals() {
        super(GabrielSKAddon.CATEGORY, "criticals", "AI-powered critical hits with multiple exploitation strategies.");
    }
    
    @Override
    public void onActivate() {
        if (mc == null) return;
        
        // Initialize AI components
        criticalAI = new CriticalAI(mc);
        mlPredictor = new CriticalPredictor(mc);
        
        // Initialize manual strategies
        packetCritical = new PacketCritical();
        velocityCritical = new VelocityCritical();
        jumpResetCritical = new JumpResetCritical();
        multiPacketCritical = new MultiPacketCritical();
        
        info("Criticals activated with AI mode: " + mode.get());
    }
    
    @Override
    public void onDeactivate() {
        if (showStats.get()) {
            displayStatistics();
        }
    }
    
    @EventHandler
    private void onAttack(AttackEntityEvent event) {
        if (mc.player == null || mc.world == null) return;
        
        Entity target = event.entity;
        boolean success = false;
        
        try {
            switch (mode.get()) {
                case AI -> {
                    // AI mode: Use ML prediction and adaptive strategy selection
                    success = executeAICritical(target);
                }
                case Smart -> {
                    // Smart mode: Use ML prediction with manual strategy
                    success = executeSmartCritical(target);
                }
                case Manual -> {
                    // Manual mode: Use selected strategy without AI
                    success = executeManualCritical(target);
                }
            }
            
            // Update statistics
            totalAttempts++;
            if (success) {
                successfulCrits++;
                
                // Record for ML learning
                if (useML.get() && mlPredictor != null) {
                    String strategyName = getLastUsedStrategyName();
                    mlPredictor.recordCriticalAttempt(target, true, strategyName);
                }
            }
            
            // Display periodic statistics
            if (showStats.get()) {
                long now = System.currentTimeMillis();
                if (now - lastStatsDisplay > statsInterval.get() * 1000L) {
                    displayStatistics();
                    lastStatsDisplay = now;
                }
            }
            
        } catch (Exception e) {
            error("Critical execution error: " + e.getMessage());
        }
    }
    
    @EventHandler
    private void onTick(TickEvent.Post event) {
        // Periodic AI updates can go here if needed
    }
    
    /**
     * Execute critical using full AI system
     */
    private boolean executeAICritical(Entity target) {
        if (criticalAI == null) return false;
        
        // Check ML prediction if enabled
        if (useML.get() && mlPredictor != null) {
            if (!mlPredictor.shouldUseCritical(target, mlThreshold.get())) {
                return false; // ML says not a good time
            }
        }
        
        // Use AI to execute critical with optimal strategy
        return criticalAI.executeCritical(target);
    }
    
    /**
     * Execute critical with ML prediction but manual strategy
     */
    private boolean executeSmartCritical(Entity target) {
        if (mlPredictor == null) return executeManualCritical(target);
        
        // Check ML prediction
        if (!mlPredictor.shouldUseCritical(target, mlThreshold.get())) {
            return false; // ML says not a good time
        }
        
        // Use manual strategy
        return executeManualCritical(target);
    }
    
    /**
     * Execute critical with manual strategy selection
     */
    private boolean executeManualCritical(Entity target) {
        CriticalStrategy strat = getManualStrategy();
        if (strat == null || !strat.canExecute(mc)) {
            return false;
        }
        
        return strat.execute(mc, target);
    }
    
    /**
     * Get manual strategy based on setting
     */
    private CriticalStrategy getManualStrategy() {
        return switch (strategy.get()) {
            case Packet -> packetCritical;
            case Velocity -> velocityCritical;
            case JumpReset -> jumpResetCritical;
            case MultiPacket -> multiPacketCritical;
        };
    }
    
    /**
     * Get name of last used strategy
     */
    private String getLastUsedStrategyName() {
        if (mode.get() == Mode.AI && criticalAI != null) {
            CriticalStrategy strat = criticalAI.getCurrentStrategy();
            return strat != null ? strat.getName() : "Unknown";
        }
        return strategy.get().name();
    }
    
    /**
     * Display statistics in chat
     */
    private void displayStatistics() {
        if (mc.player == null) return;
        
        double successRate = totalAttempts > 0 ? 
            (double) successfulCrits / totalAttempts * 100.0 : 0.0;
        
        ChatUtils.info("§b[Criticals Stats]§r");
        ChatUtils.info("  Attempts: §e" + totalAttempts + "§r");
        ChatUtils.info("  Successful: §a" + successfulCrits + "§r");
        ChatUtils.info("  Success Rate: §6" + String.format("%.1f%%", successRate) + "§r");
        
        if (mode.get() == Mode.AI && criticalAI != null) {
            ChatUtils.info("  AI Success Rate: §6" + 
                String.format("%.1f%%", criticalAI.getSuccessRate() * 100.0) + "§r");
            ChatUtils.info("  Detection Level: §c" + 
                String.format("%.1f%%", criticalAI.getDetectionLevel() * 100.0) + "§r");
            
            CriticalStrategy current = criticalAI.getCurrentStrategy();
            if (current != null) {
                ChatUtils.info("  Current Strategy: §d" + current.getName() + "§r");
            }
        }
        
        if (useML.get() && mlPredictor != null) {
            ChatUtils.info("  ML Prediction: §aEnabled§r");
        }
    }
    
    public enum Mode {
        AI,      // Full AI with ML prediction and adaptive strategies
        Smart,   // ML prediction with manual strategy
        Manual   // Manual strategy only
    }
    
    public enum StrategyType {
        Packet,      // Classic packet critical
        Velocity,    // Velocity-based critical
        JumpReset,   // Jump reset exploit
        MultiPacket  // Multi-packet advanced
    }
}
