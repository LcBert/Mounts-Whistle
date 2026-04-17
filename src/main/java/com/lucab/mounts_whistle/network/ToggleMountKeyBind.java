package com.lucab.mounts_whistle.network;

import com.lucab.mounts_whistle.MountsWhistle;
import com.lucab.mounts_whistle.config.ModConfig;
import com.lucab.mounts_whistle.events.MountHelper;
import com.lucab.mounts_whistle.items.ItemsRegistry;
import io.wispforest.accessories.api.AccessoriesCapability;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import top.theillusivec4.curios.api.CuriosApi;

public record ToggleMountKeyBind(PacketActionType action) implements CustomPacketPayload {
    public static final Type<ToggleMountKeyBind> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(MountsWhistle.MOD_ID, "toggle_mount_keybind"));

    public static final StreamCodec<FriendlyByteBuf, ToggleMountKeyBind> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.fromCodec(PacketActionType.CODEC),
            ToggleMountKeyBind::action,
            ToggleMountKeyBind::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(ToggleMountKeyBind packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                switch (packet.action) {
                    case TOGGLE_MOUNT -> toggleMount(player);
                    case TOGGLE_AUTO_RIDE -> toggleAutoRide(player);
                }
            }
        });
    }

    private static ItemStack getStack(ServerPlayer player) {
        ItemStack curiosStack = CuriosApi.getCuriosInventory(player)
                .flatMap(inventory -> inventory.getStacksHandler("mounts_whistle")
                        .map(stacks -> stacks.getStacks().getStackInSlot(0)))
                .filter(stack -> stack.getItem() == ItemsRegistry.MOUNTS_WHISTLE.get())
                .orElse(ItemStack.EMPTY);

        if (!curiosStack.isEmpty()) return curiosStack;

        AccessoriesCapability capability = AccessoriesCapability.get(player);
        if (capability != null) {
            for (var slot : capability.getAllEquipped()) {
                if (slot.reference().slotName().equals("mounts_whistle") && slot.stack().getItem() == ItemsRegistry.MOUNTS_WHISTLE.get()) {
                    return slot.stack();
                }
            }
        }

        return ItemStack.EMPTY;
    }

    private static void toggleMount(ServerPlayer player) {
        ItemStack stack = getStack(player);
        if (stack.isEmpty()) return;
        MountHelper.toggleMount(player, stack, false);
    }

    private static void toggleAutoRide(ServerPlayer player) {
        if (!ModConfig.INSTANCE.protection.enableAutoRide) return;

        ItemStack stack = getStack(player);
        if (stack.isEmpty()) return;

        MountHelper.toggleAutoRide(player, stack);
    }
}
