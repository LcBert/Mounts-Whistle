package com.lucab.mounts_whistle;

import com.lucab.mounts_whistle.config.ModConfig;
import com.lucab.mounts_whistle.creative.ModCreativeTabs;
import com.lucab.mounts_whistle.data_components.WhistleDataComponents;
import com.lucab.mounts_whistle.items.ItemsRegistry;
import com.lucab.mounts_whistle.sounds.ModSounds;

import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;

@Mod(MountsWhistle.MOD_ID)
public class MountsWhistle {
    public static final String MOD_ID = "mounts_whistle";
    public static final Logger LOGGER = LogUtils.getLogger();

    public MountsWhistle(IEventBus modEventBus, ModContainer modContainer) {
        ItemsRegistry.ITEM_REGISTRY.register(modEventBus);
        ModCreativeTabs.CREATIVE_MODE_TABS.register(modEventBus);

        WhistleDataComponents.register(modEventBus);
        ModSounds.SOUND_EVENTS.register(modEventBus);

        //Utils.config = ConfigSchema.load(FMLPaths.CONFIGDIR.get().resolve(Utils.MOD_ID + ".json").toFile());
        ModConfig.load();
    }
}
