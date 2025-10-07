package dev.diona.southside.gui.hud;

import cc.polyfrost.oneconfig.config.core.OneColor;
import cc.polyfrost.oneconfig.config.options.impl.Dropdown;
import cc.polyfrost.oneconfig.config.options.impl.Slider;
import cc.polyfrost.oneconfig.config.options.impl.Switch;
import cc.polyfrost.oneconfig.hud.Hud;
import cc.polyfrost.oneconfig.internal.assets.SVGs;
import cc.polyfrost.oneconfig.libs.universal.UMatrixStack;
import cc.polyfrost.oneconfig.renderer.NanoVGHelper;
import cc.polyfrost.oneconfig.renderer.font.Fonts;
import cc.polyfrost.oneconfig.renderer.font.Fontss;
import dev.diona.southside.Southside;
import dev.diona.southside.util.render.ChromaJS;
import dev.diona.southside.util.render.RenderUtil;
import dev.diona.southside.util.render.RoundUtil;
import net.java.games.input.Version;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.network.NetworkPlayerInfo;
import org.apache.commons.lang3.StringUtils;

import java.awt.*;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import static dev.diona.southside.Southside.MC.mc;
import static dev.diona.southside.Southside.user;

public class WatermarkHud extends Hud {
    public NanoVGHelper nanovg = NanoVGHelper.INSTANCE;
    public Dropdown mode = new Dropdown("Mode", "Southside", new String[]{"Southside","Exhibition","Mugen","Naven"});
    public Dropdown oldmugenmode = new Dropdown("Styles", "FPS", new String[]{"FPS", "Time", "None"});//() -> mode.isMode("Mugen"));
    private float lastTextWidth = 0;
    private float lastTextHeight = 0;
    float width;
    public final Switch exhibitionShowFPS = new Switch("ShowFPS", true);
    public final Switch exhibitionShowPing = new Switch("ShowPing", false);
    public final Switch exhibitionShowIP = new Switch("ShowIP", false);
    public final Switch exhibitionShowTime = new Switch("ShowTime", false);
    public final Slider customSpeed = new Slider("Custom Speed", 10D, 5D, 20D);
    public final Dropdown exhibitionEColor = new Dropdown("'E'Color", new String[]{
            "Custom", "Rainbow", "LightRainbow", "Astolfo", "Weird", "Valentine"
    }, "Choose which page will show when you open OneConfig", 5, 1);
    public float waterW = 0f, waterH = 0f;
    public boolean boolW = false, boolH = false;
    public float Ra = 2f;

    public final cc.polyfrost.oneconfig.config.options.impl.Color customColor1 = new cc.polyfrost.oneconfig.config.options.impl.Color(
            "Custom Color A", new OneColor(Color.WHITE));
    public final cc.polyfrost.oneconfig.config.options.impl.Color customColor2 = new cc.polyfrost.oneconfig.config.options.impl.Color(
            "Custom Color B", new OneColor(Color.WHITE));

    public final float smoothing = 0.16f;
    public final long bigPause = 800L;
    public final long smallPause = 400L;
    public boolean isPaused = false;
    public long pauseStartTime = 0L;
    public long currentPause = 0L;
    public ChromaJS.Scale rainbow = null;

    public WatermarkHud() {
        reloadRainbow();
    }

    public void reloadRainbow() {
        rainbow = new ChromaJS.Scale(customColor1.getValue().toJavaColor(), customColor2.getValue().toJavaColor(), 8);
    }

    public void anim(float maxW, float maxH) {
        long now = System.currentTimeMillis();
        if (isPaused) {
            if (now - pauseStartTime >= currentPause) {
                isPaused = false;
            }
            return;
        }

        float targetW = waterW;
        float targetH = waterH;

        if (!boolW && !boolH) {
            targetW = maxW;
            Ra = 2f;
        } else if (boolW && !boolH) {
            targetH = 1f;
        } else if (boolW && boolH) {
            targetW = 1f;
            Ra = 0.4f;
        } else {
            targetH = maxH;
            Ra = 1f;
        }

        waterW += (targetW - waterW) * smoothing;
        waterH += (targetH - waterH) * smoothing;

        if (!boolW && !boolH && waterW >= maxW - 0.1f) {
            boolW = true;
            pauseStart(now, smallPause);
        } else if (boolW && !boolH && waterH <= 1f + 0.1f) {
            boolH = true;
            pauseStart(now, bigPause);
        } else if (boolW && boolH && waterW <= 1f + 0.1f) {
            boolW = false;
            pauseStart(now, smallPause);
        } else if (!boolW && boolH && waterH >= maxH - 0.1f) {
            boolH = false;
            pauseStart(now, smallPause);
        }
    }

    public void pauseStart(long now, long duration) {
        isPaused = true;
        pauseStartTime = now;
        currentPause = duration;
    }

    public int getRainbow(int speed, int offset, float saturation) {
        float hue = (float) ((System.currentTimeMillis() / 10 * customSpeed.getValue().doubleValue() + offset) % speed);
        hue /= speed;
        return Color.getHSBColor(hue, saturation, 1f).getRGB();
    }

    public Color getGradientOffset(Color color1, Color color2, double offset) {
        if (offset > 1) {
            double left = offset % 1;
            long off = (long) offset;
            offset = off % 2 == 0 ? left : 1 - left;
        }
        double inverse = 1 - offset;
        return new Color(
                (int) (color1.getRed() * inverse + color2.getRed() * offset),
                (int) (color1.getGreen() * inverse + color2.getGreen() * offset),
                (int) (color1.getBlue() * inverse + color2.getBlue() * offset)
        );
    }

    public int getCustomOffset(double offset) {
        if (rainbow == null) reloadRainbow();
        if (offset > 1) {
            double left = offset % 1;
            long off = (long) offset;
            offset = off % 2 == 0 ? left : 1 - left;
        }
        return rainbow.getColorRGB(offset);
    }

    public int resolveColor(float maybeY) {
        switch (exhibitionEColor.getValue()) {
            case 0:
                return getCustomOffset((System.currentTimeMillis() / 100D * customSpeed.getValue().doubleValue()) / 100D + (maybeY / 50));
            case 1:
                return getRainbow(6000, (int) (maybeY * 30), 0.85f);
            case 2:
                return getRainbow(6000, (int) (maybeY * 30), 0.55f);
            case 3:
                return getGradientOffset(new Color(255, 60, 234), new Color(27, 179, 255),
                        (System.currentTimeMillis() / 100D * customSpeed.getValue().doubleValue()) / 100D + (maybeY / 50)).getRGB();
            case 4:
                return getGradientOffset(new Color(128, 171, 255), new Color(160, 72, 255),
                        (System.currentTimeMillis() / 100D * customSpeed.getValue().doubleValue()) / 100D + (maybeY / 50)).getRGB();
            case 5:
                return getGradientOffset(new Color(255, 129, 202), new Color(255, 15, 0),
                        (System.currentTimeMillis() / 100D * customSpeed.getValue().doubleValue()) / 100D + (maybeY / 50)).getRGB();
            default:
                return Color.WHITE.getRGB();
        }
    }
    @Override
    public void draw(UMatrixStack matrices, float x, float y, float scale, boolean example) {
        switch (this.mode.getMode()) {
            case "Southside":
                nanovg.setupAndDraw(true, vg -> {
                    final String tempClientName = Southside.CLIENT_NAME;
                    lastTextWidth = nanovg.getTextWidth(vg, tempClientName, 10, Fontss.Southside) + 6;
                    lastTextHeight = nanovg.getTextHeight(vg, 10, Fontss.Southside);
                    float width = getWidth(scale, example);
                    float height = getHeight(scale, example);
                    nanovg.drawDropShadow(vg, x, y, width, height, 10, 0F, 5, new Color(0, 0, 0, 127));
                    nanovg.drawRoundedRect(vg, x, y, width, height, new Color(0, 0, 0, 100).getRGB(), 5);
                    nanovg.drawRoundedRect(vg, x + 5, (float) (y + 2.5 * scale), 1 * scale, height - 5f * scale, Color.WHITE.getRGB(), 1 * scale / 2);
                    nanovg.drawDropShadow(vg, x + 5, (float) (y + 2.5 * scale), 1 * scale, height - 5f * scale, 3, 0.01F, 1 * scale / 2, new Color(255, 255, 255, 255));
                    nanovg.drawSvg(vg, SVGs.SOUTHSIDE, x + (width - lastTextWidth) / 2f + 3f * scale - 17.5f, y + (height - lastTextHeight) / 2.7f + scale - 5.5f, 20, 20, Color.WHITE.getRGB(), 100);

                    nanovg.drawRawTextWithFormatting(vg, tempClientName, x + (width - lastTextWidth) / 2 + 3F * scale + 2.5f, y + (height - lastTextHeight) / 2.7f + scale, -1, 10, Fontss.Southside);
                });
                break;
            case "Exhibition":
                nanovg.setupAndDraw(true, (vg) -> {
                    try {
                        int fps = Minecraft.getDebugFPS();
                        float maybeY = y;
                        float startX = x + 4;
                        float startY = y + 6;
                        float fontSize = 12.0F;
                        float textHeight = nanovg.getTextHeight(vg, fontSize, Fontss.Naven);
                        StringBuilder displayText = new StringBuilder(Southside.CLIENT_NAME);
                        if (exhibitionShowIP.getValue()) {
                            String ip = mc.getCurrentServerData() != null ?
                                    mc.getCurrentServerData().serverIP : "SinglePlayer";
                            displayText.append(" [").append(ip).append("]");
                        }
                        if (exhibitionShowTime.getValue()) {
                            displayText.append(" [").append(LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))).append("]");
                        }
                        if (exhibitionShowFPS.getValue()) {
                            displayText.append(" [").append(fps).append("FPS]");
                        }
                        if (exhibitionShowPing.getValue()) {
                            int ping = mc.getConnection() != null && mc.player != null ?
                                    mc.getConnection().getPlayerInfo(mc.player.getUniqueID()).getResponseTime() : 0;
                            displayText.append(" [").append(ping).append("ms]");
                        }
                        String firstLine = displayText.toString();
                        String firstChar = firstLine.isEmpty() ? "" : firstLine.substring(0, 1);
                        String remainingText = firstLine.length() > 1 ? firstLine.substring(1) : "";
                        float firstCharWidth = firstChar.isEmpty() ? 0 : nanovg.getTextWidth(vg, firstChar, fontSize, Fontss.Exhibition);
                        float remainingWidth = nanovg.getTextWidth(vg, remainingText, fontSize, Fontss.Exhibition);
                        if (!firstChar.isEmpty()) {
                            int color = resolveColor(maybeY);
                            nanovg.drawText(vg, firstChar, startX, startY, color, fontSize, Fontss.Exhibition);
                        }
                        int whiteColor = Color.WHITE.getRGB();
                        int grayColor = new Color(150, 150, 150).getRGB();
                        float currentX = startX + firstCharWidth;

                        for (int i = 0; i < remainingText.length(); i++) {
                            char c = remainingText.charAt(i);
                            float charWidth = nanovg.getTextWidth(vg, String.valueOf(c), fontSize, Fontss.Exhibition);
                            int charColor = (c == '[' || c == ']') ? grayColor : whiteColor;
                            nanovg.drawText(vg, String.valueOf(c), currentX, startY, charColor, fontSize, Fontss.Exhibition);
                            currentX += charWidth;
                        }
                        this.lastTextWidth = firstCharWidth + remainingWidth + 8;
                        this.lastTextHeight = textHeight + 4;
                    } catch (Exception e) {
                        e.printStackTrace();
                        this.lastTextWidth = 100;
                        this.lastTextHeight = 20;
                    }
                });
                break;
            case "Naven":
                nanovg.setupAndDraw(true, (vg) -> {
                        RenderUtil.resetColor();
                        SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
                        String timeString = format.format(new Date());
                        String text = "SouthSide | Modern-Beta | " + user + " | " +
                                Minecraft.getDebugFPS() + " FPS | " + timeString;
                        FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
                        int textWidth = fontRenderer.getStringWidth(text);
                        int textHeight = fontRenderer.FONT_HEIGHT;
                        float backgroundWidth = textWidth + 14.0f;
                        float backgroundHeight = textHeight + 8.0f;
                        float backgroundX = 5.0f;
                        float backgroundY = 5.0f;
                        RoundUtil.drawRound(backgroundX, backgroundY,
                                backgroundWidth, backgroundHeight,
                                5.0f, new Color(0, 0, 0, 150));
                        RoundUtil.drawRound(backgroundX, backgroundY,
                                backgroundWidth, 2.0f, 5.0f,
                                new Color(160, 42, 42, 200));
                        float textX = backgroundX + (backgroundWidth - textWidth) / 2.0f;
                        float textY = backgroundY + (backgroundHeight - textHeight) / 2.0f + 1.0f;
                        fontRenderer.drawString(text, (int)textX, (int)textY, 0xFFFFFFFF);
                        RenderUtil.resetColor();
                });
                break;
            case "Mugen":
                nanovg.setupAndDraw(true, (vg) -> {
                    int fps = Minecraft.getDebugFPS();
                    String clientName = Southside.CLIENT_NAME;
                    String subtext = this.oldmugenmode.isMode("FPS") ? fps + " FPS" :
                            this.oldmugenmode.isMode("Time") ? LocalTime.now().format(
                                    DateTimeFormatter.ofPattern("HH:mm")) : "";
                    final float PADDING = 2.0f;
                    float xOffset = x + PADDING;
                    float yOffset = y + PADDING;
                    float titleFontSize = 13.0f;
                    float subFontSize = 10.0f;

                    float titleWidth = nanovg.getTextWidth(vg, clientName, titleFontSize, Fontss.Southside);
                    float titleHeight = nanovg.getTextHeight(vg, titleFontSize, Fontss.Southside);
                    float subWidth = !subtext.isEmpty() ? nanovg.getTextWidth(vg, subtext, subFontSize, Fontss.Southside) : 0;
                    float subHeight = !subtext.isEmpty() ? nanovg.getTextHeight(vg, subFontSize, Fontss.Southside) : 0;
                    float containerWidth = Math.max(titleWidth, subWidth) + (PADDING * 2);
                    float containerHeight = titleHeight + (subtext.isEmpty() ? 0 : subHeight + 1) + (PADDING * 3);

                    anim(containerWidth, containerHeight);
                    nanovg.drawRect(vg, xOffset, yOffset, waterW, waterH, new Color(102, 204, 255, 200).getRGB());
                    nanovg.drawRawTextWithFormatting(vg, clientName,
                            xOffset + PADDING + 0.5f,
                            yOffset + PADDING + 0.5f,
                            new Color(30, 30, 30).getRGB(),
                            titleFontSize,
                            Fontss.Southside
                    );
                    nanovg.drawRawTextWithFormatting(vg, clientName,
                            xOffset + PADDING,
                            yOffset + PADDING,
                            Color.WHITE.getRGB(),
                            titleFontSize,
                            Fontss.Southside
                    );
                    if (!subtext.isEmpty()) {
                        float subY = yOffset + PADDING + titleHeight + 1;

                        nanovg.drawRawTextWithFormatting(vg, subtext,
                                xOffset + PADDING + 0.5f,
                                subY + 0.5f,
                                new Color(40, 40, 40).getRGB(),
                                subFontSize,
                                Fontss.Southside
                        );
                        nanovg.drawRawTextWithFormatting(vg, subtext,
                                xOffset + PADDING,
                                subY,
                                new Color(220, 220, 220).getRGB(),
                                subFontSize,
                                Fontss.Southside
                        );
                    }

                    this.lastTextWidth = containerWidth;
                    this.lastTextHeight = containerHeight;
                });
        }
    }

    @Override
    protected float getWidth(float scale, boolean example) {
        return lastTextWidth * scale;
    }

    @Override
    protected float getHeight(float scale, boolean example) {
        return lastTextHeight * scale;
    }
}
