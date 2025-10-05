package com.gabrielsk.modules.movement;

import com.gabrielsk.GabrielSKAddon;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;

public class Sprint extends Module {
    public Sprint() {
        super(GabrielSKAddon.CATEGORY, "sprint", "Automatically sprints.");
    }
    
    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (mc.player == null) return;
        
        if (mc.player.input.movementForward > 0 && !mc.player.horizontalCollision && 
            !mc.player.isSneaking() && mc.player.getHungerManager().getFoodLevel() > 6) {
            mc.player.setSprinting(true);
        }
    }
}
