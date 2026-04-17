package com.lucab.mounts_whistle.events;

import com.lucab.mounts_whistle.MountsWhistle;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

@EventBusSubscriber(modid = MountsWhistle.MOD_ID)
public class WhistleUse {
    @SubscribeEvent
    public static void onWhistleUse(PlayerInteractEvent.RightClickItem event) {
        ToggleMount.toggleMount(event.getEntity(), event.getItemStack(), true);
    }
}
