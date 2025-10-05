package com.gabrielsk.modules.utility;

import com.gabrielsk.GabrielSKAddon;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.player.PlayerEntity;

/**
 * AutoLog: Automatically disconnects on low health or nearby players.
 */
public class AutoLog extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Integer> health = sgGeneral.add(new IntSetting.Builder()
        .name("health")
        .description("Disconnect when health drops below this.")
        .defaultValue(6)
        .min(1)
        .max(20)
        .sliderMax(20)
        .build());

    private final Setting<Boolean> onPlayerNearby = sgGeneral.add(new BoolSetting.Builder()
        .name("on-player-nearby")
        .description("Disconnect when player enters range.")
        .defaultValue(false)
        .build());

    private final Setting<Double> playerRange = sgGeneral.add(new DoubleSetting.Builder()
        .name("player-range")
        .description("Range to detect players.")
        .defaultValue(8.0)
        .min(1.0)
        .max(32.0)
        .sliderMax(32.0)
        .visible(onPlayerNearby::get)
        .build());

    private final Setting<Boolean> toggleOff = sgGeneral.add(new BoolSetting.Builder()
        .name("toggle-off")
        .description("Disable module after disconnect.")
        .defaultValue(true)
        .build());

    public AutoLog() {
        super(GabrielSKAddon.CATEGORY, "auto-log", "Auto-disconnects on danger.");
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (mc.player == null || mc.world == null) return;

        boolean shouldLog = false;
        String reason = "";

        // Check health
        if (mc.player.getHealth() <= health.get()) {
            shouldLog = true;
            reason = "Low health (" + mc.player.getHealth() + ")";
        }

        // Check nearby players
        if (!shouldLog && onPlayerNearby.get()) {
            for (PlayerEntity player : mc.world.getPlayers()) {
                if (player == mc.player) continue;
                if (mc.player.distanceTo(player) <= playerRange.get()) {
                    shouldLog = true;
                    reason = "Player nearby: " + player.getName().getString();
                    break;
                }
            }
        }

        if (shouldLog) {
            info("Disconnecting: " + reason);
            if (mc.world != null) {
                mc.world.disconnect();
            }
            if (mc.getNetworkHandler() != null) {
                mc.getNetworkHandler().getConnection().disconnect(net.minecraft.text.Text.literal("AutoLog: " + reason));
            }
            if (toggleOff.get()) {
                toggle();
            }
        }
    }
}
