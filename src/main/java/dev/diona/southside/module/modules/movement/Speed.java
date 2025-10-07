package dev.diona.southside.module.modules.movement;

import cc.polyfrost.oneconfig.config.options.impl.Slider;
import cc.polyfrost.oneconfig.config.options.impl.Switch;
import dev.diona.southside.Southside;
import dev.diona.southside.event.EventState;
import dev.diona.southside.event.events.MotionEvent;
import dev.diona.southside.module.Category;
import dev.diona.southside.module.Module;
import dev.diona.southside.module.modules.combat.KillAura;
import dev.diona.southside.module.modules.player.Blink;
import dev.diona.southside.module.modules.world.Scaffold;
import dev.diona.southside.util.misc.FakePlayer;
import dev.diona.southside.util.player.MovementUtil;
import dev.diona.southside.util.player.PlayerUtil;
import dev.diona.southside.util.player.RotationUtil;
import me.bush.eventbus.annotation.EventListener;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.projectile.EntityFishHook;
import net.minecraft.util.math.AxisAlignedBB;

public class Speed
        extends Module {
    public static Speed INSTANCE;
    public Slider speedOption = new Slider("Speed", (Number)0.05, (Number)0.01, (Number)0.15, 0.01);
    public Switch followTargetOption = new Switch("Follow Target", true);
    public Switch onlyJumpOption = new Switch("Only follow when jump", true);
    public Switch hurttimeCheck = new Switch("HurtTime Check", false);
    public Switch adaptiveOption = new Switch("Adaptive", true);

    public Speed(String name, String description, Category category, boolean visible) {
        super(name, description, category, visible);
        INSTANCE = this;
    }

    @Override
    public boolean onDisable() {
        Speed.mc.gameSettings.keyBindLeft.setPressed(GameSettings.isKeyDown(Speed.mc.gameSettings.keyBindLeft));
        return super.onDisable();
    }

    @EventListener
    public void onPre(MotionEvent event) {
        if (event.getState() != EventState.PRE) {
            return;
        }
        if (this.shouldSkipSpeedAdjustment()) {
            return;
        }
        AxisAlignedBB playerBox = Speed.mc.player.getEntityBoundingBox().expand(1.0, 1.0, 1.0);
        int entityCount = this.countNearbyEntities(playerBox);
        if (entityCount > 0 && MovementUtil.isMoving() && Speed.mc.player.isSprinting()) {
            this.adjustSpeedBasedOnEntities(entityCount);
        } else {
            Speed.mc.gameSettings.keyBindLeft.setPressed(GameSettings.isKeyDown(Speed.mc.gameSettings.keyBindLeft));
        }
    }

    private boolean shouldSkipSpeedAdjustment() {
        return Southside.moduleManager.getModuleByClass(Scaffold.class).isEnabled() || Speed.mc.currentScreen instanceof GuiChat || Speed.mc.player.hurtTime > 6 && (Boolean)this.hurttimeCheck.getValue() != false;
    }

    private int countNearbyEntities(AxisAlignedBB playerBox) {
        int entityCount = 0;
        for (Entity entity : Speed.mc.world.loadedEntityList) {
            if (!this.isEntityValid(entity) || !playerBox.intersects(entity.getEntityBoundingBox())) continue;
            ++entityCount;
        }
        return entityCount;
    }

    private boolean isEntityValid(Entity entity) {
        return (entity instanceof EntityLivingBase || entity instanceof EntityBoat || entity instanceof EntityMinecart || entity instanceof EntityFishHook) && !(entity instanceof EntityArmorStand) && !(entity instanceof FakePlayer) && entity.getEntityId() != Speed.mc.player.getEntityId() && entity.getEntityId() != -8 && entity.getEntityId() != -1337 && !Blink.isInstanceEnabled();
    }

    private void adjustSpeedBasedOnEntities(int entityCount) {
        double strafeOffset = (double)Math.min(entityCount, 3) * ((Number)this.speedOption.getValue()).doubleValue();
        float yaw = this.getMoveYaw();
        double mx = -Math.sin(Math.toRadians(yaw));
        double mz = Math.cos(Math.toRadians(yaw));
        this.adjustMotion(strafeOffset, mx, mz);
        if (entityCount < 4 && this.shouldFollow()) {
            Speed.mc.gameSettings.keyBindLeft.setPressed(true);
        } else {
            Speed.mc.gameSettings.keyBindLeft.setPressed(GameSettings.isKeyDown(Speed.mc.gameSettings.keyBindLeft));
        }
    }

    private void adjustMotion(double strafeOffset, double mx, double mz) {
        if (Speed.mc.player.movementInput.moveForward == 0.0f && Speed.mc.player.movementInput.moveStrafe == 0.0f) {
            Speed.mc.player.motionX = Math.abs(Speed.mc.player.motionX) > strafeOffset ? Speed.mc.player.motionX - Math.signum(Speed.mc.player.motionX) * strafeOffset : 0.0;
            Speed.mc.player.motionZ = Math.abs(Speed.mc.player.motionZ) > strafeOffset ? Speed.mc.player.motionZ - Math.signum(Speed.mc.player.motionZ) * strafeOffset : 0.0;
        } else {
            Speed.mc.player.motionX += mx * strafeOffset;
            Speed.mc.player.motionZ += mz * strafeOffset;
        }
    }

    public boolean shouldFollow() {
        return !(!this.isEnabled() || Southside.moduleManager.getModuleByClass(Scaffold.class).isEnabled() || KillAura.getTarget() == null || (Boolean)this.onlyJumpOption.getValue() != false && !Speed.mc.gameSettings.keyBindJump.isKeyDown() || !PlayerUtil.isBlockUnder(KillAura.getTarget().getPosition().getY(), KillAura.getTarget()) && (Boolean)this.adaptiveOption.getValue() != false || (Boolean)this.followTargetOption.getValue() == false);
    }

    private float getMoveYaw() {
        float moveYaw = Speed.mc.player.rotationYaw;
        if (Speed.mc.player.moveForward != 0.0f || Speed.mc.player.moveStrafing != 0.0f) {
            if (Speed.mc.player.moveForward > 0.0f) {
                moveYaw += Speed.mc.player.moveStrafing > 0.0f ? -45.0f : 45.0f;
            } else if (Speed.mc.player.moveForward < 0.0f) {
                moveYaw -= Speed.mc.player.moveStrafing > 0.0f ? -45.0f : 45.0f;
                moveYaw += 180.0f;
            } else {
                moveYaw += Speed.mc.player.moveStrafing > 0.0f ? -90.0f : 90.0f;
            }
        }
        if (RotationUtil.targetRotation != null && KillAura.getTarget() != null && ((Boolean)this.followTargetOption.getValue()).booleanValue() && (!((Boolean)this.onlyJumpOption.getValue()).booleanValue() || Speed.mc.gameSettings.keyBindJump.isKeyDown())) {
            moveYaw = RotationUtil.targetRotation.yaw;
        }
        return moveYaw;
    }

    @Override
    public String getSuffix() {
        return "Grim";
    }
}


