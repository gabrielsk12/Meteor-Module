package com.gabrielsk;

import com.gabrielsk.modules.automation.*;
import com.gabrielsk.modules.utility.*;
import meteordevelopment.meteorclient.addons.MeteorAddon;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Modules;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Gabriel_SK Automation Modules Add-on for Meteor Client
 * 
 * Features:
 * - AI-powered automation bots with legit behavior
 * - Pathfinding-enabled bots (GardenBot, TorchGridBot, WarehouseBot)
 * - Human-like pacing, LOS checks, and movement patterns
 */
public class GabrielSKAddon extends MeteorAddon {
    
    public static final Logger LOG = LoggerFactory.getLogger("meteor-modules");
    public static final String VERSION = "1.0.0";
    public static final Category CATEGORY = new Category("Gabriel_SK");
    
    @Override
    public void onInitialize() {
        LOG.info("╔════════════════════════════════════════╗");
        LOG.info("║  Gabriel_SK Modules v{}", VERSION);
        LOG.info("║  Initializing Automation Bots...      ║");
        LOG.info("╚════════════════════════════════════════╝");
        
        // Register automation modules
        registerAutomationModules();
        
        LOG.info("§a[Complete] All modules loaded successfully!");
    }
    
    /**
     * Register automation modules
     */
    private void registerAutomationModules() {
        LOG.info("[Automation] Loading automation modules...");
        
        // Core automation bots
        Modules.get().add(new LawnBot());
        Modules.get().add(new EverythingBot());
        Modules.get().add(new GardenBot());
        Modules.get().add(new TorchGridBot());
        Modules.get().add(new WarehouseBot());
        
        // Advanced automation
        Modules.get().add(new AutoMiner());
        Modules.get().add(new AutoFish());
        Modules.get().add(new TreeChopper());
        Modules.get().add(new AutoBreed());
        Modules.get().add(new AutoTotem());
        Modules.get().add(new AutoEat());
        
        // Utility modules
        Modules.get().add(new AutoTool());
        Modules.get().add(new Replenish());
        Modules.get().add(new AutoLog());
        Modules.get().add(new DeathPosition());
        Modules.get().add(new AutoRespawn());
        
        LOG.info("[Automation] ✓ 16 modules loaded (5 bots + 11 utilities)");
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
