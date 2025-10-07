package dev.diona.southside.gui;

import cc.polyfrost.oneconfig.renderer.NanoVGHelper;
import cc.polyfrost.oneconfig.renderer.font.Fonts;
import dev.diona.southside.Southside;
import dev.diona.southside.util.render.RoundUtil;
import dev.diona.southside.util.render.blur.KawaseBloom;
import dev.diona.southside.util.render.blur.KawaseBlur;
import dev.diona.southside.util.render.glyph.GlyphFontManager;
import dev.diona.southside.util.render.glyph.GlyphFontRenderer;
import dev.diona.southside.util.shader.ShaderElement;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.util.ResourceLocation;
import org.lwjglx.opengl.GL11;

import java.awt.*;

public final class GuiMenuButton
        extends GuiButton {

    public GuiMenuButton(int buttonId, int x, int y, int widthIn, int heightIn, String buttonText, String icon) {
        super(buttonId, x, y, widthIn, heightIn, buttonText);
        this.width = 150;
        this.height = 25;
    }

//    private Framebuffer stencilFramebuffer = new Framebuffer(1, 1, false);

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
//        stencilFramebuffer = ShaderElement.createFrameBuffer(stencilFramebuffer);
//        stencilFramebuffer.framebufferClear();
        if (this.visible) {
            NanoVGHelper nanovg = NanoVGHelper.INSTANCE;
            final var scaledResolution = new ScaledResolution(Minecraft.getMinecraft());
            this.hovered = mouseX >= this.x + this.width / 2 && mouseY >= this.y && mouseX < this.x + this.width / 2 + 75 && mouseY < this.y + 90;
            this.mouseDragged(mc, mouseX, mouseY);
            var fontSize = 13;
            nanovg.setupAndDraw(true, vg -> {
                var textWidth = nanovg.getTextWidth(vg, this.displayString, fontSize, Fonts.WQY);
                nanovg.drawRoundedRect(vg, x, y, this.width, this.height, new Color(255, 255, 255, 180).getRGB(), 5f);
//                stencilFramebuffer.bindFramebuffer(false);
//                RoundUtil.drawRound(x, y, this.width, this.height, 6f, Color.WHITE);
//                stencilFramebuffer.unbindFramebuffer();
//                KawaseBlur.renderBlur(stencilFramebuffer.framebufferTexture, 3, 1);
//                KawaseBloom.shadow(() -> RoundUtil.drawRound(x, y, this.width, this.height, 6f, new Color(0, 0, 0,220)), 2, 1);
//                RoundUtil.drawRound(x, y, this.width, this.height, 6f, new Color(32, 32, 32,110));
                nanovg.drawText(
                        vg,
                        this.displayString,
                        this.x + (this.width / 2f) - textWidth / 2,
                        this.y + this.height / 2f,
                        new Color(0, 0, 0, 255).getRGB(),
                        fontSize,
                        Fonts.WQY
                );
            });
        }
    }

    @Override
    public boolean mousePressed(Minecraft mc, int mouseX, int mouseY) {
        return this.visible && withinBox(x, y, width, height, mouseX, mouseY);
    }

    public static boolean withinBox(int x, int y, int w, int h, int mouseX, int mouseY) {
        return mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h;
    }
}