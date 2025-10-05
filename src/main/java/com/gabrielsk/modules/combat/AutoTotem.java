package com.gabrielsk.modules.combat;

import com.gabrielsk.GabrielSKAddon;
import meteordevelopment.meteorclient.events.world.TickEvent;
import com.gabrielsk.ai.legit.Humanizer;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.item.Items;

public class AutoTotem extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgLegit = settings.createGroup("Legit");
    
    private final Setting<Integer> health = sgGeneral.add(new IntSetting.Builder()
        .name("health").description("Health to equip totem.").defaultValue(10).min(0).max(20).sliderMax(20).build());
    
    private final Setting<Boolean> elytra = sgGeneral.add(new BoolSetting.Builder()
        .name("elytra-priority").description("Keep elytra if flying.").defaultValue(false).build());
    
    private final Setting<Boolean> smart = sgGeneral.add(new BoolSetting.Builder()
        .name("smart").description("Only equip when in danger.").defaultValue(true).build());

    // Legit
    private final Setting<Boolean> legitMode = sgLegit.add(new BoolSetting.Builder()
        .name("enable").description("Enable human-like swap pacing.").defaultValue(true).build());

    private final Setting<Integer> swapDelayMs = sgLegit.add(new IntSetting.Builder()
        .name("swap-delay-ms").description("Delay between swaps (ms).")
        .defaultValue(180).min(50).max(600).sliderMax(600).visible(legitMode::get).build());

    private final Setting<Double> swapJitter = sgLegit.add(new DoubleSetting.Builder()
        .name("swap-jitter").description("Randomization on swap delay (0-1).")
        .defaultValue(0.25).min(0.0).max(1.0).sliderMax(1.0).visible(legitMode::get).build());

    private long lastSwapAt = 0L;
    
    public AutoTotem() {
        super(GabrielSKAddon.CATEGORY, "auto-totem", "Automatically equips totems.");
    }
    
    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (mc.player == null || mc.player.currentScreenHandler == null) return;
        
        boolean needsTotem = mc.player.getHealth() + mc.player.getAbsorptionAmount() <= health.get();
        if (smart.get() && !needsTotem) return;
        
    if (elytra.get() && mc.player.isFallFlying()) return;
        
        if (mc.player.getOffHandStack().getItem() == Items.TOTEM_OF_UNDYING) return;
        
        int totemSlot = -1;
        for (int i = 0; i < 9; i++) {
            if (mc.player.getInventory().getStack(i).getItem() == Items.TOTEM_OF_UNDYING) {
                totemSlot = i;
                break;
            }
        }
        
        if (totemSlot == -1) return;
        
        if (legitMode.get()) {
            long now = System.currentTimeMillis();
            long delay = Humanizer.reactionMs(swapDelayMs.get(), swapJitter.get());
            if (now - lastSwapAt < delay) return;
        }
        mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId,
            totemSlot < 9 ? totemSlot + 36 : totemSlot, 40,
            net.minecraft.screen.slot.SlotActionType.SWAP, mc.player);
        lastSwapAt = System.currentTimeMillis();
    }
}
