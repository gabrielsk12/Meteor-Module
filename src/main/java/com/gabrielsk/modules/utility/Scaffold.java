package com.gabrielsk.modules.utility;

import com.gabrielsk.GabrielSKAddon;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.player.Rotations;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.BlockItem;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class Scaffold extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    
    private final Setting<Boolean> rotate = sgGeneral.add(new BoolSetting.Builder()
        .name("rotate").description("Rotate when placing.").defaultValue(true).build());
    
    private final Setting<Boolean> tower = sgGeneral.add(new BoolSetting.Builder()
        .name("tower").description("Automatically tower up.").defaultValue(false).build());
    
    private final Setting<Boolean> safeWalk = sgGeneral.add(new BoolSetting.Builder()
        .name("safe-walk").description("Prevents walking off edges.").defaultValue(true).build());
    
    public Scaffold() {
        super(GabrielSKAddon.CATEGORY, "scaffold", "Automatically places blocks under you.");
    }
    
    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (mc.player == null || mc.world == null) return;
        
        BlockPos playerPos = mc.player.getBlockPos();
        BlockPos below = playerPos.down();
        
        if (!mc.world.getBlockState(below).isReplaceable()) return;
        
        FindItemResult item = InvUtils.findInHotbar(itemStack -> 
            itemStack.getItem() instanceof BlockItem && 
            Block.getBlockFromItem(itemStack.getItem()) != Blocks.AIR);
        
        if (!item.found()) return;
        
        if (tower.get() && mc.options.jumpKey.isPressed() && mc.player.isOnGround()) {
            mc.player.setVelocity(mc.player.getVelocity().x, 0.42, mc.player.getVelocity().z);
        }
        
        BlockPos placePos = below;
        Direction direction = Direction.UP;
        
        for (Direction dir : Direction.values()) {
            if (dir == Direction.UP) continue;
            BlockPos neighbor = below.offset(dir);
            if (!mc.world.getBlockState(neighbor).isReplaceable()) {
                placePos = neighbor;
                direction = dir.getOpposite();
                break;
            }
        }
        
        if (mc.world.getBlockState(placePos).isReplaceable()) return;
        
        Vec3d hitVec = new Vec3d(placePos.getX() + 0.5, placePos.getY() + 0.5, placePos.getZ() + 0.5);
        
        // Make variables final for lambda capture
        final BlockPos finalPlacePos = placePos;
        final Direction finalDirection = direction;
        final Vec3d finalHitVec = hitVec;
        
        if (rotate.get()) {
            Rotations.rotate(Rotations.getYaw(finalHitVec), Rotations.getPitch(finalHitVec), () -> place(item, finalPlacePos, finalDirection, finalHitVec));
        } else {
            place(item, finalPlacePos, finalDirection, finalHitVec);
        }
    }
    
    private void place(FindItemResult item, BlockPos pos, Direction direction, Vec3d hitVec) {
        InvUtils.swap(item.slot(), false);
        mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, 
            new BlockHitResult(hitVec, direction, pos, false));
        InvUtils.swapBack();
    }
}
