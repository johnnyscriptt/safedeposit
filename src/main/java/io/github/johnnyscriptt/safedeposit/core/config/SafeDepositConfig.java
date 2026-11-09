package io.github.johnnyscriptt.safedeposit.core.config;

import io.github.johnnyscriptt.safedeposit.core.LockedSlots;

/**
 * User settings, Gson-serialized to config/safedeposit.json by the client layer. Plain
 * mutable fields so Gson needs no adapters; {@link #clamp()} runs after every load and edit
 * so a hand-edited file can't produce nonsense.
 */
public final class SafeDepositConfig {

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
