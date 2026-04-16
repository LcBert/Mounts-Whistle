package com.lucab.mounts_whistle.creative;

import com.lucab.mounts_whistle.ModEnchantments;
import com.lucab.mounts_whistle.MountsWhistle;
import com.lucab.mounts_whistle.items.ItemsRegistry;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MountsWhistle.MOD_ID);

    public static final Supplier<CreativeModeTab> MOUNTS_WHISTLE_TAB = CREATIVE_MODE_TABS.register(
            "mounts_whistle",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.mounts_whistle.mounts_whistle"))
                    .icon(() -> new ItemStack(ItemsRegistry.MOUNTS_WHISTLE.get()))
                    .displayItems((parameters, output) -> {
                        output.accept(ItemsRegistry.MOUNTS_WHISTLE.get());

                        HolderLookup.RegistryLookup<Enchantment> registryLookup =
                                parameters.holders().lookupOrThrow(Registries.ENCHANTMENT);

                        addEnchantmentBooks(output, registryLookup, ModEnchantments.MOUNT_SPEED);
                        addEnchantmentBooks(output, registryLookup, ModEnchantments.MOUNT_JUMP);
                    })
                    .build()
    );

    private static void addEnchantmentBooks(CreativeModeTab.Output output,
                                            HolderLookup.RegistryLookup<Enchantment> registryLookup,
                                            net.minecraft.resources.ResourceKey<Enchantment> enchantmentKey) {
        var enchantment = registryLookup.getOrThrow(enchantmentKey);
        int maxLevel = enchantment.value().getMaxLevel();

        for (int level = 1; level <= maxLevel; level++) {
            ItemStack book = createEnchantmentBook(registryLookup, enchantmentKey, level);
            if (!book.isEmpty()) {
                output.accept(book, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            }
        }
    }

    private static ItemStack createEnchantmentBook(HolderLookup.RegistryLookup<Enchantment> registryLookup,
                                                   net.minecraft.resources.ResourceKey<Enchantment> enchantmentKey,
                                                   int level) {
        ItemStack enchantedBook = new ItemStack(Items.ENCHANTED_BOOK);
        ItemEnchantments.Mutable storedEnchantments = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);

        var enchantment = registryLookup.getOrThrow(enchantmentKey);
        if (level < 1 || level > enchantment.value().getMaxLevel()) {
            return ItemStack.EMPTY;
        }

        storedEnchantments.set(enchantment, level);

        ItemEnchantments finalEnchantments = storedEnchantments.toImmutable();
        if (finalEnchantments.isEmpty()) {
            return ItemStack.EMPTY;
        }

        enchantedBook.set(DataComponents.STORED_ENCHANTMENTS, finalEnchantments);
        return enchantedBook;
    }
}

