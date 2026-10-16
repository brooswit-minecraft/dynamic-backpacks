package io.github.brooswitminecraft.dynamicbackpacks;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

class CubeIndexingTest {
    @Test
    void volumeSizeIsTheClosedFormCubeCountWithNoIteration() {
        assertEquals(1, CubeIndexing.volumeSize(0));
        assertEquals(27, CubeIndexing.volumeSize(1));
        assertEquals((2 * 8 + 1) * (2 * 8 + 1) * (2 * 8 + 1), CubeIndexing.volumeSize(8));
    }

    @Test
    void volumeSizeStaysCheapEvenAtTheLargestAllowedRadius() {
        assertEquals((2 * 128 + 1) * (2 * 128 + 1) * (2 * 128 + 1), CubeIndexing.volumeSize(128));
    }

    @Test
    void offsetForIndexZeroIsTheMinimumCorner() {
        assertArrayEquals(new int[] {-1, -1, -1}, CubeIndexing.offsetForIndex(1, 0));
    }

    @Test
    void offsetForIndexCentersOnTheOriginAtTheMiddleIndex() {
        int radius = 1;
        int centerIndex = CubeIndexing.volumeSize(radius) / 2;
        assertArrayEquals(new int[] {0, 0, 0}, CubeIndexing.offsetForIndex(radius, centerIndex));
    }

    @Test
    void offsetForIndexCoversEveryDistinctPositionInTheCubeExactlyOnce() {
        int radius = 2;
        int volume = CubeIndexing.volumeSize(radius);
        Set<String> seen = new HashSet<>();
        for (int i = 0; i < volume; i++) {
            int[] offset = CubeIndexing.offsetForIndex(radius, i);
            assertTrue(seen.add(offset[0] + "," + offset[1] + "," + offset[2]));
        }
        assertEquals(volume, seen.size());
    }

    @Test
    void isWithinSphereAcceptsTheOriginAndRejectsACubeCorner() {
        assertTrue(CubeIndexing.isWithinSphere(0, 0, 0, 4));
        assertFalse(CubeIndexing.isWithinSphere(4, 4, 4, 4));
    }

    @Test
    void isWithinSphereAcceptsExactlyOnTheRadiusBoundary() {
        assertTrue(CubeIndexing.isWithinSphere(4, 0, 0, 4));
    }
}
