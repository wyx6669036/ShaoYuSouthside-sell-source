package dev.diona.southside.gui.hud;

import cc.polyfrost.oneconfig.config.core.OneColor;
import cc.polyfrost.oneconfig.config.options.impl.Dropdown;
import cc.polyfrost.oneconfig.config.options.impl.Slider;
import cc.polyfrost.oneconfig.config.options.impl.Switch;
import cc.polyfrost.oneconfig.hud.Hud;
import cc.polyfrost.oneconfig.internal.gui.HudGui;
import cc.polyfrost.oneconfig.libs.universal.UMatrixStack;
import cc.polyfrost.oneconfig.libs.universal.UResolution;
import cc.polyfrost.oneconfig.renderer.NanoVGHelper;
import cc.polyfrost.oneconfig.renderer.font.Fonts;
import cc.polyfrost.oneconfig.renderer.font.Fontss;
import cc.polyfrost.oneconfig.renderer.scissor.Scissor;
import cc.polyfrost.oneconfig.renderer.scissor.ScissorHelper;
import dev.diona.southside.Southside;
import dev.diona.southside.module.Category;
import dev.diona.southside.module.Module;
import dev.diona.southside.util.misc.BezierUtil;
import dev.diona.southside.util.render.ChromaJS;

import java.awt.*;
import java.util.HashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static dev.diona.southside.Southside.MC.mc;

public class ArrayListHud extends Hud {
    public final Switch noRender = new Switch("No Render Modules", true);
    public final Switch background = new Switch("Background", true);
    public final Dropdown font = new Dropdown("Fonts", "Southside", new String[]{"Southside", "Vanilla", "OneConfig", "Naven", "Adjust", "AdjustBold", "Exhibition","b"});
    public static ArrayListHud INSTANCE;

    public final Dropdown colorValue = new Dropdown("Color", new String[]{
            "Custom", "Rainbow", "LightRainbow", "Astolfo", "Weird", "Valentine"
    }, "Choose which page will show when you open OneConfig", 5, 1);

    public final cc.polyfrost.oneconfig.config.options.impl.Color customColor1 = new cc.polyfrost.oneconfig.config.options.impl.Color(
            "Custom Color A", new OneColor(Color.WHITE));
    public final cc.polyfrost.oneconfig.config.options.impl.Color customColor2 = new cc.polyfrost.oneconfig.config.options.impl.Color(
            "Custom Color B", new OneColor(Color.WHITE));

    public final Slider customSpeed = new Slider("Custom Speed", 10D, 5D, 20D);
    public final Switch showLine = new Switch("Line", true);
    public final Dropdown linePosition = new Dropdown("Line Position", "Right", new String[]{
            "Right", "Left", "Full", "Top", "Down" , "FullModules"
    });
    public final Switch ConnectLine = new Switch("Line Connect", true);
    private ChromaJS.Scale rainbow = null;
    private CopyOnWriteArrayList<ArrayListModule> enabledModules = new CopyOnWriteArrayList<>();
    private HashMap<Module, ArrayListModule> modules = new HashMap<>();

    private float lastScale = 1;
    private float lastHeight = 50;
    private float lastWidth = 50;

    public ArrayListHud(float x, float y, int positionAlignment, float scale) {
        super(x, y, positionAlignment, scale);
        INSTANCE = this;
    }

    public void reloadRainbow() {
        rainbow = new ChromaJS.Scale(customColor1.getValue().toJavaColor(), customColor2.getValue().toJavaColor(), 8);
    }

    private cc.polyfrost.oneconfig.renderer.font.Font getCurrentFont() {
        return switch (font.getMode()) {
            case "Vanilla" -> Fontss.Vanilla;
            case "Southside" -> Fontss.Southside;
            case "OneConfig" -> Fontss.OneConfig;
            case "Naven" -> Fontss.Naven;
            case "Adjust" -> Fontss.Adjust;
            case "AdjustBold" -> Fontss.AdjustBold;
            case "Exhibition" -> Fontss.Exhibition;
            default -> Fontss.Naven;
        };
    }

    public void syncModules() {
        cc.polyfrost.oneconfig.renderer.font.Font fontToUse = getCurrentFont();
        float fontSize = 20 * 0.3f * lastScale;

        if (modules.isEmpty()) {
            for (Module module : Southside.moduleManager.getModules()) {
                modules.put(module, new ArrayListModule(this, module, -1));
            }
        }

        enabledModules = new CopyOnWriteArrayList<>();
        for (Module module : Southside.moduleManager.getModules()) {
            if (shouldDisplay(module)) {
                enabledModules.add(modules.get(module));
            }
        }

        NanoVGHelper nanoVGHelper = NanoVGHelper.INSTANCE;
        nanoVGHelper.setupAndDraw(true, vg -> enabledModules.sort((o1, o2) -> {
            float w1 = nanoVGHelper.getTextWidth(vg, Southside.moduleManager.formatRaw(o1.module), fontSize, fontToUse);
            float w2 = nanoVGHelper.getTextWidth(vg, Southside.moduleManager.formatRaw(o2.module), fontSize, fontToUse);
            return Float.compare(w2, w1);
        }));
    }

    @Override
    protected void draw(UMatrixStack matrices, float x, float y, float scale, boolean example) {
        if (rainbow == null) reloadRainbow();
        syncModules();
        lastScale = scale;

        NanoVGHelper nanoVGHelper = NanoVGHelper.INSTANCE;
        ScissorHelper scissorHelper = ScissorHelper.INSTANCE;
        AtomicBoolean updatedSize = new AtomicBoolean(false);
        AtomicReference<Float> thisHeight = new AtomicReference<>(0f);
        AtomicReference<Float> thisWidth = new AtomicReference<>(0f);
        cc.polyfrost.oneconfig.renderer.font.Font fontToUse = getCurrentFont();

        nanoVGHelper.setupAndDraw(true, vg -> {
            float fontSize = 20 * 0.3F * scale;
            float moduleY = 0;
            int reverse = switch (position.getValue().anchor) {
                case BOTTOM_LEFT, BOTTOM_CENTER, BOTTOM_RIGHT -> -1;
                default -> 1;
            };
            int reverseX = switch (position.getValue().anchor) {
                case TOP_LEFT, MIDDLE_LEFT, BOTTOM_LEFT -> -1;
                default -> 1;
            };







            
            Scissor scissor = switch (position.getValue().anchor) {
                case TOP_LEFT, MIDDLE_LEFT, BOTTOM_LEFT ->
                        scissorHelper.scissor(vg, x, 0, UResolution.getScaledWidth(), UResolution.getScaledHeight());
                default ->
                        scissorHelper.scissor(vg, 0, 0, x + this.getWidth(scale, example), UResolution.getScaledHeight());
            };

            for (ArrayListModule module : enabledModules) {
                module.y.update(reverse * moduleY);
                module.y.freeze();
                module.x.update(0);
                float width = module.draw(vg, x, y, background.getValue(), scale);
                moduleY += nanoVGHelper.getTextHeight(vg, fontSize, fontToUse) + 10 * 0.3F * scale;
                updatedSize.set(true);
                thisWidth.set(Math.max(thisWidth.get(), width));
            }

            thisHeight.set(moduleY);

            for (ArrayListModule module : modules.values()) {
                if (shouldDisplay(module.module)) continue;
                module.y.update(0);
                module.x.update(scale * reverseX * 110 * 0.3F);
                module.draw(vg, x, y, background.getValue(), scale);
            }

            scissorHelper.resetScissor(vg, scissor);
        });

        if (updatedSize.get()) {
            if (mc.currentScreen instanceof HudGui hudGui && hudGui.isScaling) return;
            lastHeight = thisHeight.get() / scale;
            lastWidth = thisWidth.get() / scale;
        }
    }

    private boolean shouldDisplay(Module module) {
        return module.isEnabled() && module.isVisible() && (module.getCategory() != Category.Render || !noRender.getValue());
    }

    @Override
    protected float getWidth(float scale, boolean example) {
        return lastWidth * scale;
    }

    @Override
    protected float getHeight(float scale, boolean example) {
        return lastHeight * scale;
    }

    public int getRainbow(int speed, int offset, float s) {
        float hue = (float) ((System.currentTimeMillis() / 10 * customSpeed.getValue().doubleValue() + offset) % speed);
        hue /= speed;
        return Color.getHSBColor(hue, s, 1f).getRGB();
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
                (int) (color1.getBlue() * inverse + color2.getBlue() * offset));
    }

    public int getCustomOffset(double offset) {
        if (offset > 1) {
            double left = offset % 1;
            long off = (long) offset;
            offset = off % 2 == 0 ? left : 1 - left;
        }
        return rainbow.getColorRGB(offset);
    }

    public static class ArrayListModule {
        public ArrayListHud parent;
        public Module module;
        private final int color;
        public BezierUtil x = new BezierUtil(3, 0), y = new BezierUtil(3, 0);

        public ArrayListModule(ArrayListHud parent, Module module, int color) {
            this.parent = parent;
            this.module = module;
            this.color = color;
        }

        public float draw(long vg, float targetX, float targetY, boolean background, float scale) {
            float fontSize = 20 * 0.3F * scale;
            int reverseX = switch (parent.position.getValue().anchor) {
                case TOP_LEFT, MIDDLE_LEFT, BOTTOM_LEFT -> -1;
                default -> 1;
            };
            if ((reverseX == 1 && x.get() >= 100 * 0.3F * scale) || (reverseX == -1 && x.get() <= -100 * 0.3F * scale))
                return 0f;

            NanoVGHelper nanoVGHelper = NanoVGHelper.INSTANCE;
            cc.polyfrost.oneconfig.renderer.font.Font fontToUse = parent.getCurrentFont();

            float moduleWidth = nanoVGHelper.getTextWidth(vg, Southside.moduleManager.formatRaw(module), fontSize, fontToUse);
            float xPos = targetX + x.get() - moduleWidth;
            float yPos = targetY + y.get();
            float fontHeight = nanoVGHelper.getTextHeight(vg, fontSize, fontToUse);
            float height = fontHeight + 10 * 0.3F * scale;

            switch (parent.position.getValue().anchor) {
                case TOP_LEFT, MIDDLE_LEFT -> xPos += moduleWidth + 7F * 0.3F * scale;
                case BOTTOM_LEFT -> {
                    xPos += moduleWidth + 7F * 0.3F * scale;
                    yPos += parent.getHeight(scale, false) - height;
                }
                case TOP_RIGHT, MIDDLE_RIGHT, TOP_CENTER, MIDDLE_CENTER -> xPos += parent.getWidth(scale, false);
                case BOTTOM_RIGHT, BOTTOM_CENTER -> {
                    xPos += parent.getWidth(scale, false);
                    yPos += parent.getHeight(scale, false) - height;
                }
            }

            if (background) {
                nanoVGHelper.drawRect(vg, xPos - 7F * 0.3F * scale, yPos, moduleWidth + 7F * 0.3F * scale, height, new Color(32, 32, 32, 100).getRGB());
            }

            float maybeY = yPos + height / 2F + 1 * 0.3F * scale;
            int colour = switch (parent.colorValue.getValue()) {
                case 0 ->
                        parent.getCustomOffset((System.currentTimeMillis() / 100D * parent.customSpeed.getValue().doubleValue()) / 100D + (maybeY / 50));
                case 1 -> parent.getRainbow(6000, (int) (maybeY * 30), 0.85f);
                case 2 -> parent.getRainbow(6000, (int) (maybeY * 30), 0.55f);
                case 3 -> parent.getGradientOffset(new Color(255, 255, 255), new Color(255, 255, 255),
                        (System.currentTimeMillis() / 100D * parent.customSpeed.getValue().doubleValue()) / 100D + (maybeY / 50)).getRGB();
                case 4 -> parent.getGradientOffset(new Color(255, 255, 255), new Color(255, 255, 255),
                        (System.currentTimeMillis() / 100D * parent.customSpeed.getValue().doubleValue()) / 100D + (maybeY / 50)).getRGB();
                case 5 -> parent.getGradientOffset(new Color(255, 255, 255), new Color(255, 255, 255),
                        (System.currentTimeMillis() / 100D * parent.customSpeed.getValue().doubleValue()) / 100D + (maybeY / 50)).getRGB();
                default -> -1;
            };
            if (parent.showLine.getValue()) {
                float lineWidth = 2.5f * 0.3F * scale;
                float boxX = xPos - 7F * 0.3F * scale;
                float boxY = yPos;
                float boxW = moduleWidth + 7F * 0.3F * scale;
                float boxH = height;

                switch (parent.linePosition.getMode()) {
                    case "Left" ->
                            nanoVGHelper.drawRect(vg, boxX, boxY, lineWidth, boxH, colour);

                    case "Right" ->
                            nanoVGHelper.drawRect(vg, boxX + boxW - lineWidth, boxY, lineWidth, boxH, colour);

                    case "Top" -> {
                        if (!parent.enabledModules.isEmpty()) {
                            ArrayListModule topModule = parent.enabledModules.get(0);
                            float topWidth = nanoVGHelper.getTextWidth(vg, Southside.moduleManager.formatRaw(topModule.module), 20 * 0.3F * scale, parent.getCurrentFont());
                            nanoVGHelper.drawRect(vg, boxX, boxY, topWidth, lineWidth, colour);
                        }
                    }

                    case "Down" -> {
                        if (!parent.enabledModules.isEmpty()) {
                            ArrayListModule bottomModule = parent.enabledModules.get(parent.enabledModules.size() - 1);
                            float bottomWidth = nanoVGHelper.getTextWidth(vg, Southside.moduleManager.formatRaw(bottomModule.module), 20 * 0.3F * scale, parent.getCurrentFont());
                            float bottomY = boxY + boxH - lineWidth;
                            nanoVGHelper.drawRect(vg, boxX, bottomY, bottomWidth, lineWidth, colour);
                        }
                    }

                    case "Full" -> {
                        nanoVGHelper.drawRect(vg, boxX, boxY, boxW, lineWidth, colour);
                        nanoVGHelper.drawRect(vg, boxX, boxY + boxH - lineWidth, boxW, lineWidth, colour);
                        nanoVGHelper.drawRect(vg, boxX, boxY, lineWidth, boxH, colour);
                        nanoVGHelper.drawRect(vg, boxX + boxW - lineWidth, boxY, lineWidth, boxH, colour);
                    }
                }
            }

            nanoVGHelper.drawTextWithFormatting(vg, Southside.moduleManager.format(module),
                    xPos - 2F * 0.3F * scale, maybeY, colour, fontSize, fontToUse);

            return moduleWidth + 7F * 0.3F * scale;
        }
    }
}