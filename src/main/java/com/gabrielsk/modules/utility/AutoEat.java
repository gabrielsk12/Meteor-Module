package com.gabrielsk.modules.utility;

import com.gabrielsk.GabrielSKAddon;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.component.type.FoodComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;

public class AutoEat extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    
    private final Setting<Integer> hunger = sgGeneral.add(new IntSetting.Builder()
        .name("hunger").description("Hunger level to eat at.").defaultValue(15).min(0).max(20).sliderMax(20).build());
    
    private final Setting<Boolean> pauseOnEat = sgGeneral.add(new BoolSetting.Builder()
        .name("pause-aura").description("Pause combat when eating.").defaultValue(true).build());
    
    private boolean eating = false;
    private int eatTimer = 0;
    
    public AutoEat() {
        super(GabrielSKAddon.CATEGORY, "auto-eat", "Automatically eats food.");
    }
    
    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (mc.player == null) return;
        
        if (eating) {
            if (eatTimer > 0) {
                eatTimer--;
                mc.options.useKey.setPressed(true);
            } else {
                mc.options.useKey.setPressed(false);
                eating = false;
            }
            return;
        }
        
        if (mc.player.getHungerManager().getFoodLevel() >= hunger.get()) return;
        
        int foodSlot = -1;
        int bestFood = 0;
        
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            FoodComponent food = stack.getItem().getFoodComponent();
            
            if (food != null) {
                int nutrition = food.getHunger();
                if (nutrition > bestFood) {
                    bestFood = nutrition;
                    foodSlot = i;
                }
            }
        }
        
        if (foodSlot == -1) return;
        
        int previousSlot = mc.player.getInventory().selectedSlot;
        mc.player.getInventory().selectedSlot = foodSlot;
        
        mc.options.useKey.setPressed(true);
        eating = true;
        eatTimer = 32;
    }
}
