package dev.diona.southside.module.modules.world;

import cc.polyfrost.oneconfig.config.options.impl.Switch;
import dev.diona.southside.event.events.HigherPacketEvent;
import dev.diona.southside.event.events.NewRender2DEvent;
import dev.diona.southside.event.events.TickEvent;
import dev.diona.southside.module.Category;
import dev.diona.southside.module.Module;
import dev.diona.southside.util.network.PacketUtil;
import dev.diona.southside.util.render.RoundUtil;
import me.bush.eventbus.annotation.EventListener;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.network.Packet;

import java.awt.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class BlockFly extends Module {
    public final Switch renderMode = new Switch("NavenRender", false);
    public final Switch renderMode2 = new Switch("Render", true);

    private final LinkedList<List<Packet<?>>> queue = new LinkedList<>();
    public boolean release = false;
    private int tickCounter = 0;
    private static final int MAX_QUEUE_SIZE = 20;
    private static final int MIN_QUEUE_SIZE = 5;
    private static final int PACKETS_PER_TICK = 16;

    public BlockFly(String name, String description, Category category, boolean visible) {
        super(name, description, category, visible);
    }

    @Override
    public boolean onDisable() {
        release = false;
        if (!queue.isEmpty()) {
            queue.forEach(this::sendTick);
            queue.clear();
        }
        return true;
    }

    @Override
    public boolean onEnable() {
        queue.clear();
        queue.add(new ArrayList<>());
        release = false;
        tickCounter = 0;
        return true;
    }

    @EventListener
    public void onPacket(HigherPacketEvent e) {
        Packet<?> packet = e.getPacket();
        if (PacketUtil.isCPacket(packet)) {
            mc.addScheduledTask(() -> {
                if (!queue.isEmpty()) {
                    queue.getLast().add(packet);
                }
            });
            e.setCancelled(true);
        }
    }

    @EventListener
    public void onUpdate(TickEvent e) {
        tickCounter++;
        queue.add(new ArrayList<>());
        if (queue.size() >= MAX_QUEUE_SIZE && !release) {
            release = true;
        }
        if (queue.size() <= MIN_QUEUE_SIZE && release) {
            release = false;
        }
        if (release && queue.size() > 1) {
            for (int i = 0; i < PACKETS_PER_TICK && !queue.isEmpty(); i++) {
                sendTick(queue.getFirst());
                queue.removeFirst();
            }
        }
        while (queue.size() > MAX_QUEUE_SIZE * 2) {
            queue.removeFirst();
        }
    }

    private void sendTick(List<Packet<?>> tick) {
        if (mc.getConnection() != null) {
            tick.forEach(packet -> mc.getConnection().sendPacketNoHigherEvent(packet));
        }
    }

    @EventListener
    public void onRender2D(NewRender2DEvent e) {
        ScaledResolution resolution = new ScaledResolution(mc);
        int screenWidth = resolution.getScaledWidth();
        int screenHeight = resolution.getScaledHeight();
        if (renderMode2.getValue()) {
            int x = screenWidth / 2 - 100;
            int y = screenHeight / 3 - 20;
            float progress = Math.min(queue.size(), MAX_QUEUE_SIZE) / (float) MAX_QUEUE_SIZE;

            RoundUtil.drawRound(x, y, 200, 3, 2, new Color(0, 0, 0, 150));
            RoundUtil.drawRound(x, y, 200 * progress, 3, 2, Color.RED);
        }
        if (renderMode.getValue()) {
            int x = screenWidth / 2 - 40;
            int y = screenHeight / 2 - 15;
            float progress = Math.min(queue.size(), MAX_QUEUE_SIZE) / (float) MAX_QUEUE_SIZE;

            RoundUtil.drawRound(x, y, 80, 3, 2, new Color(0, 0, 0, 150));
            RoundUtil.drawRound(x, y, 80 * progress, 3, 2, new Color(143, 49, 46, 220));
            }
        }
    }
