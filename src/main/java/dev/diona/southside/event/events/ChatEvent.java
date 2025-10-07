package dev.diona.southside.event.events;


import me.bush.eventbus.event.Event;
import net.minecraft.util.text.ITextComponent;

public class ChatEvent extends Event {

    /**
     * Introduced in 1.8:
     * 0 : Standard Text Message
     * 1 : 'System' message, displayed as standard text.
     * 2 : 'Status' message, displayed above action bar, where song notifications are.
     */
    public final byte type;
    public ITextComponent message;
    private final String Message;

    public ChatEvent(byte type, ITextComponent message) {
        this.type = type;
        this.message = message;
        this.Message = message.getUnformattedText();
    }

    public String getMessage() {
        return Message;
    }

    @Override
    protected boolean isCancellable() {
        return true;
    }
}
