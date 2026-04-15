package com.lucab.mounts_whistle;

import com.lucab.mounts_whistle.config.ModConfig;
import com.lucab.mounts_whistle.data_components.WhistleDataComponents;
import com.lucab.mounts_whistle.items.ItemsRegistry;
import com.lucab.mounts_whistle.sounds.ModSounds;

import com.mojang.logging.LogUtils;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import org.slf4j.Logger;

@Mod(MountsWhistle.MOD_ID)
public class MountsWhistle {
    public static final String MOD_ID = "mounts_whistle";
    public static final Logger LOGGER = LogUtils.getLogger();

    public MountsWhistle(IEventBus modEventBus, ModContainer modContainer) {
        ItemsRegistry.ITEM_REGISTRY.register(modEventBus);

        WhistleDataComponents.register(modEventBus);
        ModSounds.SOUND_EVENTS.register(modEventBus);

        modEventBus.addListener(this::addCreative);

        //Utils.config = ConfigSchema.load(FMLPaths.CONFIGDIR.get().resolve(Utils.MOD_ID + ".json").toFile());
        ModConfig.load();
    }

    public void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(ItemsRegistry.MOUNTS_WHISTLE);
        }
    }
}
