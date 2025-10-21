package com.lucab.mounts_whistle.items;

import com.lucab.mounts_whistle.Utils;

import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ItemsRegistry {
    public static final DeferredRegister.Items ITEM_REGISTRY = DeferredRegister.createItems(Utils.MOD_ID);

    public static final DeferredItem<Item> MOUNTS_WHISTLE = ITEM_REGISTRY
            .register("mounts_whistle", MountsWhistle::new);
}
