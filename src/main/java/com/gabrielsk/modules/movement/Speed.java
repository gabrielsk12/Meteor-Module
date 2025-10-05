package com.gabrielsk.modules.movement;

import com.gabrielsk.GabrielSKAddon;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;

public class Speed extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    
    private final Setting<Mode> mode = sgGeneral.add(new EnumSetting.Builder<Mode>()
        .name("mode").description("Speed mode.").defaultValue(Mode.Strafe).build());
    
    private final Setting<Double> speed = sgGeneral.add(new DoubleSetting.Builder()
        .name("speed").description("Speed multiplier.").defaultValue(1.5).min(0.1).max(10).sliderMax(5).build());
    
    private final Setting<Boolean> onlyOnGround = sgGeneral.add(new BoolSetting.Builder()
        .name("only-on-ground").description("Only apply speed on ground.").defaultValue(true).build());
    
    public Speed() {
        super(GabrielSKAddon.CATEGORY, "speed", "Makes you move faster.");
    }
    
    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (mc.player == null) return;
        if (onlyOnGround.get() && !mc.player.isOnGround()) return;
        
        switch (mode.get()) {
            case Strafe -> {
                double forward = mc.player.input.movementForward;
                double strafe = mc.player.input.movementSideways;
                float yaw = mc.player.getYaw();
                
                if (forward == 0 && strafe == 0) {
                    mc.player.setVelocity(0, mc.player.getVelocity().y, 0);
                    return;
                }
                
                if (forward != 0) {
                    if (strafe > 0) yaw += (forward > 0 ? -45 : 45);
                    else if (strafe < 0) yaw += (forward > 0 ? 45 : -45);
                    strafe = 0;
                    if (forward > 0) forward = 1;
                    else forward = -1;
                }
                
                double mx = Math.cos(Math.toRadians(yaw + 90));
                double mz = Math.sin(Math.toRadians(yaw + 90));
                
                mc.player.setVelocity(
                    forward * speed.get() * mx + strafe * speed.get() * mz,
                    mc.player.getVelocity().y,
                    forward * speed.get() * mz - strafe * speed.get() * mx
                );
            }
            case Vanilla -> {
                double multiplier = speed.get() / 10.0;
                mc.player.setVelocity(
                    mc.player.getVelocity().x * (1 + multiplier),
                    mc.player.getVelocity().y,
                    mc.player.getVelocity().z * (1 + multiplier)
                );
            }
        }
    }
    
    public enum Mode {
        Strafe,
        Vanilla
    }
}
