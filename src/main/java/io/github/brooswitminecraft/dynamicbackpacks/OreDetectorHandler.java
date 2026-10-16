package io.github.brooswitminecraft.dynamicbackpacks;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import io.github.brooswitminecraft.dynamicbackpacks.OreDetectorLogic.SampleResult;

/**
 * Emerald Backpack proximity ore detector: an expanding-radius search that
 * dings and resets whenever ore is found nearby, so nearby ore dings often
 * and distant ore dings slowly, without ever computing the actual nearest
 * ore distance.
 *
 * <p>The search volume for a radius can be large, so each player tick
 * samples at most {@code oreDetectorReadsPerTick} positions (see
 * {@link OreDetectorLogic}) rather than scanning the whole volume
 * synchronously in one tick, and the radius is capped
 * ({@code oreDetectorMaxRadius}) so that per-tick cost stays bounded even
 * for a player who never finds ore.
 *
 * <p>The volume itself is never materialized: a sample index maps to a
 * cube offset by pure arithmetic ({@link #offsetForIndex}), and positions
 * outside the actual sphere, outside a loaded chunk, or outside the
 * world's build height are rejected cheaply, with no allocation and no
 * block-state read.
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
                id -> new OreDetectorLogic(BackpackConfig.ORE_DETECTOR_READS_PER_TICK.get(),
                        BackpackConfig.ORE_DETECTOR_BASE_RADIUS.get(), BackpackConfig.ORE_DETECTOR_RADIUS_STEP.get(),
                        BackpackConfig.ORE_DETECTOR_MAX_RADIUS.get()));

        Level level = player.level();
        BlockPos center = player.blockPosition();
        int radius = state.radius();
        boolean found = state.tick(CubeIndexing.volumeSize(radius), index -> sample(level, center, radius, index));
        if (found) {
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.5F, 1.4F);
        }
    }

    private static SampleResult sample(Level level, BlockPos center, int radius, int index) {
        int[] offset = CubeIndexing.offsetForIndex(radius, index);
        int dx = offset[0];
        int dy = offset[1];
        int dz = offset[2];
        if (!CubeIndexing.isWithinSphere(dx, dy, dz, radius)) {
            return SampleResult.NOT_FOUND;
        }

        BlockPos pos = center.offset(dx, dy, dz);
        if (level.isOutsideBuildHeight(pos) || !level.isLoaded(pos)) {
            return SampleResult.UNAVAILABLE;
        }
        return level.getBlockState(pos).is(Tags.Blocks.ORES) ? SampleResult.FOUND : SampleResult.NOT_FOUND;
    }

    public static void onLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        STATES.remove(event.getEntity().getUUID());
    }
}
