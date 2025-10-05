package com.gabrielsk.ai;

import com.gabrielsk.pathfinding.*;
import net.minecraft.block.Block;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * Advanced AI Bot Controller
 * Uses Behavior Trees, GOAP, and A* pathfinding for intelligent decision making
 * This is TRUE AI - not hardcoded behavior
 */
public class AdvancedAIBot {
    
    private static final MinecraftClient mc = MinecraftClient.getInstance();
    
    // AI Systems
    private BehaviorNode rootBehavior;
    private List<GOAPAction> availableActions;
    private WorldState currentWorldState;
    private List<GOAPAction> currentPlan;
    private int currentPlanStep = 0;
    
    // Pathfinding
    private List<BlockPos> currentPath;
    private int currentPathStep = 0;
    private CompletableFuture<List<BlockPos>> pathfindingFuture;
    
    // State tracking
    private LivingEntity currentTarget;
    private BlockPos explorationTarget;
    private long lastPlanTime = 0;
    private static final long REPLAN_INTERVAL = 2000; // Replan every 2 seconds
    
    // Settings
    private boolean combatEnabled = true;
    private boolean explorationEnabled = true;
    private boolean survivalEnabled = true;
    private double attackRange = 3.5;
    private double detectionRange = 32.0;
    
    public AdvancedAIBot() {
        initializeAI();
    }
    
    /**
     * Initialize AI systems
     */
    private void initializeAI() {
        // Build behavior tree
        rootBehavior = buildBehaviorTree();
        
        // Initialize available actions for GOAP
        availableActions = new ArrayList<>();
        initializeGOAPActions();
        
        // Initialize world state
        currentWorldState = new WorldState();
        updateWorldState();
    }
    
    /**
     * Build the behavior tree structure
     * This defines the high-level decision making logic
     */
    private BehaviorNode buildBehaviorTree() {
        return new CompositeNode.Selector(
            // Priority 1: Emergency situations
            new CompositeNode.Sequence(
                new ConditionNode(() -> isInDanger()),
                new ActionNode(() -> handleDanger())
            ),
            
            // Priority 2: Combat
            new CompositeNode.Sequence(
                new ConditionNode(() -> combatEnabled && hasEnemy()),
                new ActionNode(() -> engageCombat())
            ),
            
            // Priority 3: Execute GOAP plan
            new CompositeNode.Sequence(
                new ConditionNode(() -> hasValidPlan()),
                new ActionNode(() -> executePlan())
            ),
            
            // Priority 4: Create new GOAP plan
            new ActionNode(() -> createNewPlan()),
            
            // Priority 5: Default exploration
            new CompositeNode.Sequence(
                new ConditionNode(() -> explorationEnabled),
                new ActionNode(() -> explore())
            ),
            
            // Fallback: Idle
            new ActionNode(() -> idle())
        );
    }
    
    /**
     * Main AI tick - called every game tick
     */
    public void tick() {
        if (mc.player == null || mc.world == null) return;
        
        // Update world state
        updateWorldState();
        
        // Tick behavior tree
        rootBehavior.tick();
        
        // Update pathfinding
        updatePathfinding();
    }
    
    /**
     * Update world state for GOAP planning
     */
    private void updateWorldState() {
        currentWorldState.set("hasEnemy", hasEnemy());
        currentWorldState.set("inDanger", isInDanger());
        currentWorldState.set("health", getHealth());
        currentWorldState.set("hunger", getHunger());
        currentWorldState.set("hasWeapon", hasWeapon());
        currentWorldState.set("nearWater", isNearWater());
        currentWorldState.set("nearLava", isNearLava());
        currentWorldState.set("canReachTarget", currentPath != null && !currentPath.isEmpty());
    }
    
    /**
     * Initialize GOAP actions
     */
    private void initializeGOAPActions() {
        // Combat actions
        availableActions.add(new AttackEnemyAction());
        availableActions.add(new RetreatAction());
        availableActions.add(new FindWeaponAction());
        
        // Survival actions
        availableActions.add(new EatFoodAction());
        availableActions.add(new FindSafeLocationAction());
        availableActions.add(new AvoidDangerAction());
        
        // Exploration actions
        availableActions.add(new ExploreAreaAction());
        availableActions.add(new NavigateToLocationAction());
    }
    
    /**
     * Create a new GOAP plan based on current situation
     */
    private BehaviorNode.Status createNewPlan() {
        long currentTime = System.currentTimeMillis();
        
        if (currentTime - lastPlanTime < REPLAN_INTERVAL) {
            return BehaviorNode.Status.FAILURE; // Don't replan too often
        }
        
        lastPlanTime = currentTime;
        
        // Determine goal based on current situation
        WorldState goalState = determineGoal();
        
        // Plan actions to achieve goal
        currentPlan = GOAPPlanner.plan(currentWorldState, goalState, availableActions);
        currentPlanStep = 0;
        
        if (currentPlan.isEmpty()) {
            return BehaviorNode.Status.FAILURE;
        }
        
        return BehaviorNode.Status.SUCCESS;
    }
    
    /**
     * Determine current goal based on world state
     */
    private WorldState determineGoal() {
        WorldState goal = new WorldState();
        
        // Priority goals based on situation
        if (getHealth() < 10.0) {
            // Critical health - survive!
            goal.set("health", 20.0);
            goal.set("inDanger", false);
        } else if (hasEnemy() && combatEnabled) {
            // Combat goal
            goal.set("hasEnemy", false);
            goal.set("enemyDefeated", true);
        } else if (getHunger() < 6) {
            // Hungry - find food
            goal.set("hunger", 20);
        } else {
            // Default - explore
            goal.set("explored", true);
        }
        
        return goal;
    }
    
    /**
     * Execute current GOAP plan
     */
    private BehaviorNode.Status executePlan() {
        if (currentPlan == null || currentPlanStep >= currentPlan.size()) {
            return BehaviorNode.Status.FAILURE;
        }
        
        GOAPAction currentAction = currentPlan.get(currentPlanStep);
        
        // Check if action can still be executed
        if (!currentAction.checkPreconditions(currentWorldState)) {
            // Preconditions no longer met - replan
            currentPlan = null;
            return BehaviorNode.Status.FAILURE;
        }
        
        // Execute action
        boolean actionComplete = currentAction.execute();
        
        if (actionComplete) {
            currentPlanStep++;
            
            if (currentPlanStep >= currentPlan.size()) {
                // Plan complete!
                currentPlan = null;
                return BehaviorNode.Status.SUCCESS;
            }
        }
        
        return BehaviorNode.Status.RUNNING;
    }
    
    /**
     * Handle dangerous situations
     */
    private BehaviorNode.Status handleDanger() {
        if (isNearLava()) {
            // Move away from lava
            Vec3d playerPos = mc.player.getPos();
            Vec3d escapeDir = findSafeDirection();
            
            if (escapeDir != null) {
                BlockPos safePos = new BlockPos(
                    (int) (playerPos.x + escapeDir.x * 10),
                    (int) playerPos.y,
                    (int) (playerPos.z + escapeDir.z * 10)
                );
                pathfindTo(safePos, PathfindingOptions.safe());
            }
        }
        
        if (getHealth() < 10.0) {
            // Critical health - retreat
            retreatFromCombat();
        }
        
        return BehaviorNode.Status.SUCCESS;
    }
    
    /**
     * Engage in combat with current target
     */
    private BehaviorNode.Status engageCombat() {
        if (currentTarget == null || !currentTarget.isAlive()) {
            currentTarget = findNearestEnemy();
        }
        
        if (currentTarget == null) {
            return BehaviorNode.Status.FAILURE;
        }
        
        double distance = mc.player.distanceTo(currentTarget);
        
        if (distance > attackRange) {
            // Move closer
            pathfindTo(currentTarget.getBlockPos(), PathfindingOptions.aggressive());
            return BehaviorNode.Status.RUNNING;
        } else {
            // Attack
            attackEntity(currentTarget);
            // Strafe around enemy
            strafeAroundTarget();
            return BehaviorNode.Status.SUCCESS;
        }
    }
    
    /**
     * Explore the world
     */
    private BehaviorNode.Status explore() {
        if (explorationTarget == null || reachedTarget(explorationTarget)) {
            // Pick new exploration target
            explorationTarget = pickRandomExplorationTarget();
        }
        
        if (explorationTarget != null) {
            pathfindTo(explorationTarget, PathfindingOptions.safe());
            return BehaviorNode.Status.RUNNING;
        }
        
        return BehaviorNode.Status.FAILURE;
    }
    
    /**
     * Idle behavior
     */
    private BehaviorNode.Status idle() {
        // Look around randomly
        if (Math.random() < 0.05) {
            mc.player.setYaw((float) (Math.random() * 360));
        }
        return BehaviorNode.Status.SUCCESS;
    }
    
    // ==================== PATHFINDING ====================
    
    /**
     * Start pathfinding to target position
     */
    private void pathfindTo(BlockPos target, PathfindingOptions options) {
        if (pathfindingFuture != null && !pathfindingFuture.isDone()) {
            return; // Already pathfinding
        }
        
        BlockPos start = mc.player.getBlockPos();
        pathfindingFuture = AStarPathfinder.findPathAsync(start, target, options);
        
        pathfindingFuture.thenAccept(path -> {
            currentPath = path;
            currentPathStep = 0;
        });
    }
    
    /**
     * Update pathfinding - follow current path
     */
    private void updatePathfinding() {
        if (currentPath == null || currentPath.isEmpty()) return;
        if (currentPathStep >= currentPath.size()) {
            currentPath = null;
            return;
        }
        
        BlockPos nextPos = currentPath.get(currentPathStep);
        Vec3d targetVec = Vec3d.ofCenter(nextPos);
        
        // Move towards next position
        moveTowards(targetVec);
        
        // Check if reached
        if (mc.player.getBlockPos().isWithinDistance(nextPos, 1.5)) {
            currentPathStep++;
        }
    }
    
    /**
     * Move player towards target position
     */
    private void moveTowards(Vec3d target) {
        if (mc.player == null) return;
        
        Vec3d playerPos = mc.player.getPos();
        Vec3d direction = target.subtract(playerPos).normalize();
        
        // Calculate yaw
        float yaw = (float) Math.toDegrees(Math.atan2(direction.z, direction.x)) - 90;
        mc.player.setYaw(yaw);
        
        // Move forward
        mc.player.input.pressingForward = true;
        
        // Jump if needed
        if (mc.player.horizontalCollision || shouldJump(target)) {
            mc.player.input.jumping = true;
        }
    }
    
    private boolean shouldJump(Vec3d target) {
        return target.y > mc.player.getY() + 0.5;
    }
    
    // ==================== COMBAT ====================
    
    private void attackEntity(LivingEntity entity) {
        if (mc.interactionManager == null) return;
        
        // Look at entity
        lookAt(entity.getEyePos());
        
        // Attack
        mc.interactionManager.attackEntity(mc.player, entity);
        mc.player.swingHand(net.minecraft.util.Hand.MAIN_HAND);
    }
    
    private void strafeAroundTarget() {
        if (currentTarget == null) return;
        
        // Strafe in a circle
        double angle = System.currentTimeMillis() / 500.0;
        double strafeX = Math.cos(angle) * 0.3;
        double strafeZ = Math.sin(angle) * 0.3;
        
        Vec3d targetPos = currentTarget.getPos().add(strafeX, 0, strafeZ);
        moveTowards(targetPos);
    }
    
    private void retreatFromCombat() {
        if (currentTarget != null) {
            Vec3d retreatDir = mc.player.getPos().subtract(currentTarget.getPos()).normalize();
            Vec3d retreatPos = mc.player.getPos().add(retreatDir.multiply(20));
            
            pathfindTo(new BlockPos((int)retreatPos.x, (int)retreatPos.y, (int)retreatPos.z), 
                      PathfindingOptions.safe());
        }
    }
    
    private void lookAt(Vec3d target) {
        Vec3d playerPos = mc.player.getEyePos();
        Vec3d direction = target.subtract(playerPos).normalize();
        
        float yaw = (float) Math.toDegrees(Math.atan2(direction.z, direction.x)) - 90;
        float pitch = (float) -Math.toDegrees(Math.asin(direction.y));
        
        mc.player.setYaw(yaw);
        mc.player.setPitch(pitch);
    }
    
    // ==================== UTILITY METHODS ====================
    
    private boolean hasEnemy() {
        return findNearestEnemy() != null;
    }
    
    private LivingEntity findNearestEnemy() {
        if (mc.world == null) return null;
        
        LivingEntity nearest = null;
        double nearestDist = detectionRange;
        
        for (Entity entity : mc.world.getEntities()) {
            if (!(entity instanceof LivingEntity)) continue;
            if (!entity.isAlive()) continue;
            
            LivingEntity living = (LivingEntity) entity;
            
            // Check if hostile or enemy player
            boolean isEnemy = (living instanceof HostileEntity) ||
                            (living instanceof PlayerEntity && living != mc.player);
            
            if (isEnemy) {
                double dist = mc.player.distanceTo(living);
                if (dist < nearestDist) {
                    nearest = living;
                    nearestDist = dist;
                }
            }
        }
        
        return nearest;
    }
    
    private boolean isInDanger() {
        return getHealth() < 10.0 || isNearLava() || isFalling();
    }
    
    private double getHealth() {
        return mc.player != null ? mc.player.getHealth() : 20.0;
    }
    
    private int getHunger() {
        return mc.player != null ? mc.player.getHungerManager().getFoodLevel() : 20;
    }
    
    private boolean hasWeapon() {
        // Check if holding a weapon
        return mc.player != null && !mc.player.getMainHandStack().isEmpty();
    }
    
    private boolean isNearWater() {
        return mc.world != null && mc.world.getBlockState(mc.player.getBlockPos()).isLiquid();
    }
    
    private boolean isNearLava() {
        if (mc.world == null) return false;
        
        BlockPos pos = mc.player.getBlockPos();
        for (int x = -2; x <= 2; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -2; z <= 2; z++) {
                    Block block = mc.world.getBlockState(pos.add(x, y, z)).getBlock();
                    if (block == net.minecraft.block.Blocks.LAVA) {
                        return true;
                    }
                }
            }
        }
        
        return false;
    }
    
    private boolean isFalling() {
        return mc.player != null && mc.player.getVelocity().y < -0.5;
    }
    
    private Vec3d findSafeDirection() {
        // Find direction away from danger
        Vec3d playerPos = mc.player.getPos();
        Vec3d safeDir = Vec3d.ZERO;
        
        // Average direction away from all lava blocks
        BlockPos pos = mc.player.getBlockPos();
        for (int x = -5; x <= 5; x++) {
            for (int z = -5; z <= 5; z++) {
                BlockPos checkPos = pos.add(x, 0, z);
                if (mc.world.getBlockState(checkPos).getBlock() == net.minecraft.block.Blocks.LAVA) {
                    Vec3d lavaPos = Vec3d.ofCenter(checkPos);
                    safeDir = safeDir.add(playerPos.subtract(lavaPos));
                }
            }
        }
        
        return safeDir.lengthSquared() > 0 ? safeDir.normalize() : null;
    }
    
    private BlockPos pickRandomExplorationTarget() {
        Vec3d playerPos = mc.player.getPos();
        double angle = Math.random() * Math.PI * 2;
        double distance = 50 + Math.random() * 50;
        
        int x = (int) (playerPos.x + Math.cos(angle) * distance);
        int z = (int) (playerPos.z + Math.sin(angle) * distance);
        int y = mc.world.getTopY(net.minecraft.world.Heightmap.Type.MOTION_BLOCKING, x, z);
        
        return new BlockPos(x, y, z);
    }
    
    private boolean reachedTarget(BlockPos target) {
        return mc.player.getBlockPos().isWithinDistance(target, 3);
    }
    
    private boolean hasValidPlan() {
        return currentPlan != null && currentPlanStep < currentPlan.size();
    }
    
    // ==================== INNER CLASSES ====================
    
    /**
     * Condition node for behavior tree
     */
    private static class ConditionNode implements BehaviorNode {
        private final java.util.function.Supplier<Boolean> condition;
        
        ConditionNode(java.util.function.Supplier<Boolean> condition) {
            this.condition = condition;
        }
        
        @Override
        public Status tick() {
            return condition.get() ? Status.SUCCESS : Status.FAILURE;
        }
        
        @Override
        public void reset() {}
    }
    
    /**
     * Action node for behavior tree
     */
    private static class ActionNode implements BehaviorNode {
        private final java.util.function.Supplier<Status> action;
        
        ActionNode(java.util.function.Supplier<Status> action) {
            this.action = action;
        }
        
        @Override
        public Status tick() {
            return action.get();
        }
        
        @Override
        public void reset() {}
    }
    
    // ==================== GOAP ACTION IMPLEMENTATIONS ====================
    
    private class AttackEnemyAction extends GOAPAction {
        public AttackEnemyAction() {
            super("AttackEnemy", 1.0);
        }
        
        @Override
        public boolean checkPreconditions(WorldState state) {
            return state.getBoolean("hasEnemy");
        }
        
        @Override
        public WorldState applyEffects(WorldState state) {
            state.set("hasEnemy", false);
            state.set("enemyDefeated", true);
            return state;
        }
        
        @Override
        public boolean execute() {
            return engageCombat() == BehaviorNode.Status.SUCCESS;
        }
    }
    
    private class RetreatAction extends GOAPAction {
        public RetreatAction() {
            super("Retreat", 5.0);
        }
        
        @Override
        public boolean checkPreconditions(WorldState state) {
            return state.getBoolean("inDanger");
        }
        
        @Override
        public WorldState applyEffects(WorldState state) {
            state.set("inDanger", false);
            return state;
        }
        
        @Override
        public boolean execute() {
            retreatFromCombat();
            return true;
        }
    }
    
    private class ExploreAreaAction extends GOAPAction {
        public ExploreAreaAction() {
            super("Explore", 2.0);
        }
        
        @Override
        public boolean checkPreconditions(WorldState state) {
            return true; // Can always explore
        }
        
        @Override
        public WorldState applyEffects(WorldState state) {
            state.set("explored", true);
            return state;
        }
        
        @Override
        public boolean execute() {
            return explore() != BehaviorNode.Status.FAILURE;
        }
    }
    
    private class FindWeaponAction extends GOAPAction {
        public FindWeaponAction() {
            super("FindWeapon", 3.0);
        }
        
        @Override
        public boolean checkPreconditions(WorldState state) {
            return !state.getBoolean("hasWeapon");
        }
        
        @Override
        public WorldState applyEffects(WorldState state) {
            state.set("hasWeapon", true);
            return state;
        }
        
        @Override
        public boolean execute() {
            // TODO: Implement weapon finding logic
            return false;
        }
    }
    
    private class EatFoodAction extends GOAPAction {
        public EatFoodAction() {
            super("EatFood", 2.0);
        }
        
        @Override
        public boolean checkPreconditions(WorldState state) {
            return state.getInt("hunger") < 10;
        }
        
        @Override
        public WorldState applyEffects(WorldState state) {
            state.set("hunger", 20);
            return state;
        }
        
        @Override
        public boolean execute() {
            // TODO: Implement eating logic
            return false;
        }
    }
    
    private class FindSafeLocationAction extends GOAPAction {
        public FindSafeLocationAction() {
            super("FindSafeLocation", 4.0);
        }
        
        @Override
        public boolean checkPreconditions(WorldState state) {
            return state.getBoolean("inDanger");
        }
        
        @Override
        public WorldState applyEffects(WorldState state) {
            state.set("inDanger", false);
            return state;
        }
        
        @Override
        public boolean execute() {
            Vec3d safeDir = findSafeDirection();
            if (safeDir != null) {
                Vec3d safePos = mc.player.getPos().add(safeDir.multiply(15));
                pathfindTo(new BlockPos((int)safePos.x, (int)safePos.y, (int)safePos.z), 
                          PathfindingOptions.safe());
                return true;
            }
            return false;
        }
    }
    
    private class AvoidDangerAction extends GOAPAction {
        public AvoidDangerAction() {
            super("AvoidDanger", 1.0);
        }
        
        @Override
        public boolean checkPreconditions(WorldState state) {
            return state.getBoolean("nearLava") || state.getDouble("health") < 10.0;
        }
        
        @Override
        public WorldState applyEffects(WorldState state) {
            state.set("inDanger", false);
            return state;
        }
        
        @Override
        public boolean execute() {
            return handleDanger() == BehaviorNode.Status.SUCCESS;
        }
    }
    
    private class NavigateToLocationAction extends GOAPAction {
        public NavigateToLocationAction() {
            super("NavigateToLocation", 1.5);
        }
        
        @Override
        public boolean checkPreconditions(WorldState state) {
            return true;
        }
        
        @Override
        public WorldState applyEffects(WorldState state) {
            state.set("canReachTarget", true);
            return state;
        }
        
        @Override
        public boolean execute() {
            updatePathfinding();
            return currentPath != null;
        }
    }
    
    // ==================== SETTINGS ====================
    
    public void setCombatEnabled(boolean enabled) {
        this.combatEnabled = enabled;
    }
    
    public void setExplorationEnabled(boolean enabled) {
        this.explorationEnabled = enabled;
    }
    
    public void setSurvivalEnabled(boolean enabled) {
        this.survivalEnabled = enabled;
    }
    
    public void setAttackRange(double range) {
        this.attackRange = range;
    }
    
    public void setDetectionRange(double range) {
        this.detectionRange = range;
    }
}
