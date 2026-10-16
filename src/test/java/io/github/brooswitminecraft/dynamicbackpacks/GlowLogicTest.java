package io.github.brooswitminecraft.dynamicbackpacks;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class GlowLogicTest {
    @Test
    void refreshIsNotDueBeforeTheInterval() {
        assertFalse(GlowLogic.isRefreshDue(99, 100));
    }

    @Test
    void refreshIsDueAtTheInterval() {
        assertTrue(GlowLogic.isRefreshDue(100, 100));
    }

    @Test
    void longerDurationThanIntervalAvoidsFlicker() {
        assertTrue(GlowLogic.overlapsWithoutFlicker(200, 100));
    }

    @Test
    void durationShorterThanOrEqualToIntervalWouldFlicker() {
        assertFalse(GlowLogic.overlapsWithoutFlicker(100, 100));
        assertFalse(GlowLogic.overlapsWithoutFlicker(50, 100));
    }
}
