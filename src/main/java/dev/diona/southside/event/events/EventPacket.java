package dev.diona.southside.event.events;

import net.minecraft.network.Packet;

public class EventPacket {
    private Packet<?> packet;
    private final EventState eventState;
    public boolean cancelled;
    public EventPacket(Packet<?> packet, EventState eventState) {
        this.packet = packet;
        this.eventState = eventState;
    }

    public Packet<?> getPacket() {
        return packet;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void setPacket(Packet<?> packet) {
        this.packet = packet;
    }

    public EventState getEventState() {
        return eventState;
    }

    public enum EventState {
        SEND,
        RECEIVE
    }
}