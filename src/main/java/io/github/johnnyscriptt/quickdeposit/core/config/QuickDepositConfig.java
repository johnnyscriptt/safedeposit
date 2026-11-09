package io.github.johnnyscriptt.quickdeposit.core.config;

import io.github.johnnyscriptt.quickdeposit.core.LockedSlots;

/**
 * User settings, Gson-serialized to config/quickdeposit.json by the client layer. Plain
 * mutable fields so Gson needs no adapters; {@link #clamp()} runs after every load and edit
 * so a hand-edited file can't produce nonsense.
 */
public final class QuickDepositConfig {

    /** Locked inventory slots as a {@link LockedSlots} bit mask (bit i = inventory index i). */
    public long lockedMask;

    public LockedSlots locked() {
        return LockedSlots.fromLong(lockedMask);
    }

    public void toggle(int index) {
        lockedMask = locked().toggle(index).toLong();
    }

    public void clamp() {
        lockedMask = locked().toLong();
    }
}
