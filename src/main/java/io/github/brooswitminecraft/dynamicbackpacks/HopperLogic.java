package io.github.brooswitminecraft.dynamicbackpacks;

/**
 * Pure slot-selection/cooldown state for the Hopper Pack, free of Minecraft
 * types so it can be unit tested. Mirrors a vanilla hopper's cadence: one
 * transfer attempt per interval, never more.
 */
public final class HopperLogic {
    private final int intervalTicks;
    private int elapsedTicks;
    private int nextSlot;

    public HopperLogic(int intervalTicks) {
        this.intervalTicks = intervalTicks;
    }

    /** Advances one tick; returns true exactly when a transfer attempt is due. */
    public boolean tick() {
        elapsedTicks++;
        if (elapsedTicks < intervalTicks) {
            return false;
        }
        elapsedTicks = 0;
        return true;
    }

    /**
     * Round-robin starting slot for this attempt's scan, within
     * {@code mainSlots}: each due attempt starts one slot further on so a
     * single stuck/unacceptable stack cannot starve the rest of the
     * inventory.
     */
    public int nextStartSlot(int mainSlots) {
        if (mainSlots <= 0) {
            return 0;
        }
        int start = nextSlot % mainSlots;
        nextSlot = (nextSlot + 1) % mainSlots;
        return start;
    }
}
