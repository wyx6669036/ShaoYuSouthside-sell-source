package dev.diona.southside.module.modules.combat;

import dev.diona.southside.Southside;
import dev.diona.southside.event.events.AttackEvent;
import dev.diona.southside.event.events.HigherPacketEvent;
import dev.diona.southside.event.events.UpdateEvent;
import dev.diona.southside.module.Category;
import dev.diona.southside.module.Module;
import dev.diona.southside.module.modules.player.Blink;
import dev.diona.southside.util.network.PacketUtil;
import me.bush.eventbus.annotation.EventListener;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.CPacketUseEntity;

import java.util.ArrayList;
import java.util.List;

public class BackTrack extends Module {

    public BackTrack(String name, String description, Category category, boolean visible) {
        super(name, description, category, visible);
    }

    private List<Packet<?>> packets = new ArrayList<>();
    private boolean working = false;
    private Entity target;

    @Override
    public boolean onEnable() {
        working = false;
        return true;
    }

    @Override
    public boolean onDisable() {
        blink();
        return super.onDisable();
    }

    @EventListener
    public void onAttack(AttackEvent event) {
        if (Southside.moduleManager.getModuleByClass(AutoGapple.class).isEnabled() || Southside.moduleManager.getModuleByClass(Blink.class).isEnabled()) {
            if (!packets.isEmpty()) {
                blink();
            }
            working = false;
            return;
        }
        target = event.getTargetEntity();
        working = true;

    }
    @EventListener
    public void onUpdate(UpdateEvent event) {
        if (Southside.moduleManager.getModuleByClass(AutoGapple.class).isEnabled() || Southside.moduleManager.getModuleByClass(Blink.class).isEnabled()) {
            if (!packets.isEmpty()) {
                blink();
            }
            working = false;
            return;
        }
        for (Entity entity : mc.world.loadedEntityList) {
            if (entity == mc.player || entity instanceof EntityItem) continue;
            if (mc.player.getDistance(entity) <= 8) {
                if (this.target == entity && mc.player.getDistance(entity) <= 1) {
                    working = false;
                    if (!packets.isEmpty()) {
                        blink();
                    }
                    return;
                }

                if (this.target != null && ((EntityLivingBase) this.target).hurtTime < 5 && this.target == entity) {
                    send(true);
                }
                return;
            }
        }
        working = false;
        blink();
    }
    @EventListener
    public void onPacket(HigherPacketEvent event) {
        if (Southside.moduleManager.getModuleByClass(AutoGapple.class).isEnabled() || Southside.moduleManager.getModuleByClass(Blink.class).isEnabled()) {
            if (!packets.isEmpty()) {
                blink();
            }
            working = false;
            return;
        }
        if (!packets.isEmpty()) {

            if (packets.size() > 70) {
                send(false);
            }
        }

        if (!working) {
            if (!packets.isEmpty()) {
                blink();
            }
            return;
        }

        Packet<?> packet = event.getPacket();

        if (PacketUtil.isEssential(packet)) return;

        if (PacketUtil.isCPacket(packet)) {

            packets.add(packet);
            event.setCancelled(true);
        }
    }
    void send(boolean mustC02) {
        if (packets.isEmpty())
            return;

        Packet<?> packet = packets.get(0);
        packets.remove(0);
        mc.getConnection().sendPacketNoHigherEvent(packet);
        if (!(packet instanceof CPacketUseEntity) && mustC02) {
            send(true);
        }
    }
    void blink() {
        if (packets.isEmpty())
            return;
        while (!packets.isEmpty()) {
            Packet<?> packet = packets.get(0);
            packets.remove(0);
            mc.getConnection().sendPacketNoHigherEvent(packet);
        }
    }
}
