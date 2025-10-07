package dev.diona.southside.gui.alt;

import cc.polyfrost.oneconfig.renderer.NanoVGHelper;
import cc.polyfrost.oneconfig.renderer.font.Fonts;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.Session;
import org.apache.commons.io.IOUtils;

import java.awt.*;
import java.io.IOException;

public final class GuiAltManager extends GuiScreen {
    private volatile MicrosoftLogin microsoftLogin;
    private volatile boolean closed = false;

    private final GuiScreen parentScreen;

    public GuiAltManager(GuiScreen parentScreen) {
        this.parentScreen = parentScreen;

        final Thread thread = new Thread("MicrosoftLogin Thread") {
            @Override
            public void run() {
                try {
                    microsoftLogin = new MicrosoftLogin();

                    while (!closed) {
                        if (microsoftLogin.logged) {
                            IOUtils.closeQuietly(microsoftLogin);

                            closed = true;

                            microsoftLogin.setStatusColor(new Color(20,255,20).getRGB());
                            microsoftLogin.setStatus("Login successful! " + microsoftLogin.getUserName());

                            mc.session = new Session(microsoftLogin.getUserName(), microsoftLogin.getUuid(), microsoftLogin.getAccessToken(), "mojang");

                            break;
                        }
                    }
                } catch (Throwable e) {
                    closed = true;

                    e.printStackTrace();

                    IOUtils.closeQuietly(microsoftLogin);

                    microsoftLogin.setStatusColor(new Color(255,50,50).getRGB());
                    microsoftLogin.setStatus("Login failed! " + e.getClass().getName() + ":" + e.getMessage());
                }
            }
        };

        thread.setDaemon(true);
        thread.start();
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        super.actionPerformed(button);

        if (button.id == 0) {
            if (microsoftLogin != null && !closed) {
                microsoftLogin.close();
                closed = true;
                IOUtils.closeQuietly(microsoftLogin);

            }

            mc.displayGuiScreen(parentScreen);
        }
    }

    @Override
    public void initGui() {
        super.initGui();

        buttonList.add(new GuiButton(0,width / 2 - 100,height / 2 + 50,"Back"));
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        drawBackground(0);
        super.drawScreen(mouseX, mouseY, partialTicks);
        NanoVGHelper nanovg = NanoVGHelper.INSTANCE;
        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();

        nanovg.setupAndDraw(true, vg -> {
            if (microsoftLogin == null) {
                nanovg.drawCenteredText(
                        vg, "Logging in...",
                        (int) (width / 2.0f), (int) (height / 2.0f - 5f),
                        new Color(255,255,50).getRGB(),
                        15,
                        Fonts.WQY
                );
            } else {
                nanovg.drawCenteredText(
                        vg, microsoftLogin.getStatus(),
                        (int) (width / 2.0f), (int) (height / 2.0f - 5f),
                        microsoftLogin.getStatusColor(),
                        15,
                        Fonts.WQY
                );
            }
        });
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }

    @Override
    public void onGuiClosed() {

        microsoftLogin.close();
        closed = true;
        IOUtils.closeQuietly(microsoftLogin);

        super.onGuiClosed();
    }
}
