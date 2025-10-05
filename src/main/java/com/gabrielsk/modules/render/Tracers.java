package com.gabrielsk.modules.render;

import com.gabrielsk.GabrielSKAddon;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.util.math.Vec3d;

public class Tracers extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    
    private final Setting<Boolean> players = sgGeneral.add(new BoolSetting.Builder()
        .name("players").description("Show players.").defaultValue(true).build());
    
    private final Setting<Boolean> mobs = sgGeneral.add(new BoolSetting.Builder()
        .name("mobs").description("Show mobs.").defaultValue(false).build());
    
    private final Setting<SettingColor> playersColor = sgGeneral.add(new ColorSetting.Builder()
        .name("players-color").description("Players color.").defaultValue(new SettingColor(255, 0, 0)).build());
    
    private final Setting<SettingColor> mobsColor = sgGeneral.add(new ColorSetting.Builder()
        .name("mobs-color").description("Mobs color.").defaultValue(new SettingColor(255, 165, 0)).build());
    
    public Tracers() {
        super(GabrielSKAddon.CATEGORY, "tracers", "Draws lines to entities.");
    }
    
    @EventHandler
    private void onRender(Render3DEvent event) {
        if (mc.world == null || mc.player == null) return;
        
        Vec3d playerPos = mc.player.getCameraPosVec(event.tickDelta);
        
        for (Entity entity : mc.world.getEntities()) {
            if (entity == mc.player) continue;
            
            SettingColor color = null;
            
            if (players.get() && entity instanceof PlayerEntity) {
                color = playersColor.get();
            } else if (mobs.get() && entity instanceof Monster) {
                color = mobsColor.get();
            }
            
            if (color != null) {
                Vec3d entityPos = entity.getLerpedPos(event.tickDelta);
                event.renderer.line(playerPos.x, playerPos.y, playerPos.z, 
                    entityPos.x, entityPos.y + entity.getHeight() / 2, entityPos.z, color);
            }
        }
    }
}
