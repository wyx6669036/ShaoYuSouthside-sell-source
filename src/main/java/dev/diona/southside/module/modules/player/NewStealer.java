package dev.diona.southside.module.modules.player;

import cc.polyfrost.oneconfig.config.options.impl.Slider;
import cc.polyfrost.oneconfig.config.options.impl.Switch;
import dev.diona.southside.Southside;
import dev.diona.southside.event.events.PacketEvent;
import dev.diona.southside.event.events.UpdateEvent;
import dev.diona.southside.module.Category;
import dev.diona.southside.module.Module;
import dev.diona.southside.util.misc.TimerUtil;
import dev.diona.southside.util.player.Rotation;
import dev.diona.southside.util.player.RotationUtil;
import me.bush.eventbus.annotation.EventListener;
import net.minecraft.inventory.*;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.CPacketClickWindow;
import net.minecraft.network.play.client.CPacketCloseWindow;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.network.play.server.SPacketOpenWindow;
import net.minecraft.network.play.server.SPacketWindowItems;
import net.minecraft.tileentity.TileEntityBrewingStand;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;

import java.util.*;

public class NewStealer extends Module {

    public NewStealer(String name, String description, Category category, boolean visible) {
        super(name, description, category, visible);
    }

    public final Switch aura = new Switch("Aura", false);
    public final Slider auraDelay = new Slider("AuraDelay", 300D, 0D, 1000D, 10D);


    private TimerUtil delayTimer = new TimerUtil();
    private TimerUtil auraDelayTimer = new TimerUtil();
    boolean hasWindow = false;
    int windowid = 0;


    @EventListener
    public void onUpdate(UpdateEvent event) {
        if (!hasWindow) {
            /*
             * Chest Aura
             */

            if (aura.getValue() && !Southside.moduleManager.getModuleByClass(Blink.class).isEnabled()) {
                final Stealer stealer = (Stealer) Southside.moduleManager.getModuleByClass(Stealer.class);
                final var tile = mc.world.loadedTileEntityList.stream()
                        .filter(container -> container instanceof TileEntityChest || container instanceof TileEntityFurnace || container instanceof TileEntityBrewingStand)
                        .filter(entity -> !stealer.stolen.contains(entity.getPos()))
                        .filter(tileEntity -> mc.player.getDistance(tileEntity.getPos().getX(), tileEntity.getPos().getY(), tileEntity.getPos().getZ()) <= 4.5F).min(Comparator.comparingDouble(entity -> mc.player.getDistanceSq(entity.getPos())));
                if (tile.isPresent() && auraDelayTimer.hasReached(auraDelay.getValue().intValue())) {
                    final var container = tile.get();
                    final BlockPos chestPos = container.getPos();
                    Rotation rotation = RotationUtil.getRotationBlock(chestPos, 0);
                    RotationUtil.setTargetRotation(rotation.onPost(() -> {
                        CPacketPlayerTryUseItemOnBlock packet = new CPacketPlayerTryUseItemOnBlock(chestPos, Stealer.getFacingDirection(chestPos), EnumHand.MAIN_HAND, 0, 0, 0);
                        Objects.requireNonNull(mc.getConnection()).sendPacket(packet);
                        if (!(container instanceof TileEntityChest)) {
                            stealer.stolen.add(chestPos);
                        }
                        stealer.stolen.add(chestPos);
                        auraDelayTimer.reset();
                    }), 0);
                }
            }
        } else {
            auraDelayTimer.reset();
        }

        if (delayTimer.hasReached(300) && hasWindow) {
            mc.getConnection().getNetworkManager().sendPacket(new CPacketCloseWindow(windowid));
            hasWindow = false;
            windowid = 0;
        }
    }

    @EventListener
    public void onPacket(PacketEvent event) {
        final Packet<?> packet = event.getPacket();

        if (packet instanceof SPacketOpenWindow) {
            delayTimer.reset();
            hasWindow = true;
            windowid = ((SPacketOpenWindow) packet).getWindowId();
            event.setCancelled(true);
        }
        if (packet instanceof SPacketWindowItems) {
            if (((SPacketWindowItems) packet).getWindowId() != windowid)
                return;

            switch (((SPacketWindowItems) packet).getItemStacks().size()) {
                case 63 :
                    for (int i = 0; i <= 26; i++) {
                        Objects.requireNonNull(mc.getConnection()).getNetworkManager().sendPacket(new CPacketClickWindow(((SPacketWindowItems) packet).getWindowId(), i, 1, ClickType.QUICK_MOVE, ((SPacketWindowItems) packet).getItemStacks().get(i), (short) 0));
                    }
                    break;

                case 39, 41:
                    for (int i = 0; i <= 2; i++) {
                        Objects.requireNonNull(mc.getConnection()).getNetworkManager().sendPacket(new CPacketClickWindow(((SPacketWindowItems) packet).getWindowId(), i, 1, ClickType.QUICK_MOVE, ((SPacketWindowItems) packet).getItemStacks().get(i), (short) 0));
                    }
                    break;
            }
        }

        if (hasWindow) {
            if (packet instanceof CPacketClickWindow) {
                if (((CPacketClickWindow) packet).getWindowId() != windowid) {
                    event.setCancelled(true);
                }
            }
        }
    }
}
