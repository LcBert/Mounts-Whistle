package com.lucab.mounts_whistle.events;

import java.util.List;

import com.lucab.mounts_whistle.Utils;
import com.lucab.mounts_whistle.data_components.WhistleDataComponents;
import com.lucab.mounts_whistle.items.ItemsRegistry;

import net.minecraft.world.entity.animal.horse.Mule;
import net.minecraft.world.entity.animal.horse.Donkey;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

@EventBusSubscriber(modid = Utils.MOD_ID)
public class InterractMounts {
    @SubscribeEvent
    public static void mountsInterract(PlayerInteractEvent.EntityInteract event) {
        var mounts = List.of(Mule.class, Donkey.class, Horse.class);
        var player = event.getEntity();
        var target = event.getTarget();
        var item = event.getItemStack();

        if (!event.getLevel().isClientSide()) {
            if (mounts.contains(event.getTarget().getClass())) {
                if (item.getItem().equals(ItemsRegistry.MOUNTS_WHISTLE.asItem())) {
                    if (!((AbstractHorse) target).isTamed()) {
                        if (!item.getOrDefault(WhistleDataComponents.HAS_MOUNT, false)) {
                            ((AbstractHorse) target).tameWithName(player);
                            ((AbstractHorse) target).equipSaddle(new ItemStack(Items.SADDLE), null);
                            item.set(WhistleDataComponents.HAS_MOUNT, true);
                            item.set(WhistleDataComponents.MOUNT_UUID, target.getUUID().toString());
                        } else {
                            Utils.messagePlayer(player, "This whistle is already bound to an mount");
                        }
                    } else {
                        Utils.messagePlayer(player, "This mount is already tamed");
                    }
                    event.setCanceled(true);
                }
            }
        }
    }
}
