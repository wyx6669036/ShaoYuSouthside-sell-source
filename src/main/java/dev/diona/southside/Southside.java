package dev.diona.southside;

import dev.diona.southside.managers.*;
import dev.diona.southside.util.player.RotationUtil;
import dev.diona.southside.util.render.glyph.GlyphFontManager;
import dev.diona.southside.util.player.MovementUtils;
import me.bush.eventbus.bus.EventBus;
import net.minecraft.client.Minecraft;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjglx.opengl.Display;

public class Southside {
    public static final String CLIENT_NAME = "Southside";
    public static final String CLIENT_VERSION = "B5.2";
    public static final ClientType type = ClientType.DEV;
    public static String user = Minecraft.getMinecraft().getSession().getUsername();

    public static final Southside INSTANCE = new Southside();

    public static Logger LOGGER = LogManager.getLogger(Southside.class);
    public static EventBus eventBus = new EventBus();

    public static ModuleManager moduleManager;
    public static FontManager fontManager;
    public static RenderManager renderManager;
    public static FileManager fileManager;
    public static CommandManager commandManager;
    public static StyleManager styleManager;

    public static void start() {
        LOGGER.info("Starting Southside...");
        GlyphFontManager.INSTANCE.initialize();

        RotationUtil.initialize();
        Southside.eventBus.subscribe(MovementUtils.INSTANCE);

        // Initialize managers
        renderManager = new RenderManager();
        fontManager = new FontManager();
        moduleManager = new ModuleManager();

        fileManager = new FileManager();
        commandManager = new CommandManager();

        Display.setTitle(Southside.CLIENT_NAME + " Premium  " + Southside.CLIENT_VERSION + " - Minecraft 1.12.2 | " + type.name());
    }

    public static void stop() {
        LOGGER.info("Stopping Southside...");
    }

    // 添加showNotification方法
    public void showNotification(String message, int duration) {
        // 这里实现通知逻辑
        System.out.println("Notification: " + message);
    }

    public interface MC {
        Minecraft mc = Minecraft.getMinecraft();
    }

    public enum ClientType {
        RELEASE,
        BETA,
        DEV
    }
}