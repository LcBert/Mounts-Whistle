package com.lucab.mounts_whistle.events;

import com.lucab.mounts_whistle.MountsWhistle;
import com.lucab.mounts_whistle.config.ModConfig;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

import java.util.UUID;

@EventBusSubscriber(modid = MountsWhistle.MOD_ID)
public class MountDespawn {
    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Post event) {
        Entity entity = event.getEntity();
        Level level = entity.level();
        MinecraftServer server = level.getServer();
        AbstractHorse mount = MountHelper.getMount(entity);
        ModConfig.Config config = ModConfig.INSTANCE;

        if (mount == null || !mount.isTamed() || !MountHelper.mountsTimer.containsKey(mount.getUUID())) return;

        if (mount.getPassengers().stream().findFirst().isPresent()) {
            MountHelper.mountsTimer.put(entity.getUUID(), System.currentTimeMillis());
        }

        long timeExceed = System.currentTimeMillis() - MountHelper.mountsTimer.get(mount.getUUID());
        if ((timeExceed / 1000) >= config.despawn.despawnTime) MountHelper.despawnMount(level, mount, null);

        UUID ownerUUID = mount.getOwnerUUID();
        if (ownerUUID == null) return;
        Player player = level.getPlayerByUUID(ownerUUID);
        if (player == null) {
            MountHelper.despawnMount(level, mount, null);
            return;
        }

        if (entity.distanceTo(player) > config.despawn.despawnDistance) MountHelper.despawnMount(level, mount, null);
    }
}
