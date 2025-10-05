package com.gabrielsk;

import com.gabrielsk.modules.automation.*;
import com.gabrielsk.modules.combat.*;
import com.gabrielsk.modules.movement.*;
import com.gabrielsk.modules.render.*;
import com.gabrielsk.modules.utility.*;
import meteordevelopment.meteorclient.addons.MeteorAddon;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Modules;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Gabriel_SK Complete AI/ML Module Collection for Meteor Client
 * 
 * Features:
 * - AI-powered combat modules with machine learning
 * - Advanced automation bots with legit behavior  
 * - Pathfinding and GOAP AI systems
 * - Neural network-based player prediction
 * - Human-like movement patterns and behavior simulation
 * - GPU-accelerated computation support
 * - Survival SMP utilities and automation
 */
public class GabrielSKAddon extends MeteorAddon {
    
    public static final Logger LOG = LoggerFactory.getLogger("meteor-modules");
    public static final String VERSION = "1.1.0";
    public static final Category CATEGORY = new Category("Gabriel_SK");
    
    @Override
    public void onInitialize() {
        LOG.info("╔════════════════════════════════════════╗");
        LOG.info("║  Gabriel_SK AI/ML Modules v{}", VERSION);
        LOG.info("║  Initializing ALL modules...          ║");
        LOG.info("╚════════════════════════════════════════╝");
        
        // Register ALL modules - combat, movement, render, automation, utility
        registerCombatModules();
        registerMovementModules();
        registerRenderModules();
        registerAutomationModules();
        registerUtilityModules();
        
        LOG.info("§a[Complete] ALL {} modules loaded successfully!", getTotalModuleCount());
    }
    
    /**
     * Register AI-powered combat modules
     */
    private void registerCombatModules() {
        LOG.info("[Combat] Loading AI combat modules...");
        
        Modules.get().add(new AdvancedCrystalAura());
        Modules.get().add(new KillAura());
        Modules.get().add(new Criticals());
        Modules.get().add(new com.gabrielsk.modules.combat.AutoTotem());
        Modules.get().add(new Surround());
        Modules.get().add(new Velocity());
        
        LOG.info("[Combat] ✓ 6 combat modules loaded");
    }
    
    /**
     * Register AI movement modules
     */
    private void registerMovementModules() {
        LOG.info("[Movement] Loading AI movement modules...");
        
        Modules.get().add(new AISpeed());
        Modules.get().add(new Flight());
        Modules.get().add(new Jesus());
        Modules.get().add(new NoFall());
        Modules.get().add(new Speed());
        Modules.get().add(new Sprint());
        Modules.get().add(new Step());
        
        LOG.info("[Movement] ✓ 7 movement modules loaded");
    }
    
    /**
     * Register render/ESP modules
     */
    private void registerRenderModules() {
        LOG.info("[Render] Loading render modules...");
        
        Modules.get().add(new ChestESP());
        Modules.get().add(new ESP());
        Modules.get().add(new FullBright());
        Modules.get().add(new HoleESP());
        Modules.get().add(new Tracers());
        
        LOG.info("[Render] ✓ 5 render modules loaded");
    }
    
    /**
     * Register automation modules (AI bots + survival automation)
     */
    private void registerAutomationModules() {
        LOG.info("[Automation] Loading automation modules...");
        
        // Core AI automation bots with pathfinding
        Modules.get().add(new LawnBot());
        Modules.get().add(new EverythingBot());
        Modules.get().add(new GardenBot());
        Modules.get().add(new TorchGridBot());
        Modules.get().add(new WarehouseBot());
        
        // Survival SMP automation
        Modules.get().add(new AutoMiner());
        Modules.get().add(new AutoFish());
        Modules.get().add(new TreeChopper());
        Modules.get().add(new AutoBreed());
        Modules.get().add(new com.gabrielsk.modules.automation.AutoTotem());
        
        LOG.info("[Automation] ✓ 10 automation modules loaded");
    }
    
    /**
     * Register utility modules
     */
    private void registerUtilityModules() {
        LOG.info("[Utility] Loading utility modules...");
        
        Modules.get().add(new AutoArmor());
        Modules.get().add(new AutoEat());
        Modules.get().add(new AutoTool());
        Modules.get().add(new AutoLog());
        Modules.get().add(new AutoRespawn());
        Modules.get().add(new ChestStealer());
        Modules.get().add(new DeathPosition());
        Modules.get().add(new FastBreak());
        Modules.get().add(new Nuker());
        Modules.get().add(new Replenish());
        Modules.get().add(new Scaffold());
        
        LOG.info("[Utility] ✓ 11 utility modules loaded");
    }
    
    private int getTotalModuleCount() {
        return 6 + 7 + 5 + 10 + 11; // 39 total modules
    }
    
    @Override
    public String getPackage() {
        return "com.gabrielsk";
    }
    
    @Override
    public void onRegisterCategories() {
        Modules.registerCategory(CATEGORY);
    }
}
