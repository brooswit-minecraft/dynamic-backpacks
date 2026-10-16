package io.github.brooswitminecraft.dynamicbackpacks;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.function.IntFunction;

import org.junit.jupiter.api.Test;

import io.github.brooswitminecraft.dynamicbackpacks.OreDetectorLogic.SampleResult;

class OreDetectorLogicTest {
    private static IntFunction<SampleResult> allNotFound() {
        return i -> SampleResult.NOT_FOUND;
    }

    @Test
    void startsAtTheBaseRadius() {
        OreDetectorLogic logic = new OreDetectorLogic(4, 8, 8, 40);
        assertEquals(8, logic.radius());
    }

    @Test
    void onlySamplesUpToTheReadsPerTickBudgetInOneCall() {
        int[] reads = {0};
        IntFunction<SampleResult> countingSampler = i -> {
            reads[0]++;
            return SampleResult.NOT_FOUND;
        };
        OreDetectorLogic logic = new OreDetectorLogic(4, 8, 8, 40);
        logic.tick(1000, countingSampler);
        assertEquals(4, reads[0]);
    }

    @Test
    void neverSamplesPastTheVolumeSizeEvenWithBudgetLeft() {
        int[] reads = {0};
        IntFunction<SampleResult> countingSampler = i -> {
            reads[0]++;
            return SampleResult.NOT_FOUND;
        };
        OreDetectorLogic logic = new OreDetectorLogic(100, 8, 8, 40);
        logic.tick(5, countingSampler);
        assertEquals(5, reads[0]);
    }

    @Test
    void resumesTheCursorAcrossTicksRatherThanRescanningFromTheStart() {
        List<Integer> seen = new java.util.ArrayList<>();
        IntFunction<SampleResult> sampler = i -> {
            seen.add(i);
            return SampleResult.NOT_FOUND;
        };
        OreDetectorLogic logic = new OreDetectorLogic(3, 8, 8, 40);
        logic.tick(9, sampler);
        logic.tick(9, sampler);
        assertEquals(List.of(0, 1, 2, 3, 4, 5), seen);
    }

    @Test
    void unavailablePositionsAreSkippedWithoutBeingTreatedAsAHit() {
        IntFunction<SampleResult> sampler = i -> SampleResult.UNAVAILABLE;
        OreDetectorLogic logic = new OreDetectorLogic(4, 8, 8, 40);
        assertFalse(logic.tick(4, sampler));
        assertEquals(8, logic.radius());
    }

    @Test
    void exhaustingTheVolumeWithNoHitExpandsTheRadius() {
        OreDetectorLogic logic = new OreDetectorLogic(10, 8, 8, 40);
        logic.tick(4, allNotFound());
        assertEquals(16, logic.radius());
    }

    @Test
    void radiusGrowthIsCappedAtTheConfiguredMaximum() {
        OreDetectorLogic logic = new OreDetectorLogic(10, 8, 8, 20);
        logic.tick(1, allNotFound());
        assertEquals(16, logic.radius());
        logic.tick(1, allNotFound());
        assertEquals(20, logic.radius());
        logic.tick(1, allNotFound());
        assertEquals(20, logic.radius(), "radius must never grow past the configured maximum");
    }

    @Test
    void doesNotUseAStaleVolumeSizeForTheNewRadiusAfterExpanding() {
        int[] reads = {0};
        IntFunction<SampleResult> countingSampler = i -> {
            reads[0]++;
            return SampleResult.NOT_FOUND;
        };
        OreDetectorLogic logic = new OreDetectorLogic(10, 8, 8, 40);
        logic.tick(4, countingSampler);
        assertEquals(4, reads[0], "expanding should stop sampling for this call rather than continuing against a volume sized for the old radius");
    }

    @Test
    void foundResetsBackToTheBaseRadius() {
        OreDetectorLogic logic = new OreDetectorLogic(10, 8, 8, 40);
        logic.tick(4, allNotFound());
        assertEquals(16, logic.radius());

        IntFunction<SampleResult> hitOnFirst = i -> i == 0 ? SampleResult.FOUND : SampleResult.NOT_FOUND;
        assertTrue(logic.tick(100, hitOnFirst));
        assertEquals(8, logic.radius());
    }

    @Test
    void foundResetsTheCursorSoTheNextVolumeIsScannedFromTheStart() {
        List<Integer> seen = new java.util.ArrayList<>();
        IntFunction<SampleResult> hitOnIndexTwo = i -> {
            if (i == 2) {
                return SampleResult.FOUND;
            }
            return SampleResult.NOT_FOUND;
        };
        OreDetectorLogic logic = new OreDetectorLogic(10, 8, 8, 40);
        assertTrue(logic.tick(9, hitOnIndexTwo));

        IntFunction<SampleResult> recordingSampler = i -> {
            seen.add(i);
            return SampleResult.NOT_FOUND;
        };
        logic.tick(9, recordingSampler);
        assertEquals(0, seen.get(0));
    }
}
