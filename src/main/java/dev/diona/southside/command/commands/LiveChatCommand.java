package dev.diona.southside.command.commands;

import dev.diona.southside.command.Command;
import dev.diona.southside.util.player.ChatUtil;
import dev.yalan.live.LiveClient;
import dev.yalan.live.netty.LiveProto;

public class LiveChatCommand extends Command {
    public LiveChatCommand(String description) {
        super(description);
    }

    @Override
    public void run(String[] args) {
        if (args.length < 2) {
            printUsage();
            return;
        }

        final StringBuilder sb = new StringBuilder();
        final int len = args.length;

        for (int i = 1; i < len; i++) {
            sb.append(args[i]);

            if (i != len - 1) {
                sb.append(' ');
            }
        }

        if (LiveClient.INSTANCE.isActive()) {
            LiveClient.INSTANCE.sendPacket(LiveProto.createChat(sb.toString()));
        } else {
            ChatUtil.sendText("LiveService is disconnected");
        }
    }

    @Override
    public void printUsage() {
        ChatUtil.sendText("Usage: .i <message>");
    }
}
