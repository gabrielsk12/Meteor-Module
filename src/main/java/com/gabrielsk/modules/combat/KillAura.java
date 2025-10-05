package com.gabrielsk.modules.combat;

import com.gabrielsk.GabrielSKAddon;
import com.gabrielsk.ai.BehaviorNode;
import com.gabrielsk.ai.CompositeNode;
import com.gabrielsk.ai.legit.LegitController;
import com.gabrielsk.ai.legit.Visibility;
import com.gabrielsk.math.MathUtils;
import com.gabrielsk.utils.CombatUtils;
import com.gabrielsk.utils.PlayerMovement;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;

import java.util.*;

public class KillAura extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgAI = settings.createGroup("AI");
    private final SettingGroup sgLegit = settings.createGroup("Legit");
    
    private final Setting<Double> range = sgGeneral.add(new DoubleSetting.Builder()
        .name("range").description("Attack range.").defaultValue(4.5).min(0).max(6).sliderMax(6).build());
    
    private final Setting<Boolean> players = sgGeneral.add(new BoolSetting.Builder()
        .name("players").description("Attack players.").defaultValue(true).build());
    
    private final Setting<Boolean> mobs = sgGeneral.add(new BoolSetting.Builder()
        .name("mobs").description("Attack mobs.").defaultValue(false).build());
    
    private final Setting<TargetMode> targetMode = sgAI.add(new EnumSetting.Builder<TargetMode>()
        .name("target-mode").description("AI target selection.").defaultValue(TargetMode.Smart).build());
    
    private final Setting<Boolean> predictMovement = sgAI.add(new BoolSetting.Builder()
        .name("predict-movement").description("Predict target movement.").defaultValue(true).build());
    
    private final Setting<Boolean> humanLike = sgAI.add(new BoolSetting.Builder()
        .name("human-like").description("Human-like behavior patterns.").defaultValue(true).build());
    
    private final Setting<Integer> minDelay = sgAI.add(new IntSetting.Builder()
        .name("min-delay").description("Min attack delay (ticks).").defaultValue(8).min(0).max(20).sliderMax(20).build());
    
    private final Setting<Integer> maxDelay = sgAI.add(new IntSetting.Builder()
        .name("max-delay").description("Max attack delay (ticks).").defaultValue(12).min(0).max(20).sliderMax(20).build());
    // Legit settings
    private final Setting<Boolean> legitMode = sgLegit.add(new BoolSetting.Builder()
        .name("enable").description("Enable legit (human-like) constraints.").defaultValue(true).build());

    private final Setting<Double> fov = sgLegit.add(new DoubleSetting.Builder()
        .name("fov").description("Max field of view angle to attack (deg).")
        .defaultValue(90.0).min(10.0).max(180.0).sliderMax(180.0).visible(legitMode::get).build());

    private final Setting<Boolean> requireLos = sgLegit.add(new BoolSetting.Builder()
        .name("require-los").description("Require clear line of sight to target.")
        .defaultValue(true).visible(legitMode::get).build());

    private final Setting<Double> cpsMin = sgLegit.add(new DoubleSetting.Builder()
        .name("cps-min").description("Minimum clicks per second.")
        .defaultValue(6.0).min(1.0).max(20.0).sliderMax(20.0).visible(legitMode::get).build());

    private final Setting<Double> cpsMax = sgLegit.add(new DoubleSetting.Builder()
        .name("cps-max").description("Maximum clicks per second.")
        .defaultValue(9.5).min(1.0).max(20.0).sliderMax(20.0).visible(legitMode::get).build());

    private final Setting<Double> cpsJitter = sgLegit.add(new DoubleSetting.Builder()
        .name("cps-jitter").description("CPS randomization (0-1).")
        .defaultValue(0.15).min(0.0).max(1.0).sliderMax(1.0).visible(legitMode::get).build());

    private final Setting<Integer> reactionMs = sgLegit.add(new IntSetting.Builder()
        .name("reaction-ms").description("Human reaction time on target acquire (ms).")
        .defaultValue(140).min(0).max(500).sliderMax(500).visible(legitMode::get).build());

    
    private final Setting<Boolean> rotate = sgGeneral.add(new BoolSetting.Builder()
        .name("rotate").description("Rotate to target.").defaultValue(true).build());
    
    private int tickCounter = 0;
    private int currentDelay = 10;
    private Entity lastTarget = null;
    private Map<Entity, ThreatData> threatMap = new HashMap<>();
    private BehaviorNode behaviorTree;
    private Random random = new Random();
    private final LegitController legit = new LegitController();
    private long targetAcquiredAt = 0L;
    
    public KillAura() {
        super(GabrielSKAddon.CATEGORY, "kill-aura", "AI-powered automatic combat.");
        initBehaviorTree();
    }
    
    private void initBehaviorTree() {
        behaviorTree = new CompositeNode.Sequence(Arrays.asList(
            (BehaviorNode) () -> mc.player != null && mc.world != null ? BehaviorNode.Status.SUCCESS : BehaviorNode.Status.FAILURE,
            (BehaviorNode) () -> findTarget() != null ? BehaviorNode.Status.SUCCESS : BehaviorNode.Status.FAILURE,
            (BehaviorNode) () -> {
                Entity target = findTarget();
                if (target == null) return BehaviorNode.Status.FAILURE;
                
                // Legit checks: FOV and LOS before rotating
                if (legitMode.get()) {
                    if (!Visibility.withinFov(mc, target, fov.get())) return BehaviorNode.Status.RUNNING;
                    if (requireLos.get() && !Visibility.hasLineOfSight(mc, target.getPos())) return BehaviorNode.Status.RUNNING;
                }

                if (humanLike.get() && rotate.get()) {
                    Vec3d targetPos = predictMovement.get() ? 
                        MathUtils.predictEntityPosition(target, 0.1) : target.getPos();
                    PlayerMovement.humanRotate(mc.player, targetPos);
                    
                    if (random.nextDouble() < 0.05) {
                        return BehaviorNode.Status.RUNNING;
                    }
                }
                
                return BehaviorNode.Status.SUCCESS;
            },
            (BehaviorNode) () -> {
                Entity target = findTarget();
                if (target == null) return BehaviorNode.Status.FAILURE;
                
                // Legit pacing: CPS-based gating
                if (legitMode.get()) {
                    // When acquiring new target, wait reaction time once
                    if (lastTarget != target) {
                        targetAcquiredAt = System.currentTimeMillis();
                        lastTarget = target;
                        legit.useCps(cpsMin.get(), cpsMax.get(), cpsJitter.get());
                        return BehaviorNode.Status.RUNNING;
                    }
                    // Wait human reaction time before first swing
                    if (System.currentTimeMillis() - targetAcquiredAt < reactionMs.get()) {
                        return BehaviorNode.Status.RUNNING;
                    }
                    // CPS window
                    if (!legit.canAct()) return BehaviorNode.Status.RUNNING;
                } else if (tickCounter < currentDelay) {
                    return BehaviorNode.Status.RUNNING;
                }

                // Final LOS/FOV recheck right before attack in legit
                if (legitMode.get()) {
                    if (!Visibility.withinFov(mc, target, fov.get())) return BehaviorNode.Status.RUNNING;
                    if (requireLos.get() && !Visibility.hasLineOfSight(mc, target.getPos())) return BehaviorNode.Status.RUNNING;
                }

                if (!legitMode.get() || tickCounter >= currentDelay) {
                    attackTarget(target);
                    tickCounter = 0;
                    if (humanLike.get()) {
                        currentDelay = minDelay.get() + random.nextInt(Math.max(1, maxDelay.get() - minDelay.get() + 1));
                    } else {
                        currentDelay = minDelay.get();
                    }
                    if (legitMode.get()) {
                        // Mark CPS schedule for next act
                        legit.useCps(cpsMin.get(), cpsMax.get(), cpsJitter.get());
                        legit.markActed(0, 0); // lastActionAt update
                    }
                    return BehaviorNode.Status.SUCCESS;
                }
                
                return BehaviorNode.Status.RUNNING;
            }
        ));
    }
    
    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (mc.player == null || mc.world == null) return;
        
        updateThreatMap();
        tickCounter++;
        
        behaviorTree.tick();
    }
    
    private void updateThreatMap() {
        threatMap.entrySet().removeIf(entry -> !entry.getKey().isAlive());
        
        for (Entity entity : mc.world.getEntities()) {
            if (isValidTarget(entity)) {
                ThreatData threat = threatMap.computeIfAbsent(entity, e -> new ThreatData());
                threat.update((LivingEntity) entity, mc.player);
            }
        }
    }
    
    private Entity findTarget() {
        List<Entity> validTargets = new ArrayList<>();
        
        for (Entity entity : mc.world.getEntities()) {
            if (entity == mc.player) continue;
            if (!isValidTarget(entity)) continue;
            
            double dist = mc.player.squaredDistanceTo(entity);
            if (dist > range.get() * range.get()) continue;
            
            validTargets.add(entity);
        }
        
        if (validTargets.isEmpty()) return null;
        
        return switch (targetMode.get()) {
            case Closest -> findClosest(validTargets);
            case LowestHealth -> findLowestHealth(validTargets);
            case HighestThreat -> findHighestThreat(validTargets);
            case Smart -> findSmartest(validTargets);
        };
    }
    
    private boolean isValidTarget(Entity entity) {
        if (!(entity instanceof LivingEntity living) || !living.isAlive()) return false;
        
        if (players.get() && entity instanceof PlayerEntity) return true;
        if (mobs.get() && entity instanceof HostileEntity) return true;
        
        return false;
    }
    
    private Entity findClosest(List<Entity> targets) {
        Entity closest = null;
        double closestDist = Double.MAX_VALUE;
        
        for (Entity entity : targets) {
            double dist = mc.player.squaredDistanceTo(entity);
            if (dist < closestDist) {
                closestDist = dist;
                closest = entity;
            }
        }
        
        return closest;
    }
    
    private Entity findLowestHealth(List<Entity> targets) {
        LivingEntity lowest = null;
        float lowestHealth = Float.MAX_VALUE;
        
        for (Entity entity : targets) {
            if (entity instanceof LivingEntity living) {
                float health = living.getHealth() + living.getAbsorptionAmount();
                if (health < lowestHealth) {
                    lowestHealth = health;
                    lowest = living;
                }
            }
        }
        
        return lowest;
    }
    
    private Entity findHighestThreat(List<Entity> targets) {
        Entity highest = null;
        double highestThreat = 0;
        
        for (Entity entity : targets) {
            ThreatData threat = threatMap.get(entity);
            if (threat != null && threat.threatLevel > highestThreat) {
                highestThreat = threat.threatLevel;
                highest = entity;
            }
        }
        
        return highest != null ? highest : findClosest(targets);
    }
    
    private Entity findSmartest(List<Entity> targets) {
        if (lastTarget != null && lastTarget.isAlive() && targets.contains(lastTarget)) {
            LivingEntity living = (LivingEntity) lastTarget;
            float healthPercent = living.getHealth() / living.getMaxHealth();
            
            if (healthPercent < 0.5 && random.nextDouble() > 0.2) {
                return lastTarget;
            }
        }
        
        Entity highestThreat = findHighestThreat(targets);
        if (highestThreat != null) {
            ThreatData threat = threatMap.get(highestThreat);
            if (threat != null && threat.threatLevel > 50) {
                return highestThreat;
            }
        }
        
        return findLowestHealth(targets);
    }
    
    private void attackTarget(Entity target) {
        mc.interactionManager.attackEntity(mc.player, target);
        mc.player.swingHand(Hand.MAIN_HAND);
        lastTarget = target;
    }
    
    private static class ThreatData {
        double threatLevel = 0;
        long lastUpdate = 0;
        
        void update(LivingEntity entity, PlayerEntity player) {
            double dist = player.squaredDistanceTo(entity);
            double distFactor = 1.0 / (1.0 + dist);
            
            double healthFactor = (entity.getHealth() + entity.getAbsorptionAmount()) / entity.getMaxHealth();
            
            double damageFactor = 1.0;
            if (entity instanceof PlayerEntity) {
                damageFactor = CombatUtils.getPlayerDamage((PlayerEntity) entity);
            }
            
            threatLevel = (distFactor * 50) + (healthFactor * 30) + (damageFactor * 20);
            lastUpdate = System.currentTimeMillis();
        }
    }
    
    public enum TargetMode {
        Closest,
        LowestHealth,
        HighestThreat,
        Smart
    }
}
