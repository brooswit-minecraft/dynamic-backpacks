package io.github.brooswitminecraft.dynamicbackpacks;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/**
 * Glow Pack: periodically refreshes vanilla Glowing while equipped. The
 * refresh interval is shorter than the applied duration so the effect
 * never visibly runs out between refreshes; unequipping or breaking the
 * pack simply stops refreshing and lets the remaining duration expire.
 */
public final class GlowHandler {
    private static final Map<UUID, Integer> TICKS_SINCE_REFRESH = new HashMap<>();

    private GlowHandler() {
    }

    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
        if (!(chest.getItem() instanceof BackpackItem pack) || pack.utility() != BackpackItem.Utility.GLOW) {
            TICKS_SINCE_REFRESH.remove(player.getUUID());
            return;
        }

        int refreshInterval = BackpackConfig.GLOW_REFRESH_INTERVAL_TICKS.get();
        int ticksSinceRefresh = TICKS_SINCE_REFRESH.merge(player.getUUID(), 1, Integer::sum);
        if (!GlowLogic.isRefreshDue(ticksSinceRefresh, refreshInterval)) {
            return;
        }
        TICKS_SINCE_REFRESH.put(player.getUUID(), 0);
        player.addEffect(new MobEffectInstance(MobEffects.GLOWING, BackpackConfig.GLOW_EFFECT_DURATION_TICKS.get(), 0, true, false));
    }

    public static void onLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        TICKS_SINCE_REFRESH.remove(event.getEntity().getUUID());
    }
}
