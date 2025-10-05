package com.gabrielsk;

import com.gabrielsk.modules.automation.EverythingBot;
import com.gabrielsk.modules.automation.GardenBot;
import com.gabrielsk.modules.automation.LawnBot;
import com.gabrielsk.modules.automation.TorchGridBot;
import com.gabrielsk.modules.automation.WarehouseBot;
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
        
        Modules.get().add(new LawnBot());
        Modules.get().add(new EverythingBot());
        Modules.get().add(new GardenBot());
        Modules.get().add(new TorchGridBot());
        Modules.get().add(new WarehouseBot());
        
        LOG.info("[Automation] ✓ 5 automation modules loaded");
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
