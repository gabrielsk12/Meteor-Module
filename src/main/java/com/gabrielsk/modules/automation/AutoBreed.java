package com.gabrielsk.modules.automation;

import com.gabrielsk.GabrielSKAddon;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;

import java.util.*;

/**
 * AutoBreed: Automatically breeds animals.
 */
public class AutoBreed extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Double> range = sgGeneral.add(new DoubleSetting.Builder()
        .name("range")
        .description("Range to detect animals.")
        .defaultValue(4.5)
        .min(1.0)
        .max(6.0)
        .sliderMax(6.0)
        .build());

    private final Setting<Integer> delay = sgGeneral.add(new IntSetting.Builder()
        .name("delay")
        .description("Delay between breeding (ticks).")
        .defaultValue(10)
        .min(0)
        .max(40)
        .sliderMax(40)
        .build());

    private final Setting<Boolean> cows = sgGeneral.add(new BoolSetting.Builder()
        .name("cows")
        .description("Breed cows.")
        .defaultValue(true)
        .build());

    private final Setting<Boolean> sheep = sgGeneral.add(new BoolSetting.Builder()
        .name("sheep")
        .description("Breed sheep.")
        .defaultValue(true)
        .build());

    private final Setting<Boolean> pigs = sgGeneral.add(new BoolSetting.Builder()
        .name("pigs")
        .description("Breed pigs.")
        .defaultValue(true)
        .build());

    private final Setting<Boolean> chickens = sgGeneral.add(new BoolSetting.Builder()
        .name("chickens")
        .description("Breed chickens.")
        .defaultValue(true)
        .build());

    private int tickCounter = 0;

    public AutoBreed() {
        super(GabrielSKAddon.CATEGORY, "auto-breed", "Automatically breeds animals.");
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;

        if (tickCounter > 0) {
            tickCounter--;
            return;
        }

        // Find nearest breedable animal
        AnimalEntity target = findBreedableAnimal();
        if (target == null) return;

        // Get breeding item
        FindItemResult food = getBreedingItem(target);
        if (!food.found()) return;

        // Switch to food and breed
        InvUtils.swap(food.slot(), false);
        mc.interactionManager.interactEntity(mc.player, target, Hand.MAIN_HAND);
        tickCounter = delay.get();
    }

    private AnimalEntity findBreedableAnimal() {
        List<AnimalEntity> animals = new ArrayList<>();
        
        for (var entity : mc.world.getEntities()) {
            if (!(entity instanceof AnimalEntity animal)) continue;
            if (mc.player.distanceTo(animal) > range.get()) continue;
            if (!animal.isAlive() || animal.isBaby() || animal.isInLove()) continue;
            
            String name = entity.getType().toString();
            if (cows.get() && name.contains("cow")) animals.add(animal);
            else if (sheep.get() && name.contains("sheep")) animals.add(animal);
            else if (pigs.get() && name.contains("pig")) animals.add(animal);
            else if (chickens.get() && name.contains("chicken")) animals.add(animal);
        }

        return animals.isEmpty() ? null : animals.get(0);
    }

    private FindItemResult getBreedingItem(AnimalEntity animal) {
        String type = animal.getType().toString();
        
        if (type.contains("cow")) {
            return InvUtils.findInHotbar(Items.WHEAT);
        } else if (type.contains("sheep")) {
            return InvUtils.findInHotbar(Items.WHEAT);
        } else if (type.contains("pig")) {
            return InvUtils.findInHotbar(Items.CARROT, Items.POTATO, Items.BEETROOT);
        } else if (type.contains("chicken")) {
            return InvUtils.findInHotbar(Items.WHEAT_SEEDS, Items.BEETROOT_SEEDS, Items.MELON_SEEDS, Items.PUMPKIN_SEEDS);
        }
        
        return new FindItemResult(0, 0);
    }
}
