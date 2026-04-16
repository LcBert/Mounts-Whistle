package com.lucab.mounts_whistle;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.enchantment.Enchantment;

public class ModEnchantments {
    public static final ResourceKey<Enchantment> MOUNT_SPEED =
            ResourceKey.create(Registries.ENCHANTMENT, ResourceLocation.fromNamespaceAndPath(MountsWhistle.MOD_ID, "mount_speed"));

    public static final ResourceKey<Enchantment> MOUNT_JUMP =
            ResourceKey.create(Registries.ENCHANTMENT, ResourceLocation.fromNamespaceAndPath(MountsWhistle.MOD_ID, "mount_jump"));
}
