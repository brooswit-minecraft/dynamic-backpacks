package io.github.brooswitminecraft.dynamicbackpacks;

/**
 * Pure arithmetic mapping between a linear sample index and a 3D offset
 * within a {@code (2*radius+1)^3} cube centered on the origin, free of
 * Minecraft types so it can be unit tested directly as the production
 * code path (not a stand-in for it).
 *
 * <p>This exists so a search volume never needs to be materialized as a
 * list: {@link #volumeSize} is a closed-form count, and
 * {@link #offsetForIndex} computes a single offset in constant time and
 * space, with no allocation and no up-front iteration over the volume.
 */
public final class CubeIndexing {
    private CubeIndexing() {
    }

    /** Side length of the cube a radius's indices range over. */
    public static int sideLength(int radius) {
        return 2 * radius + 1;
    }

    /** Total indices in the cube; most will fail {@link #isWithinSphere} and so never need a read. */
    public static int volumeSize(int radius) {
        int side = sideLength(radius);
        return side * side * side;
    }

    /**
     * The {dx, dy, dz} offset for {@code index}, where {@code index} ranges
     * over {@code [0, volumeSize(radius))}. Computed by pure arithmetic: no
     * allocation beyond the returned array, and no dependency on any other
     * index having been computed first.
     */
    public static int[] offsetForIndex(int radius, int index) {
        int side = sideLength(radius);
        int dz = index % side - radius;
        int rem = index / side;
        int dy = rem % side - radius;
        int dx = rem / side - radius;
        return new int[] {dx, dy, dz};
    }

    /** Whether an offset falls within the sphere of the given radius, not just its bounding cube. */
    public static boolean isWithinSphere(int dx, int dy, int dz, int radius) {
        return dx * dx + dy * dy + dz * dz <= radius * radius;
    }
}
