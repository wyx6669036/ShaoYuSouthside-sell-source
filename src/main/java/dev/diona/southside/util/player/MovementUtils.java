package dev.diona.southside.util.player;

import dev.diona.southside.event.EventState;
import dev.diona.southside.event.events.*;
import me.bush.eventbus.annotation.EventListener;
import net.minecraft.client.Minecraft;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.server.SPacketEntityVelocity;

public class MovementUtils {
    public static final MovementUtils INSTANCE = new MovementUtils();
    public static int noMovePackets = 0;
    public static boolean cancelMove = false;
    private static double motionX = 0.0f;
    private static double motionY = 0.0f;
    private static double motionZ = 0.0f;
    private static float fallDistance = 0f;
    private static int moveTicks = 0;
    private static Minecraft mc = Minecraft.getMinecraft();

    public static void cancelMove() {
        if (mc.player == null)
            return;
        if (cancelMove)
            return;
        cancelMove = true;
        motionX = mc.player.motionX;
        motionY = mc.player.motionY;
        motionZ = mc.player.motionZ;
        fallDistance = mc.player.fallDistance;
    }
    public static void resetMove() {
        cancelMove = false;
        moveTicks = 0;
    }
    @EventListener
    public void onMotion(final MotionEvent event) {
        if (event.getState() == EventState.POST) {
            if (moveTicks > 0) {
                motionX = mc.player.motionX;
                motionZ = mc.player.motionZ;

                motionY = mc.player.motionY;
                fallDistance = mc.player.fallDistance;
                moveTicks--;
            }
        }
    }
    @EventListener
    public void onUpdate(UpdateEvent event) {
        if (cancelMove) {

            if (moveTicks > 0)
                return;
            mc.player.motionX = motionX;
            mc.player.motionZ = motionZ;
            mc.player.motionY = motionY;
            mc.player.fallDistance = fallDistance;
        }
    }
    @EventListener
    public void onTick(TickEvent event) {
        if (mc.player == null) {
            MovementUtils.resetMove();
            return;
        }
        if (cancelMove) {

            if (noMovePackets >= 19) {
                mc.player.motionX = motionX;
                mc.player.motionY = motionY;
                mc.player.motionZ = motionZ;
                mc.player.fallDistance = fallDistance;
                moveTicks++;
            }
            if (moveTicks > 0)
                return;

            mc.player.motionX = motionX;
            mc.player.motionZ = motionZ;
            mc.player.motionY = motionY;
            mc.player.fallDistance = fallDistance;
        }
    }
    @EventListener
    public void onMove(final MoveEvent event) {
        if (cancelMove) {

            if (moveTicks > 0) {
                return;
            }
            event.setCancelled(true);
        }
    }
    @EventListener
    public void onPacketReceive(final PacketEvent event) {
        if (event.getPacket() instanceof SPacketEntityVelocity && cancelMove) {
            if (((SPacketEntityVelocity) event.getPacket()).getEntityID() == mc.player.getEntityId()) {
                mc.player.motionX = motionX;
                mc.player.motionY = motionY;
                mc.player.motionZ = motionZ;
                mc.player.fallDistance = fallDistance;
                moveTicks++;
            }
        }
    }
    public static void onPacket(Packet<?> packet) {
        if (packet instanceof CPacketPlayer) {
            if (((CPacketPlayer) packet).isMoving()) {
                noMovePackets = 0;
            } else {
                noMovePackets++;
            }
        }
    }
}
