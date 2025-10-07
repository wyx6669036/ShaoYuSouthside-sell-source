package dev.diona.southside.module.modules.misc;

import dev.diona.southside.Southside;
import dev.diona.southside.event.EventState;
import dev.diona.southside.event.events.AttackEvent;
import dev.diona.southside.event.events.ChatEvent;
import dev.diona.southside.event.events.MotionEvent;
import dev.diona.southside.event.events.PacketEvent;
import dev.diona.southside.module.Category;
import dev.diona.southside.module.Module;
import dev.diona.southside.module.modules.client.Notification;
import dev.diona.southside.module.modules.client.Target;
import dev.diona.southside.module.modules.combat.AutoL;
import dev.diona.southside.util.player.ChatUtil;
import me.bush.eventbus.annotation.EventListener;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.SPacketOpenWindow;

import java.util.HashSet;
import java.util.Set;

public class AutoReport extends Module {
    private EntityPlayer target;
    private final Set<String> reportedMessages = new HashSet<>();

    public AutoReport(String name, String description, Category category, boolean visible) {
        super(name, description, category, visible);
    }

    @Override
    public boolean onEnable() {

        return super.onEnable();
    }

    @EventListener
    public void onMotion(MotionEvent event) {
        if (event.getState() == EventState.POST) return;
        if (mc.player.isSpectator()) {
            target = null;
        }
        //!Target.isTarget(target)
        if (target != null && !mc.world.playerEntities.contains(target) && target.isDead) {
            mc.player.sendChatMessage("/report " + target.getName());
            target = null;
        }
    }

    @EventListener
    public void onAttack(AttackEvent event) {
        final Entity target = event.getTargetEntity();

        if (target instanceof EntityPlayer && Target.isTarget(target)) {
            this.target = (EntityPlayer) target;
        }
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
        if (message.contains("操作过快,请稍后再试") && !reportedMessages.contains(message)) {
            Notification.addNotificationKeepTime("The reporting speed is too fast.", "Auto Report", Notification.NotificationType.WARN, 2);
            reportedMessages.add(message);
            event.setCancelled(true);
        }
        if (message.contains("举报成功") && !reportedMessages.contains(message)) {
            Notification.addNotificationKeepTime("Report successful: " + message, "Auto Report", Notification.NotificationType.INFO, 5);
            ChatUtil.info("已经举报一个玩家");
            reportedMessages.add(message);
            event.setCancelled(true);
        }
        if (message.contains("举报玩家不在线")) {
            Notification.addNotificationKeepTime("被妈妈喊去吃饭了不在线", "Auto Report", Notification.NotificationType.INFO, 5);
            event.setCancelled(true);
        }
    }
}
