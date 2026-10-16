package io.github.brooswitminecraft.dynamicbackpacks;

/**
 * Pure expanding-radius search state for the Emerald Backpack. The caller
 * runs a block search each time {@link #tick} says one is due and reports
 * back whether it found anything, so this class stays free of Minecraft
 * types and is unit testable.
 */
public final class OreDetectorLogic {
    private final int intervalTicks;
    private final int baseRadius;
    private final int radiusStep;

    private int elapsedTicks;
    private int radius;

    public OreDetectorLogic(int intervalTicks, int baseRadius, int radiusStep) {
        this.intervalTicks = intervalTicks;
        this.baseRadius = baseRadius;
        this.radiusStep = radiusStep;
        this.radius = baseRadius;
    }

    /** Current search radius a due search should use. */
    public int radius() {
        return radius;
    }

    /**
     * Advances one tick. Returns true exactly when a search is due this
     * tick, at the radius returned by {@link #radius()} at the time of
     * the call.
     */
    public boolean tick() {
        elapsedTicks++;
        if (elapsedTicks < intervalTicks) {
            return false;
        }
        elapsedTicks = 0;
        return true;
    }

    /** Call after a due search found nothing: expand for the next attempt. */
    public void expand() {
        radius += radiusStep;
    }

    /** Call after a due search found ore: ding and start over at the base radius. */
    public void reset() {
        radius = baseRadius;
        elapsedTicks = 0;
    }
}
