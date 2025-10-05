package com.gabrielsk.modules.utility;

import com.gabrielsk.GabrielSKAddon;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.item.ItemStack;

/**
 * Replenish: Automatically refills hotbar from inventory.
 */
public class Replenish extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Integer> threshold = sgGeneral.add(new IntSetting.Builder()
        .name("threshold")
        .description("Refill when stack count is below this.")
        .defaultValue(10)
        .min(1)
        .max(64)
        .sliderMax(64)
        .build());

    private final Setting<Integer> delay = sgGeneral.add(new IntSetting.Builder()
        .name("delay")
        .description("Delay between refills (ticks).")
        .defaultValue(5)
        .min(0)
        .max(20)
        .sliderMax(20)
        .build());

    private int tickCounter = 0;

    public Replenish() {
        super(GabrielSKAddon.CATEGORY, "replenish", "Auto-refills hotbar from inventory.");
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        // Safety checks
        if (mc == null || mc.player == null) return;
        if (mc.player.isRemoved()) return;

        try {
            if (tickCounter > 0) {
                tickCounter--;
                return;
            }

            // Check each hotbar slot
            for (int i = 0; i < 9; i++) {
                try {
                    ItemStack stack = mc.player.getInventory().getStack(i);
                    if (stack == null) continue;
                    
                    // Skip if empty or above threshold
                    if (stack.isEmpty() || stack.getCount() >= threshold.get()) continue;

                    // Find matching item in inventory
                    int invSlot = findMatchingItem(stack);
                    if (invSlot == -1) continue;

                    // Move items to hotbar
                    InvUtils.move().from(invSlot).to(i);
                    tickCounter = delay.get();
                    return; // Only one refill per cycle
                } catch (Exception e) {
                    continue; // Skip invalid slots
                }
            }
        } catch (Exception e) {
            // Silently fail to prevent crashes
        }
    }

    private int findMatchingItem(ItemStack target) {
        if (target == null || target.isEmpty()) return -1;
        
        try {
            for (int i = 9; i < mc.player.getInventory().size(); i++) {
                try {
                    ItemStack stack = mc.player.getInventory().getStack(i);
                    if (stack != null && !stack.isEmpty() && stack.getItem() == target.getItem()) {
                        return i;
                    }
                } catch (Exception e) {
                    continue; // Skip invalid slots
                }
            }
        } catch (Exception e) {
            // Silently fail
        }
        return -1;
    }
}
