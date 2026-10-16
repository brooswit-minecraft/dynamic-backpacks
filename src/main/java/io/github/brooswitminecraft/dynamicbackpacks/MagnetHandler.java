package io.github.brooswitminecraft.dynamicbackpacks;

import java.util.List;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/** Gold/Netherite Backpack item attraction. */
public final class MagnetHandler {
    /** Main inventory slots are indices 9..35 of Inventory.items; 0..8 is the hotbar. */
    private static final int MAIN_START = 9;

    private MagnetHandler() {
    }

    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
        if (!(chest.getItem() instanceof BackpackItem pack) || pack.utility() != BackpackItem.Utility.MAGNET) {
            return;
        }
        if (player.isSpectator()) {
            return;
        }

        Inventory inventory = player.getInventory();
        int occupied = 0;
        for (int i = 0; i < CarryLogic.MAIN_SLOTS; i++) {
            if (!inventory.items.get(MAIN_START + i).isEmpty()) {
                occupied++;
            }
        }

        double radius = BackpackConfig.MAGNET_RADIUS.get();
        double pull = MagnetLogic.availablePull(BackpackConfig.MAGNET_PULL_STRENGTH.get(), occupied, CarryLogic.MAIN_SLOTS);
        if (pull <= 0) {
            return;
        }

        AABB area = player.getBoundingBox().inflate(radius);
        List<ItemEntity> items = player.level().getEntitiesOfClass(ItemEntity.class, area, ItemEntity::isAlive);
        if (items.isEmpty()) {
            return;
        }

        List<Double> distances = items.stream().map(item -> (double) item.distanceTo(player)).toList();
        double[] shares = MagnetLogic.distributePull(pull, distances);
        for (int i = 0; i < items.size(); i++) {
            ItemEntity item = items.get(i);
            Vec3 toPlayer = player.position().subtract(item.position());
            if (toPlayer.lengthSqr() < 1.0e-4) {
                continue;
            }
            Vec3 accel = toPlayer.normalize().scale(shares[i]);
            item.setDeltaMovement(item.getDeltaMovement().add(accel.x, accel.y * 0.5, accel.z));
        }
    }
}
