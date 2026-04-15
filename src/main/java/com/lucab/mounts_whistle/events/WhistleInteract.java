package com.lucab.mounts_whistle.events;

import com.lucab.mounts_whistle.MountsWhistle;
import com.lucab.mounts_whistle.config.ModConfig;
import com.lucab.mounts_whistle.data_components.WhistleDataComponents;
import com.lucab.mounts_whistle.items.ItemsRegistry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.item.ItemTossEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

import java.util.UUID;

@EventBusSubscriber(modid = MountsWhistle.MOD_ID)
public class WhistleInteract {
    // TODO
    @SubscribeEvent
    public static void onItemDrop(ItemTossEvent event) {
        Level level = event.getPlayer().level();
        ItemStack stack = event.getEntity().getItem();
        ModConfig.Config config = ModConfig.INSTANCE;

        if (!config.despawnWhenDrop) return;

        if (stack.getItem() == ItemsRegistry.MOUNTS_WHISTLE.get()) {
            String mounstUUID = stack.get(WhistleDataComponents.MOUNT_UUID);

            if (level instanceof ServerLevel serverLevel) {
                Entity entity = serverLevel.getEntity(UUID.fromString(mounstUUID));
                if (entity != null) {
                }
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerInteractEntity(PlayerInteractEvent.EntityInteract event) {
        Player player = event.getEntity();
        Entity entity = event.getTarget();
        Level level = player.level();
        ItemStack stack = event.getItemStack();
        AbstractHorse mount = MountHelper.getMount(entity);
        if (mount == null) return;
        ModConfig.Config config = ModConfig.INSTANCE;

        if (level.isClientSide) return;

        boolean isWhistleEquipped = stack.getItem() == ItemsRegistry.MOUNTS_WHISTLE.get();
        boolean isMountTamed = mount.isTamed();
        UUID ownerUUID = mount.getOwnerUUID();

        // Handle mount ride based in config and player uuid
        if (config.onlyRideOwner
                && ownerUUID != null
                && !ownerUUID.equals(player.getUUID())
                && isMountTamed) {
            player.displayClientMessage(Component.translatable("message.mounts_whistle.mount_not_own"), true);
            event.setCanceled(true);
            return;
        }

        // Tame mount if whistle has not already tamed a mount and entity is not tamed
        if (isWhistleEquipped && !isMountTamed) {
            if (stack.getOrDefault(WhistleDataComponents.HAS_MOUNT, false)) {
                player.displayClientMessage(Component.translatable("message.mounts_whistle.whistle_already_bound"), true);
                event.setCanceled(true);
                return;
            }

            mount.tameWithName(player);
            if (config.equipSaddle) mount.equipSaddle(new ItemStack(Items.SADDLE), SoundSource.AMBIENT);
            stack.set(WhistleDataComponents.HAS_MOUNT, true);
            stack.set(WhistleDataComponents.MOUNT_UUID, mount.getUUID().toString());
            ResourceLocation mountTypeId = BuiltInRegistries.ENTITY_TYPE.getKey(mount.getType());
            if (mountTypeId != null) {
                stack.set(WhistleDataComponents.MOUNT_TYPE, mountTypeId.toString());
            }
            if (mount instanceof Horse horse) {
                stack.set(WhistleDataComponents.MOUNT_VARIANT, horse.getVariant());
                stack.set(WhistleDataComponents.MOUNT_MARKINGS, horse.getMarkings().name());
            }
            stack.set(WhistleDataComponents.WHISTLE_OWNER_UUID, mount.getOwnerUUID().toString());
            MountHelper.mountsTimer.put(entity.getUUID(), System.currentTimeMillis());
            // TODO: rename mount
        }

        // Disable taming mount already tamed
        if (isWhistleEquipped && isMountTamed) {
            player.displayClientMessage(Component.translatable("message.mounts_whistle.mount_already_tamed"), true);
            event.setCanceled(true);
            return;
        }

        // Disable mount feed
        if (stack.has(DataComponents.FOOD)) event.setCanceled(true);
    }
}
