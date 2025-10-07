package dev.diona.southside.module.modules.combat;

import cc.polyfrost.oneconfig.config.options.impl.Slider;
import cc.polyfrost.oneconfig.config.options.impl.Switch;
import cc.polyfrost.oneconfig.renderer.NanoVGHelper;
import cc.polyfrost.oneconfig.renderer.font.Fonts;
import dev.diona.southside.event.events.*;
import dev.diona.southside.module.Category;
import dev.diona.southside.module.Module;
import dev.diona.southside.module.modules.client.Notification;
import dev.diona.southside.util.network.PacketUtil;
import dev.diona.southside.util.player.ChatUtil;
import dev.diona.southside.util.player.MovementUtils;
import dev.diona.southside.util.render.ColorUtil;
import dev.diona.southside.util.render.RoundUtil;
import dev.diona.southside.util.render.animations.impl.ContinualAnimation;
import jnic.JNICInclude;
import me.bush.eventbus.annotation.EventListener;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.*;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextFormatting;

import java.awt.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@JNICInclude
public class AutoGapple extends Module {
    private static AutoGapple INSTANCE;
    public final Switch autoValue = new Switch("Auto", false);
    public final Switch debugValue = new Switch("Debug", false);
    public final Switch cancelMove = new Switch("Cancel Move", true);
    public final Switch hudValue = new Switch("OldHUD", false);
    public final Switch hudValue2 = new Switch("NewHUD", false);
    public final Switch hudValue3 = new Switch("NewNavenHUD", false);
    public final Switch hudValue4 = new Switch("OLDNavenHUD", false);
    public final Slider hurttime = new Slider("HurtTime", 0, 0, 5, 1);
    public final Slider eatingTicksValue = new Slider("Eating Ticks", 32, 1, 64, 1);
    public List<Packet<?>> packets = new ArrayList<>();
    public final ContinualAnimation animation = new ContinualAnimation();
    boolean velocityed = true;
    boolean eating = false;
    boolean restart = true;
    private int slot = -1;
    int c03s = 0;
    public final AtomicInteger c03ss = new AtomicInteger(0);
    private float smoothedProgress = 0f;
    private long lastUpdateTime = System.currentTimeMillis();
    private float progressValue = 0.0f;
    private float progressTarget = 0.0f;
    private final float progressSpeed = 0.2f;
    public AutoGapple(String name, String description, Category category, boolean visible) {
        super(name, description, category, visible);
        INSTANCE = this;
    }

    @Override
    public boolean onEnable() {
        if (cancelMove.getValue()) {
            MovementUtils.cancelMove();
        }
        this.packets.clear();
        this.eating = true;
        this.c03s = 0;
        velocityed = false;
        this.slot = this.findItem(Items.GOLDEN_APPLE);
        if (AutoGapple.isDebug()) ChatUtil.info("slot:" + slot);
        if (this.slot == -1) {
            Notification.addNotificationKeepTime("You haven't any gapple!", "Auto Gapple", Notification.NotificationType.WARN, 3.0);
            this.setEnable(false);
            return false;
        }
        c03ss.set(0);
        smoothedProgress = 0f;
        progressValue = 0.0f;
        progressTarget = 0.0f;
        return true;
    }

    private int findItem(Item item) {
        for (int i = 36; i < 45; ++i) {
            ItemStack stack = AutoGapple.mc.player.inventoryContainer.getSlot(i).getStack();
            if (stack == null || stack.getItem() != item) continue;
            return i - 36;
        }
        return -1;
    }

    private boolean hasGappleInSlot(int slot) {
        if (slot < 0 || slot > 8) return false;
        ItemStack stack = mc.player.inventory.getStackInSlot(slot);
        return stack != null && stack.getItem() == Items.GOLDEN_APPLE;
    }


    @Override
    public boolean onDisable() {
        eating = false;
        velocityed = false;
        if (cancelMove.getValue()) {
            MovementUtils.resetMove();
        }
        blink();
        return true;
    }

    @EventListener
    public final void onPacket(HigherPacketEvent event) {
        Packet<?> packet = event.getPacket();

        if (!PacketUtil.isEssential(packet) && PacketUtil.isCPacket(packet)) {
            event.setCancelled(true);
            packets.add(packet);
        }

        if (packet instanceof CPacketUseEntity) {
            Object target = KillAura.getTarget();
            if (target != null && target instanceof EntityLivingBase) {
                EntityLivingBase livingTarget = (EntityLivingBase) target;
                if (livingTarget.hurtTime <= hurttime.getValue().intValue()) {
                    send();
                }
            }
        }

        c03s = (int) packets.stream().filter(p -> p instanceof CPacketPlayer).count();
    }

    @EventListener
    public final void onUpdate(final UpdateEvent event) {
        if (mc.player == null || mc.player.isDead) {
            this.setEnable(false);
            return;
        }

        if (eating) {
            mc.gameSettings.keyBindSprint.setPressed(false);
        }
        if (this.slot == -1 || !hasGappleInSlot(this.slot)) {
            Notification.addNotificationKeepTime("You haven't any gapple!", "Auto Gapple", Notification.NotificationType.WARN, 3);
            this.setEnable(false);
            return;
        }

        if (this.c03s >= eatingTicksValue.getValue().intValue()) {
            c03s = 0;
            if (mc.player.inventory.currentItem != slot) {
                mc.getConnection().sendPacketNoHigherEvent(new CPacketHeldItemChange(slot));
                mc.getConnection().sendPacketNoHigherEvent(new CPacketPlayerTryUseItem(EnumHand.MAIN_HAND));
                ChatUtil.info("我漏防了[呲牙]");
                blink();
                c03ss.set(0);
                progressTarget = 0.0f;
                mc.getConnection().sendPacketNoHigherEvent(new CPacketHeldItemChange(mc.player.inventory.currentItem));
            } else {
                mc.getConnection().sendPacketNoHigherEvent(new CPacketPlayerTryUseItem(EnumHand.MAIN_HAND));
                blink();
            }
            packets.clear();
            this.setEnable(false);
            if (this.autoValue.getValue()) {
                restart = true;
                setEnable(true);
                restart = false;
            } else {
                restart = false;
            }
            if (eating) {
                mc.player.moveForward *= 0.2f;
                mc.player.moveStrafing *= 0.2f;
            }
        }

        long currentTime = System.currentTimeMillis();
        float delta = (currentTime - lastUpdateTime) / 1000f;
        lastUpdateTime = currentTime;

        float targetProgress = Math.min(c03s, eatingTicksValue.getValue().intValue()) / (float) eatingTicksValue.getValue().intValue();
        smoothedProgress += (targetProgress - smoothedProgress) * delta * 10f;

        progressTarget = Math.min(c03s, eatingTicksValue.getValue().intValue()) / (float) eatingTicksValue.getValue().intValue() * 100.0f;
        progressValue += (progressTarget - progressValue) * progressSpeed;
        progressValue = MathHelper.clamp(progressValue, 0.0f, 100.0f);
    }

    @EventListener
    public final void onWorld(final WorldEvent event) {
        this.setEnable(false);
    }

    @EventListener
    public void onRender(NewRender2DEvent event) {
        if (hudValue.getValue()) {
            NanoVGHelper nanovg = NanoVGHelper.INSTANCE;
            ScaledResolution resolution = new ScaledResolution(mc);
            int x = resolution.getScaledWidth() / 2;
            int y = resolution.getScaledHeight() - 75;
            float thickness = 5.0f;
            float percentage = (float) Math.min(this.c03s, 32) / 34.0f;
            int width = 100;
            int half = 50;
            this.animation.animate(98.0f * percentage, 40);
            RoundUtil.drawRound(x - 50 - 1, y - 1 - 12, 101.0f, (int) (thickness + 1.0f) + 12 + 3, 2.0f, new Color(17, 17, 17, 215));
            RoundUtil.drawRound(x - 50 - 1, y - 1, 101.0f, (int) (thickness + 1.0f), 2.0f, new Color(17, 17, 17, 215));
            RoundUtil.drawGradientHorizontal(x - 50, y + 1, this.animation.getOutput(), thickness, 2.0f, new Color(this.color(0)), new Color(this.color(90)));
            nanovg.setupAndDraw(true, vg -> {
                nanovg.drawText(vg, "Time", x - 12, y - 1 - 11 + 6, Color.WHITE.getRGB(), 10.0f, Fonts.BOLD);
                nanovg.drawText(vg, new DecimalFormat("0.0").format(percentage * 100.0f) + "%", x - 9, (float) y + 4.5f, new Color(207, 207, 207).getRGB(), 6.0f, Fonts.BOLD);
            });
        }
        if (hudValue2.getValue()) {
            ScaledResolution sr = new ScaledResolution(mc);
            String text;
            if (SmartBlock.isBlocking()) {
                text = String.format(TextFormatting.WHITE + "Gapple: " + TextFormatting.YELLOW + this.c03s + "/" + TextFormatting.GREEN + eatingTicksValue.getValue().intValue() + TextFormatting.WHITE + "(" + TextFormatting.YELLOW + "Smart Blocking" + TextFormatting.WHITE + ")");
            } else {
                text = String.format(TextFormatting.WHITE + "Gapple: " + TextFormatting.YELLOW + this.c03s + "/" + TextFormatting.GREEN + eatingTicksValue.getValue().intValue());
            }
            mc.fontRenderer.drawStringWithShadow(text, (float) sr.getScaledWidth() / 2 - (float) mc.fontRenderer.getStringWidth(text) / 2, (float) sr.getScaledHeight() / 2 - 20, -1);
        }
        if (hudValue3.getValue()) {
            ScaledResolution sr = new ScaledResolution(mc);
            int x = sr.getScaledWidth() / 2 - 50;
            int y = sr.getScaledHeight() / 2 + 15;
            RoundUtil.drawRound(x, y, 100, 5, 2, new Color(0, 0, 0, 150));
            RoundUtil.drawRound(x, y, progressValue, 5, 2, new Color(150, 45, 45));
            String text = "Eating: " + c03s + "/" + eatingTicksValue.getValue().intValue();
            mc.fontRenderer.drawStringWithShadow(text, x + 50 - mc.fontRenderer.getStringWidth(text) / 2, y - 10, -1);
        }
        if (hudValue4.getValue()) {
            float radius = 2.0f;
            ScaledResolution sr = new ScaledResolution(mc);
            float progress = Math.min((c03s / 34.0f), 1.0f);
            float barWidth = 80.0f;
            float barHeight = 2.0f;
            int centerX = sr.getScaledWidth() / 2;
            int centerY = sr.getScaledHeight() / 2;
            float startX = centerX - barWidth / 2;
            float startY = centerY - 30;
            String text = "Eating Ticks";
            int textWidth = mc.fontRenderer.getStringWidth(text);
            mc.fontRenderer.drawStringWithShadow(text, centerX - textWidth / 2f, startY - 3, -1);
            RoundUtil.drawGradientRound(startX, startY + 7.5F, barWidth, barHeight, 3.0F, new Color(0, 0, 0, 200), new Color(0, 0, 0, 150), new Color(0, 0, 0, 150), new Color(0, 0, 0, 150));
            float target = barWidth * progress;
            RoundUtil.drawGradientRound(startX, startY + 7.5F, target, barHeight, 3.0F, new Color(143, 49, 46, 220), new Color(143, 49, 46, 220), new Color(143, 49, 46, 220), new Color(143, 49, 46, 220));
        }
    }

    public int color(int counter) {
        return color(counter, 1);
    }

    public int color(int counter, float alpha) {
        return ColorUtil.applyOpacity((ColorUtil.colorSwitch(new Color(79, 79, 82), new Color(0, 0, 0), 2000.0F, counter, 75L, 2).getRGB()), alpha);
    }

    void send() {
        if (packets.isEmpty())
            return;

        Packet<?> packet = packets.get(0);
        packets.remove(0);
        if (packet instanceof CPacketHeldItemChange || (packet instanceof CPacketPlayerDigging && ((CPacketPlayerDigging) packet).getAction() == CPacketPlayerDigging.Action.RELEASE_USE_ITEM)) {
            send();
            return;
        }
        mc.getConnection().sendPacketNoHigherEvent(packet);
        if (!(packet instanceof CPacketUseEntity)) {
            send();
        }
    }

    void blink() {
        if (packets.isEmpty())
            return;
        while (!packets.isEmpty()) {
            Packet<?> packet = packets.get(0);
            packets.remove(0);
            if (packet instanceof CPacketChatMessage || packet instanceof CPacketHeldItemChange || packet instanceof net.minecraft.network.play.client.CPacketClickWindow || packet instanceof net.minecraft.network.play.client.CPacketCloseWindow ||  (packet instanceof CPacketPlayerDigging && ((CPacketPlayerDigging) packet).getAction() == CPacketPlayerDigging.Action.RELEASE_USE_ITEM))
                continue;
            mc.getConnection().sendPacketNoHigherEvent(packet);
        }
        c03ss.set(0);
        progressTarget = 0.0f;
    }

    public static boolean isEating() {
        return INSTANCE.isEnabled() && INSTANCE.eating;
    }

    public static boolean isDebug() {
        return INSTANCE.isEnabled() && INSTANCE.debugValue.getValue();
    }
}