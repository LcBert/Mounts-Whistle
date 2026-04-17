package com.lucab.mounts_whistle.events;

import com.lucab.mounts_whistle.MountsWhistle;
import com.lucab.mounts_whistle.config.ModConfig;
import com.lucab.mounts_whistle.data_components.WhistleDataComponents;
import com.lucab.mounts_whistle.items.ItemsRegistry;
import com.lucab.mounts_whistle.sounds.ModSounds;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.horse.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MountHelper {
    public static final Map<UUID, Long> mountsTimer = new HashMap<>();

    public static void toggleMount(Player player, ItemStack stack, boolean swing) {
        Level level = player.level();
        if (level.isClientSide) return;
        ModConfig.Config config = ModConfig.INSTANCE;

        if (stack.getItem() != ItemsRegistry.MOUNTS_WHISTLE.get()) return;

        if (player.isShiftKeyDown() && config.protection.enableAutoRide) {
            MountHelper.toggleAutoRide(player, stack);
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
                if (swing) player.swing(player.getUsedItemHand(), true);
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

                if (swing) player.swing(player.getUsedItemHand(), true);
                level.playSound(null, player.getX(), player.getY(), player.getZ(), ModSounds.WHISTLE_USE.get(), SoundSource.PLAYERS);
            }
        }
    }

    public static AbstractHorse getMount(Entity entity) {
        ModConfig.Config config = ModConfig.INSTANCE;
        if (!(entity instanceof AbstractHorse mount)) return null;

        if (config.mountsList.isEmpty()) {
            return mount;
        } else {
            String entityType = entity.getType().toString();
            if (config.mountsList.contains(entityType)) return mount;
        }
        return null;
    }

    public static void despawnMount(Level level, AbstractHorse mount, @Nullable ItemStack stack) {
        ModConfig.Config config = ModConfig.INSTANCE;
        dropMountInventory(level, mount, stack);
        mount.remove(Entity.RemovalReason.DISCARDED);
        if (level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.SMOKE, mount.getX(), mount.getY() + 0.5, mount.getZ(), 100, 0.5, 0.5, 0.5, 0.05);
        }

        mountsTimer.remove(mount.getUUID());
    }

    public static void dropMountInventory(Level level, AbstractHorse mount, @Nullable ItemStack stack) {
        ModConfig.Config config = ModConfig.INSTANCE;
        // Drop saddle
        if (config.inventory.dropSaddle) {
            if (mount.isSaddled()) {
                ItemStack saddle = new ItemStack(Items.SADDLE);
                level.addFreshEntity(new ItemEntity(level, mount.getX(), mount.getY(), mount.getZ(), saddle));
                if (stack != null) stack.remove(WhistleDataComponents.SADDLE_ITEM);
            }
        } else {
            if (stack != null) stack.set(WhistleDataComponents.SADDLE_ITEM, mount.isSaddled());
        }

        // Drop Armor
        ItemStack armor = mount.getBodyArmorItem();
        if (config.inventory.dropArmor) {
            level.addFreshEntity(new ItemEntity(level, mount.getX(), mount.getY(), mount.getZ(), armor));
            if (stack != null) stack.remove(WhistleDataComponents.ARMOR_ITEM);
        } else {
            if (stack != null) stack.set(WhistleDataComponents.ARMOR_ITEM, armor.getItem());
        }

        // Drop chest content
        if (mount instanceof AbstractChestedHorse chestedMount) {
            if (chestedMount.hasChest()) {
                Container container = chestedMount.getInventory();
                for (int i = 1; i < container.getContainerSize(); i++) {
                    level.addFreshEntity(new ItemEntity(level, mount.getX(), mount.getY(), mount.getZ(), container.getItem(i)));
                }
                level.addFreshEntity(new ItemEntity(level, mount.getX(), mount.getY(), mount.getZ(), Items.CHEST.getDefaultInstance()));
            }
        }

    }

    public static AbstractHorse getMountByUUID(Level level, UUID uuid) {
        if (level instanceof ServerLevel serverLevel) {
            if (serverLevel.getEntity(uuid) instanceof AbstractHorse mount) {
                return mount;
            }
        }
        return null;
    }

    public static void applyHorseAppearance(Horse horse, Variant variant, Markings markings) {
        int packedVariant = (variant.getId() & 0xFF) | ((markings.getId() << 8) & 0xFF00);
        CompoundTag tag = new CompoundTag();
        horse.addAdditionalSaveData(tag);
        tag.putInt("Variant", packedVariant);
        horse.readAdditionalSaveData(tag);
    }

    public static float getWhistleSpeedAttribute(Player player, ItemStack stack) {
        ModConfig.Config config = ModConfig.INSTANCE;
        int level = stack.getEnchantmentLevel(player.level().registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(ResourceKey.create(Registries.ENCHANTMENT, ResourceLocation.fromNamespaceAndPath(MountsWhistle.MOD_ID, "mount_speed"))));

        return config.attributeModifier.enchantModifier.speedModifier * (float) level;
    }

    public static float getWhistleJumpAttribute(Player player, ItemStack stack) {
        ModConfig.Config config = ModConfig.INSTANCE;
        int level = stack.getEnchantmentLevel(player.level().registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(ResourceKey.create(Registries.ENCHANTMENT, ResourceLocation.fromNamespaceAndPath(MountsWhistle.MOD_ID, "mount_jump"))));

        return config.attributeModifier.enchantModifier.jumpModifier * (float) level;
    }

    public static void toggleAutoRide(Player player, ItemStack stack) {
        boolean autoRide = !stack.getOrDefault(WhistleDataComponents.AUTO_RIDE, false);
        stack.set(WhistleDataComponents.AUTO_RIDE, autoRide);
        player.displayClientMessage(Component.translatable("message.mounts_whistle.auto_ride_" + (autoRide ? "enabled" : "disabled")), true);
    }
}
