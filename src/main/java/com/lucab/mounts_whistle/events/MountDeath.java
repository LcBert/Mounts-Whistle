package com.lucab.mounts_whistle.events;

import com.lucab.mounts_whistle.MountsWhistle;
import com.lucab.mounts_whistle.config.ModConfig;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

@EventBusSubscriber(modid = MountsWhistle.MOD_ID)
public class MountDeath {
    @SubscribeEvent
    public static void onMountDeath(LivingDeathEvent event) {
        AbstractHorse mount = MountHelper.getMount(event.getEntity());
        if (mount == null) return;

        if(mount.isTamed()) {
            MountHelper.dropMountInventory(event.getEntity().level(), mount, null);
            event.setCanceled(true);
            mount.kill();
        }
    }

    @SubscribeEvent
    public static void onMountDamage(LivingIncomingDamageEvent event) {
        ModConfig.Config config = ModConfig.INSTANCE;
        AbstractHorse mount = MountHelper.getMount(event.getEntity());
        if (mount == null) return;

        if (config.protection.mountInvulnerable) event.setCanceled(true);
    }
}
