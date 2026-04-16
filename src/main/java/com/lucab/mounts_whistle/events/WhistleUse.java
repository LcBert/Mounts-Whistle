package com.lucab.mounts_whistle.events;

import com.lucab.mounts_whistle.MountsWhistle;
import com.lucab.mounts_whistle.config.ModConfig;
import com.lucab.mounts_whistle.data_components.WhistleDataComponents;
import com.lucab.mounts_whistle.items.ItemsRegistry;
import com.lucab.mounts_whistle.sounds.ModSounds;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.entity.animal.horse.Markings;
import net.minecraft.world.entity.animal.horse.Variant;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

import java.util.UUID;

@EventBusSubscriber(modid = MountsWhistle.MOD_ID)
public class WhistleUse {
    @SubscribeEvent
    public static void onWhistleUse(PlayerInteractEvent.RightClickItem event) {
        Player player = event.getEntity();
        Level level = player.level();
        if (level.isClientSide) return;
        ItemStack stack = event.getItemStack();
        ModConfig.Config config = ModConfig.INSTANCE;

        if (stack.getItem() != ItemsRegistry.MOUNTS_WHISTLE.get()) return;

        if (player.isShiftKeyDown() && config.protection.enableAutoRide) {
            boolean autoRide = !stack.getOrDefault(WhistleDataComponents.AUTO_RIDE, false);
            stack.set(WhistleDataComponents.AUTO_RIDE, autoRide);
            player.displayClientMessage(Component.translatable("message.mounts_whistle.auto_ride_" + (autoRide ? "enabled" : "disabled")), true);
            return;
        }

        String mountUuidRaw = stack.getOrDefault(WhistleDataComponents.MOUNT_UUID, "");
        UUID mountUuid;
        try {
            mountUuid = UUID.fromString(mountUuidRaw);
        } catch (IllegalArgumentException ex) {
            player.displayClientMessage(Component.literal("This whistle has invalid mount data."), true);
            return;
        }

        AbstractHorse mount = MountHelper.getMountByUUID(level, mountUuid);

        // Mount share
        String whistleOwner = stack.getOrDefault(WhistleDataComponents.WHISTLE_OWNER_UUID, "");
        if (!config.protection.whistleShare && !whistleOwner.equals(player.getUUID().toString()))
            return;

        if (mount != null) {
            // Mount alive -> Despawn
            if (level instanceof ServerLevel) {
                MountHelper.despawnMount(level, mount, stack);
                player.swing(event.getHand(), true);
            }
        } else {
            // Mount not alive -> Spawn
            if (level instanceof ServerLevel serverLevel) {
                String mountTypeRaw = stack.getOrDefault(WhistleDataComponents.MOUNT_TYPE, "");
                ResourceLocation mountTypeId = ResourceLocation.tryParse(mountTypeRaw);
                if (mountTypeId == null) {
                    player.displayClientMessage(Component.literal("This whistle has invalid mount type data."), true);
                    return;
                }

                EntityType<?> entityType = BuiltInRegistries.ENTITY_TYPE.getOptional(mountTypeId).orElse(null);
                if (entityType == null) {
                    player.displayClientMessage(Component.literal("Stored mount type is not registered."), true);
                    return;
                }

                Entity entity = entityType.spawn(serverLevel, player.blockPosition(), MobSpawnType.MOB_SUMMONED);
                if (!(entity instanceof AbstractHorse newMount)) {
                    if (entity != null) entity.discard();
                    player.displayClientMessage(Component.literal("Stored mount type is not a valid mount."), true);
                    return;
                }

                MountHelper.mountsTimer.put(entity.getUUID(), System.currentTimeMillis());

                if (config.inventory.equipSaddle)
                    newMount.equipSaddle(new ItemStack(Items.SADDLE), SoundSource.AMBIENT);
                if (newMount instanceof Horse horse) {
                    Variant variant = stack.getOrDefault(WhistleDataComponents.MOUNT_VARIANT, Variant.BLACK);
                    String markingsRaw = stack.getOrDefault(WhistleDataComponents.MOUNT_MARKINGS, Markings.NONE.name());
                    Markings markings;
                    try {
                        markings = Markings.valueOf(markingsRaw);
                    } catch (IllegalArgumentException ex) {
                        markings = Markings.NONE;
                    }
                    MountHelper.applyHorseAppearance(horse, variant, markings);
                }
                Item armorItem = stack.getOrDefault(WhistleDataComponents.ARMOR_ITEM, Items.AIR);
                newMount.setBodyArmorItem(new ItemStack(armorItem));

                stack.set(WhistleDataComponents.MOUNT_UUID, newMount.getUUID().toString());
                newMount.tameWithName(player);
                float speedAttribute = config.attributeModifier.baseSpeedAttribute + MountHelper.getWhistleSpeedAttribute(player, stack);
                float jumpAttribute = config.attributeModifier.baseJumpAttribute + MountHelper.getWhistleJumpAttribute(player, stack);

                newMount.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(speedAttribute);
                newMount.getAttribute(Attributes.JUMP_STRENGTH).setBaseValue(jumpAttribute);
                if (stack.getOrDefault(WhistleDataComponents.AUTO_RIDE, false) && config.protection.enableAutoRide)
                    player.startRiding(newMount);

                player.swing(event.getHand(), true);
                level.playSound(null, player.getX(), player.getY(), player.getZ(), ModSounds.WHISTLE_USE.get(), SoundSource.PLAYERS);
            }
        }
    }
}
