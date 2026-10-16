package io.github.brooswitminecraft.dynamicbackpacks;

import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.Holder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.function.Consumer;

/**
 * A chest-slot pack. It is an ArmorItem so it equips, takes combat durability
 * damage and dispenses like a chestplate, but its material has zero defense, so
 * wearing one gives up the chestplate's armor points. Preventing a spill never
 * costs durability.
 */
public class BackpackItem extends ArmorItem {
    /** One material, one weird utility (see the design rule in the spec). */
    public enum Utility {
        NONE,
        /** Gold/Netherite: pulls nearby dropped items in. */
        MAGNET,
        /** Emerald: dings when ore is nearby, searching an expanding radius. */
        ORE_DETECTOR,
        /** Glow Pack: keeps the wearer glowing. */
        GLOW,
        /** Diving Pack: converts drowning damage into pack durability. */
        DIVING,
        /** Hopper Pack: pushes carried items downward into storage below. */
        HOPPER
    }

    private final CarryLogic.Protection protection;
    private final Utility utility;
    @Nullable
    private final Supplier<? extends Item> degradesTo;

    public BackpackItem(Holder<ArmorMaterial> material, Item.Properties properties, CarryLogic.Protection protection,
            @Nullable Supplier<? extends Item> degradesTo) {
        this(material, properties, protection, Utility.NONE, degradesTo);
    }

    public BackpackItem(Holder<ArmorMaterial> material, Item.Properties properties, CarryLogic.Protection protection,
            Utility utility, @Nullable Supplier<? extends Item> degradesTo) {
        super(material, ArmorItem.Type.CHESTPLATE, properties);
        this.protection = protection;
        this.utility = utility;
        this.degradesTo = degradesTo;
    }

    public CarryLogic.Protection protection() {
        return protection;
    }

    public Utility utility() {
        return utility;
    }

    /**
     * When this pack is about to break while worn, swap in a fresh pack of the
     * next stage down. Vanilla then still shrinks the spent stack and plays the
     * break effect; a pack with no degrade target (the Stick Pack) just breaks.
     */
    @Override
    public <T extends LivingEntity> int damageItem(ItemStack stack, int amount, @Nullable T entity, Consumer<Item> onBroken) {
        if (degradesTo != null && entity != null && amount > 0
                && stack.getDamageValue() + amount >= stack.getMaxDamage()
                && entity.getItemBySlot(EquipmentSlot.CHEST) == stack) {
            entity.setItemSlot(EquipmentSlot.CHEST, new ItemStack(degradesTo.get()));
        }
        return amount;
    }
}
