package io.github.brooswitminecraft.dynamicbackpacks;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import io.github.brooswitminecraft.dynamicbackpacks.WalletLogic.Plan;
import io.github.brooswitminecraft.dynamicbackpacks.WalletLogic.SlotKind;
import io.github.brooswitminecraft.dynamicbackpacks.WalletLogic.Target;

class WalletLogicTest {
    private static SlotKind[] kinds(int n) {
        SlotKind[] kinds = new SlotKind[n];
        java.util.Arrays.fill(kinds, SlotKind.EMPTY);
        return kinds;
    }

    // --- computeTarget: the ticket's own acceptance checks ---

    @Test
    void fiftyFivePlusOneBlockBecomesSixtyFourLoose() {
        Target target = WalletLogic.computeTarget(55 + 9);
        assertEquals(64, target.loose());
        assertEquals(0, target.blocks());
    }

    @Test
    void fiftyFiveAloneIsUnchanged() {
        Target target = WalletLogic.computeTarget(55);
        assertEquals(55, target.loose());
        assertEquals(0, target.blocks());
    }

    @Test
    void fiftySixLooseIsUnchanged() {
        Target target = WalletLogic.computeTarget(56);
        assertEquals(56, target.loose());
        assertEquals(0, target.blocks());
    }

    @Test
    void sixtyFourLooseIsUnchanged() {
        Target target = WalletLogic.computeTarget(64);
        assertEquals(64, target.loose());
        assertEquals(0, target.blocks());
    }

    @Test
    void sixtyFiveBecomesFiftySixPlusOneBlock() {
        Target target = WalletLogic.computeTarget(65);
        assertEquals(56, target.loose());
        assertEquals(1, target.blocks());
    }

    @Test
    void zeroWithSufficientBlocksUnpacksToSixtyThreeLoose() {
        // 100 blocks (value 900) is "sufficient" - far more than needed to fill the band.
        Target target = WalletLogic.computeTarget(9 * 100);
        assertEquals(63, target.loose());
        assertEquals(93, target.blocks());
    }

    @Test
    void insufficientValueUnpacksAsFarAsPossibleWithoutLoss() {
        // 3 blocks (value 27) can never reach the 56 floor even fully unpacked.
        Target target = WalletLogic.computeTarget(27);
        assertEquals(27, target.loose());
        assertEquals(0, target.blocks());
    }

    @Test
    void valueIsPreservedAcrossEveryTarget() {
        for (long value = 0; value <= 2000; value++) {
            Target target = WalletLogic.computeTarget(value);
            assertEquals(value, target.loose() + 9L * target.blocks(), "value drifted at " + value);
        }
    }

    @Test
    void looseTargetIsNeverOutsideTheReserveBandOnceValueReachesTheFloor() {
        for (long value = 56; value <= 2000; value++) {
            Target target = WalletLogic.computeTarget(value);
            assertTrue(target.loose() >= 56 && target.loose() <= 64, "loose out of band at " + value);
        }
    }

    // --- plan(): applying the target to actual slots ---

    @Test
    void consolidatesMultiplePartialStacksByTotalLooseCount() {
        SlotKind[] kinds = kinds(36);
        long[] counts = new long[36];
        kinds[0] = SlotKind.EMERALD;
        counts[0] = 30;
        kinds[20] = SlotKind.EMERALD;
        counts[20] = 30; // total loose 60, already in [56,64]: no blocks needed
        kinds[9] = SlotKind.EMERALD;
        counts[9] = 5;

        Plan plan = WalletLogic.plan(kinds, counts, -1);
        assertEqualValue(kinds, counts, plan);
        assertEquals(65, sumByKind(plan, SlotKind.EMERALD) + 9 * sumByKind(plan, SlotKind.BLOCK));
        // 65 total -> target is 56 loose + 1 block.
        assertEquals(56, sumByKind(plan, SlotKind.EMERALD));
        assertEquals(1, sumByKind(plan, SlotKind.BLOCK));
    }

    @Test
    void preservesTheSelectedSlotAsTheLooseReserveWhenFeasible() {
        SlotKind[] kinds = kinds(36);
        long[] counts = new long[36];
        kinds[2] = SlotKind.EMERALD; // hotbar slot 2, selected
        counts[2] = 40;
        kinds[15] = SlotKind.EMERALD;
        counts[15] = 30; // total 70 -> target 61 loose + 1 block

        Plan plan = WalletLogic.plan(kinds, counts, 2);
        assertEquals(SlotKind.EMERALD, plan.kinds()[2]);
        assertEquals(61, plan.counts()[2]);
    }

    @Test
    void doesNotDisplaceABlockFromTheSelectedSlotWhenAlreadyStable() {
        // 60 loose emeralds elsewhere, 1 emerald block sitting in the selected hotbar slot.
        // Total value (60 + 9 = 69) already matches the target split (60 loose, 1 block):
        // the selected slot must stay a block, not get overwritten with the loose reserve.
        SlotKind[] kinds = kinds(36);
        long[] counts = new long[36];
        kinds[9] = SlotKind.EMERALD;
        counts[9] = 60;
        kinds[0] = SlotKind.BLOCK;
        counts[0] = 1;

        assertNull(WalletLogic.plan(kinds, counts, 0), "an already-stable layout must not move the selected block");
    }

    @Test
    void doesNotMergeBlockStacksAcrossSeparateSlotsWhenTotalsAlreadyMatchTheTarget() {
        // slot9 = 60 loose, slot0 = 1 block, selected slot8 = 1 block: totals (60 loose, 2 blocks)
        // already match the target for value 78, even though the 2 blocks sit in two separate
        // stacks rather than one. No conversion is needed, so neither slot should move.
        SlotKind[] kinds = kinds(36);
        long[] counts = new long[36];
        kinds[9] = SlotKind.EMERALD;
        counts[9] = 60;
        kinds[0] = SlotKind.BLOCK;
        counts[0] = 1;
        kinds[8] = SlotKind.BLOCK;
        counts[8] = 1;

        assertNull(WalletLogic.plan(kinds, counts, 8),
                "totals already match the target; splitting blocks across two stacks is not itself a reason to rearrange");
    }

    @Test
    void preferringTheSelectedSlotNeverConvertsABlockOrEmptySlotJustToHonorThePreference() {
        // Selected slot holds a block; total value needs a loose reserve, but it must land
        // on the existing emerald slot rather than displacing the selected block.
        SlotKind[] kinds = kinds(36);
        long[] counts = new long[36];
        kinds[0] = SlotKind.BLOCK;
        counts[0] = 2; // value 18, below the reserve floor together with the loose below
        kinds[9] = SlotKind.EMERALD;
        counts[9] = 50; // total value 68 -> target 59 loose + 1 block

        Plan plan = WalletLogic.plan(kinds, counts, 0);
        assertEqualValue(kinds, counts, plan);
        assertEquals(SlotKind.BLOCK, plan.kinds()[0], "selected slot must stay a block, not become the loose reserve");
        assertEquals(59, sumByKind(plan, SlotKind.EMERALD));
        assertEquals(1, sumByKind(plan, SlotKind.BLOCK));
    }

    @Test
    void fallsBackOffThePreferredSlotWhenItIsIneligible() {
        SlotKind[] kinds = kinds(36);
        long[] counts = new long[36];
        kinds[2] = SlotKind.OTHER; // selected slot holds an unrelated item, e.g. a sword
        kinds[9] = SlotKind.EMERALD;
        counts[9] = 70; // -> target 61 loose + 1 block

        Plan plan = WalletLogic.plan(kinds, counts, 2);
        assertEquals(SlotKind.OTHER, plan.kinds()[2]);
        assertEquals(61, sumByKind(plan, SlotKind.EMERALD));
        assertEquals(1, sumByKind(plan, SlotKind.BLOCK));
    }

    @Test
    void repeatedPlanningAfterNormalizationMakesNoFurtherChanges() {
        SlotKind[] kinds = kinds(36);
        long[] counts = new long[36];
        kinds[0] = SlotKind.EMERALD;
        counts[0] = 13;
        kinds[1] = SlotKind.BLOCK;
        counts[1] = 20;

        Plan first = WalletLogic.plan(kinds, counts, -1);
        assertTrue(first != null, "first pass should normalize");
        Plan second = WalletLogic.plan(first.kinds(), first.counts(), -1);
        assertNull(second, "second pass over an already-normalized wallet must be a no-op");
    }

    @Test
    void doesNothingWhenAlreadyAtTheStableTarget() {
        SlotKind[] kinds = kinds(36);
        long[] counts = new long[36];
        kinds[0] = SlotKind.EMERALD;
        counts[0] = 60;

        assertNull(WalletLogic.plan(kinds, counts, -1));
    }

    @Test
    void otherSlotsAreNeverTouchedOrCountedTowardCapacity() {
        SlotKind[] kinds = kinds(36);
        long[] counts = new long[36];
        for (int i = 0; i < 36; i++) {
            kinds[i] = SlotKind.OTHER;
            counts[i] = 1;
        }
        kinds[0] = SlotKind.EMERALD;
        counts[0] = 70; // needs a second slot for the surplus block, but none is available

        assertNull(WalletLogic.plan(kinds, counts, -1));
    }

    @Test
    void defersAConversionThatWouldNeedMoreEligibleSlotsThanAreAvailable() {
        SlotKind[] kinds = kinds(36);
        long[] counts = new long[36];
        // Exactly one eligible slot, holding a single value that needs two slots once split.
        for (int i = 1; i < 36; i++) {
            kinds[i] = SlotKind.OTHER;
        }
        kinds[0] = SlotKind.BLOCK;
        counts[0] = 64; // value 576 -> target is 63 loose + 57 blocks: needs 2 eligible slots, only 1 is available

        Plan plan = WalletLogic.plan(kinds, counts, -1);
        assertNull(plan, "only one eligible slot exists but the target needs two");
    }

    @Test
    void largeBlockCountsSpanMultipleSixtyFourStacks() {
        SlotKind[] kinds = kinds(36);
        long[] counts = new long[36];
        kinds[0] = SlotKind.BLOCK;
        counts[0] = 200; // value 1800

        Plan plan = WalletLogic.plan(kinds, counts, -1);
        assertEqualValue(kinds, counts, plan);
        long blockTotal = sumByKind(plan, SlotKind.BLOCK);
        int blockSlotsUsed = 0;
        for (int i = 0; i < 36; i++) {
            if (plan.kinds()[i] == SlotKind.BLOCK) {
                assertTrue(plan.counts()[i] <= 64);
                blockSlotsUsed++;
            }
        }
        assertEquals(WalletLogic.stacksNeeded(blockTotal), blockSlotsUsed);
    }

    private static long sumByKind(Plan plan, SlotKind kind) {
        long total = 0;
        for (int i = 0; i < plan.kinds().length; i++) {
            if (plan.kinds()[i] == kind) {
                total += plan.counts()[i];
            }
        }
        return total;
    }

    private static void assertEqualValue(SlotKind[] beforeKinds, long[] beforeCounts, Plan plan) {
        long before = 0;
        for (int i = 0; i < beforeKinds.length; i++) {
            if (beforeKinds[i] == SlotKind.EMERALD) {
                before += beforeCounts[i];
            } else if (beforeKinds[i] == SlotKind.BLOCK) {
                before += 9 * beforeCounts[i];
            }
        }
        long after = sumByKind(plan, SlotKind.EMERALD) + 9 * sumByKind(plan, SlotKind.BLOCK);
        assertEquals(before, after, "total emerald value must be preserved");
    }
}
