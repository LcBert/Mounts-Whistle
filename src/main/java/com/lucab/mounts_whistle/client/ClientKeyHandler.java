package com.lucab.mounts_whistle.client;

import com.lucab.mounts_whistle.MountsWhistle;
import com.lucab.mounts_whistle.network.ToggleMountKeyBind;
import net.minecraft.client.Minecraft;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.awt.event.KeyEvent;

@EventBusSubscriber(modid = MountsWhistle.MOD_ID)
public class ClientKeyHandler {
    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post e) {
        Minecraft mc = Minecraft.getInstance();

        if (KeyBindings.WHISTLE_KEY.consumeClick()) {
            PacketDistributor.sendToServer(new ToggleMountKeyBind());
        }
    }
}
