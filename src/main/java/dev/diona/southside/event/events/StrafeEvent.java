package dev.diona.southside.event.events;

import me.bush.eventbus.event.Event;

public class StrafeEvent extends Event {
    private float strafe;
    private float forward;
    private float friction;
    private float yaw;
    public boolean jumped = false;

    public StrafeEvent(float strafe, float forward, float friction, float yaw) {
        this.strafe = strafe;
        this.forward = forward;
        this.friction = friction;
        this.yaw = yaw;
    }

    public float getStrafe() {
        return strafe;
    }

    public float getForward() {
        return forward;
    }

    public float getFriction() {
        return friction;
    }

    public float getYaw() {
        return yaw;
    }

    public void setStrafe(float strafe) {
        this.strafe = strafe;
    }

    public void setForward(float forward) {
        this.forward = forward;
    }

    public void setFriction(float friction) {
        this.friction = friction;
    }

    public void setYaw(float yaw) {
        this.yaw = yaw;
    }

    @Override
    protected boolean isCancellable() {
        return true;
    }
}
