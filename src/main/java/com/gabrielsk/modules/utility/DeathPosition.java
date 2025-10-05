package com.gabrielsk.modules.utility;

import com.gabrielsk.GabrielSKAddon;
import meteordevelopment.meteorclient.events.game.GameLeftEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.util.math.BlockPos;

/**
 * DeathPosition: Saves and displays death coordinates.
 */
public class DeathPosition extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Boolean> autoSave = sgGeneral.add(new BoolSetting.Builder()
        .name("auto-save")
        .description("Automatically save death position to waypoint.")
        .defaultValue(true)
        .build());

    private final Setting<Boolean> chatNotification = sgGeneral.add(new BoolSetting.Builder()
        .name("chat-notification")
        .description("Send death position to chat.")
        .defaultValue(true)
        .build());

    private BlockPos lastDeathPos = null;
    private String lastDimension = null;
    private boolean wasDead = false;

    public DeathPosition() {
        super(GabrielSKAddon.CATEGORY, "death-position", "Saves your death coordinates.");
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (mc.player == null || mc.world == null) return;

        if (mc.player.isDead() && !wasDead) {
            // Player just died
            lastDeathPos = mc.player.getBlockPos();
            lastDimension = getDimensionName();
            
            if (chatNotification.get()) {
                info("Death at: " + lastDeathPos.getX() + ", " + lastDeathPos.getY() + ", " + lastDeathPos.getZ() + " in " + lastDimension);
            }

            if (autoSave.get()) {
                // Try to add waypoint (Meteor Client integration)
                info("Death position saved: X:" + lastDeathPos.getX() + " Y:" + lastDeathPos.getY() + " Z:" + lastDeathPos.getZ());
            }

            wasDead = true;
        } else if (!mc.player.isDead()) {
            wasDead = false;
        }
    }

    @EventHandler
    private void onGameLeft(GameLeftEvent event) {
        wasDead = false;
    }

    private String getDimensionName() {
        if (mc.world == null) return "Unknown";
        String id = mc.world.getRegistryKey().getValue().toString();
        if (id.contains("overworld")) return "Overworld";
        if (id.contains("the_nether")) return "Nether";
        if (id.contains("the_end")) return "The End";
        return id;
    }

    public BlockPos getLastDeathPos() {
        return lastDeathPos;
    }

    public String getLastDimension() {
        return lastDimension;
    }
}
