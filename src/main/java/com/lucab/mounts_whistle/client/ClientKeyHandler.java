package com.lucab.mounts_whistle.client;

import com.lucab.mounts_whistle.MountsWhistle;
import com.lucab.mounts_whistle.network.PacketActionType;
import com.lucab.mounts_whistle.network.ToggleMountKeyBind;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid = MountsWhistle.MOD_ID)
public class ClientKeyHandler {
    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post e) {
        if (KeyBindings.WHISTLE_TOGGLE_MOUNT.consumeClick()) {
            PacketDistributor.sendToServer(new ToggleMountKeyBind(PacketActionType.TOGGLE_MOUNT));
        }

        if (KeyBindings.WHISTLE_TOGGLE_AUTO_RIDE.consumeClick()) {
            PacketDistributor.sendToServer(new ToggleMountKeyBind(PacketActionType.TOGGLE_AUTO_RIDE));
        }
    }
}
