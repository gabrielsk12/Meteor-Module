package com.gabrielsk.modules.combat;

import com.gabrielsk.GabrielSKAddon;
import com.gabrielsk.math.MathUtils;
import com.gabrielsk.ai.legit.Humanizer;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ExplosionS2CPacket;
import net.minecraft.util.math.Vec3d;

import java.util.*;

public class Velocity extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgAI = settings.createGroup("AI");
    private final SettingGroup sgLegit = settings.createGroup("Legit");
    
    private final Setting<Mode> mode = sgGeneral.add(new EnumSetting.Builder<Mode>()
        .name("mode").description("Velocity mode.").defaultValue(Mode.Smart).build());
    
    private final Setting<Double> horizontal = sgGeneral.add(new DoubleSetting.Builder()
        .name("horizontal").description("Horizontal velocity multiplier.").defaultValue(0).min(0).max(1).sliderMax(1).build());
    
    private final Setting<Double> vertical = sgGeneral.add(new DoubleSetting.Builder()
        .name("vertical").description("Vertical velocity multiplier.").defaultValue(0).min(0).max(1).sliderMax(1).build());
    
    private final Setting<Boolean> antiVoid = sgAI.add(new BoolSetting.Builder()
        .name("anti-void").description("Don't reduce velocity above void.").defaultValue(true).build());
    
    private final Setting<Boolean> smartReduction = sgAI.add(new BoolSetting.Builder()
        .name("smart-reduction").description("AI calculates optimal reduction.").defaultValue(true).build());
    
    private final Setting<Boolean> predictLanding = sgAI.add(new BoolSetting.Builder()
        .name("predict-landing").description("Predict and optimize landing.").defaultValue(true).build());

    // Legit settings
    private final Setting<Boolean> legitMode = sgLegit.add(new BoolSetting.Builder()
        .name("enable").description("Enable legit velocity shaping.").defaultValue(true).build());

    private final Setting<Double> randomizePct = sgLegit.add(new DoubleSetting.Builder()
        .name("randomize").description("Randomization on reductions (0-1).")
        .defaultValue(0.12).min(0.0).max(1.0).sliderMax(1.0).visible(legitMode::get).build());

    private final Setting<Double> clampStep = sgLegit.add(new DoubleSetting.Builder()
        .name("clamp-step").description("Max change per event to look natural.")
        .defaultValue(0.15).min(0.05).max(0.5).sliderMax(0.5).visible(legitMode::get).build());
    
    private Queue<VelocityEvent> recentEvents = new LinkedList<>();
    private long lastHitTime = 0;
    private Random random = new Random();
    
    public Velocity() {
        super(GabrielSKAddon.CATEGORY, "velocity", "AI-powered velocity control.");
    }
    
    @EventHandler
    private void onPacketReceive(PacketEvent.Receive event) {
        if (mc.player == null || mc.world == null) return;
        
        if (event.packet instanceof EntityVelocityUpdateS2CPacket packet) {
            if (packet.getEntityId() != mc.player.getId()) return;
            
            Vec3d velocity = new Vec3d(
                packet.getVelocityX() / 8000.0,
                packet.getVelocityY() / 8000.0,
                packet.getVelocityZ() / 8000.0
            );
            
            Vec3d modifiedVelocity = calculateOptimalVelocity(velocity);
            
            if (modifiedVelocity == null) {
                event.cancel();
                return;
            }
            
            mc.player.setVelocity(modifiedVelocity);
            event.cancel();
            
            recentEvents.add(new VelocityEvent(velocity, System.currentTimeMillis()));
            if (recentEvents.size() > 10) recentEvents.poll();
            lastHitTime = System.currentTimeMillis();
        }
        else if (event.packet instanceof ExplosionS2CPacket packet) {
            Vec3d velocity = new Vec3d(
                packet.getPlayerVelocityX(),
                packet.getPlayerVelocityY(),
                packet.getPlayerVelocityZ()
            );
            
            Vec3d modifiedVelocity = calculateOptimalVelocity(velocity);
            if (modifiedVelocity != null) {
                mc.player.setVelocity(modifiedVelocity);
            }
        }
    }
    
    private Vec3d calculateOptimalVelocity(Vec3d originalVelocity) {
        if (mode.get() == Mode.Cancel) {
            return null;
        }
        
        if (mode.get() == Mode.Simple) {
            double h = horizontal.get();
            double v = vertical.get();
            if (legitMode.get()) {
                // Add small randomness
                double r = randomizePct.get();
                h *= 1.0 + ((Math.random() * 2 - 1) * r * 0.25);
                v *= 1.0 + ((Math.random() * 2 - 1) * r * 0.25);
            }
            return new Vec3d(
                originalVelocity.x * h,
                originalVelocity.y * v,
                originalVelocity.z * h
            );
        }
        
        if (antiVoid.get() && isAboveVoid()) {
            return originalVelocity.multiply(0.8, 1.0, 0.8);
        }
        
        if (smartReduction.get()) {
            double threatLevel = calculateThreatLevel();
            double optimalH = MathUtils.lerp(horizontal.get(), 0.5, Math.min(threatLevel / 100.0, 1.0));
            double optimalV = vertical.get();
            
            if (predictLanding.get()) {
                // Simple position prediction: pos + velocity * time * modifier
                Vec3d predictedVelocity = originalVelocity.multiply(optimalH, optimalV, optimalH);
                Vec3d predictedPos = mc.player.getPos().add(predictedVelocity.multiply(1.0));
                
                if (wouldLandSafely(predictedPos)) {
                    optimalV = Math.max(optimalV, 0.3);
                }
            }
            
            if (random.nextDouble() < 0.2 || legitMode.get()) {
                double r = randomizePct.get();
                optimalH += (random.nextDouble() - 0.5) * 0.1 * r;
                optimalV += (random.nextDouble() - 0.5) * 0.1 * r;
                optimalH = Math.max(0, Math.min(1, optimalH));
                optimalV = Math.max(0, Math.min(1, optimalV));
            }

            if (legitMode.get()) {
                // Clamp change magnitude to look natural
                double step = clampStep.get();
                optimalH = clampDelta(horizontal.get(), optimalH, step);
                optimalV = clampDelta(vertical.get(), optimalV, step);
            }
            
            return new Vec3d(
                originalVelocity.x * optimalH,
                originalVelocity.y * optimalV,
                originalVelocity.z * optimalH
            );
        }
        
        return new Vec3d(
            originalVelocity.x * horizontal.get(),
            originalVelocity.y * vertical.get(),
            originalVelocity.z * horizontal.get()
        );
    }

    private double clampDelta(double base, double target, double maxStep) {
        double delta = target - base;
        if (Math.abs(delta) <= maxStep) return target;
        return base + Math.copySign(maxStep, delta);
    }
    
    private boolean isAboveVoid() {
        for (int i = 0; i < mc.player.getY(); i++) {
            if (!mc.world.getBlockState(mc.player.getBlockPos().down(i)).isAir()) {
                return false;
            }
        }
        return true;
    }
    
    private double calculateThreatLevel() {
        double threat = 0;
        
        long timeSinceHit = System.currentTimeMillis() - lastHitTime;
        if (timeSinceHit < 3000) {
            threat += 50 * (1.0 - timeSinceHit / 3000.0);
        }
        
        int recentHits = recentEvents.size();
        threat += recentHits * 5;
        
        if (mc.player.getHealth() < 10) {
            threat += 30;
        }
        
        return Math.min(threat, 100);
    }
    
    private boolean wouldLandSafely(Vec3d predictedPos) {
        if (mc.world == null) return false;
        
        for (int y = (int) predictedPos.y; y > predictedPos.y - 10; y--) {
            if (!mc.world.getBlockState(mc.player.getBlockPos().withY(y)).isAir()) {
                return true;
            }
        }
        
        return false;
    }
    
    private static class VelocityEvent {
        Vec3d velocity;
        long timestamp;
        
        VelocityEvent(Vec3d velocity, long timestamp) {
            this.velocity = velocity;
            this.timestamp = timestamp;
        }
    }
    
    public enum Mode {
        Cancel,
        Simple,
        Smart
    }
}
