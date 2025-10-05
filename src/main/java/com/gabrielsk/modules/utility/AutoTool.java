package com.gabrielsk.modules.utility;

import com.gabrielsk.GabrielSKAddon;
import meteordevelopment.meteorclient.events.entity.player.StartBreakingBlockEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.BlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.ItemStack;

/**
 * AutoTool: Automatically switches to the best tool for mining.
 */
public class AutoTool extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Boolean> preferSilkTouch = sgGeneral.add(new BoolSetting.Builder()
        .name("prefer-silk-touch")
        .description("Prefer silk touch tools when available.")
        .defaultValue(false)
        .build());

    private final Setting<Boolean> preferFortune = sgGeneral.add(new BoolSetting.Builder()
        .name("prefer-fortune")
        .description("Prefer fortune tools when available.")
        .defaultValue(true)
        .build());

    private final Setting<Boolean> switchBack = sgGeneral.add(new BoolSetting.Builder()
        .name("switch-back")
        .description("Switch back to previous item after mining.")
        .defaultValue(true)
        .build());

    private int previousSlot = -1;

    public AutoTool() {
        super(GabrielSKAddon.CATEGORY, "auto-tool", "Automatically switches to best tool.");
    }

    @EventHandler
    private void onStartBreakingBlock(StartBreakingBlockEvent event) {
        if (mc.player == null || mc.world == null) return;

        // Save current slot
        if (switchBack.get() && previousSlot == -1) {
            previousSlot = mc.player.getInventory().main.get(mc.player.getInventory().selectedSlot).isEmpty() ? -1 : mc.player.getInventory().selectedSlot;
        }

        // Find best tool manually
        BlockState state = mc.world.getBlockState(event.blockPos);
        int bestSlot = -1;
        float bestSpeed = 1.0f;

        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.isEmpty()) continue;

            float speed = stack.getMiningSpeedMultiplier(state);
            
            // Apply enchantment preferences
            if (preferSilkTouch.get() && EnchantmentHelper.getLevel(mc.world.getRegistryManager().getOrThrow(net.minecraft.registry.RegistryKeys.ENCHANTMENT).getOrThrow(Enchantments.SILK_TOUCH), stack) > 0) {
                speed += 1000.0f; // Heavily prefer silk touch
            }
            
            if (preferFortune.get() && EnchantmentHelper.getLevel(mc.world.getRegistryManager().getOrThrow(net.minecraft.registry.RegistryKeys.ENCHANTMENT).getOrThrow(Enchantments.FORTUNE), stack) > 0) {
                speed += 500.0f; // Prefer fortune
            }

            if (speed > bestSpeed) {
                bestSpeed = speed;
                bestSlot = i;
            }
        }

        // Switch to best tool
        if (bestSlot != -1 && bestSlot != mc.player.getInventory().selectedSlot) {
            InvUtils.swap(bestSlot, false);
        }
    }
}
