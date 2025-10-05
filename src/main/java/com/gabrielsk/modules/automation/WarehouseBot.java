package com.gabrielsk.modules.automation;

import com.gabrielsk.GabrielSKAddon;
import com.gabrielsk.ai.legit.Humanizer;
import com.gabrielsk.ai.legit.Visibility;
import com.gabrielsk.pathfinding.AStarPathfinder;
import com.gabrielsk.pathfinding.PathfindingOptions;

import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import java.util.*;

/**
 * WarehouseBot: Finds nearby storage (chests/barrels), pathfinds there, opens, and deposits items.
 */
public class WarehouseBot extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgLegit = settings.createGroup("Legit");
    private final SettingGroup sgFilter = settings.createGroup("Filter");

    private final Setting<Double> radius = sgGeneral.add(new DoubleSetting.Builder()
        .name("radius").description("Search radius for storage blocks.")
        .defaultValue(16.0).min(6.0).max(48.0).sliderMax(64.0).build());

    private final Setting<Boolean> includeShulkers = sgGeneral.add(new BoolSetting.Builder()
        .name("include-shulkers").description("Also use placed shulker boxes as storage.")
        .defaultValue(false).build());

    // Legit
    private final Setting<Boolean> legitMode = sgLegit.add(new BoolSetting.Builder()
        .name("enable").description("Enable human-like LOS, pacing, and movement.")
        .defaultValue(true).build());
    private final Setting<Boolean> requireLos = sgLegit.add(new BoolSetting.Builder()
        .name("require-los").description("Require LOS to chest/barrel before interacting.")
        .defaultValue(true).visible(legitMode::get).build());
    private final Setting<Integer> openDelayMs = sgLegit.add(new IntSetting.Builder()
        .name("open-delay-ms").description("Delay between open/close actions (ms).")
        .defaultValue(160).min(60).max(800).sliderMax(1000).visible(legitMode::get).build());
    private final Setting<Double> openJitter = sgLegit.add(new DoubleSetting.Builder()
        .name("open-jitter").description("Randomization (0-1)")
        .defaultValue(0.25).min(0.0).max(1.0).sliderMax(1.0).visible(legitMode::get).build());
    private final Setting<Double> walkSpeed = sgLegit.add(new DoubleSetting.Builder()
        .name("walk-speed").description("Walking speed towards path nodes.")
        .defaultValue(0.14).min(0.05).max(0.3).sliderMax(0.3).visible(legitMode::get).build());

    // Filters
    private final Setting<Boolean> keepToolsWeapons = sgFilter.add(new BoolSetting.Builder()
        .name("keep-tools-weapons").description("Keep common tools/weapons/armor in inventory.")
        .defaultValue(true).build());
    private final Setting<Boolean> keepFood = sgFilter.add(new BoolSetting.Builder()
        .name("keep-food").description("Keep common food items.")
        .defaultValue(true).build());
    private final Setting<Boolean> keepBlocks = sgFilter.add(new BoolSetting.Builder()
        .name("keep-blocks").description("Keep small stack of building blocks (first stack only).")
        .defaultValue(true).build());

    private enum State { IDLE, WALKING, OPENING, TRANSFERRING }
    private State state = State.IDLE;

    private List<BlockPos> path = Collections.emptyList();
    private int pathIndex = 0;
    private BlockPos targetChest = null;
    private long lastActionAt = 0L;
    private boolean keptOneBlockStack = false;

    public WarehouseBot() {
        super(GabrielSKAddon.CATEGORY, "warehouse-bot", "Auto-stores your inventory into nearby chests/barrels.");
    }

    @Override
    public void onActivate() {
        reset();
    }

    private void reset() {
        state = State.IDLE;
        path = Collections.emptyList();
        pathIndex = 0;
        targetChest = null;
        keptOneBlockStack = false;
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;

        switch (state) {
            case IDLE -> {
                if (!hasDepositableItems()) return;
                targetChest = findNearestStorage();
                if (targetChest != null) {
                    buildPathToAdjacent(targetChest);
                    state = State.WALKING;
                }
            }
            case WALKING -> {
                if (targetChest == null) { state = State.IDLE; return; }
                if (!path.isEmpty() && pathIndex < path.size()) {
                    walkTowardsNode(path.get(pathIndex));
                    if (isNear(path.get(pathIndex), 0.75)) pathIndex++;
                } else {
                    state = State.OPENING;
                }
            }
            case OPENING -> {
                if (!isValidStorage(targetChest)) { reset(); return; }
                if (legitMode.get()) {
                    long now = System.currentTimeMillis();
                    long delay = Humanizer.reactionMs(openDelayMs.get(), openJitter.get());
                    if (now - lastActionAt < delay) return;
                    if (requireLos.get() && !Visibility.hasLineOfSight(mc, Vec3d.ofCenter(targetChest))) return;
                }
                // Face and open
                meteordevelopment.meteorclient.utils.player.Rotations.rotate(meteordevelopment.meteorclient.utils.player.Rotations.getYaw(Vec3d.ofCenter(targetChest)), meteordevelopment.meteorclient.utils.player.Rotations.getPitch(Vec3d.ofCenter(targetChest)));
                BlockHitResult bhr = new BlockHitResult(Vec3d.ofCenter(targetChest), Direction.UP, targetChest, false);
                mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, bhr);
                lastActionAt = System.currentTimeMillis();
                state = State.TRANSFERRING;
            }
            case TRANSFERRING -> {
                if (!isStorageScreenOpen()) {
                    // Wait a bit for screen to open
                    return;
                }
                transferInventoryToStorage();
                // Close and finish
                mc.player.closeHandledScreen();
                lastActionAt = System.currentTimeMillis();
                reset();
            }
        }
    }

    private boolean hasDepositableItems() {
        var inv = mc.player.getInventory();
        keptOneBlockStack = false;
        for (int i = 0; i < inv.size(); i++) {
            var stack = inv.getStack(i);
            if (stack.isEmpty()) continue;
            if (shouldKeep(stack.getItem())) continue;
            return true;
        }
        return false;
    }

    private boolean shouldKeep(Item item) {
        if (keepToolsWeapons.get()) {
            if (isToolWeaponArmor(item) || item == Items.TOTEM_OF_UNDYING || item == Items.ENDER_PEARL || item == Items.WATER_BUCKET) return true;
        }
        if (keepFood.get()) {
            if (isFood(item)) return true;
        }
        if (keepBlocks.get()) {
            if (!keptOneBlockStack && isBuildingBlock(item)) {
                keptOneBlockStack = true;
                return true;
            }
        }
        return false;
    }

    private boolean isToolWeaponArmor(Item item) {
        return item == Items.NETHERITE_SWORD || item == Items.DIAMOND_SWORD || item == Items.IRON_SWORD ||
               item == Items.NETHERITE_AXE || item == Items.DIAMOND_AXE || item == Items.IRON_AXE ||
               item == Items.NETHERITE_PICKAXE || item == Items.DIAMOND_PICKAXE || item == Items.IRON_PICKAXE ||
               item == Items.NETHERITE_SHOVEL || item == Items.DIAMOND_SHOVEL || item == Items.IRON_SHOVEL ||
               item == Items.NETHERITE_HELMET || item == Items.NETHERITE_CHESTPLATE || item == Items.NETHERITE_LEGGINGS || item == Items.NETHERITE_BOOTS ||
               item == Items.DIAMOND_HELMET || item == Items.DIAMOND_CHESTPLATE || item == Items.DIAMOND_LEGGINGS || item == Items.DIAMOND_BOOTS ||
               item == Items.IRON_HELMET || item == Items.IRON_CHESTPLATE || item == Items.IRON_LEGGINGS || item == Items.IRON_BOOTS ||
               item == Items.SHIELD || item == Items.BOW || item == Items.CROSSBOW || item == Items.TRIDENT;
    }

    private boolean isFood(Item item) {
        // Check if item has food component in 1.21.1
        return item.getComponents().contains(net.minecraft.component.DataComponentTypes.FOOD) || 
               item == Items.GOLDEN_APPLE || item == Items.ENCHANTED_GOLDEN_APPLE;
    }

    private boolean isBuildingBlock(Item item) {
        // Heuristic: keep one stack of common blocks
     return item == Blocks.COBBLESTONE.asItem() ||
         item == Blocks.STONE.asItem() ||
         item == Blocks.DIRT.asItem() ||
         item == Blocks.NETHERRACK.asItem() ||
         item == Blocks.DEEPSLATE.asItem() ||
         item == Blocks.OAK_PLANKS.asItem() ||
         item == Blocks.COBBLED_DEEPSLATE.asItem();
    }

    private BlockPos findNearestStorage() {
        BlockPos center = mc.player.getBlockPos();
        int r = (int) Math.floor(radius.get());
        BlockPos best = null;
        double bestDist = Double.MAX_VALUE;
        for (int dx = -r; dx <= r; dx++) {
            for (int dy = -2; dy <= 2; dy++) {
                for (int dz = -r; dz <= r; dz++) {
                    BlockPos p = center.add(dx, dy, dz);
                    if (!isValidStorage(p)) continue;
                    double d = mc.player.squaredDistanceTo(p.getX() + 0.5, p.getY() + 0.5, p.getZ() + 0.5);
                    if (d < bestDist) { bestDist = d; best = p; }
                }
            }
        }
        return best;
    }

    private boolean isValidStorage(BlockPos pos) {
        Block b = mc.world.getBlockState(pos).getBlock();
        if (b == Blocks.CHEST || b == Blocks.TRAPPED_CHEST || b == Blocks.BARREL) return true;
        if (includeShulkers.get()) {
            // Any shulker box color
            return b == Blocks.SHULKER_BOX || b == Blocks.WHITE_SHULKER_BOX || b == Blocks.LIGHT_GRAY_SHULKER_BOX || b == Blocks.GRAY_SHULKER_BOX ||
                   b == Blocks.BLACK_SHULKER_BOX || b == Blocks.BROWN_SHULKER_BOX || b == Blocks.RED_SHULKER_BOX || b == Blocks.ORANGE_SHULKER_BOX ||
                   b == Blocks.YELLOW_SHULKER_BOX || b == Blocks.LIME_SHULKER_BOX || b == Blocks.GREEN_SHULKER_BOX || b == Blocks.CYAN_SHULKER_BOX ||
                   b == Blocks.LIGHT_BLUE_SHULKER_BOX || b == Blocks.BLUE_SHULKER_BOX || b == Blocks.PURPLE_SHULKER_BOX || b == Blocks.MAGENTA_SHULKER_BOX ||
                   b == Blocks.PINK_SHULKER_BOX;
        }
        return false;
    }

    private void buildPathToAdjacent(BlockPos target) {
        List<BlockPos> adj = List.of(target.north(), target.south(), target.east(), target.west());
        BlockPos start = mc.player.getBlockPos();
        PathfindingOptions opts = PathfindingOptions.safe();
        List<BlockPos> best = Collections.emptyList();
        for (BlockPos a : adj) {
            List<BlockPos> p = AStarPathfinder.findPath(start, a, opts);
            if (!p.isEmpty() && (best.isEmpty() || p.size() < best.size())) best = p;
        }
        path = best;
        pathIndex = 0;
    }

    private void walkTowardsNode(BlockPos node) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;
        Vec3d target = Vec3d.ofCenter(node);
        meteordevelopment.meteorclient.utils.player.Rotations.rotate(meteordevelopment.meteorclient.utils.player.Rotations.getYaw(target), meteordevelopment.meteorclient.utils.player.Rotations.getPitch(target));
        Vec3d diff = target.subtract(mc.player.getPos());
        Vec3d dir = new Vec3d(diff.x, 0, diff.z).normalize();
        if (Double.isFinite(dir.x) && Double.isFinite(dir.z)) {
            double speed = walkSpeed.get();
            Vec3d vel = mc.player.getVelocity();
            mc.player.setVelocity(dir.x * speed, vel.y, dir.z * speed);
            mc.player.velocityModified = true;
        }
    }

    private boolean isNear(BlockPos node, double radius) {
        Vec3d c = Vec3d.ofCenter(node);
        return mc.player.getPos().squaredDistanceTo(c) <= radius * radius;
    }

    private boolean isStorageScreenOpen() {
        if (mc.player == null) return false;
        ScreenHandler h = mc.player.currentScreenHandler;
        if (h == null) return false;
        // Not the player inventory screen
        return h != mc.player.playerScreenHandler && h.slots.size() > 36;
    }

    private void transferInventoryToStorage() {
        ScreenHandler h = mc.player.currentScreenHandler;
        if (h == null) return;
        int totalSlots = h.slots.size();
        int playerStart = totalSlots - 36;
        // Iterate player's inventory slots (27 inv + 9 hotbar)
        for (int i = playerStart; i < totalSlots; i++) {
            Slot slot = h.getSlot(i);
            if (!slot.hasStack()) continue;
            Item item = slot.getStack().getItem();
            if (shouldKeep(item)) continue;
            mc.interactionManager.clickSlot(h.syncId, i, 0, SlotActionType.QUICK_MOVE, mc.player);
        }
    }
}
