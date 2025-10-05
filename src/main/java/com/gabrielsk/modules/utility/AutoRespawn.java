package com.gabrielsk.modules.utility;

import com.gabrielsk.GabrielSKAddon;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.screen.DeathScreen;

/**
 * AutoRespawn: Automatically respawns after death.
 */
public class AutoRespawn extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Integer> delay = sgGeneral.add(new IntSetting.Builder()
        .name("delay")
        .description("Delay before respawning (ticks).")
        .defaultValue(20)
        .min(0)
        .max(100)
        .sliderMax(100)
        .build());

    private int tickCounter = 0;

    public AutoRespawn() {
        super(GabrielSKAddon.CATEGORY, "auto-respawn", "Automatically respawns after death.");
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (!(mc.currentScreen instanceof DeathScreen)) {
            tickCounter = 0;
            return;
        }

        if (tickCounter < delay.get()) {
            tickCounter++;
            return;
        }

        mc.player.requestRespawn();
        mc.setScreen(null);
        tickCounter = 0;
    }
}
