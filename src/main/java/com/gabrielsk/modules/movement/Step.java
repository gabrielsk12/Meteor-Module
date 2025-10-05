package com.gabrielsk.modules.movement;

import com.gabrielsk.GabrielSKAddon;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;

public class Step extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    
    private final Setting<Double> height = sgGeneral.add(new DoubleSetting.Builder()
        .name("height").description("Step height.").defaultValue(1.0).min(0).max(10).sliderMax(5).build());
    
    private float oldStepHeight;
    
    public Step() {
        super(GabrielSKAddon.CATEGORY, "step", "Allows you to step up blocks.");
    }
    
    @Override
    public void onActivate() {
        if (mc.player != null) {
            oldStepHeight = mc.player.getStepHeight();
        }
    }
    
    @Override
    public void onDeactivate() {
        if (mc.player != null) {
            mc.player.setStepHeight(oldStepHeight);
        }
    }
    
    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (mc.player == null) return;
        mc.player.setStepHeight(height.get().floatValue());
    }
}
