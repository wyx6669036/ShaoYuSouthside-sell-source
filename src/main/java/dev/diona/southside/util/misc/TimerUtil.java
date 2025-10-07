package dev.diona.southside.util.misc;

public class TimerUtil {
    public long lastMS;
    public long lastMS_new = System.currentTimeMillis();

    public long getCurrentMS() {
        return System.nanoTime() / 1000000L;
    }

    public boolean hasReached(double milliseconds) {
        if (milliseconds == 0) {
            return true;
        }
        return (double) (this.getCurrentMS() - this.lastMS) >= milliseconds;
    }

    public void reset() {
        this.lastMS = this.getCurrentMS();
    }

    public long passed() {
        return this.getCurrentMS() - this.lastMS;
    }

    public long getTime() {
        return System.nanoTime() / 1000000L;
    }

    public TimerUtil delay(int delay) {
        this.lastMS = this.getCurrentMS() + delay;
        return this;
    }

    public boolean hasTimeElapsed(long time) {
        return System.currentTimeMillis() - this.lastMS > time;
    }

    public void reset_new() {
        this.lastMS_new = System.currentTimeMillis();
    }

    public boolean hasTimeElapsed(long time, boolean reset) {
        if (System.currentTimeMillis() - lastMS_new > time) {
            if (reset) reset_new();
            return true;
        }

        return false;
    }

    public void setTime(long time) {
        this.lastMS = time;
    }
}