package com.gabrielsk.modules.utility;

import com.gabrielsk.GabrielSKAddon;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.SlotActionType;

public class AutoArmor extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    
    private final Setting<Integer> delay = sgGeneral.add(new IntSetting.Builder()
        .name("delay").description("Delay between equipping (ticks).").defaultValue(1).min(0).max(20).sliderMax(20).build());
    
    private int timer = 0;
    
    public AutoArmor() {
        super(GabrielSKAddon.CATEGORY, "auto-armor", "Automatically equips armor.");
    }
    
    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (mc.player == null || mc.player.currentScreenHandler == null) return;
        
        if (timer > 0) {
            timer--;
            return;
        }
        
        for (int i = 0; i < 4; i++) {
            EquipmentSlot slot = getSlot(i);
            ItemStack current = mc.player.getEquippedStack(slot);
            
            int bestSlot = -1;
            double bestValue = getArmorValue(current);
            
            for (int j = 0; j < 36; j++) {
                ItemStack stack = mc.player.getInventory().getStack(j);
                if (!(stack.getItem() instanceof ArmorItem armor)) continue;
                
                if (armor.getSlotType() != slot) continue;
                
                double value = getArmorValue(stack);
                if (value > bestValue) {
                    bestValue = value;
                    bestSlot = j;
                }
            }
            
            if (bestSlot != -1) {
                mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, 
                    bestSlot < 9 ? bestSlot + 36 : bestSlot, 0, SlotActionType.PICKUP, mc.player);
                mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, 
                    8 - i, 0, SlotActionType.PICKUP, mc.player);
                mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, 
                    bestSlot < 9 ? bestSlot + 36 : bestSlot, 0, SlotActionType.PICKUP, mc.player);
                timer = delay.get();
                break;
            }
        }
    }
    
    private EquipmentSlot getSlot(int i) {
        return switch (i) {
            case 0 -> EquipmentSlot.FEET;
            case 1 -> EquipmentSlot.LEGS;
            case 2 -> EquipmentSlot.CHEST;
            case 3 -> EquipmentSlot.HEAD;
            default -> EquipmentSlot.CHEST;
        };
    }
    
    private double getArmorValue(ItemStack stack) {
        if (!(stack.getItem() instanceof ArmorItem armor)) return 0;
        
        double value = armor.getProtection();
        value += armor.getToughness() / 4.0;
        
        // Add enchantment bonuses (simplified for MC 1.21+)
        // Count total enchantments as bonus
        value += EnchantmentHelper.getEnchantments(stack).getEnchantments().size() * 0.5;
        
        return value;
    }
}
