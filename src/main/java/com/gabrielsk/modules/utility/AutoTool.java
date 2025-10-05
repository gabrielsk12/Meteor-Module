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
            ItemStack currentStack = mc.player.getInventory().getStack(mc.player.getInventory().selectedSlot);
            previousSlot = currentStack.isEmpty() ? -1 : mc.player.getInventory().selectedSlot;
        }

        // Use Meteor's built-in tool finding
        var result = InvUtils.findFastestTool(mc.world.getBlockState(event.blockPos));
        if (result.found()) {
            InvUtils.swap(result.slot(), false);
        }
    }
}
