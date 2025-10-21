package com.lucab.mounts_whistle.events;

import java.util.List;
import java.util.UUID;

import com.lucab.mounts_whistle.Config;
import com.lucab.mounts_whistle.Utils;
import com.lucab.mounts_whistle.data_components.WhistleDataComponents;
import com.lucab.mounts_whistle.items.ItemsRegistry;

import net.minecraft.world.entity.animal.horse.Mule;
import net.minecraft.world.entity.animal.horse.Donkey;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Entity.RemovalReason;
import net.minecraft.world.entity.EntityType;
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
        var mounts_food = List.of(Items.WHEAT, Items.GOLDEN_APPLE, Items.ENCHANTED_GOLDEN_APPLE);
        var player = event.getEntity();
        var target = event.getTarget();
        var item = event.getItemStack();

        if (event.getLevel().isClientSide()) {
            return;
        }
        if (!mounts.contains(target.getClass())) {
            return;
        }

        var is_whistle_equip = item.getItem().equals(ItemsRegistry.MOUNTS_WHISTLE.asItem());
        var mounts_is_tamed = ((AbstractHorse) target).isTamed();

        if (is_whistle_equip && !mounts_is_tamed) {
            if (!item.getOrDefault(WhistleDataComponents.HAS_MOUNT, false)) {
                ((AbstractHorse) target).tameWithName(player);
                ((AbstractHorse) target).equipSaddle(new ItemStack(Items.SADDLE), null);
                item.set(WhistleDataComponents.HAS_MOUNT, true);
                item.set(WhistleDataComponents.MOUNT_UUID, target.getUUID().toString());
            } else {
                Utils.messagePlayer(player, "This whistle is already bound to a mount");
            }
        }

        if (is_whistle_equip && mounts_is_tamed) {
            Utils.messagePlayer(player, "This mount is already tamed");
        }

        /**
         * DISABLE RIDE OTHER PLAYERS MOUNTS
         * This will cancel the event when a player try
         * to ride a mount that is not his own
         */
        if (mounts_is_tamed && Config.ONLY_RIDE_OWNER.getAsBoolean()) {
            if (!((AbstractHorse) target).getOwnerUUID().equals(player.getUUID())) {
                Utils.messagePlayer(player, "This mount is not your");
                event.setCanceled(true);
                return;
            }
        }

        if (is_whistle_equip) {
            event.setCanceled(true);
        }

        /**
         * DISABLE MOUNTS FEED
         * This will cancel the event when a player try to feed a mount
         * by right-click a mounts entity inside the "mounts" list
         * whit an item that is inside the "mounts_food" list
         */
        if (mounts_food.contains(item.getItem())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void whistleUse(PlayerInteractEvent.RightClickItem event) {
        var player = event.getEntity();
        var level = event.getLevel();
        var item = event.getItemStack();

        if (event.getLevel().isClientSide())
            return;

        if (!item.getOrDefault(WhistleDataComponents.HAS_MOUNT, false)) {
            Utils.messagePlayer(player, "Whistle has not a mounts bound", false);
            event.setCanceled(true);
            return;
        }

        if (item.getItem().equals(ItemsRegistry.MOUNTS_WHISTLE.get())) {
            var item_mounts_uuid = item.get(WhistleDataComponents.MOUNT_UUID);

            if (level instanceof ServerLevel serverLevel) {
                Entity entity = serverLevel.getEntity(UUID.fromString(item_mounts_uuid));
                if (entity != null) {
                    item.set(WhistleDataComponents.MOUNT_TYPE, entity.getType().toString());
                    entity.remove(RemovalReason.DISCARDED);
                } else {
                    var mount_type = item.get(WhistleDataComponents.MOUNT_TYPE);
                    Entity mount = new Horse(EntityType.HORSE, serverLevel);
                    switch (mount_type) {
                        case "entity.minecraft.mule": {
                            mount = new Mule(EntityType.MULE, serverLevel);
                            break;
                        }
                        case "entity.minecraft.donkey": {
                            mount = new Donkey(EntityType.DONKEY, serverLevel);
                            break;
                        }
                        case "entity.minecraft.horse": {
                            mount = new Horse(EntityType.HORSE, serverLevel);
                            break;
                        }
                    }
                    mount.setPos(player.getX(), player.getY(), player.getZ());
                    ((AbstractHorse) mount).tameWithName(player);
                    ((AbstractHorse) mount).equipSaddle(new ItemStack(Items.SADDLE), null);
                    level.addFreshEntity(mount);
                    item.set(WhistleDataComponents.MOUNT_UUID, mount.getUUID().toString());
                }
            }
        }
    }
}
