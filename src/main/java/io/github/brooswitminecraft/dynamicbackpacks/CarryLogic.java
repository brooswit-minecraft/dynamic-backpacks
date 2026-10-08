package io.github.brooswitminecraft.dynamicbackpacks;

import java.util.function.IntUnaryOperator;

/**
 * Pure carrying-capacity rules, free of Minecraft types so they can be unit
 * tested. "Inventory" here means the main inventory only (the 27 slots above
 * the hotbar); the hotbar is never counted or spilled.
 */
public final class CarryLogic {
    /** Slots in the player's main inventory (3 rows of 9, excluding the hotbar). */
    public static final int MAIN_SLOTS = 27;

    private CarryLogic() {
    }

    /** How much of the main inventory a worn chest-slot item protects. */
    public enum Protection {
        /** Not a backpack: only armor points protect. */
        NONE,
        /** Stick Pack: half of the main inventory is guaranteed safe. */
        HALF,
        /** Leather Backpack and up: the whole main inventory is safe. */
        FULL
    }

    /**
     * Number of occupied main-inventory slots that can be carried without any
     * spill risk. One armor point is one safe slot; a backpack replaces the
     * chest armor and contributes no armor itself, so the armor value passed in
     * comes from the other pieces. A half-protection pack guarantees at least
     * half the slots, rounded down, and is never worse than armor alone.
     */
    public static int safeSlots(int armorPoints, Protection protection, int mainSlots) {
        int armor = Math.max(0, armorPoints);
        return switch (protection) {
            case FULL -> mainSlots;
            case HALF -> Math.min(mainSlots, Math.max(armor, mainSlots / 2));
            case NONE -> Math.min(mainSlots, armor);
        };
    }

    public static boolean isOverCapacity(int occupiedSlots, int safeSlots) {
        return occupiedSlots > safeSlots;
    }

    /**
     * Picks the main-inventory slot index (0 until mainSlots) a spill check
     * targets. Every slot is eligible, empty ones included, so a check on a
     * mostly empty inventory usually does nothing.
     */
    public static int pickSlot(int mainSlots, IntUnaryOperator randomBelow) {
        return randomBelow.applyAsInt(mainSlots);
    }

    /**
     * Turns distance traveled into whole spill checks. Standing still adds
     * nothing, so it never spills; moving faster covers more distance per tick
     * and so gets more checks.
     */
    public static final class SpillClock {
        private double accumulated;

        /**
         * Adds distance and returns how many checks are now due, at most
         * maxChecks (the remainder beyond that is discarded rather than banked).
         */
        public int advance(double distance, double blocksPerCheck, int maxChecks) {
            if (distance <= 0 || blocksPerCheck <= 0 || maxChecks <= 0) {
                return 0;
            }
            accumulated += distance;
            int due = (int) (accumulated / blocksPerCheck);
            accumulated -= due * blocksPerCheck;
            return Math.min(due, maxChecks);
        }

        /** Forgets banked distance, e.g. while not overloaded. */
        public void reset() {
            accumulated = 0;
        }

        public double accumulated() {
            return accumulated;
        }
    }
}
