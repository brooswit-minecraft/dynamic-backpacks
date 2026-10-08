package io.github.brooswitminecraft.dynamicbackpacks;

import net.neoforged.neoforge.common.ModConfigSpec;

/** Server-side tunables (config/dynamicbackpacks-server.toml per world). */
public final class BackpackConfig {
    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.DoubleValue BLOCKS_PER_SPILL_CHECK;
    public static final ModConfigSpec.IntValue MAX_CHECKS_PER_TICK;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        BLOCKS_PER_SPILL_CHECK = builder
                .comment("Blocks an overloaded player must travel for each spill check. Lower spills more often.")
                .defineInRange("blocksPerSpillCheck", 4.0, 0.25, 1024.0);
        MAX_CHECKS_PER_TICK = builder
                .comment("Upper bound on spill checks in a single tick, so very fast movement cannot empty the inventory at once.")
                .defineInRange("maxChecksPerTick", 2, 1, 27);
        SPEC = builder.build();
    }

    private BackpackConfig() {
    }
}
