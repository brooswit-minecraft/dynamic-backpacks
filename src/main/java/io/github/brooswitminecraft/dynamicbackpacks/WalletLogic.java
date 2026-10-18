package io.github.brooswitminecraft.dynamicbackpacks;

import java.util.Arrays;

/**
 * Pure emerald-wallet consolidation rules, free of Minecraft types so they
 * can be unit tested. Operates over a caller-supplied slot range (the main
 * inventory including the hotbar). Eligible slots are {@link SlotKind#EMPTY},
 * {@link SlotKind#EMERALD} or {@link SlotKind#BLOCK}; {@link SlotKind#OTHER}
 * slots (any other item) are never touched or counted toward total value.
 */
public final class WalletLogic {
    /** Loose emeralds per emerald block, matching the vanilla crafting ratio. */
    public static final int EMERALDS_PER_BLOCK = 9;
    /** Lower bound of the stable loose-emerald reserve band. */
    public static final int RESERVE_MIN = 56;
    /** Upper bound of the stable loose-emerald reserve band; also the vanilla max stack size for both items. */
    public static final int RESERVE_MAX = 64;
    private static final int MAX_STACK_SIZE = 64;

    private WalletLogic() {
    }

    public enum SlotKind { EMPTY, EMERALD, BLOCK, OTHER }

    /** How many loose emeralds and how many blocks the wallet should settle on. */
    public record Target(long loose, long blocks) {
    }

    /** A new slot arrangement reaching the target, to be applied in place of the current one. */
    public record Plan(SlotKind[] kinds, long[] counts) {
    }

    /**
     * Splits a total emerald value (loose + 9*blocks) between loose emeralds
     * and blocks. Below the reserve floor there isn't enough value to fill
     * the band even by unpacking every block, so everything stays/becomes
     * loose. At or above the floor, the target is the one loose count in
     * [56,64] congruent to the total value mod 9 - the same target no matter
     * how the value currently happens to be split between loose and blocks,
     * which is what makes repeated passes idempotent.
     */
    public static Target computeTarget(long totalValue) {
        if (totalValue < RESERVE_MIN) {
            return new Target(totalValue, 0);
        }
        long looseTarget = RESERVE_MIN + ((totalValue - RESERVE_MIN) % EMERALDS_PER_BLOCK);
        long blocksTarget = (totalValue - looseTarget) / EMERALDS_PER_BLOCK;
        return new Target(looseTarget, blocksTarget);
    }

    /** Number of 1..64-sized stacks needed to hold {@code count} items of one kind. */
    public static int stacksNeeded(long count) {
        return count <= 0 ? 0 : (int) ((count + MAX_STACK_SIZE - 1) / MAX_STACK_SIZE);
    }

    /**
     * Plans a new arrangement of the given eligible slots that reaches the
     * stable target for their combined value. Each required stack prefers a
     * slot that already holds that same kind (the loose reserve additionally
     * preferring {@code preferredLooseSlot} when that slot already holds
     * loose emeralds, never one that would have to convert a block or empty
     * slot just to honor the preference), falling back to an empty slot and
     * only then to any other eligible slot. This keeps an already-stable
     * layout untouched - e.g. a block sitting in the selected hotbar slot is
     * never displaced by the loose reserve - rather than shuffling slots
     * whenever the total happens to be reachable a different way. Returns
     * {@code null} when the target is already met (a no-op, preserving
     * idempotence) or when the target's stacks do not fit in the number of
     * EMPTY/EMERALD/BLOCK slots available - OTHER slots are never touched,
     * so a conversion that would need more of them is deferred rather than
     * applied.
     */
    public static Plan plan(SlotKind[] kinds, long[] counts, int preferredLooseSlot) {
        int n = kinds.length;
        long totalLoose = 0;
        long totalBlocks = 0;
        for (int i = 0; i < n; i++) {
            if (kinds[i] == SlotKind.EMERALD) {
                totalLoose += counts[i];
            } else if (kinds[i] == SlotKind.BLOCK) {
                totalBlocks += counts[i];
            }
        }
        long totalValue = totalLoose + (long) EMERALDS_PER_BLOCK * totalBlocks;
        Target target = computeTarget(totalValue);
        if (totalLoose == target.loose() && totalBlocks == target.blocks()) {
            // Totals already match the target split: no conversion is needed, so the layout is left
            // alone even if blocks (or loose emeralds) happen to sit in more than one stack - merging
            // same-kind stacks that need no conversion is not this wallet's job.
            return null;
        }
        long[] neededStacks = buildStackList(target);

        int available = 0;
        for (SlotKind kind : kinds) {
            if (kind != SlotKind.OTHER) {
                available++;
            }
        }
        if (neededStacks.length > available) {
            return null;
        }

        SlotKind[] newKinds = new SlotKind[n];
        long[] newCounts = new long[n];
        for (int i = 0; i < n; i++) {
            if (kinds[i] == SlotKind.OTHER) {
                newKinds[i] = SlotKind.OTHER;
                newCounts[i] = counts[i];
            } else {
                newKinds[i] = SlotKind.EMPTY;
                newCounts[i] = 0;
            }
        }

        boolean[] used = new boolean[n];
        int idx = 0;
        if (target.loose() > 0) {
            int slot = (preferredLooseSlot >= 0 && preferredLooseSlot < n && kinds[preferredLooseSlot] == SlotKind.EMERALD)
                    ? preferredLooseSlot
                    : pickSlotFor(kinds, used, SlotKind.EMERALD);
            newKinds[slot] = SlotKind.EMERALD;
            newCounts[slot] = neededStacks[0];
            used[slot] = true;
            idx = 1;
        }
        for (; idx < neededStacks.length; idx++) {
            int slot = pickSlotFor(kinds, used, SlotKind.BLOCK);
            newKinds[slot] = SlotKind.BLOCK;
            newCounts[slot] = neededStacks[idx];
            used[slot] = true;
        }

        if (Arrays.equals(newKinds, kinds) && Arrays.equals(newCounts, counts)) {
            return null;
        }
        return new Plan(newKinds, newCounts);
    }

    /**
     * Picks a slot for a required stack of {@code want}: a not-yet-used slot
     * already holding that kind first, an empty slot next, and only as a
     * last resort any other not-yet-used eligible slot (the caller has
     * already verified enough eligible slots exist in total).
     */
    private static int pickSlotFor(SlotKind[] kinds, boolean[] used, SlotKind want) {
        int fallbackEmpty = -1;
        int fallbackAny = -1;
        for (int i = 0; i < kinds.length; i++) {
            if (kinds[i] == SlotKind.OTHER || used[i]) {
                continue;
            }
            if (kinds[i] == want) {
                return i;
            }
            if (kinds[i] == SlotKind.EMPTY && fallbackEmpty < 0) {
                fallbackEmpty = i;
            }
            if (fallbackAny < 0) {
                fallbackAny = i;
            }
        }
        if (fallbackEmpty >= 0) {
            return fallbackEmpty;
        }
        if (fallbackAny >= 0) {
            return fallbackAny;
        }
        throw new IllegalStateException("caller already verified enough eligible slots");
    }

    private static long[] buildStackList(Target target) {
        int blockStacks = stacksNeeded(target.blocks());
        long[] result = new long[(target.loose() > 0 ? 1 : 0) + blockStacks];
        int i = 0;
        if (target.loose() > 0) {
            result[i++] = target.loose();
        }
        long remaining = target.blocks();
        for (; i < result.length; i++) {
            long take = Math.min(MAX_STACK_SIZE, remaining);
            result[i] = take;
            remaining -= take;
        }
        return result;
    }
}
