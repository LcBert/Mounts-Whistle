package com.lucab.mounts_whistle;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.event.AddReloadListenerEvent;

@EventBusSubscriber
public class ReloadConfig {
    @SubscribeEvent
    public static void onReload(AddReloadListenerEvent event) {
        Utils.config = ConfigSchema.load(FMLPaths.CONFIGDIR.get().resolve(Utils.MOD_ID + ".json").toFile());
    }
}