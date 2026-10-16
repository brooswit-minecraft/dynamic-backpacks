package io.github.brooswitminecraft.dynamicbackpacks;

import java.util.function.IntFunction;

/**
 * Pure expanding-radius search state for the Emerald Backpack, free of
 * Minecraft types so it can be unit tested. Scanning a whole cubic/spherical
 * volume synchronously in one server tick is unbounded work for a large
 * radius, so this spreads the scan of one volume across many ticks: each
 * {@link #tick} call samples at most {@code readsPerTick} positions from
 * the volume, resuming a cursor across calls, and the radius itself is
 * capped so the per-radius volume (and therefore the worst case per-tick
 * work) stays bounded even if ore is never found.
 *
 * <p>The caller owns geometry and world access entirely: it decides how
 * many positions exist for the current {@link #radius()} ({@code
 * volumeSize}) and supplies a {@code sampler} that maps a position index to
 * a {@link SampleResult}, applying its own loaded-chunk/build-height guards
 * before ever touching block state.
 */
public final class OreDetectorLogic {
    /** What a single sampled position turned out to be. */
    public enum SampleResult {
        FOUND,
        NOT_FOUND,
        /** Position could not be read (unloaded chunk, outside build height, ...); never counted as a hit. */
        UNAVAILABLE
    }

    private final int readsPerTick;
    private final int baseRadius;
    private final int radiusStep;
    private final int maxRadius;

    private int radius;
    private int cursor;

    public OreDetectorLogic(int readsPerTick, int baseRadius, int radiusStep, int maxRadius) {
        this.readsPerTick = readsPerTick;
        this.baseRadius = baseRadius;
        this.radiusStep = radiusStep;
        this.maxRadius = Math.max(baseRadius, maxRadius);
        this.radius = baseRadius;
    }

    /** Current search radius; the volume the caller should scan against. */
    public int radius() {
        return radius;
    }

    /**
     * Samples up to {@code readsPerTick} positions of the current volume
     * (size {@code volumeSize}, caller-computed for {@link #radius()}),
     * resuming from where the previous call left off.
     *
     * <p>Returns true exactly when a sampled position this call was
     * {@link SampleResult#FOUND}; the radius/cursor have already been reset
     * to the base radius in that case. If the cursor reaches the end of the
     * volume with no hit, the radius expands (capped at the configured
     * maximum) and the cursor restarts at zero for the caller's next call
     * with the new, larger volume size — this method never samples past a
     * volume boundary using a stale {@code volumeSize} for a different
     * radius.
     */
    public boolean tick(int volumeSize, IntFunction<SampleResult> sampler) {
        int budget = readsPerTick;
        while (budget-- > 0) {
            if (cursor >= volumeSize) {
                cursor = 0;
                if (radius < maxRadius) {
                    radius = Math.min(maxRadius, radius + radiusStep);
                }
                return false;
            }
            SampleResult result = sampler.apply(cursor);
            cursor++;
            if (result == SampleResult.FOUND) {
                radius = baseRadius;
                cursor = 0;
                return true;
            }
        }
        return false;
    }
}
