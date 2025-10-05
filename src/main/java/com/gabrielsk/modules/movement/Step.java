package com.gabrielsk.modules.movement;

import com.gabrielsk.GabrielSKAddon;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.util.math.Vec3d;

public class Step extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    
    private final Setting<Double> height = sgGeneral.add(new DoubleSetting.Builder()
        .name("height").description("Step height.").defaultValue(1.0).min(0).max(10).sliderMax(5).build());
    
    public Step() {
        super(GabrielSKAddon.CATEGORY, "step", "Allows you to step up blocks.");
    }
    
    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (mc.player == null) return;
        
        // Simple step implementation: boost player up if on ground and colliding
        if (mc.player.isOnGround() && mc.player.horizontalCollision) {
            double stepHeight = height.get();
            // Apply upward velocity to "step" up
            mc.player.setVelocity(mc.player.getVelocity().add(0, 0.2 * stepHeight, 0));
        }
    }
}
