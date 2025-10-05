package com.gabrielsk.modules.movement;

import com.gabrielsk.GabrielSKAddon;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.util.math.Vec3d;

public class Flight extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    
    private final Setting<Mode> mode = sgGeneral.add(new EnumSetting.Builder<Mode>()
        .name("mode").description("Flight mode.").defaultValue(Mode.Vanilla).build());
    
    private final Setting<Double> speed = sgGeneral.add(new DoubleSetting.Builder()
        .name("speed").description("Flight speed.").defaultValue(0.5).min(0.1).max(10).sliderMax(5).build());
    
    private final Setting<Double> verticalSpeed = sgGeneral.add(new DoubleSetting.Builder()
        .name("vertical-speed").description("Vertical flight speed.").defaultValue(0.5).min(0.1).max(10).sliderMax(5).build());
    
    public Flight() {
        super(GabrielSKAddon.CATEGORY, "flight", "Allows you to fly.");
    }
    
    @Override
    public void onActivate() {
        if (mc.player != null && mode.get() == Mode.Vanilla) {
            mc.player.getAbilities().flying = true;
            mc.player.getAbilities().allowFlying = true;
        }
    }
    
    @Override
    public void onDeactivate() {
        if (mc.player != null && !mc.player.isCreative() && !mc.player.isSpectator()) {
            mc.player.getAbilities().flying = false;
            mc.player.getAbilities().allowFlying = false;
        }
    }
    
    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (mc.player == null) return;
        
        switch (mode.get()) {
            case Vanilla -> {
                mc.player.getAbilities().setFlySpeed((float) (speed.get() / 10.0));
            }
            case Static -> {
                mc.player.setVelocity(0, 0, 0);
                mc.player.velocityModified = true;
                
                Vec3d velocity = Vec3d.ZERO;
                
                if (mc.options.jumpKey.isPressed()) {
                    velocity = velocity.add(0, verticalSpeed.get(), 0);
                }
                if (mc.options.sneakKey.isPressed()) {
                    velocity = velocity.subtract(0, verticalSpeed.get(), 0);
                }
                
                double forward = 0;
                double strafe = 0;
                
                if (mc.options.forwardKey.isPressed()) forward += 1;
                if (mc.options.backKey.isPressed()) forward -= 1;
                if (mc.options.rightKey.isPressed()) strafe += 1;
                if (mc.options.leftKey.isPressed()) strafe -= 1;
                
                if (forward != 0 || strafe != 0) {
                    float yaw = mc.player.getYaw();
                    if (forward != 0) {
                        if (strafe > 0) yaw += (forward > 0 ? -45 : 45);
                        else if (strafe < 0) yaw += (forward > 0 ? 45 : -45);
                        strafe = 0;
                        forward = forward > 0 ? 1 : -1;
                    }
                    
                    double mx = Math.cos(Math.toRadians(yaw + 90));
                    double mz = Math.sin(Math.toRadians(yaw + 90));
                    
                    velocity = velocity.add(
                        forward * speed.get() * mx + strafe * speed.get() * mz,
                        0,
                        forward * speed.get() * mz - strafe * speed.get() * mx
                    );
                }
                
                mc.player.setVelocity(velocity);
            }
        }
    }
    
    public enum Mode {
        Vanilla,
        Static
    }
}
