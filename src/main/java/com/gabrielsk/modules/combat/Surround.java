package com.gabrielsk.modules.combat;

import com.gabrielsk.GabrielSKAddon;
import meteordevelopment.meteorclient.events.world.TickEvent;
import com.gabrielsk.ai.legit.Humanizer;
import com.gabrielsk.ai.legit.Visibility;
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

public class Surround extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgLegit = settings.createGroup("Legit");
    
    private final Setting<Boolean> disableOnMove = sgGeneral.add(new BoolSetting.Builder()
        .name("disable-on-move").description("Disable when moving.").defaultValue(true).build());
    
    private final Setting<Boolean> center = sgGeneral.add(new BoolSetting.Builder()
        .name("center").description("Center player on block.").defaultValue(true).build());
    
    private final Setting<Boolean> doubleHeight = sgGeneral.add(new BoolSetting.Builder()
        .name("double-height").description("Place blocks at head level.").defaultValue(false).build());

    // Legit
    private final Setting<Boolean> legitMode = sgLegit.add(new BoolSetting.Builder()
        .name("enable").description("Enable legit checks for placements.").defaultValue(true).build());

    private final Setting<Boolean> requireLos = sgLegit.add(new BoolSetting.Builder()
        .name("require-los").description("Require clear LOS to placement face.").defaultValue(true).visible(legitMode::get).build());

    private final Setting<Integer> placeDelayMs = sgLegit.add(new IntSetting.Builder()
        .name("place-delay-ms").description("Human-like delay between placements.")
        .defaultValue(95).min(0).max(500).sliderMax(500).visible(legitMode::get).build());

    private final Setting<Double> placeJitter = sgLegit.add(new DoubleSetting.Builder()
        .name("place-jitter").description("Randomization on delays (0-1).")
        .defaultValue(0.2).min(0.0).max(1.0).sliderMax(1.0).visible(legitMode::get).build());

    private long lastPlaceAt = 0L;
    
    public Surround() {
        super(GabrielSKAddon.CATEGORY, "surround", "Surrounds you with obsidian.");
    }
    
    @Override
    public void onActivate() {
        if (mc.player == null) return;
        
        if (center.get()) {
            BlockPos pos = mc.player.getBlockPos();
            double x = pos.getX() + 0.5;
            double z = pos.getZ() + 0.5;
            // Small human-like delay to adjust position
            mc.player.setPosition(x, mc.player.getY(), z);
            lastPlaceAt = System.currentTimeMillis();
        }
    }
    
    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (mc.player == null || mc.world == null) return;
        
        if (disableOnMove.get() && (mc.player.input.movementForward != 0 || mc.player.input.movementSideways != 0)) {
            return;
        }
        
        FindItemResult obsidian = InvUtils.findInHotbar(Items.OBSIDIAN);
        if (!obsidian.found()) return;
        
        BlockPos playerPos = mc.player.getBlockPos();
        
        Direction[] directions = {Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST};
        
        for (Direction dir : directions) {
            // Human-like pacing
            if (legitMode.get()) {
                long now = System.currentTimeMillis();
                long delay = Humanizer.reactionMs(placeDelayMs.get(), placeJitter.get());
                if (now - lastPlaceAt < delay) continue;
            }
            placeBlock(playerPos.offset(dir), obsidian);
            if (doubleHeight.get()) {
                placeBlock(playerPos.offset(dir).up(), obsidian);
            }
        }
    }
    
    private void placeBlock(BlockPos pos, FindItemResult item) {
        if (mc.world.getBlockState(pos).getBlock() != Blocks.AIR) return;
        
        for (Direction dir : Direction.values()) {
            BlockPos neighbor = pos.offset(dir);
            if (mc.world.getBlockState(neighbor).getBlock() == Blocks.AIR) continue;
            
            Vec3d hitVec = new Vec3d(neighbor.getX() + 0.5, neighbor.getY() + 0.5, neighbor.getZ() + 0.5);

            if (legitMode.get() && requireLos.get() && !Visibility.hasLineOfSight(mc, hitVec)) continue;
            
            InvUtils.swap(item.slot(), false);
            mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND,
                new BlockHitResult(hitVec, dir.getOpposite(), neighbor, false));
            InvUtils.swapBack();
            lastPlaceAt = System.currentTimeMillis();
            break;
        }
    }
}
