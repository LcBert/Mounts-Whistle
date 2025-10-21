package com.lucab.mounts_whistle.data_components;

import java.util.function.UnaryOperator;

import com.lucab.mounts_whistle.Utils;
import com.mojang.serialization.Codec;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class WhistleDataComponents {
    public static final DeferredRegister<DataComponentType<?>> DATA_COMPONENT_TYPES = DeferredRegister
            .create(Registries.DATA_COMPONENT_TYPE, Utils.MOD_ID);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> HAS_MOUNT = register(
            "has_mount",
            builder -> builder.persistent(Codec.BOOL));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<String>> MOUNT_UUID = register(
            "mount_uuid",
            builder -> builder.persistent(Codec.STRING));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<String>> MOUNT_TYPE = register(
            "mount_type",
            builder -> builder.persistent(Codec.STRING));

    private static <T> DeferredHolder<DataComponentType<?>, DataComponentType<T>> register(String name,
            UnaryOperator<DataComponentType.Builder<T>> builderOperator) {
        return DATA_COMPONENT_TYPES.register(name,
                () -> builderOperator.apply(DataComponentType.builder()).build());
    }

    public static void register(IEventBus eventBus) {
        DATA_COMPONENT_TYPES.register(eventBus);
    }
}
