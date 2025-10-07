package dev.diona.southside.module.modules.player;

import cc.polyfrost.oneconfig.config.options.impl.Switch;
import dev.diona.southside.Southside;
import dev.diona.southside.event.events.NewRender2DEvent;
import dev.diona.southside.event.events.Render2DEvent;
import dev.diona.southside.event.events.WorldEvent;
import dev.diona.southside.module.Category;
import dev.diona.southside.module.Module;
import dev.diona.southside.util.misc.BezierUtil;
import dev.diona.southside.util.misc.MathUtil;
import dev.diona.southside.util.render.RenderUtil;
import dev.diona.southside.util.render.RoundUtil;
import me.bush.eventbus.annotation.EventListener;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjglx.input.Keyboard;
import org.lwjglx.input.Mouse;

import java.awt.*;

import static dev.diona.southside.managers.RenderManager.sr;

public class BalancedTimer extends Module {
    private static BalancedTimer INSTANCE;
    public static final int RELEASE_SPEED = 5;
    private final BezierUtil yAnimation = new BezierUtil(4, 0);
    private final BezierUtil xAnimation = new BezierUtil(4, 0);
    public static int balance = 0;
    private boolean hasStored = false;
    public static Stage stage = Stage.IDLE;
    private int storedTasks = 0;
    public final Switch renderMode = new Switch("NavenRender", true);

    public BalancedTimer(String name, String description, Category category, boolean visible) {
        super(name, description, category, visible);
        INSTANCE = this;
    }

    @EventListener
    public void onWorld(WorldEvent event) {
        balance = 0;
        stage = Stage.IDLE;
    }

    public static void preTick() {
        if (INSTANCE == null || mc.player == null || !INSTANCE.isEnabled()) {
            stage = Stage.IDLE;
            mc.getTimer().tickLength = 50F;
            balance = 0;
            return;
        }
        if ((Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) && Keyboard.isKeyDown(Keyboard.KEY_1)) || Mouse.isButtonDown(3)) {
            mc.playerStuckTicks++;
            stage = Stage.STORE;
            mc.getTimer().tickLength = 50F;
            balance = 0;
            if (!INSTANCE.hasStored) {
                INSTANCE.hasStored = true;
                INSTANCE.storedTasks = mc.scheduledTasks.size();
            }
        } else if (((Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) && Keyboard.isKeyDown(Keyboard.KEY_2)) || Mouse.isButtonDown(4)) && !mc.scheduledTasks.isEmpty()) {
            stage = Stage.RELEASE;
            mc.getTimer().tickLength = 50F / RELEASE_SPEED;
            balance++;
            if (mc.scheduledTasks.size() <= INSTANCE.storedTasks) {
                INSTANCE.hasStored = false;
                INSTANCE.storedTasks = 0;
            }
        } else {
            stage = Stage.IDLE;
            mc.getTimer().tickLength = 50F;
            balance = 0;
            if (INSTANCE.hasStored && mc.scheduledTasks.size() <= INSTANCE.storedTasks) {
                INSTANCE.hasStored = false;
                INSTANCE.storedTasks = 0;
            }
        }

        if (!mc.scheduledTasks.isEmpty()) {
            INSTANCE.yAnimation.update(0);
        } else {
            INSTANCE.yAnimation.update(-30);
            INSTANCE.xAnimation.update(0);
        }
    }

    @EventListener
    public void onBloom2D(Render2DEvent event) {
        // super.onUIElementBloom(event);
        ScaledResolution sr = event.getSr();
        float x = sr.getScaledHeight() / 2F;
        float y = sr.getScaledWidth() / 2F + yAnimation.get();
        float fontSize = 12F;
        String text = "Timer Balance: " + Math.max(0, MathUtil.round((mc.scheduledTasks.size() - 1) * 0.05, 1)) + "s";
        float width = Southside.fontManager.font.getStringWidth(fontSize, text) + 10, height = 20;
        xAnimation.update(width);

        RenderUtil.scissorStart(sr.getScaledHeight() / 2F, sr.getScaledWidth() / 2F, this.xAnimation.get(), height);
        RenderUtil.drawRect(x, y, x + this.xAnimation.get(), y + height, new Color(0, 0, 0, 129).getRGB());

        Southside.fontManager.font.drawString(fontSize, text, x + 5, y + 5, Color.WHITE);
        RenderUtil.scissorEnd();
    }

    @EventListener
    public void onRender(NewRender2DEvent event) {
        if (renderMode.getValue()) {
            if (hasStored) {
                float radius = 2.0f;
                ScaledResolution sr = new ScaledResolution(mc);
                float barWidth = 80.0f;
                ScaledResolution scaledResolution = new ScaledResolution(mc);
                int screenWidth = scaledResolution.getScaledWidth();
                int screenHeight = scaledResolution.getScaledHeight();
                float width = 80.0f;
                float height = 3.0f;
                float progress = Math.min(mc.scheduledTasks.size() * 0.8f, 80.0f);
                int x = (int) (screenWidth / 2f - width / 2f);
                int y = screenHeight / 2 + 15;
                int centerX = sr.getScaledWidth() / 2;
                int centerY = sr.getScaledHeight() / 2;
                float startX = centerX - barWidth / 2;
                String text = "Time Balance";
                int textWidth = mc.fontRenderer.getStringWidth(text);
                mc.fontRenderer.drawStringWithShadow(text, centerX - textWidth / 2f, y - 12, -1);
                RoundUtil.drawRound(x, y, width, height, radius, new Color(0, 0, 0, 150));
                RoundUtil.drawRound(x, y, progress, height, radius, new Color(143, 49, 46, 220));
            }
        }
    }

    public enum Stage {
        STORE,
        IDLE,
        RELEASE
    }

    public static boolean isInstanceEnabled() {
        return INSTANCE != null && INSTANCE.isEnabled();
    }
}