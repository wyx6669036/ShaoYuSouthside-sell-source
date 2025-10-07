package dev.diona.southside.module.modules.player;

import cc.polyfrost.oneconfig.config.options.impl.Slider;
import cc.polyfrost.oneconfig.config.options.impl.Switch;
import dev.diona.southside.Southside;
import dev.diona.southside.event.events.*;
import dev.diona.southside.module.Category;
import dev.diona.southside.module.Module;
import dev.diona.southside.module.modules.combat.AutoGapple;
import dev.diona.southside.util.misc.FakePlayer;
import dev.diona.southside.util.network.PacketUtil;
import dev.diona.southside.util.player.ChatUtil;
import dev.diona.southside.util.player.FakePlayerUtil;
import dev.diona.southside.util.world.ProjectileUtil;
import jnic.JNICInclude;
import me.bush.eventbus.annotation.EventListener;
import net.minecraft.entity.Entity;
import net.minecraft.entity.IProjectile;
import net.minecraft.entity.item.EntityTNTPrimed;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntityEgg;
import net.minecraft.entity.projectile.EntitySnowball;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.CPacketAnimation;
import net.minecraft.network.play.client.CPacketConfirmTransaction;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;

@JNICInclude
public class FakeLag extends Module {
    private List<List<Packet<?>>> packets = new ArrayList<>();
    public static FakePlayer fakePlayer;
    private int ticks;
    private boolean closing;
    public static final Slider releaseTickValue = new Slider("Release Tick", 5, 1, 30, 0.1D);
    public static final Switch autoSendValue = new Switch("Auto Send", true);
    public static final Switch debugValue = new Switch("Debug", false);
    public static final Slider minRangeValue = new Slider("minRange", 6, 1, 12, 1D);
    public static final Slider sendTickValue = new Slider("Send Tick", 3, 1, 30, 1D);
    public static final Slider releaseDelayValue = new Slider("Release Delay", 100, 1, 100, 1D);
    private List<Vec3d> realPos = new ArrayList<>();
    private Vec3d lastPos;
    boolean stop;
    private boolean c0f;
    public int c0fs;
    int attackTicks;

    public static boolean isDelay() {
        return Southside.moduleManager.getModuleByClass(FakeLag.class).isEnabled();
    }

    public FakeLag(String name, String description, Category category, boolean visible) {
        super(name, description, category, visible);
    }

    @Override
    public boolean onEnable() {
//        if (AutoGapple.isGapple()) {
//            Southside.moduleManager.getModuleByClass(AutoGapple.class).setEnable(false);
//        }

        c0fs = 0;

        if (mc.player == null) {
            this.setEnable(false);
            return false;
        }
        stop = false;
        packets.add(new ArrayList<>());
        ticks = 0;
        attackTicks = 0;
        closing = false;
        realPos.add(new Vec3d(
                mc.player.posX,
                mc.player.posY,
                mc.player.posZ
        ));
        fakePlayer = FakePlayerUtil.spawnFakePlayer();
        lastPos = new Vec3d(mc.player.posX, mc.player.posY, mc.player.posZ);

        return true;
    }

    @EventListener
    public void onWorld(WorldEvent event) {
        packets.clear();
        realPos.clear();
        ticks = 0;
        attackTicks = 0;
        closing = true;
        this.setEnable(false);
    }

    @Override
    public boolean onDisable() {
        closing = true;
        packets.forEach(this::sendTick);
        packets.clear();
        realPos.clear();
        if (!packets.isEmpty()) {
            return false;
        }
        try {
            if (fakePlayer != null)
                mc.world.removeEntity(fakePlayer);
        } catch (Exception ignored) {
        }

        return true;
    }

    @EventListener
    public void onPacket(HigherPacketEvent event) {
        Packet<?> packet = event.getPacket();

        if (PacketUtil.isEssential(packet)) return;

        if (PacketUtil.isCPacket(packet)) {

            mc.addScheduledTask(() -> {
                packets.get(packets.size() - 1).add(packet);
            });
            event.setCancelled(true);
        }
    }

    @EventListener
    public void onAttack(AttackEvent event) {
        attackTicks++;
//        if (event.getTargetEntity().isDead
//                || event.getTargetEntity() == null) {
//            this.poll();
//        }

        if ((autoSendValue.getValue()
                && attackTicks >= sendTickValue.getValue().intValue())
                && packets.size() >= 3) {
            this.poll();

            attackTicks = 0;
        }
        if (fakePlayer.getDistanceToEntity(event.getTargetEntity()) >= minRangeValue.getValue().floatValue() && packets.size() >= 3) {
            releaseAll();
        }
    }



    @EventListener
    public void onTick(TickEvent event){
        while (true) {
            boolean wasAimed = false;
            for (Entity entity : mc.world.getLoadedEntityList()) {
                if (entity instanceof EntityArrow arrow && !arrow.inGround ||
                        entity instanceof EntitySnowball || entity instanceof EntityEgg ||
                        entity instanceof EntityTNTPrimed
                ) {
                    if (this.isAntiAim(entity)) {
                        wasAimed = true;
                        break;
                    }
                }
            }
            if ((wasAimed || mc.player.hurtTime >= 3) && packets.size() >= 3) {
                this.poll();
            } else {
                break;
            }
        }

        if (debugValue.getValue()) {
            String tick = "Ticks: " + ticks;
            String packet = "Packets: " + (packets.size());
            String attackTick = "attackTicks: " + (attackTicks);

            ChatUtil.info(attackTick);
            ChatUtil.info(packet);
            ChatUtil.info(tick);

        }
        if (!mc.player.isEntityAlive() && !closing) {
            this.setEnable(false);
        }
        if ((fakePlayer.getDistanceToEntity(mc.player) >= minRangeValue.getValue().floatValue())) {
            stop = true;
            this.poll();
        }

        if ((ticks >= (int)releaseTickValue.getValue().floatValue()) && !stop) {

            if ((ticks >= releaseTickValue.getValue().intValue())) {
                ticks = ticks / releaseTickValue.getValue().intValue();
            }
            if ((packets.size() > releaseDelayValue.getValue().intValue() || (ticks % 10 == 0))) {
                if (packets.isEmpty()) return;
                this.poll();
            }
        }
        ticks++;
        packets.add(new ArrayList<>());
        realPos.add(new Vec3d(
                mc.player.posX,
                mc.player.posY,
                mc.player.posZ
        ));
    }

    private boolean isAntiAim(Entity entity) {
        final float width = 1.2F;
        final float height = 2.2F;
        if (entity instanceof IProjectile projectile) {
            float motionSlowdown = 0.99F, size = 1.2F, gravity = 0.05F;
            if (projectile instanceof EntityArrow) {
                motionSlowdown = 0.99F;
                size = 1.2F;
                gravity = 0.05F;
            } else if (projectile instanceof EntitySnowball || projectile instanceof EntityEgg) {
                motionSlowdown = 0.99F;
                gravity = 1.2F;
                size = 0.25F;
            }
            return ProjectileUtil.predictBox(
                    entity.posX,
                    entity.posY,
                    entity.posZ,
                    entity.motionX,
                    entity.motionY,
                    entity.motionZ,
                    motionSlowdown,
                    size,
                    gravity,
                    realPos,
                    width / 2,
                    height
            );
        } else if (entity instanceof EntityTNTPrimed tnt) {
            Vec3d pos = realPos.get(0);
            return (tnt.getDistanceSq(
                    pos.x,
                    pos.y,
                    pos.z
            ) <= 30 && tnt.getFuse() <= 10);
        }
        return false;
    }

    private void poll() {
        if (packets.isEmpty()) return;
        this.sendTick(packets.get(0));
        packets.remove(0);
    }

    private void releaseAll() {
        packets.forEach(this::sendTick);
        packets.clear();
    }

    private void sendTick(List<Packet<?>> tick) {
        tick.forEach(packet -> {
            mc.getConnection().sendPacketNoHigherEvent(packet);
            this.handleFakePlayerPacket(packet);
        });
        if (!realPos.isEmpty()) {
            realPos.remove(0);
        }
    }

    private void handleFakePlayerPacket(Packet<?> packet) {
        if (packet instanceof CPacketPlayer.Position position) {
            fakePlayer.setPositionAndRotationDirect(
                    position.getX(0D),
                    position.getY(0D),
                    position.getZ(0D),
                    fakePlayer.rotationYaw,
                    fakePlayer.rotationPitch,
                    3, true
            );
            lastPos = new Vec3d(position.getX(0D), position.getY(0D), position.getZ(0D));
            fakePlayer.onGround = position.isOnGround();
        } else if (packet instanceof CPacketPlayer.Rotation rotation) {
            fakePlayer.setPositionAndRotationDirect(
                    fakePlayer.posX,
                    fakePlayer.posY,
                    fakePlayer.posZ,
                    rotation.getYaw(0F),
                    rotation.getPitch(0F),
                    3,
                    true
            );
            fakePlayer.onGround = rotation.isOnGround();

            fakePlayer.rotationYawHead = rotation.getYaw(0F);
            fakePlayer.rotationYaw = rotation.getYaw(0F);
            fakePlayer.rotationPitch = rotation.getPitch(0F);
        } else if (packet instanceof CPacketPlayer.PositionRotation positionRotation) {
            fakePlayer.setPositionAndRotationDirect(
                    positionRotation.getX(0D),
                    positionRotation.getY(0D),
                    positionRotation.getZ(0D),
                    positionRotation.getYaw(0F),
                    positionRotation.getPitch(0F),
                    3,
                    true
            );
            fakePlayer.onGround = positionRotation.isOnGround();

            lastPos = new Vec3d(positionRotation.getX(0D), positionRotation.getY(0D), positionRotation.getZ(0D));
            fakePlayer.rotationYawHead = positionRotation.getYaw(0F);
            fakePlayer.rotationYaw = positionRotation.getYaw(0F);
            fakePlayer.rotationPitch = positionRotation.getPitch(0F);
        } else if (packet instanceof CPacketEntityAction action) {
            if (action.getAction() == CPacketEntityAction.Action.START_SPRINTING) {
                fakePlayer.setSprinting(true);
            } else if (action.getAction() == CPacketEntityAction.Action.STOP_SPRINTING) {
                fakePlayer.setSprinting(false);
            } else if (action.getAction() == CPacketEntityAction.Action.START_SNEAKING) {
                fakePlayer.setSneaking(true);
            } else if (action.getAction() == CPacketEntityAction.Action.STOP_SNEAKING) {
                fakePlayer.setSneaking(false);
            }
        } else if (packet instanceof CPacketAnimation animation) {
            fakePlayer.swingArm(animation.getHand());
        }
    }
}
