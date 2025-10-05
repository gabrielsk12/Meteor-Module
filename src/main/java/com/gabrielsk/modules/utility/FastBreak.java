package com.gabrielsk.modules.utility;

import com.gabrielsk.GabrielSKAddon;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.BlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;

public class FastBreak extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    
    private final Setting<Boolean> autoTool = sgGeneral.add(new BoolSetting.Builder()
        .name("auto-tool").description("Automatically switch to best tool.").defaultValue(true).build());
    
    private final Setting<Double> multiplier = sgGeneral.add(new DoubleSetting.Builder()
        .name("multiplier").description("Break speed multiplier.").defaultValue(2.0).min(0.1).max(10).sliderMax(5).build());
    
    public FastBreak() {
        super(GabrielSKAddon.CATEGORY, "fast-break", "Break blocks faster.");
    }
    
    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (mc.player == null || mc.interactionManager == null) return;
        
        if (!mc.options.attackKey.isPressed()) return;
        
        HitResult hit = mc.crosshairTarget;
        if (hit == null || hit.getType() != HitResult.Type.BLOCK) return;
        
        BlockHitResult blockHit = (BlockHitResult) hit;
        BlockState state = mc.world.getBlockState(blockHit.getBlockPos());
        
        if (autoTool.get()) {
            int bestSlot = -1;
            double bestSpeed = 0;
            
            for (int i = 0; i < 9; i++) {
                ItemStack stack = mc.player.getInventory().getStack(i);
                double speed = stack.getMiningSpeedMultiplier(state);
                
                if (EnchantmentHelper.getLevel(Enchantments.EFFICIENCY, stack) > 0) {
                    speed += EnchantmentHelper.getLevel(Enchantments.EFFICIENCY, stack) * EnchantmentHelper.getLevel(Enchantments.EFFICIENCY, stack) + 1;
                }
                
                if (speed > bestSpeed) {
                    bestSpeed = speed;
                    bestSlot = i;
                }
            }
            
            if (bestSlot != -1 && mc.player.getInventory().selectedSlot != bestSlot) {
                mc.player.getInventory().selectedSlot = bestSlot;
            }
        }
        
        mc.interactionManager.updateBlockBreakingProgress(blockHit.getBlockPos(), blockHit.getSide());
    }
}
