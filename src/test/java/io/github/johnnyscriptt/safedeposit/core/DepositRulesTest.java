package io.github.johnnyscriptt.safedeposit.core;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class DepositRulesTest {

    @Test
    void firstRequestIsAllowed() {
        assertTrue(DepositRules.cooldownOk(DepositRules.NEVER_REQUESTED, 100));
    }

    @Test
    void requestsInsideTheCooldownAreDropped() {
        assertFalse(DepositRules.cooldownOk(100, 100));
        assertFalse(DepositRules.cooldownOk(100, 100 + DepositRules.COOLDOWN_TICKS - 1));
    }

    @Test
    void requestsAfterTheCooldownAreAllowed() {
        assertTrue(DepositRules.cooldownOk(100, 100 + DepositRules.COOLDOWN_TICKS));
    }

    @Test
    void aTickCounterThatWentBackwardsDoesNotStrandThePlayer() {
        assertTrue(DepositRules.cooldownOk(5000, 10));
    }
}
