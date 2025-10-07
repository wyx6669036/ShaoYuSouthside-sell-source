package dev.diona.southside.module.modules.client;

import dev.diona.southside.event.events.MoveEvent;
import dev.diona.southside.event.events.PacketEvent;
import dev.diona.southside.event.events.TickEvent;
import dev.diona.southside.module.Category;
import dev.diona.southside.module.Module;
import dev.diona.southside.module.modules.combat.AutoGapple;
import dev.diona.southside.util.player.MovementUtils;
import me.bush.eventbus.annotation.EventListener;
import net.minecraft.client.gui.GuiScreen;
import dev.diona.southside.util.player.ChatUtil;
import net.minecraft.init.Blocks;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.CPacketHeldItemChange;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.server.SPacketBlockChange;
import net.minecraft.network.play.server.SPacketEntityVelocity;
import net.minecraft.util.math.BlockPos;

public class Debug extends Module {
    private static double motionX = 0.0;
    private static double motionY = 0.0;
    private static double motionZ = 0.0;
    private boolean needSkip = false;
    public Debug(String name, String description, Category category, boolean visible) {
        super(name, description, category, visible);
    }

    @Override
    public boolean onEnable() {
        ChatUtil.info("Debug onEnable");
        return true;
    }

    @EventListener
    public void onPacket(PacketEvent event) {
        if (event.getPacket() instanceof SPacketBlockChange){
            event.cancel();
        }
    }

    @Override
    public boolean onDisable() {
        ChatUtil.info("Debug onDisable");
        return true;
    }
}
