package io.github.brooswitminecraft.dynamicbackpacks;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/**
 * Emerald Backpack proximity ore detector: an expanding-radius search that
 * dings and resets whenever ore is found nearby, so nearby ore dings often
 * and distant ore dings slowly, without ever computing the actual nearest
 * ore distance.
 */
public final class OreDetectorHandler {
    private static final Map<UUID, OreDetectorLogic> STATES = new HashMap<>();

    private OreDetectorHandler() {
    }

    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
        if (!(chest.getItem() instanceof BackpackItem pack) || pack.utility() != BackpackItem.Utility.ORE_DETECTOR) {
            STATES.remove(player.getUUID());
            return;
        }

        OreDetectorLogic state = STATES.computeIfAbsent(player.getUUID(),
                id -> new OreDetectorLogic(BackpackConfig.ORE_DETECTOR_INTERVAL_TICKS.get(),
                        BackpackConfig.ORE_DETECTOR_BASE_RADIUS.get(), BackpackConfig.ORE_DETECTOR_RADIUS_STEP.get()));
        if (!state.tick()) {
            return;
        }

        if (findOreWithin(player, state.radius())) {
            player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.5F, 1.4F);
            state.reset();
        } else {
            state.expand();
        }
    }

    private static boolean findOreWithin(Player player, int radius) {
        BlockPos center = player.blockPosition();
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    if (dx * dx + dy * dy + dz * dz > radius * radius) {
                        continue;
                    }
                    BlockPos pos = center.offset(dx, dy, dz);
                    if (player.level().getBlockState(pos).is(Tags.Blocks.ORES)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static void onLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        STATES.remove(event.getEntity().getUUID());
    }
}
