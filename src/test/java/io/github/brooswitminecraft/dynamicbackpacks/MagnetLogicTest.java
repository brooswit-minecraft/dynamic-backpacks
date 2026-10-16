package io.github.brooswitminecraft.dynamicbackpacks;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

class MagnetLogicTest {
    @Test
    void emptyInventoryGetsFullBasePull() {
        assertEquals(1.0, MagnetLogic.availablePull(1.0, 0, 27), 1e-9);
    }

    @Test
    void fullInventoryHasEffectivelyZeroPull() {
        assertEquals(0.0, MagnetLogic.availablePull(1.0, 27, 27), 1e-9);
    }

    @Test
    void pullShrinksLinearlyAsInventoryFills() {
        assertEquals(0.5, MagnetLogic.availablePull(1.0, 13, 26), 1e-9);
    }

    @Test
    void overfullInventoryIsClampedNotNegative() {
        assertEquals(0.0, MagnetLogic.availablePull(1.0, 50, 27), 1e-9);
    }

    @Test
    void zeroBudgetGivesNoSharesAtAll() {
        double[] shares = MagnetLogic.distributePull(0.0, List.of(1.0, 5.0));
        assertEquals(0.0, shares[0]);
        assertEquals(0.0, shares[1]);
    }

    @Test
    void closerItemGetsAStrongerShareThanADistantOne() {
        double[] shares = MagnetLogic.distributePull(1.0, List.of(1.0, 10.0));
        assertTrue(shares[0] > shares[1]);
    }

    @Test
    void sharesAlwaysSumToTheAvailableBudget() {
        double[] shares = MagnetLogic.distributePull(2.0, List.of(1.0, 3.0, 7.0));
        assertEquals(2.0, shares[0] + shares[1] + shares[2], 1e-9);
    }

    @Test
    void aCrowdOfItemsEachGetsOnlyASliverComparedToOneAlone() {
        double[] alone = MagnetLogic.distributePull(1.0, List.of(2.0));
        double[] crowd = MagnetLogic.distributePull(1.0, List.of(2.0, 2.0, 2.0, 2.0, 2.0));
        assertTrue(crowd[0] < alone[0]);
    }
}
