package com.lucab.mounts_whistle.network;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;

public enum PacketActionType implements StringRepresentable {
    TOGGLE_MOUNT("toggle_mount"),
    TOGGLE_AUTO_RIDE("toggle_auto_ride");

    public static final Codec<PacketActionType> CODEC = StringRepresentable.fromEnum(PacketActionType::values);
    private final String name;

    PacketActionType(final String name) {
        this.name = name;
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }
}
