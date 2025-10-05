package com.gabrielsk.modules.render;

import com.gabrielsk.GabrielSKAddon;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.block.entity.EnderChestBlockEntity;
import net.minecraft.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;

public class ChestESP extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    
    private final Setting<Boolean> chest = sgGeneral.add(new BoolSetting.Builder()
        .name("chest").description("Show chests.").defaultValue(true).build());
    
    private final Setting<Boolean> enderChest = sgGeneral.add(new BoolSetting.Builder()
        .name("ender-chest").description("Show ender chests.").defaultValue(true).build());
    
    private final Setting<Boolean> shulker = sgGeneral.add(new BoolSetting.Builder()
        .name("shulker").description("Show shulker boxes.").defaultValue(true).build());
    
    private final Setting<ShapeMode> shapeMode = sgGeneral.add(new EnumSetting.Builder<ShapeMode>()
        .name("shape-mode").description("Render mode.").defaultValue(ShapeMode.Both).build());
    
    private final Setting<SettingColor> chestColor = sgGeneral.add(new ColorSetting.Builder()
        .name("chest-color").description("Chest color.").defaultValue(new SettingColor(255, 160, 0, 75)).build());
    
    private final Setting<SettingColor> enderColor = sgGeneral.add(new ColorSetting.Builder()
        .name("ender-color").description("Ender chest color.").defaultValue(new SettingColor(128, 0, 255, 75)).build());
    
    private final Setting<SettingColor> shulkerColor = sgGeneral.add(new ColorSetting.Builder()
        .name("shulker-color").description("Shulker color.").defaultValue(new SettingColor(255, 0, 255, 75)).build());
    
    public ChestESP() {
        super(GabrielSKAddon.CATEGORY, "chest-esp", "Highlights storage blocks.");
    }
    
    @EventHandler
    private void onRender(Render3DEvent event) {
        if (mc.world == null || mc.player == null) return;
        
        // Iterate through loaded block entities
        int range = 128;
        BlockPos playerPos = mc.player.getBlockPos();
        for (int x = -range; x <= range; x++) {
            for (int y = -range; y <= range; y++) {
                for (int z = -range; z <= range; z++) {
                    BlockPos pos = playerPos.add(x, y, z);
                    BlockEntity be = mc.world.getBlockEntity(pos);
                    if (be == null) continue;
                    
                    SettingColor color = null;
                    
                    if (chest.get() && be instanceof ChestBlockEntity) {
                        color = chestColor.get();
                    } else if (enderChest.get() && be instanceof EnderChestBlockEntity) {
                        color = enderColor.get();
                    } else if (shulker.get() && be instanceof ShulkerBoxBlockEntity) {
                        color = shulkerColor.get();
                    }
                    
                    if (color != null) {
                        Box box = new Box(be.getPos());
                        event.renderer.box(box, color, color, shapeMode.get(), 0);
                    }
                }
            }
        }
    }
}
