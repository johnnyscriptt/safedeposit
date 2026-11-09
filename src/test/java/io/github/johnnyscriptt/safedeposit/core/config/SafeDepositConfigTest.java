package io.github.johnnyscriptt.safedeposit.core.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.johnnyscriptt.safedeposit.core.LockedSlots;
import org.junit.jupiter.api.Test;

class SafeDepositConfigTest {

    @Test
    void defaultsToNoLocks() {
        assertEquals(LockedSlots.NONE, new SafeDepositConfig().locked());
    }

    @Test
    void clampStripsBitsBeyondTheInventory() {
        SafeDepositConfig config = new SafeDepositConfig();
        config.lockedMask = -1L;
        config.clamp();
        assertEquals((1L << 36) - 1, config.lockedMask);
    }

    @Test
    void toggleUpdatesTheStoredMask() {
        SafeDepositConfig config = new SafeDepositConfig();
        config.toggle(3);
        assertTrue(config.locked().isLocked(3));
        assertEquals(1L << 3, config.lockedMask);
    }
}
