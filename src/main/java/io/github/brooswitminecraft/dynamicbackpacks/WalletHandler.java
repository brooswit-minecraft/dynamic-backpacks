package io.github.brooswitminecraft.dynamicbackpacks;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/**
 * Always-on emerald wallet: consolidates loose emeralds and emerald blocks
 * in the player's main inventory, including the hotbar, into a stable
 * 56-64 loose reserve, converting surplus into blocks and unpacking blocks
 * to cover a shortfall. Scope is independent of the carry/spill mechanic's
 * hotbar exclusion ({@link CarryLogic#MAIN_SLOTS} is the 27 non-hotbar
 * slots; this wallet also reaches slots 0..8). Runs unconditionally, with
 * no dependency on any backpack being worn or on armor/offhand/other
 * containers, which are outside {@link Inventory#items} entirely.
 */
public final class WalletHandler {
    /** Main inventory including the hotbar: all of Inventory.items (0..8 hotbar, 9..35 main). */
    static final int WALLET_SLOTS = 36;

    private WalletHandler() {
    }

    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        Inventory inventory = player.getInventory();

        WalletLogic.SlotKind[] kinds = new WalletLogic.SlotKind[WALLET_SLOTS];
        long[] counts = new long[WALLET_SLOTS];
        for (int i = 0; i < WALLET_SLOTS; i++) {
            ItemStack stack = inventory.items.get(i);
            // A stack with any non-default data component (a custom name, enchantment, etc.) carries
            // metadata that `new ItemStack(item, count)` cannot reproduce, so it is left untouched
            // rather than merged/split with plain stacks of the same item.
            if (stack.isEmpty()) {
                kinds[i] = WalletLogic.SlotKind.EMPTY;
            } else if (stack.is(Items.EMERALD) && stack.isComponentsPatchEmpty()) {
                kinds[i] = WalletLogic.SlotKind.EMERALD;
                counts[i] = stack.getCount();
            } else if (stack.is(Items.EMERALD_BLOCK) && stack.isComponentsPatchEmpty()) {
                kinds[i] = WalletLogic.SlotKind.BLOCK;
                counts[i] = stack.getCount();
            } else {
                kinds[i] = WalletLogic.SlotKind.OTHER;
            }
        }

        WalletLogic.Plan plan = WalletLogic.plan(kinds, counts, inventory.selected);
        if (plan == null) {
            return;
        }
        for (int i = 0; i < WALLET_SLOTS; i++) {
            if (plan.kinds()[i] == kinds[i] && plan.counts()[i] == counts[i]) {
                continue;
            }
            ItemStack newStack = switch (plan.kinds()[i]) {
                case EMPTY -> ItemStack.EMPTY;
                case EMERALD -> new ItemStack(Items.EMERALD, (int) plan.counts()[i]);
                case BLOCK -> new ItemStack(Items.EMERALD_BLOCK, (int) plan.counts()[i]);
                case OTHER -> inventory.items.get(i);
            };
            inventory.items.set(i, newStack);
        }
    }
}
