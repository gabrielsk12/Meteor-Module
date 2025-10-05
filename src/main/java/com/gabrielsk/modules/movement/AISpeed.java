package com.gabrielsk.modules.movement;

import com.gabrielsk.GabrielSKAddon;
import com.gabrielsk.ai.BehaviorNode;
import com.gabrielsk.ai.CompositeNode;
import com.gabrielsk.ai.GOAPPlanner;
import com.gabrielsk.ai.WorldState;
import com.gabrielsk.math.MathUtils;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.*;

public class AISpeed extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgAI = settings.createGroup("AI");
    
    private final Setting<Mode> mode = sgGeneral.add(new EnumSetting.Builder<Mode>()
        .name("mode").description("Speed mode.").defaultValue(Mode.Smart).build());
    
    private final Setting<Double> baseSpeed = sgGeneral.add(new DoubleSetting.Builder()
        .name("base-speed").description("Base speed multiplier.").defaultValue(1.5).min(0.1).max(10).sliderMax(5).build());
    
    private final Setting<Boolean> adaptiveSpeed = sgAI.add(new BoolSetting.Builder()
        .name("adaptive").description("AI adapts speed to situation.").defaultValue(true).build());
    
    private final Setting<Boolean> avoidDanger = sgAI.add(new BoolSetting.Builder()
        .name("avoid-danger").description("Slow down near dangers.").defaultValue(true).build());
    
    private final Setting<Boolean> pathOptimization = sgAI.add(new BoolSetting.Builder()
        .name("path-optimization").description("AI optimizes movement path.").defaultValue(true).build());
    
    private final Setting<Boolean> energyConservation = sgAI.add(new BoolSetting.Builder()
        .name("energy-conservation").description("Conserve energy when safe.").defaultValue(true).build());
    
    private BehaviorNode behaviorTree;
    private GOAPPlanner planner;
    private double currentSpeed = 1.0;
    private Vec3d targetDirection = Vec3d.ZERO;
    private Random random = new Random();
    private Map<BlockPos, DangerLevel> dangerMap = new HashMap<>();
    
    public AISpeed() {
        super(GabrielSKAddon.CATEGORY, "ai-speed", "AI-powered movement speed.");
        initAI();
    }
    
    private void initAI() {
        behaviorTree = new CompositeNode.Selector(Arrays.asList(
            createDangerAvoidanceNode(),
            createOptimalSpeedNode(),
            createDefaultMovementNode()
        ));
    }
    
    private BehaviorNode createDangerAvoidanceNode() {
        return new CompositeNode.Sequence(Arrays.asList(
            (BehaviorNode) () -> avoidDanger.get() && detectDanger() ? BehaviorNode.Status.SUCCESS : BehaviorNode.Status.FAILURE,
            (BehaviorNode) () -> {
                currentSpeed = baseSpeed.get() * 0.5;
                return BehaviorNode.Status.SUCCESS;
            }
        ));
    }
    
    private BehaviorNode createOptimalSpeedNode() {
        return new CompositeNode.Sequence(Arrays.asList(
            (BehaviorNode) () -> adaptiveSpeed.get() ? BehaviorNode.Status.SUCCESS : BehaviorNode.Status.FAILURE,
            (BehaviorNode) () -> {
                currentSpeed = calculateOptimalSpeed();
                return BehaviorNode.Status.SUCCESS;
            }
        ));
    }
    
    private BehaviorNode createDefaultMovementNode() {
        return (BehaviorNode) () -> {
            currentSpeed = baseSpeed.get();
            return BehaviorNode.Status.SUCCESS;
        };
    }
    
    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (mc.player == null || mc.world == null) return;
        
        updateDangerMap();
        behaviorTree.tick();
        
        applyMovement();
    }
    
    private void updateDangerMap() {
        dangerMap.clear();
        BlockPos playerPos = mc.player.getBlockPos();
        
        for (int x = -5; x <= 5; x++) {
            for (int y = -2; y <= 2; y++) {
                for (int z = -5; z <= 5; z++) {
                    BlockPos pos = playerPos.add(x, y, z);
                    DangerLevel danger = assessDanger(pos);
                    if (danger != DangerLevel.SAFE) {
                        dangerMap.put(pos, danger);
                    }
                }
            }
        }
    }
    
    private DangerLevel assessDanger(BlockPos pos) {
        if (mc.world.getBlockState(pos).getBlock() == Blocks.LAVA) {
            return DangerLevel.CRITICAL;
        }
        
        if (mc.world.getBlockState(pos.down()).isAir() && 
            mc.world.getBlockState(pos.down(2)).isAir()) {
            return DangerLevel.HIGH;
        }
        
        for (Entity entity : mc.world.getEntities()) {
            if (entity instanceof LivingEntity living && living != mc.player) {
                double dist = entity.squaredDistanceTo(pos.getX(), pos.getY(), pos.getZ());
                if (dist < 25) {
                    return DangerLevel.MEDIUM;
                }
            }
        }
        
        return DangerLevel.SAFE;
    }
    
    private boolean detectDanger() {
        BlockPos playerPos = mc.player.getBlockPos();
        
        for (int i = 0; i < 3; i++) {
            Vec3d checkPos = mc.player.getPos().add(
                mc.player.getVelocity().x * i,
                0,
                mc.player.getVelocity().z * i
            );
            
            DangerLevel danger = dangerMap.getOrDefault(
                new BlockPos((int)checkPos.x, (int)checkPos.y, (int)checkPos.z),
                DangerLevel.SAFE
            );
            
            if (danger.ordinal() >= DangerLevel.MEDIUM.ordinal()) {
                return true;
            }
        }
        
        return false;
    }
    
    private double calculateOptimalSpeed() {
        double speed = baseSpeed.get();
        
        if (mc.player.getHealth() < 10) {
            speed *= 1.5;
        }
        
        if (energyConservation.get() && mc.player.getHungerManager().getFoodLevel() < 6) {
            speed *= 0.7;
        }
        
        if (pathOptimization.get()) {
            Vec3d velocity = mc.player.getVelocity();
            if (velocity.horizontalLength() < 0.1) {
                speed *= 1.2;
            }
        }
        
        int nearbyEnemies = 0;
        for (Entity entity : mc.world.getEntities()) {
            if (entity instanceof LivingEntity living && living != mc.player) {
                double dist = mc.player.squaredDistanceTo(entity);
                if (dist < 100) {
                    nearbyEnemies++;
                }
            }
        }
        
        if (nearbyEnemies > 3) {
            speed *= 1.3;
        }
        
        if (random.nextDouble() < 0.05) {
            speed *= (0.95 + random.nextDouble() * 0.1);
        }
        
        return Math.max(0.1, Math.min(speed, 10.0));
    }
    
    private void applyMovement() {
        if (!mc.player.isOnGround()) return;
        
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
            forward = forward > 0 ? 1 : -1;
        }
        
        double mx = Math.cos(Math.toRadians(yaw + 90));
        double mz = Math.sin(Math.toRadians(yaw + 90));
        
        mc.player.setVelocity(
            forward * currentSpeed * mx + strafe * currentSpeed * mz,
            mc.player.getVelocity().y,
            forward * currentSpeed * mz - strafe * currentSpeed * mx
        );
    }
    
    private enum DangerLevel {
        SAFE,
        LOW,
        MEDIUM,
        HIGH,
        CRITICAL
    }
    
    public enum Mode {
        Simple,
        Smart
    }
}
