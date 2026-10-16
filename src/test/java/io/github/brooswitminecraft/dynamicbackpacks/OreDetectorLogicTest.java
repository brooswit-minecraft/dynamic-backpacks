package io.github.brooswitminecraft.dynamicbackpacks;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class OreDetectorLogicTest {
    @Test
    void noSearchIsDueBeforeTheIntervalElapses() {
        OreDetectorLogic logic = new OreDetectorLogic(20, 8, 8);
        for (int i = 0; i < 19; i++) {
            assertFalse(logic.tick());
        }
    }

    @Test
    void aSearchIsDueOnTheIntervalTick() {
        OreDetectorLogic logic = new OreDetectorLogic(20, 8, 8);
        for (int i = 0; i < 19; i++) {
            logic.tick();
        }
        assertTrue(logic.tick());
    }

    @Test
    void startsAtTheBaseRadius() {
        OreDetectorLogic logic = new OreDetectorLogic(20, 8, 8);
        assertEquals(8, logic.radius());
    }

    @Test
    void expandingStepsUpByTheConfiguredAmountEachTime() {
        OreDetectorLogic logic = new OreDetectorLogic(20, 8, 8);
        logic.expand();
        assertEquals(16, logic.radius());
        logic.expand();
        assertEquals(24, logic.radius());
    }

    @Test
    void foundOreResetsBackToTheBaseRadius() {
        OreDetectorLogic logic = new OreDetectorLogic(20, 8, 8);
        logic.expand();
        logic.expand();
        logic.reset();
        assertEquals(8, logic.radius());
    }
}
