package io.github.brooswitminecraft.dynamicbackpacks;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.neoforged.fml.common.Mod;

/** Entry point for Dynamic Backpacks (MINECRAFT-66). */
@Mod(DynamicBackpacksMod.MODID)
public class DynamicBackpacksMod {
    public static final String MODID = "dynamicbackpacks";
    public static final Logger LOGGER = LogUtils.getLogger();

    public DynamicBackpacksMod() {
        LOGGER.info("Dynamic Backpacks loaded");
    }
}
