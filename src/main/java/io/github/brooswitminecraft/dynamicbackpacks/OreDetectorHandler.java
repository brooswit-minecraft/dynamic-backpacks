package io.github.brooswitminecraft.dynamicbackpacks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
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
 * for a player who never finds ore. Positions outside a loaded chunk or
 * outside the world's build height are skipped without a block-state read.
 */
public final class OreDetectorHandler {
    private static final Map<UUID, OreDetectorLogic> STATES = new HashMap<>();
    /** Sphere offsets for a given radius are the same for every player, so cache them by radius. */
    private static final Map<Integer, List<Vec3i>> OFFSETS_BY_RADIUS = new ConcurrentHashMap<>();

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
        List<Vec3i> offsets = offsetsFor(state.radius());
        boolean found = state.tick(offsets.size(), index -> sample(level, center, offsets.get(index)));
        if (found) {
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.5F, 1.4F);
        }
    }

    private static SampleResult sample(Level level, BlockPos center, Vec3i offset) {
        BlockPos pos = center.offset(offset);
        if (level.isOutsideBuildHeight(pos) || !level.isLoaded(pos)) {
            return SampleResult.UNAVAILABLE;
        }
        return level.getBlockState(pos).is(Tags.Blocks.ORES) ? SampleResult.FOUND : SampleResult.NOT_FOUND;
    }

    private static List<Vec3i> offsetsFor(int radius) {
        return OFFSETS_BY_RADIUS.computeIfAbsent(radius, OreDetectorHandler::computeSphereOffsets);
    }

    private static List<Vec3i> computeSphereOffsets(int radius) {
        List<Vec3i> offsets = new ArrayList<>();
        int radiusSquared = radius * radius;
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    if (dx * dx + dy * dy + dz * dz <= radiusSquared) {
                        offsets.add(new Vec3i(dx, dy, dz));
                    }
                }
            }
        }
        return offsets;
    }

    public static void onLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        STATES.remove(event.getEntity().getUUID());
    }
}
