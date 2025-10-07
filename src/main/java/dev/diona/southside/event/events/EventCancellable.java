package dev.diona.southside.event.events;

import org.apache.http.concurrent.Cancellable;
import org.w3c.dom.events.Event;

public abstract class EventCancellable implements Event, Cancellable {

    public boolean cancelled;

    protected EventCancellable() {
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancelled(boolean state) {
        cancelled = state;
    }

}
