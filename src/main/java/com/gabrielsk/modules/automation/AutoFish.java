package com.gabrielsk.modules.automation;

import com.gabrielsk.GabrielSKAddon;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.item.Items;
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;

/**
 * AutoFish: Automatically casts and reels in fishing rod.
 * Detects splash sound for perfect timing.
 */
public class AutoFish extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Integer> catchDelay = sgGeneral.add(new IntSetting.Builder()
        .name("catch-delay")
        .description("Ticks to wait after splash before reeling.")
        .defaultValue(3)
        .min(0)
        .max(20)
        .sliderMax(20)
        .build());

    private final Setting<Integer> castDelay = sgGeneral.add(new IntSetting.Builder()
        .name("cast-delay")
        .description("Ticks to wait before casting again.")
        .defaultValue(10)
        .min(5)
        .max(40)
        .sliderMax(40)
        .build());

    private final Setting<Boolean> autoThrow = sgGeneral.add(new BoolSetting.Builder()
        .name("auto-throw")
        .description("Automatically cast rod on activation.")
        .defaultValue(true)
        .build());

    private boolean shouldCatch = false;
    private int ticksUntilCatch = 0;
    private int ticksUntilCast = 0;

    public AutoFish() {
        super(GabrielSKAddon.CATEGORY, "auto-fish", "Automatically fishes for you.");
    }

    @Override
    public void onActivate() {
        shouldCatch = false;
        ticksUntilCatch = 0;
        ticksUntilCast = autoThrow.get() ? 5 : 0;
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (mc.player == null || mc.interactionManager == null) return;

        // Check if holding fishing rod
        if (mc.player.getMainHandStack().getItem() != Items.FISHING_ROD &&
            mc.player.getOffHandStack().getItem() != Items.FISHING_ROD) {
            return;
        }

        // Handle catching
        if (shouldCatch && ticksUntilCatch > 0) {
            ticksUntilCatch--;
            if (ticksUntilCatch == 0) {
                catchFish();
            }
        }

        // Handle casting
        if (ticksUntilCast > 0) {
            ticksUntilCast--;
            if (ticksUntilCast == 0) {
                castRod();
            }
        }

        // Auto re-cast if bobber is gone
        if (!shouldCatch && ticksUntilCast == 0 && mc.player.fishHook == null) {
            ticksUntilCast = 5;
        }
    }

    @EventHandler
    private void onPacketReceive(PacketEvent.Receive event) {
        if (!(event.packet instanceof PlaySoundS2CPacket packet)) return;
        if (mc.player == null || mc.player.fishHook == null) return;

        // Detect splash sound near bobber
        if (packet.getSound().value() == SoundEvents.ENTITY_FISHING_BOBBER_SPLASH) {
            FishingBobberEntity bobber = mc.player.fishHook;
            double distance = Math.sqrt(
                Math.pow(packet.getX() - bobber.getX(), 2) +
                Math.pow(packet.getY() - bobber.getY(), 2) +
                Math.pow(packet.getZ() - bobber.getZ(), 2)
            );

            if (distance < 3.0) {
                shouldCatch = true;
                ticksUntilCatch = catchDelay.get();
            }
        }
    }

    private void castRod() {
        Hand hand = mc.player.getMainHandStack().getItem() == Items.FISHING_ROD ? Hand.MAIN_HAND : Hand.OFF_HAND;
        mc.interactionManager.interactItem(mc.player, hand);
        shouldCatch = false;
    }

    private void catchFish() {
        Hand hand = mc.player.getMainHandStack().getItem() == Items.FISHING_ROD ? Hand.MAIN_HAND : Hand.OFF_HAND;
        mc.interactionManager.interactItem(mc.player, hand);
        ticksUntilCast = castDelay.get();
        shouldCatch = false;
    }
}
