package com.lucab.mounts_whistle.capabilities;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.capabilities.EntityCapability;
import net.neoforged.neoforge.items.IItemHandler;

public class CuriosCapabilities {
    public static final EntityCapability<IItemHandler, Void> CURIOS_INVENTORY =
            EntityCapability.createVoid(ResourceLocation.fromNamespaceAndPath("curios", "mounts_whistle"), IItemHandler.class);

}
