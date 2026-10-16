package io.github.brooswitminecraft.dynamicbackpacks;

import net.neoforged.neoforge.common.ModConfigSpec;

/** Server-side tunables (config/dynamicbackpacks-server.toml per world). */
public final class BackpackConfig {
    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.DoubleValue BLOCKS_PER_SPILL_CHECK;
    public static final ModConfigSpec.IntValue MAX_CHECKS_PER_TICK;

    public static final ModConfigSpec.DoubleValue MAGNET_RADIUS;
    public static final ModConfigSpec.DoubleValue MAGNET_PULL_STRENGTH;

    public static final ModConfigSpec.IntValue ORE_DETECTOR_READS_PER_TICK;
    public static final ModConfigSpec.IntValue ORE_DETECTOR_BASE_RADIUS;
    public static final ModConfigSpec.IntValue ORE_DETECTOR_RADIUS_STEP;
    public static final ModConfigSpec.IntValue ORE_DETECTOR_MAX_RADIUS;

    public static final ModConfigSpec.IntValue GLOW_REFRESH_INTERVAL_TICKS;
    public static final ModConfigSpec.IntValue GLOW_EFFECT_DURATION_TICKS;

    public static final ModConfigSpec.IntValue HOPPER_TRANSFER_INTERVAL_TICKS;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        BLOCKS_PER_SPILL_CHECK = builder
                .comment("Blocks an overloaded player must travel for each spill check. Lower spills more often.")
                .defineInRange("blocksPerSpillCheck", 4.0, 0.25, 1024.0);
        MAX_CHECKS_PER_TICK = builder
                .comment("Upper bound on spill checks in a single tick, so very fast movement cannot empty the inventory at once.")
                .defineInRange("maxChecksPerTick", 2, 1, 27);

        MAGNET_RADIUS = builder
                .comment("Search radius in blocks for Gold/Netherite Backpack item attraction.")
                .defineInRange("magnetRadius", 8.0, 1.0, 64.0);
        MAGNET_PULL_STRENGTH = builder
                .comment("Base pull-force budget for Gold/Netherite Backpack item attraction. A full inventory has effectively zero pull regardless of this value.")
                .defineInRange("magnetPullStrength", 0.4, 0.0, 10.0);

        ORE_DETECTOR_READS_PER_TICK = builder
                .comment("Upper bound on block-state reads the Emerald Backpack ore detector performs in a single server tick. The scan of one search volume is spread across as many ticks as it takes at this rate, rather than ever scanning a whole volume synchronously in one tick.")
                .defineInRange("oreDetectorReadsPerTick", 32, 1, 4096);
        ORE_DETECTOR_BASE_RADIUS = builder
                .comment("Starting search radius in blocks for the Emerald Backpack ore detector.")
                .defineInRange("oreDetectorBaseRadius", 8, 1, 64);
        ORE_DETECTOR_RADIUS_STEP = builder
                .comment("Blocks the Emerald Backpack search radius expands by each time its current volume is fully scanned with no hit.")
                .defineInRange("oreDetectorRadiusStep", 8, 1, 64);
        ORE_DETECTOR_MAX_RADIUS = builder
                .comment("Upper bound on the Emerald Backpack search radius, so an undetected player never grows the scanned volume (and therefore the per-tick read cost) without limit.")
                .defineInRange("oreDetectorMaxRadius", 40, 8, 128);

        GLOW_REFRESH_INTERVAL_TICKS = builder
                .comment("Ticks between each Glow Pack effect refresh.")
                .defineInRange("glowRefreshIntervalTicks", 100, 1, 1200);
        GLOW_EFFECT_DURATION_TICKS = builder
                .comment("Duration in ticks applied on each Glow Pack refresh. Must exceed the refresh interval to avoid flicker.")
                .defineInRange("glowEffectDurationTicks", 200, 1, 2400);

        HOPPER_TRANSFER_INTERVAL_TICKS = builder
                .comment("Ticks between each Hopper Pack transfer attempt, matching a vanilla hopper's cadence.")
                .defineInRange("hopperTransferIntervalTicks", 8, 1, 1200);
        SPEC = builder.build();
    }

    private BackpackConfig() {
    }
}
