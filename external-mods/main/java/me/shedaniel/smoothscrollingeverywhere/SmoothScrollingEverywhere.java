package me.shedaniel.smoothscrollingeverywhere;

import net.minecraft.util.math.MathHelper;

import java.util.function.Function;

public class SmoothScrollingEverywhere {
    private static Function<Double, Double> easingMethod = v -> v;
    public static Function<Double, Double> getEasingMethod() {
        return easingMethod;
    }

    public static long getScrollDuration() {
        return 400;
    }

    public static float getScrollStep() {
        return 50;
    }

    public static float getBounceBackMultiplier() {
        return 0.20f;
    }

    public static float handleScrollingPosition(float[] target, float scroll, float maxScroll, float delta, double start, double duration) {
        if (getBounceBackMultiplier() >= 0) {
            target[0] = clamp(target[0], maxScroll);
            if (target[0] < 0) {
                target[0] -= target[0] * (1 - getBounceBackMultiplier()) * delta / 3;
            } else if (target[0] > maxScroll) {
                target[0] = (target[0] - maxScroll) * (1 - (1 - getBounceBackMultiplier()) * delta / 3) + maxScroll;
            }
        } else
            target[0] = clamp(target[0], maxScroll, 0);
        if (!Precision.almostEquals(scroll, target[0], Precision.FLOAT_EPSILON))
            return expoEase(scroll, target[0], Math.min((System.currentTimeMillis() - start) / duration * delta * 3, 1));
        else
            return target[0];
    }

    public static float expoEase(float start, float end, double amount) {
        return start + (end - start) * getEasingMethod().apply(amount).floatValue();
    }

    public static double clamp(double v, double maxScroll) {
        return clamp(v, maxScroll, 300);
    }

    public static double clamp(double v, double maxScroll, double clampExtension) {
        return MathHelper.clamp(v, -clampExtension, maxScroll + clampExtension);
    }

    public static float clamp(float v, float maxScroll) {
        return clamp(v, maxScroll, 300);
    }

    public static float clamp(float v, float maxScroll, float clampExtension) {
        return MathHelper.clamp(v, -clampExtension, maxScroll + clampExtension);
    }

    private static class Precision {
        public static final float FLOAT_EPSILON = 1e-3f;
        public static final double DOUBLE_EPSILON = 1e-7;

        public static boolean almostEquals(float value1, float value2, float acceptableDifference) {
            return Math.abs(value1 - value2) <= acceptableDifference;
        }

        public static boolean almostEquals(double value1, double value2, double acceptableDifference) {
            return Math.abs(value1 - value2) <= acceptableDifference;
        }
    }
}