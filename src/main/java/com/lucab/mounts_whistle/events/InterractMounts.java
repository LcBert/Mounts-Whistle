package com.lucab.mounts_whistle.events;

import java.util.List;
import java.util.UUID;

import com.lucab.mounts_whistle.Functions;
import com.lucab.mounts_whistle.Utils;
import com.lucab.mounts_whistle.data_components.WhistleDataComponents;
import com.lucab.mounts_whistle.items.ItemsRegistry;

import net.minecraft.world.entity.animal.horse.Mule;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.animal.horse.Donkey;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Entity.RemovalReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

@EventBusSubscriber(modid = Utils.MOD_ID)
public class InterractMounts {

    private static String getNamespace(String key) {
        var split = key.replace(".", ":").split(":");
        return String.format("%s:%s", split[1], split[2]);
    }

    @SubscribeEvent
    public static void mountsInterract(PlayerInteractEvent.EntityInteract event) {
        var mounts_food = List.of(Items.WHEAT, Items.GOLDEN_APPLE, Items.ENCHANTED_GOLDEN_APPLE);
        var player = event.getEntity();
        var target = event.getTarget();
        var item = event.getItemStack();

        System.out.println(Utils.config.MOUNTS_LIST.toString());

        if (event.getLevel().isClientSide()) {
            return;
        }

        if (!Functions.listContains(Utils.config.MOUNTS_LIST.get("value"),
                getNamespace(target.getType().toString()))) {
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
                item.set(WhistleDataComponents.MOUNT_TYPE, target.getType().toString());
                if (target instanceof Horse horse) {
                    item.set(WhistleDataComponents.MOUNT_VARIANT, ((Horse) horse).getVariant());
                }
            } else {
                player.displayClientMessage(
                        Component.translatable("message.mounts_whistle.whistle_already_bound"),
                        true);
            }
        }

        if (is_whistle_equip && mounts_is_tamed) {
            player.displayClientMessage(
                    Component.translatable("message.mounts_whistle.mount_already_tamed"), true);
            event.setCanceled(true);
            return;
        }

        /**
         * DISABLE RIDE OTHER PLAYERS MOUNTS
         * This will cancel the event when a player try
         * to ride a mount that is not his own
         */
        if (mounts_is_tamed && Utils.config.ONLY_RIDE_OWNER.get("value").equals(true)) {
            if (!((AbstractHorse) target).getOwnerUUID().equals(player.getUUID())) {
                player.displayClientMessage(
                        Component.translatable("message.mounts_whistle.mount_not_own"), true);

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

        if (!item.getItem().equals(ItemsRegistry.MOUNTS_WHISTLE.asItem())) {
            return;
        }

        if (player.isShiftKeyDown() && Utils.config.ENABLE_AUTO_RIDE.get("value").equals(true)) {
            item.set(WhistleDataComponents.AUTO_RIDE, !item.getOrDefault(WhistleDataComponents.AUTO_RIDE, false));
            // TODO: Make colored text
            if (item.get(WhistleDataComponents.AUTO_RIDE)) {
                player.displayClientMessage(
                        Component.translatable("message.mounts_whistle.auto_ride_enabled"), true);
            } else {
                player.displayClientMessage(
                        Component.translatable("message.mounts_whistle.auto_ride_disabled"), true);
            }
            return;
        }

        if (!item.getOrDefault(WhistleDataComponents.HAS_MOUNT, false)) {
            player.displayClientMessage(
                    Component.translatable("message.mounts_whistle.no_mounts_bound"), true);
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
                    dropMountInventory(level, (LivingEntity) entity);
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
                    if (mount instanceof Horse horse)
                        horse.setVariant(item.get(WhistleDataComponents.MOUNT_VARIANT));
                    level.addFreshEntity(mount);
                    item.set(WhistleDataComponents.MOUNT_UUID, mount.getUUID().toString());
                    if (item.getOrDefault(WhistleDataComponents.AUTO_RIDE, false) &&
                            Utils.config.ENABLE_AUTO_RIDE.get("value").equals(true)) {
                        player.startRiding(mount);
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void mountsDie(LivingDeathEvent event) {
        var level = event.getEntity().level();
        var target = event.getEntity();

        if (!Functions.listContains(Utils.config.MOUNTS_LIST.get("value"), getNamespace(target.getType().toString()))) {
            return;
        }

        if (((AbstractHorse) target).isTamed()) {
            dropMountInventory(level, target);
            event.setCanceled(true);
            target.kill();
        }
    }

    private static void dropMountInventory(Level level, LivingEntity target) {
        // Drop armor
        level.addFreshEntity(new ItemEntity(level, target.getX(), target.getY(), target.getZ(),
                ((AbstractHorse) target).getBodyArmorItem().copy()));

        // Drop chest container items
        var container = ((AbstractHorse) target).getInventory();
        if (container.getContainerSize() > 1) {
            for (int i = 1; i < container.getContainerSize(); i++) {
                var item = container.getItem(i);
                if (!item.isEmpty()) {
                    var container_item_entity = new ItemEntity(level, target.getX(),
                            target.getY(), target.getZ(), item.copy());
                    level.addFreshEntity(container_item_entity);
                }
            }
            level.addFreshEntity(new ItemEntity(level, target.getX(), target.getY(), target.getZ(),
                    Items.CHEST.getDefaultInstance()));
        }
    }
}
