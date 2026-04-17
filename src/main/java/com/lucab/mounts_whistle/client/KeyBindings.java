package com.lucab.mounts_whistle.client;

import com.lucab.mounts_whistle.MountsWhistle;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import org.lwjgl.glfw.GLFW;

@EventBusSubscriber(modid = MountsWhistle.MOD_ID, value = Dist.CLIENT)
public class KeyBindings {
    public static final String KEY_CATEGORY = "key.categories." + MountsWhistle.MOD_ID;

    public static final KeyMapping WHISTLE_TOGGLE_MOUNT = new KeyMapping(
            "key." + MountsWhistle.MOD_ID + ".whistle_toggle_mount",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_V,
            KEY_CATEGORY
    );

    public static final KeyMapping WHISTLE_TOGGLE_AUTO_RIDE = new KeyMapping(
            "key." + MountsWhistle.MOD_ID + ".whistle_toggle_auto_ride",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_B,
            KEY_CATEGORY
    );

    @SubscribeEvent
    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(WHISTLE_TOGGLE_MOUNT);
        event.register(WHISTLE_TOGGLE_AUTO_RIDE);
    }
}
