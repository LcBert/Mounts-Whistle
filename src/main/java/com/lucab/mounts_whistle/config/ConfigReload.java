package com.lucab.mounts_whistle.config;

import com.lucab.mounts_whistle.MountsWhistle;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AddReloadListenerEvent;

@EventBusSubscriber(modid = MountsWhistle.MOD_ID)
public class ConfigReload {
    @SubscribeEvent
    public static void onReloadListener(AddReloadListenerEvent event) {
        ModConfig.load();
    }
}
