package com.gabrielsk.modules.utility;

import com.gabrielsk.GabrielSKAddon;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.Item;
import net.minecraft.item.Items;

/**
 * AutoEat: Automatically eats food when hungry.
 */
public class AutoEat extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Integer> hungerThreshold = sgGeneral.add(new IntSetting.Builder()
        .name("hunger-threshold")
        .description("Eat when hunger is below this level.")
        .defaultValue(16)
        .min(1)
        .max(20)
        .sliderMax(20)
        .build());

    private final Setting<Boolean> pauseBaritone = sgGeneral.add(new BoolSetting.Builder()
        .name("pause-baritone")
        .description("Pause Baritone while eating.")
        .defaultValue(true)
        .build());

    private final Setting<Boolean> preferGoldenFood = sgGeneral.add(new BoolSetting.Builder()
        .name("prefer-golden")
        .description("Prefer golden apples/carrots when available.")
        .defaultValue(false)
        .build());

    private boolean isEating = false;
    private int eatTicks = 0;

    public AutoEat() {
        super(GabrielSKAddon.CATEGORY, "auto-eat", "Automatically eats food when hungry.");
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (mc.player == null || mc.interactionManager == null) return;

        // If currently eating, wait for it to finish
        if (isEating) {
            eatTicks++;
            if (eatTicks >= 32 || !mc.player.isUsingItem()) {
                isEating = false;
                eatTicks = 0;
            }
            return;
        }

        // Check if we need to eat
        if (mc.player.getHungerManager().getFoodLevel() >= hungerThreshold.get()) {
            return;
        }

        // Find food
        FindItemResult food = findBestFood();
        if (!food.found()) return;

        // Switch to food and start eating
        InvUtils.swap(food.slot(), true);
        mc.options.useKey.setPressed(true);
        isEating = true;
        eatTicks = 0;
    }

    @Override
    public void onDeactivate() {
        if (mc.options != null) {
            mc.options.useKey.setPressed(false);
        }
        isEating = false;
    }

    private FindItemResult findBestFood() {
        // Prefer golden food if enabled
        if (preferGoldenFood.get()) {
            FindItemResult golden = InvUtils.findInHotbar(Items.GOLDEN_APPLE, Items.ENCHANTED_GOLDEN_APPLE, Items.GOLDEN_CARROT);
            if (golden.found()) return golden;
        }

        // Find any food in hotbar
        for (int i = 0; i < 9; i++) {
            Item item = mc.player.getInventory().getStack(i).getItem();
            if (isFood(item)) {
                return new FindItemResult(i, mc.player.getInventory().getStack(i).getCount());
            }
        }

        return new FindItemResult(0, 0);
    }

    private boolean isFood(Item item) {
        return item.getComponents().contains(DataComponentTypes.FOOD);
    }
}
