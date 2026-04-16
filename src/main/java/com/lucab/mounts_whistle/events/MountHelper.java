package com.lucab.mounts_whistle.events;

import com.lucab.mounts_whistle.MountsWhistle;
import com.lucab.mounts_whistle.config.ModConfig;
import com.lucab.mounts_whistle.data_components.WhistleDataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.entity.animal.horse.Markings;
import net.minecraft.world.entity.animal.horse.Variant;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MountHelper {
    public static final Map<UUID, Long> mountsTimer = new HashMap<>();

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
            serverLevel.sendParticles(ParticleTypes.SMOKE,
                    mount.getX(), mount.getY() + 0.5, mount.getZ(),
                    100, 0.5, 0.5, 0.5, 0.05);
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
        ItemStack armor = mount.getArmorSlots().iterator().next();
        if (config.inventory.dropArmor) {
            level.addFreshEntity(new ItemEntity(level, mount.getX(), mount.getY(), mount.getZ(), armor));
            if (stack != null) stack.remove(WhistleDataComponents.ARMOR_ITEM);
        } else {
            if (stack != null) stack.set(WhistleDataComponents.ARMOR_ITEM, armor.getItem());
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
        int level = stack.getEnchantmentLevel(
                player.level().registryAccess()
                        .lookupOrThrow(Registries.ENCHANTMENT)
                        .getOrThrow(ResourceKey.create(Registries.ENCHANTMENT,
                                ResourceLocation.fromNamespaceAndPath(MountsWhistle.MOD_ID, "mount_speed")))
        );

        return config.attributeModifier.enchantModifier.speedModifier * (float) level;
    }

    public static float getWhistleJumpAttribute(Player player, ItemStack stack) {
        ModConfig.Config config = ModConfig.INSTANCE;
        int level = stack.getEnchantmentLevel(
                player.level().registryAccess()
                        .lookupOrThrow(Registries.ENCHANTMENT)
                        .getOrThrow(ResourceKey.create(Registries.ENCHANTMENT,
                                ResourceLocation.fromNamespaceAndPath(MountsWhistle.MOD_ID, "mount_jump")))
        );

        return config.attributeModifier.enchantModifier.jumpModifier * (float) level;
    }
}
