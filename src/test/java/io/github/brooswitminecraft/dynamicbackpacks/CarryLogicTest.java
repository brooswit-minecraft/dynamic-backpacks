package io.github.brooswitminecraft.dynamicbackpacks;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import io.github.brooswitminecraft.dynamicbackpacks.CarryLogic.Protection;
import io.github.brooswitminecraft.dynamicbackpacks.CarryLogic.SpillClock;

class CarryLogicTest {
    @Test
    void oneArmorPointIsOneSafeSlot() {
        assertEquals(0, CarryLogic.safeSlots(0, Protection.NONE, 27));
        assertEquals(7, CarryLogic.safeSlots(7, Protection.NONE, 27));
        assertEquals(20, CarryLogic.safeSlots(20, Protection.NONE, 27));
    }

    @Test
    void armorCapacityIsCappedAtTheInventory() {
        assertEquals(27, CarryLogic.safeSlots(40, Protection.NONE, 27));
    }

    @Test
    void negativeArmorIsTreatedAsZero() {
        assertEquals(0, CarryLogic.safeSlots(-3, Protection.NONE, 27));
    }

    @Test
    void fullPackProtectsEverythingRegardlessOfArmor() {
        assertEquals(27, CarryLogic.safeSlots(0, Protection.FULL, 27));
        assertEquals(27, CarryLogic.safeSlots(20, Protection.FULL, 27));
    }

    @Test
    void halfPackGuaranteesHalfRoundedDown() {
        assertEquals(13, CarryLogic.safeSlots(0, Protection.HALF, 27));
        assertEquals(13, CarryLogic.safeSlots(5, Protection.HALF, 27));
    }

    @Test
    void halfPackNeverWorseThanArmorAlone() {
        assertEquals(18, CarryLogic.safeSlots(18, Protection.HALF, 27));
        assertEquals(27, CarryLogic.safeSlots(50, Protection.HALF, 27));
    }

    @Test
    void overCapacityOnlyWhenStrictlyMoreSlotsThanSafe() {
        assertFalse(CarryLogic.isOverCapacity(10, 10));
        assertFalse(CarryLogic.isOverCapacity(0, 0));
        assertTrue(CarryLogic.isOverCapacity(11, 10));
    }

    @Test
    void pickSlotDelegatesToTheRandomSourceWithTheSlotCount() {
        assertEquals(26, CarryLogic.pickSlot(27, bound -> bound - 1));
        assertEquals(0, CarryLogic.pickSlot(27, bound -> 0));
    }

    @Test
    void standingStillNeverProducesChecks() {
        SpillClock clock = new SpillClock();
        for (int i = 0; i < 1000; i++) {
            assertEquals(0, clock.advance(0, 4, 4));
        }
        assertEquals(0, clock.accumulated());
    }

    @Test
    void distanceAccumulatesAcrossTicksIntoWholeChecks() {
        SpillClock clock = new SpillClock();
        int total = 0;
        for (int i = 0; i < 10; i++) {
            total += clock.advance(0.5, 4, 4);
        }
        assertEquals(1, total);
        assertEquals(1.0, clock.accumulated(), 1e-9);
    }

    @Test
    void fasterMovementGetsMoreChecks() {
        SpillClock walking = new SpillClock();
        SpillClock sprinting = new SpillClock();
        int walk = 0;
        int sprint = 0;
        for (int i = 0; i < 100; i++) {
            walk += walking.advance(0.25, 4, 4);
            sprint += sprinting.advance(0.75, 4, 4);
        }
        assertTrue(sprint > walk);
        assertEquals(6, walk);
        assertEquals(18, sprint);
    }

    @Test
    void checksPerAdvanceAreCapped() {
        SpillClock clock = new SpillClock();
        assertEquals(2, clock.advance(100, 4, 2));
        assertTrue(clock.accumulated() < 4);
    }

    @Test
    void resetDropsBankedDistance() {
        SpillClock clock = new SpillClock();
        clock.advance(3.9, 4, 4);
        clock.reset();
        assertEquals(0, clock.advance(0.2, 4, 4));
    }

    @Test
    void nonPositiveSettingsDisableChecks() {
        SpillClock clock = new SpillClock();
        assertEquals(0, clock.advance(10, 0, 4));
        assertEquals(0, clock.advance(10, 4, 0));
        assertEquals(0, clock.advance(-5, 4, 4));
    }
}
