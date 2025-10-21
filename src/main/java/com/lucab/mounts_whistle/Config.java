package com.lucab.mounts_whistle;

import net.neoforged.neoforge.common.ModConfigSpec;

public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.BooleanValue ONLY_RIDE_OWNER = BUILDER
            .comment("Define if a player can ride a mount that is not their own")
            .define("only_ride_owner", true);

    static final ModConfigSpec SPEC = BUILDER.build();
}
