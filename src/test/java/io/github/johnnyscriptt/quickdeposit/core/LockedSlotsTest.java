package io.github.johnnyscriptt.quickdeposit.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class LockedSlotsTest {

    @Test
    void nothingIsLockedByDefault() {
        for (int i = 0; i < LockedSlots.SLOT_COUNT; i++) {
            assertFalse(LockedSlots.NONE.isLocked(i));
        }
    }

    @Test
    void toggleLocksThenUnlocks() {
        LockedSlots locked = LockedSlots.NONE.toggle(5);
        assertTrue(locked.isLocked(5));
        assertFalse(locked.isLocked(4));
        assertFalse(locked.toggle(5).isLocked(5));
    }

    @Test
    void firstAndLastSlotsWork() {
        LockedSlots locked = LockedSlots.NONE.toggle(0).toggle(35);
        assertTrue(locked.isLocked(0));
        assertTrue(locked.isLocked(35));
    }

    @Test
    void outOfRangeIndicesAreIgnored() {
        assertEquals(LockedSlots.NONE, LockedSlots.NONE.toggle(-1).toggle(36).toggle(99));
        assertFalse(LockedSlots.NONE.toggle(36).isLocked(36));
        assertFalse(LockedSlots.NONE.isLocked(-1));
    }

    @Test
    void roundTripsThroughLong() {
        LockedSlots locked = LockedSlots.NONE.toggle(0).toggle(8).toggle(35);
        assertEquals(locked, LockedSlots.fromLong(locked.toLong()));
    }

    @Test
    void fromLongStripsBitsAboveSlot35() {
        LockedSlots locked = LockedSlots.fromLong(-1L);
        assertEquals((1L << 36) - 1, locked.toLong());
        assertFalse(locked.isLocked(36));
    }

    @Test
    void countsLockedSlots() {
        assertEquals(0, LockedSlots.NONE.count());
        assertEquals(3, LockedSlots.NONE.toggle(1).toggle(2).toggle(30).count());
    }
}
