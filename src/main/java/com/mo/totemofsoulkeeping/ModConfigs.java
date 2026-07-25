package com.mo.totemofsoulkeeping;

import net.neoforged.neoforge.common.ModConfigSpec;

public class ModConfigs {

    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    public static final ModConfigSpec SPEC;

    public static final ModConfigSpec.BooleanValue KEEP_EXPERIENCE;

    static {
        BUILDER.comment("Totem of Soul Keeping configuration").push("general");

        KEEP_EXPERIENCE = BUILDER.comment("Whether a triggered charm also preserves experience.")
                .define("keep_experience", true);

        BUILDER.pop();
        SPEC = BUILDER.build();
    }
}
