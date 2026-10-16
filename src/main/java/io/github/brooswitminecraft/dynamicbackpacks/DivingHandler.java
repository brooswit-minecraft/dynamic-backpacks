package io.github.brooswitminecraft.dynamicbackpacks;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

/**
 * Diving Pack: the player's normal air supply behaves normally, but once it
 * runs out, drowning damage is diverted into the pack's durability instead
 * of player health. Direct combat damage is untouched and stays dangerous
 * because the pack provides no armor.
 *
 * The durability cost per point of diverted damage is not yet a design
 * decision (see the spec's open item); this uses a 1:1 mapping rather than
 * inventing a multiplier.
 */
public final class DivingHandler {
    private DivingHandler() {
    }

    public static void onLivingDamagePre(LivingDamageEvent.Pre event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || !event.getSource().is(DamageTypes.DROWN)) {
            return;
        }
        ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
        if (!(chest.getItem() instanceof BackpackItem pack) || pack.utility() != BackpackItem.Utility.DIVING) {
            return;
        }

        int durabilityDamage = Math.max(1, (int) Math.ceil(event.getNewDamage()));
        event.setNewDamage(0.0F);
        chest.hurtAndBreak(durabilityDamage, player, EquipmentSlot.CHEST);
    }
}
