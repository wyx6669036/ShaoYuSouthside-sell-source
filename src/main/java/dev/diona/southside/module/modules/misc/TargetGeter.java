package dev.diona.southside.module.modules.misc;

import dev.diona.southside.Southside;
import dev.diona.southside.event.events.ChatEvent;
import dev.diona.southside.event.events.PacketEvent;
import dev.diona.southside.module.Category;
import dev.diona.southside.module.Module;
import dev.diona.southside.module.modules.client.Notification;
import dev.diona.southside.module.modules.combat.AutoL;
import me.bush.eventbus.annotation.EventListener;
import net.minecraft.inventory.ClickType;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.SPacketOpenWindow;

import java.util.HashSet;
import java.util.Set;
public class TargetGeter extends Module {
    public static String targetName;
    private final Set<String> reportedMessages = new HashSet<>();

    public TargetGeter(String name, String description, Category category, boolean visible) {
        super(name, description, category, visible);
    }

    @Override
    public boolean onEnable() {
        if (!Southside.moduleManager.getModuleByClass(AutoL.class).isEnabled()) {
            Notification.addNotification("Please toggle AutoL.", "Auto Report", Notification.NotificationType.WARN);
            setEnable(false);
        }
        return super.onEnable();
    }

    @EventListener
    public void onPacket(PacketEvent event) {
        Packet<?> packet = event.getPacket();

        if (packet instanceof SPacketOpenWindow openWindowPacket) {
            if (openWindowPacket.getWindowTitle().toString().equals("TextComponent{text='请选择举报理由', siblings=[], style=Style{hasParent=false, color=null, bold=null, italic=null, underlined=null, obfuscated=null, clickEvent=null, hoverEvent=null, insertion=null}}")) {
                event.cancel();
                int windowId = openWindowPacket.getWindowId();

                mc.playerController.windowClick(windowId,11,0,ClickType.PICKUP,mc.player);
                Notification.addNotification("Attempting to report.", "Auto Report", Notification.NotificationType.INFO);
            }
        }
    }

    @EventListener
    public void onChat(ChatEvent event) {
        String message = event.getMessage();
        if (message.contains("操作过快,请稍后再试.") && !reportedMessages.contains(message)) {
            Notification.addNotificationKeepTime("The reporting speed is too fast.", "Auto Report", Notification.NotificationType.WARN, 2);
            reportedMessages.add(message);
        }
        if (message.contains("举报成功") && !reportedMessages.contains(message)) {
            Notification.addNotificationKeepTime("Report successful: " + message, "Auto Report", Notification.NotificationType.INFO, 5);
            reportedMessages.add(message);
        }
    }
}
