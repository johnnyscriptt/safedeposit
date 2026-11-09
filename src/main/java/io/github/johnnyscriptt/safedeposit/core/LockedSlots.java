package io.github.johnnyscriptt.safedeposit.core;

/**
 * Which of the 36 player inventory slots (0 to 8 hotbar, 9 to 35 main) Safe Deposit must
 * leave alone, as an immutable bit mask. Armour and offhand are never deposited, so they
 * have no bits. Indices outside 0..35 are ignored everywhere, and {@link #fromLong} drops
 * stray high bits, so a hand-edited config or a crafted packet can't do anything odd.
 */
public record LockedSlots(long mask) {

    public static final int SLOT_COUNT = 36;
    private static final long VALID = (1L << SLOT_COUNT) - 1;

    public static final LockedSlots NONE = new LockedSlots(0L);

    public LockedSlots {
        mask &= VALID;
    }

    public static LockedSlots fromLong(long mask) {
        return new LockedSlots(mask);
    }

    public long toLong() {
        return mask;
    }

    public boolean isLocked(int index) {
        return inRange(index) && (mask & (1L << index)) != 0;
    }

    /** A copy with {@code index} flipped; out-of-range indices return this unchanged. */
    public LockedSlots toggle(int index) {
        return inRange(index) ? new LockedSlots(mask ^ (1L << index)) : this;
    }

    public int count() {
        return Long.bitCount(mask);
    }

    private static boolean inRange(int index) {
        return index >= 0 && index < SLOT_COUNT;
    }
}
