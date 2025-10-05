package com.gabrielsk.modules.automation;

import com.gabrielsk.GabrielSKAddon;
import com.gabrielsk.ai.legit.Humanizer;
import com.gabrielsk.ai.legit.Visibility;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Blocks;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;



/**
 * EverythingBot: runs a set of simple automation tasks with legit pacing.
 * Tasks included: Lawn cleanup, AutoTorch, Basic Farm harvest/replant, Simple AutoMine (surface ores).
 */
public class EverythingBot extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgLegit = settings.createGroup("Legit");
    private final SettingGroup sgTasks = settings.createGroup("Tasks");

    private final Setting<Double> radius = sgGeneral.add(new DoubleSetting.Builder()
        .name("radius").description("Scan radius for tasks.").defaultValue(8.0).min(4.0).max(16.0).sliderMax(24.0).build());

    // Legit
    private final Setting<Boolean> legitMode = sgLegit.add(new BoolSetting.Builder()
        .name("enable").description("Enable human-like pacing/LOS.").defaultValue(true).build());
    private final Setting<Boolean> requireLos = sgLegit.add(new BoolSetting.Builder()
        .name("require-los").description("Require LOS to targets.").defaultValue(true).visible(legitMode::get).build());
    private final Setting<Integer> actionDelayMs = sgLegit.add(new IntSetting.Builder()
        .name("action-delay-ms").description("Delay between actions (ms).")
        .defaultValue(120).min(30).max(600).sliderMax(600).visible(legitMode::get).build());
    private final Setting<Double> actionJitter = sgLegit.add(new DoubleSetting.Builder()
        .name("action-jitter").description("Randomization (0-1)")
        .defaultValue(0.25).min(0.0).max(1.0).sliderMax(1.0).visible(legitMode::get).build());

    // Tasks toggles
    private final Setting<Boolean> taskLawn = sgTasks.add(new BoolSetting.Builder()
        .name("lawn-cleanup").description("Remove grass/flowers/snow.")
        .defaultValue(true).build());
    private final Setting<Boolean> taskTorch = sgTasks.add(new BoolSetting.Builder()
        .name("auto-torch").description("Place torches at dark spots.")
        .defaultValue(true).build());
    private final Setting<Boolean> taskFarm = sgTasks.add(new BoolSetting.Builder()
        .name("auto-farm").description("Harvest mature wheat and replant.")
        .defaultValue(false).build());
    private final Setting<Boolean> taskMine = sgTasks.add(new BoolSetting.Builder()
        .name("auto-mine").description("Mine surface coal/iron/copper ores.")
        .defaultValue(false).build());

    private long lastActionAt = 0L;

    public EverythingBot() {
        super(GabrielSKAddon.CATEGORY, "everything-bot", "Runs multiple small automation tasks.");
    }

    @Override
    public void onActivate() {
        lastActionAt = 0L;
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;

        if (legitMode.get()) {
            long now = System.currentTimeMillis();
            long delay = Humanizer.reactionMs(actionDelayMs.get(), actionJitter.get());
            if (now - lastActionAt < delay) return;
        }

        // Try each task in order
        if (taskLawn.get() && lawnTask(mc)) {
            lastActionAt = System.currentTimeMillis();
            return;
        }
        if (taskTorch.get() && torchTask(mc)) {
            lastActionAt = System.currentTimeMillis();
            return;
        }
        if (taskFarm.get() && farmTask(mc)) {
            lastActionAt = System.currentTimeMillis();
            return;
        }
        if (taskMine.get() && mineTask(mc)) {
            lastActionAt = System.currentTimeMillis();
            return;
        }
    }

    // Tasks implementations
    private boolean lawnTask(net.minecraft.client.MinecraftClient mc) {
        // Reuse the same logic as LawnBot but simplified: break single nearest grass/flower
        BlockPos best = findNearestInRadius((pos) -> {
            var b = mc.world.getBlockState(pos).getBlock();
            return b == Blocks.SHORT_GRASS || b == Blocks.TALL_GRASS || b == Blocks.FERN || b == Blocks.DANDELION || b == Blocks.POPPY || b == Blocks.SNOW;
        });
        if (best == null) return false;
        if (requireLos.get() && !Visibility.hasLineOfSight(mc, Vec3d.ofCenter(best))) return false;
        mc.interactionManager.attackBlock(best, Direction.UP);
        mc.player.swingHand(Hand.MAIN_HAND);
        return true;
    }

    private boolean torchTask(net.minecraft.client.MinecraftClient mc) {
        // Place a torch at feet if light level is low and block is placeable
        BlockPos pos = mc.player.getBlockPos();
        int light = mc.world.getLightLevel(pos);
        if (light > 7) return false;
        FindItemResult torch = InvUtils.findInHotbar(Items.TORCH);
        if (!torch.found()) return false;
        if (!mc.world.getBlockState(pos).isAir()) return false;
        if (requireLos.get() && !Visibility.hasLineOfSight(mc, Vec3d.ofCenter(pos))) return false;
        InvUtils.swap(torch.slot(), false);
        BlockHitResult bhr = new BlockHitResult(Vec3d.ofCenter(pos.down()), Direction.UP, pos.down(), false);
        mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, bhr);
        InvUtils.swapBack();
        return true;
    }

    private boolean farmTask(net.minecraft.client.MinecraftClient mc) {
        // Very basic wheat harvest: break mature wheat and place seeds
        BlockPos best = findNearestInRadius((pos) -> mc.world.getBlockState(pos).getBlock() == Blocks.WHEAT
            && mc.world.getBlockState(pos).get( net.minecraft.block.CropBlock.AGE ) >= 7);
        if (best == null) return false;
        if (requireLos.get() && !Visibility.hasLineOfSight(mc, Vec3d.ofCenter(best))) return false;
        mc.interactionManager.attackBlock(best, Direction.UP);
        mc.player.swingHand(Hand.MAIN_HAND);
        // Replant
        FindItemResult seeds = InvUtils.findInHotbar(Items.WHEAT_SEEDS);
        if (seeds.found()) {
            InvUtils.swap(seeds.slot(), false);
            BlockHitResult bhr = new BlockHitResult(Vec3d.ofCenter(best), Direction.UP, best, false);
            mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, bhr);
            InvUtils.swapBack();
        }
        return true;
    }

    private boolean mineTask(net.minecraft.client.MinecraftClient mc) {
        // Mine nearest surface ore (coal/iron/copper) in radius
        BlockPos best = findNearestInRadius((pos) -> {
            var b = mc.world.getBlockState(pos).getBlock();
            return b == Blocks.COAL_ORE || b == Blocks.DEEPSLATE_COAL_ORE ||
                   b == Blocks.IRON_ORE || b == Blocks.DEEPSLATE_IRON_ORE ||
                   b == Blocks.COPPER_ORE || b == Blocks.DEEPSLATE_COPPER_ORE;
        });
        if (best == null) return false;
        if (requireLos.get() && !Visibility.hasLineOfSight(mc, Vec3d.ofCenter(best))) return false;
        // Switch to pickaxe
        FindItemResult pick = InvUtils.findInHotbar(Items.DIAMOND_PICKAXE, Items.NETHERITE_PICKAXE, Items.IRON_PICKAXE, Items.STONE_PICKAXE);
        if (pick.found()) InvUtils.swap(pick.slot(), false);
        mc.interactionManager.attackBlock(best, Direction.UP);
        mc.player.swingHand(Hand.MAIN_HAND);
        InvUtils.swapBack();
        return true;
    }

    // Helpers
    private interface PosPredicate { boolean test(BlockPos pos); }

    private BlockPos findNearestInRadius(PosPredicate pred) {
        BlockPos center = mc.player.getBlockPos();
        int r = (int) Math.floor(radius.get());
        BlockPos best = null;
        double bestDist = Double.MAX_VALUE;
        for (int dx = -r; dx <= r; dx++) {
            for (int dy = -2; dy <= 2; dy++) {
                for (int dz = -r; dz <= r; dz++) {
                    BlockPos p = center.add(dx, dy, dz);
                    if (!pred.test(p)) continue;
                    double d = mc.player.squaredDistanceTo(p.getX() + 0.5, p.getY() + 0.5, p.getZ() + 0.5);
                    if (d < bestDist) { bestDist = d; best = p; }
                }
            }
        }
        return best;
    }
}
