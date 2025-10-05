package com.gabrielsk.modules.render;

import com.gabrielsk.GabrielSKAddon;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;

public class HoleESP extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    
    private final Setting<Integer> range = sgGeneral.add(new IntSetting.Builder()
        .name("range").description("Range to search for holes.").defaultValue(8).min(1).max(32).sliderMax(16).build());
    
    private final Setting<Boolean> doubles = sgGeneral.add(new BoolSetting.Builder()
        .name("doubles").description("Show double holes.").defaultValue(true).build());
    
    private final Setting<ShapeMode> shapeMode = sgGeneral.add(new EnumSetting.Builder<ShapeMode>()
        .name("shape-mode").description("Render mode.").defaultValue(ShapeMode.Both).build());
    
    private final Setting<SettingColor> safeColor = sgGeneral.add(new ColorSetting.Builder()
        .name("safe-color").description("Bedrock hole color.").defaultValue(new SettingColor(0, 255, 0, 75)).build());
    
    private final Setting<SettingColor> unsafeColor = sgGeneral.add(new ColorSetting.Builder()
        .name("unsafe-color").description("Obsidian hole color.").defaultValue(new SettingColor(255, 255, 0, 75)).build());
    
    public HoleESP() {
        super(GabrielSKAddon.CATEGORY, "hole-esp", "Highlights safe holes.");
    }
    
    @EventHandler
    private void onRender(Render3DEvent event) {
        if (mc.world == null || mc.player == null) return;
        
        BlockPos playerPos = mc.player.getBlockPos();
        
        for (int x = -range.get(); x <= range.get(); x++) {
            for (int y = -range.get(); y <= range.get(); y++) {
                for (int z = -range.get(); z <= range.get(); z++) {
                    BlockPos pos = playerPos.add(x, y, z);
                    
                    if (mc.world.getBlockState(pos).getBlock() != Blocks.AIR) continue;
                    if (mc.world.getBlockState(pos.up()).getBlock() != Blocks.AIR) continue;
                    if (mc.world.getBlockState(pos.up(2)).getBlock() != Blocks.AIR) continue;
                    
                    boolean isSafe = true;
                    boolean isHole = true;
                    
                    if (mc.world.getBlockState(pos.down()).getBlock() != Blocks.BEDROCK &&
                        mc.world.getBlockState(pos.down()).getBlock() != Blocks.OBSIDIAN) {
                        continue;
                    }
                    
                    if (mc.world.getBlockState(pos.down()).getBlock() != Blocks.BEDROCK) {
                        isSafe = false;
                    }
                    
                    for (Direction dir : Direction.Type.HORIZONTAL) {
                        BlockPos sidePos = pos.offset(dir);
                        
                        if (mc.world.getBlockState(sidePos).getBlock() != Blocks.BEDROCK &&
                            mc.world.getBlockState(sidePos).getBlock() != Blocks.OBSIDIAN) {
                            isHole = false;
                            break;
                        }
                        
                        if (mc.world.getBlockState(sidePos).getBlock() != Blocks.BEDROCK) {
                            isSafe = false;
                        }
                    }
                    
                    if (isHole) {
                        Box box = new Box(pos);
                        SettingColor color = isSafe ? safeColor.get() : unsafeColor.get();
                        event.renderer.box(box, color, color, shapeMode.get(), 0);
                    }
                }
            }
        }
    }
}
