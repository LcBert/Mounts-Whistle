package com.lucab.mounts_whistle;

import org.slf4j.Logger;
import com.mojang.logging.LogUtils;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

public class Utils {
    public static final String MOD_ID = "mounts_whistle";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static void messagePlayer(Player player, String message) {
        player.displayClientMessage(Component.literal(message), true);
    }

    public static void messagePlayer(Player player, String message, Boolean statusBar) {
        player.displayClientMessage(Component.literal(message), statusBar);
    }
}
