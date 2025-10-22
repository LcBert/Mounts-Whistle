package com.lucab.mounts_whistle;

import java.util.List;

import net.neoforged.neoforge.common.ModConfigSpec;

public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.BooleanValue ONLY_RIDE_OWNER = BUILDER
            .comment("Define if a player can ride a mount that is not their own")
            .define("only_ride_owner", true);

    public static final ModConfigSpec.ConfigValue<List<? extends String>> MOUNTS_LIST = BUILDER
            .comment("Define the list of mounts consider by the whistle")
            .defineList("mounts_list", List.of("minecraft:mule", "minecraft:donkey", "minecraft:horse"), () -> "",
                    Config::validator);

    private static boolean validator(final Object obj) {
        return true;
    }

    static final ModConfigSpec SPEC = BUILDER.build();
}
