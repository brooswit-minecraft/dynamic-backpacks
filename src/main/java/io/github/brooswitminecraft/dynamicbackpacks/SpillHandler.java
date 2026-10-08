package io.github.brooswitminecraft.dynamicbackpacks;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/** Server-side spill checks for overloaded players. */
public final class SpillHandler {
    /** A single-tick move longer than this is a teleport or respawn, not travel. */
    private static final double MAX_TRAVEL_PER_TICK = 8.0;
    /** Main inventory slots are indices 9..35 of Inventory.items; 0..8 is the hotbar. */
    private static final int MAIN_START = 9;

    private static final Map<UUID, CarryLogic.SpillClock> CLOCKS = new HashMap<>();

    private SpillHandler() {
    }

    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        CarryLogic.SpillClock clock = CLOCKS.computeIfAbsent(player.getUUID(), id -> new CarryLogic.SpillClock());
        if (player.isCreative() || player.isSpectator()) {
            clock.reset();
            return;
        }

        Inventory inventory = player.getInventory();
        int occupied = 0;
        for (int i = 0; i < CarryLogic.MAIN_SLOTS; i++) {
            if (!inventory.items.get(MAIN_START + i).isEmpty()) {
                occupied++;
            }
        }
        int safe = CarryLogic.safeSlots(player.getArmorValue(), protectionOf(player), CarryLogic.MAIN_SLOTS);
        if (!CarryLogic.isOverCapacity(occupied, safe)) {
            clock.reset();
            return;
        }

        double dx = player.getX() - player.xo;
        double dz = player.getZ() - player.zo;
        double distance = Math.sqrt(dx * dx + dz * dz);
        if (distance > MAX_TRAVEL_PER_TICK) {
            return;
        }
        int checks = clock.advance(distance, BackpackConfig.BLOCKS_PER_SPILL_CHECK.get(), BackpackConfig.MAX_CHECKS_PER_TICK.get());
        for (int i = 0; i < checks; i++) {
            int index = MAIN_START + CarryLogic.pickSlot(CarryLogic.MAIN_SLOTS, player.getRandom()::nextInt);
            ItemStack stack = inventory.removeItemNoUpdate(index);
            if (!stack.isEmpty()) {
                player.drop(stack, true, false);
            }
        }
    }

    public static void onLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        CLOCKS.remove(event.getEntity().getUUID());
    }

    private static CarryLogic.Protection protectionOf(ServerPlayer player) {
        ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
        if (chest.getItem() instanceof BackpackItem pack) {
            return pack.protection();
        }
        return CarryLogic.Protection.NONE;
    }
}
