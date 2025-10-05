package com.gabrielsk.modules.automation;

import com.gabrielsk.GabrielSKAddon;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.item.Items;

/**
 * AutoTotem: Keeps totem of undying in offhand for safety.
 */
public class AutoTotem extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Boolean> strict = sgGeneral.add(new BoolSetting.Builder()
        .name("strict")
        .description("Always keep totem in offhand, never replace.")
        .defaultValue(false)
        .build());

    private final Setting<Boolean> smartToggle = sgGeneral.add(new BoolSetting.Builder()
        .name("smart-toggle")
        .description("Disable when no totems in inventory.")
        .defaultValue(true)
        .build());

    private final Setting<Integer> delay = sgGeneral.add(new IntSetting.Builder()
        .name("delay")
        .description("Delay between totem swaps (ticks).")
        .defaultValue(1)
        .min(0)
        .max(10)
        .sliderMax(10)
        .build());

    private int tickCounter = 0;

    public AutoTotem() {
        super(GabrielSKAddon.CATEGORY, "auto-totem", "Automatically keeps totem in offhand.");
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (mc.player == null) return;

        if (tickCounter > 0) {
            tickCounter--;
            return;
        }

        // Check if totem already in offhand
        if (mc.player.getOffHandStack().getItem() == Items.TOTEM_OF_UNDYING) {
            return;
        }

        // Find totem in inventory
        FindItemResult totem = InvUtils.find(Items.TOTEM_OF_UNDYING);
        
        if (!totem.found()) {
            if (smartToggle.get()) {
                info("No totems found, disabling.");
                toggle();
            }
            return;
        }

        // Move totem to offhand
        InvUtils.move().from(totem.slot()).toOffhand();
        tickCounter = delay.get();
    }
}
