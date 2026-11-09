package io.github.johnnyscriptt.safedeposit.core;

/** Server-side limits on deposit requests. */
public final class DepositRules {

    /** 5 ticks = 250 ms between deposits per player. */
    public static final int COOLDOWN_TICKS = 5;
    public static final long NEVER_REQUESTED = Long.MIN_VALUE;

    private DepositRules() {
    }

    public static boolean cooldownOk(long lastTick, long nowTick) {
        if (lastTick == NEVER_REQUESTED || nowTick < lastTick) {
            // A tick counter that went backwards means the server restarted; don't strand
            // the player behind a timestamp that will never elapse.
            return true;
        }
        return nowTick - lastTick >= COOLDOWN_TICKS;
    }
}
