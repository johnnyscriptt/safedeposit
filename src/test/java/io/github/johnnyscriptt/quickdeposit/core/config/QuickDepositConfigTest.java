package io.github.johnnyscriptt.quickdeposit.core.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.johnnyscriptt.quickdeposit.core.LockedSlots;
import org.junit.jupiter.api.Test;

class QuickDepositConfigTest {

    @Test
    void defaultsToNoLocks() {
        assertEquals(LockedSlots.NONE, new QuickDepositConfig().locked());
    }

    @Test
    void clampStripsBitsBeyondTheInventory() {
        QuickDepositConfig config = new QuickDepositConfig();
        config.lockedMask = -1L;
        config.clamp();
        assertEquals((1L << 36) - 1, config.lockedMask);
    }

    @Test
    void toggleUpdatesTheStoredMask() {
        QuickDepositConfig config = new QuickDepositConfig();
        config.toggle(3);
        assertTrue(config.locked().isLocked(3));
        assertEquals(1L << 3, config.lockedMask);
    }
}
