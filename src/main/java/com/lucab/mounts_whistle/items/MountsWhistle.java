package com.lucab.mounts_whistle.items;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;

public class MountsWhistle extends Item {
    public MountsWhistle() {
        super(new Item.Properties().rarity(Rarity.EPIC).stacksTo(1));
    }
}
