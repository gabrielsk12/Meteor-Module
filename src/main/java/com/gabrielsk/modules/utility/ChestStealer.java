package com.gabrielsk.modules.utility;

import com.gabrielsk.GabrielSKAddon;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.slot.SlotActionType;

public class ChestStealer extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    
    private final Setting<Integer> delay = sgGeneral.add(new IntSetting.Builder()
        .name("delay").description("Delay between stealing items (ticks).").defaultValue(1).min(0).max(20).sliderMax(20).build());
    
    private final Setting<Boolean> autoClose = sgGeneral.add(new BoolSetting.Builder()
        .name("auto-close").description("Automatically close chest when empty.").defaultValue(true).build());
    
    private int timer = 0;
    
    public ChestStealer() {
        super(GabrielSKAddon.CATEGORY, "chest-stealer", "Automatically steals items from chests.");
    }
    
    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (mc.player == null || mc.player.currentScreenHandler == null) return;
        
        if (!(mc.player.currentScreenHandler instanceof GenericContainerScreenHandler handler)) return;
        
        if (timer > 0) {
            timer--;
            return;
        }
        
        boolean foundItem = false;
        
        for (int i = 0; i < handler.getRows() * 9; i++) {
            ItemStack stack = handler.getSlot(i).getStack();
            if (!stack.isEmpty()) {
                mc.interactionManager.clickSlot(handler.syncId, i, 0, SlotActionType.QUICK_MOVE, mc.player);
                timer = delay.get();
                foundItem = true;
                break;
            }
        }
        
        if (!foundItem && autoClose.get()) {
            mc.player.closeHandledScreen();
        }
    }
}
