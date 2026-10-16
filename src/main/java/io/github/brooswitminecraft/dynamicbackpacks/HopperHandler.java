package io.github.brooswitminecraft.dynamicbackpacks;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;

/**
 * Hopper Pack: while standing/moving over a block with item storage, acts
 * like a wearable hopper pushing carried items downward into it. Uses the
 * target block's own item-handler capability, so chests/barrels accept
 * general items while machines keep their own insertion rules.
 */
public final class HopperHandler {
    /** Main inventory slots are indices 9..35 of Inventory.items; 0..8 is the hotbar. */
    private static final int MAIN_START = 9;

    private static final Map<UUID, HopperLogic> STATES = new HashMap<>();

    private HopperHandler() {
    }

    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
        if (!(chest.getItem() instanceof BackpackItem pack) || pack.utility() != BackpackItem.Utility.HOPPER) {
            STATES.remove(player.getUUID());
            return;
        }

        HopperLogic state = STATES.computeIfAbsent(player.getUUID(),
                id -> new HopperLogic(BackpackConfig.HOPPER_TRANSFER_INTERVAL_TICKS.get()));
        if (!state.tick()) {
            return;
        }

        BlockPos below = player.blockPosition().below();
        IItemHandler target = player.level().getCapability(Capabilities.ItemHandler.BLOCK, below, Direction.UP);
        if (target == null) {
            return;
        }

        Inventory inventory = player.getInventory();
        int startSlot = state.nextStartSlot(CarryLogic.MAIN_SLOTS);
        for (int offset = 0; offset < CarryLogic.MAIN_SLOTS; offset++) {
            int slot = MAIN_START + (startSlot + offset) % CarryLogic.MAIN_SLOTS;
            ItemStack stack = inventory.items.get(slot);
            if (stack.isEmpty()) {
                continue;
            }
            ItemStack remainder = ItemHandlerHelper.insertItemStacked(target, stack.copy(), false);
            if (remainder.getCount() != stack.getCount()) {
                inventory.items.set(slot, remainder.isEmpty() ? ItemStack.EMPTY : remainder);
                return;
            }
        }
    }

    public static void onLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        STATES.remove(event.getEntity().getUUID());
    }
}
