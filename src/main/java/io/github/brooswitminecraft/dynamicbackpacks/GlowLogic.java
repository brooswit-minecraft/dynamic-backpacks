package io.github.brooswitminecraft.dynamicbackpacks;

/**
 * Pure refresh-cadence check for the Glow Pack. Refreshing before the
 * previously applied duration runs out (duration longer than the interval)
 * is what prevents the Glowing effect from visibly flickering off.
 */
public final class GlowLogic {
    private GlowLogic() {
    }

    public static boolean isRefreshDue(int ticksSinceLastRefresh, int refreshIntervalTicks) {
        return ticksSinceLastRefresh >= refreshIntervalTicks;
    }

    public static boolean overlapsWithoutFlicker(int effectDurationTicks, int refreshIntervalTicks) {
        return effectDurationTicks > refreshIntervalTicks;
    }
}
