package io.github.brooswitminecraft.dynamicbackpacks;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class HopperLogicTest {
    @Test
    void noTransferIsDueBeforeTheIntervalElapses() {
        HopperLogic logic = new HopperLogic(8);
        for (int i = 0; i < 7; i++) {
            assertFalse(logic.tick());
        }
    }

    @Test
    void aTransferIsDueOnTheIntervalTick() {
        HopperLogic logic = new HopperLogic(8);
        for (int i = 0; i < 7; i++) {
            logic.tick();
        }
        assertTrue(logic.tick());
    }

    @Test
    void startSlotAdvancesRoundRobinSoOneStuckSlotCannotStarveTheRest() {
        HopperLogic logic = new HopperLogic(8);
        assertEquals(0, logic.nextStartSlot(27));
        assertEquals(1, logic.nextStartSlot(27));
        assertEquals(2, logic.nextStartSlot(27));
    }

    @Test
    void startSlotWrapsAroundTheSlotCount() {
        HopperLogic logic = new HopperLogic(8);
        for (int i = 0; i < 27; i++) {
            logic.nextStartSlot(27);
        }
        assertEquals(0, logic.nextStartSlot(27));
    }
}
