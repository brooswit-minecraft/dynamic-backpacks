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

    /** Vanilla chestplate durability (material base * 16). Packs are fractions of these. */
    private static final int LEATHER_CHESTPLATE_DURABILITY = 80;
    private static final int IRON_CHESTPLATE_DURABILITY = 240;
    private static final int GOLD_CHESTPLATE_DURABILITY = 112;
    private static final int DIAMOND_CHESTPLATE_DURABILITY = 528;
    private static final int NETHERITE_CHESTPLATE_DURABILITY = 592;

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

    public static final DeferredItem<BackpackItem> IRON_BACKPACK = ITEMS.register("iron_backpack",
            () -> new BackpackItem(PACK_MATERIAL, new Item.Properties().durability(IRON_CHESTPLATE_DURABILITY / 4),
                    CarryLogic.Protection.FULL, LEATHER_BACKPACK));
    public static final DeferredItem<BackpackItem> GOLD_BACKPACK = ITEMS.register("gold_backpack",
            () -> new BackpackItem(PACK_MATERIAL, new Item.Properties().durability(GOLD_CHESTPLATE_DURABILITY / 4),
                    CarryLogic.Protection.FULL, BackpackItem.Utility.MAGNET, LEATHER_BACKPACK));
    public static final DeferredItem<BackpackItem> DIAMOND_BACKPACK = ITEMS.register("diamond_backpack",
            () -> new BackpackItem(PACK_MATERIAL, new Item.Properties().durability(DIAMOND_CHESTPLATE_DURABILITY / 4),
                    CarryLogic.Protection.FULL, LEATHER_BACKPACK));
    public static final DeferredItem<BackpackItem> NETHERITE_BACKPACK = ITEMS.register("netherite_backpack",
            () -> new BackpackItem(PACK_MATERIAL, new Item.Properties().durability(NETHERITE_CHESTPLATE_DURABILITY / 4),
                    CarryLogic.Protection.FULL, BackpackItem.Utility.MAGNET, LEATHER_BACKPACK));
    /** Durability: "Same as Gold Backpack" per the spec's upgrade table. */
    public static final DeferredItem<BackpackItem> EMERALD_BACKPACK = ITEMS.register("emerald_backpack",
            () -> new BackpackItem(PACK_MATERIAL, new Item.Properties().durability(GOLD_CHESTPLATE_DURABILITY / 4),
                    CarryLogic.Protection.FULL, BackpackItem.Utility.ORE_DETECTOR, LEATHER_BACKPACK));
    /** Durability: "Same as Iron Backpack" per the spec's upgrade table. */
    public static final DeferredItem<BackpackItem> GLOW_PACK = ITEMS.register("glow_pack",
            () -> new BackpackItem(PACK_MATERIAL, new Item.Properties().durability(IRON_CHESTPLATE_DURABILITY / 4),
                    CarryLogic.Protection.FULL, BackpackItem.Utility.GLOW, LEATHER_BACKPACK));
    /**
     * Durability is an open design decision (spec: "Diving Pack durability
     * multiplier is not yet specified; do not invent one without a design
     * decision"), so this inherits the Leather Backpack's own durability
     * rather than applying an invented multiplier.
     */
    public static final DeferredItem<BackpackItem> DIVING_PACK = ITEMS.register("diving_pack",
            () -> new BackpackItem(PACK_MATERIAL, new Item.Properties().durability(LEATHER_CHESTPLATE_DURABILITY / 4),
                    CarryLogic.Protection.FULL, BackpackItem.Utility.DIVING, LEATHER_BACKPACK));
    public static final DeferredItem<BackpackItem> HOPPER_PACK = ITEMS.register("hopper_pack",
            () -> new BackpackItem(PACK_MATERIAL, new Item.Properties().durability(LEATHER_CHESTPLATE_DURABILITY / 4),
                    CarryLogic.Protection.FULL, BackpackItem.Utility.HOPPER, LEATHER_BACKPACK));

    public DynamicBackpacksMod(IEventBus modEventBus, ModContainer modContainer) {
        ARMOR_MATERIALS.register(modEventBus);
        ITEMS.register(modEventBus);
        modEventBus.addListener(this::addCreative);
        modContainer.registerConfig(ModConfig.Type.SERVER, BackpackConfig.SPEC);
        NeoForge.EVENT_BUS.addListener(SpillHandler::onPlayerTick);
        NeoForge.EVENT_BUS.addListener(SpillHandler::onLoggedOut);
        NeoForge.EVENT_BUS.addListener(MagnetHandler::onPlayerTick);
        NeoForge.EVENT_BUS.addListener(OreDetectorHandler::onPlayerTick);
        NeoForge.EVENT_BUS.addListener(OreDetectorHandler::onLoggedOut);
        NeoForge.EVENT_BUS.addListener(GlowHandler::onPlayerTick);
        NeoForge.EVENT_BUS.addListener(GlowHandler::onLoggedOut);
        NeoForge.EVENT_BUS.addListener(DivingHandler::onLivingDamagePre);
        NeoForge.EVENT_BUS.addListener(HopperHandler::onPlayerTick);
        NeoForge.EVENT_BUS.addListener(HopperHandler::onLoggedOut);
        NeoForge.EVENT_BUS.addListener(WalletHandler::onPlayerTick);
        LOGGER.info("Dynamic Backpacks loaded");
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.COMBAT) {
            event.accept(STICK_PACK);
            event.accept(LEATHER_BACKPACK);
            event.accept(IRON_BACKPACK);
            event.accept(GOLD_BACKPACK);
            event.accept(DIAMOND_BACKPACK);
            event.accept(NETHERITE_BACKPACK);
            event.accept(EMERALD_BACKPACK);
            event.accept(GLOW_PACK);
            event.accept(DIVING_PACK);
            event.accept(HOPPER_PACK);
        }
    }
}
