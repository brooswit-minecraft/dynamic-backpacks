package io.github.brooswitminecraft.dynamicbackpacks;

import java.util.EnumMap;
import java.util.List;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Entry point and registry for Dynamic Backpacks (MINECRAFT-66). */
@Mod(DynamicBackpacksMod.MODID)
public class DynamicBackpacksMod {
    public static final String MODID = "dynamicbackpacks";
    public static final Logger LOGGER = LogUtils.getLogger();

    /** Vanilla leather chestplate durability (5 * 16). Packs are fractions of it. */
    private static final int LEATHER_CHESTPLATE_DURABILITY = 80;

    public static final DeferredRegister<ArmorMaterial> ARMOR_MATERIALS = DeferredRegister.create(Registries.ARMOR_MATERIAL, MODID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);

    /** Zero defense: a pack protects cargo, never the wearer. */
    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> PACK_MATERIAL = ARMOR_MATERIALS.register("backpack",
            () -> new ArmorMaterial(new EnumMap<>(ArmorItem.Type.class), 0, SoundEvents.ARMOR_EQUIP_LEATHER,
                    () -> Ingredient.EMPTY, List.of(new ArmorMaterial.Layer(ResourceLocation.fromNamespaceAndPath(MODID, "backpack"))),
                    0.0F, 0.0F));

    public static final DeferredItem<BackpackItem> STICK_PACK = ITEMS.register("stick_pack",
            () -> new BackpackItem(PACK_MATERIAL, new Item.Properties().durability(LEATHER_CHESTPLATE_DURABILITY / 8),
                    CarryLogic.Protection.HALF, null));
    public static final DeferredItem<BackpackItem> LEATHER_BACKPACK = ITEMS.register("leather_backpack",
            () -> new BackpackItem(PACK_MATERIAL, new Item.Properties().durability(LEATHER_CHESTPLATE_DURABILITY / 4),
                    CarryLogic.Protection.FULL, STICK_PACK));

    public DynamicBackpacksMod(IEventBus modEventBus, ModContainer modContainer) {
        ARMOR_MATERIALS.register(modEventBus);
        ITEMS.register(modEventBus);
        modEventBus.addListener(this::addCreative);
        modContainer.registerConfig(ModConfig.Type.SERVER, BackpackConfig.SPEC);
        NeoForge.EVENT_BUS.addListener(SpillHandler::onPlayerTick);
        NeoForge.EVENT_BUS.addListener(SpillHandler::onLoggedOut);
        LOGGER.info("Dynamic Backpacks loaded");
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.COMBAT) {
            event.accept(STICK_PACK);
            event.accept(LEATHER_BACKPACK);
        }
    }
}
