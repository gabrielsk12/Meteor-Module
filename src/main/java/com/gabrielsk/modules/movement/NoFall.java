package com.gabrielsk.modules.movement;

import com.gabrielsk.GabrielSKAddon;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;

public class NoFall extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    
    private final Setting<Mode> mode = sgGeneral.add(new EnumSetting.Builder<Mode>()
        .name("mode").description("No fall mode.").defaultValue(Mode.Packet).build());
    
    public NoFall() {
        super(GabrielSKAddon.CATEGORY, "no-fall", "Prevents fall damage.");
    }
    
    @EventHandler
    private void onPacketSend(PacketEvent.Send event) {
        if (mc.player == null) return;
        
        if (mode.get() == Mode.Packet && event.packet instanceof PlayerMoveC2SPacket packet) {
            if (mc.player.fallDistance > 2) {
                ((PlayerMoveC2SPacket.IAccessor) packet).setOnGround(true);
            }
        }
    }
    
    public enum Mode {
        Packet
    }
    
    public interface PlayerMoveC2SPacket {
        interface IAccessor {
            void setOnGround(boolean onGround);
        }
    }
}
