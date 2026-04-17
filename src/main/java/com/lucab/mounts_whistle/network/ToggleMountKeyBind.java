package com.lucab.mounts_whistle.network;

import com.lucab.mounts_whistle.MountsWhistle;
import com.lucab.mounts_whistle.capabilities.CuriosCapabilities;
import com.lucab.mounts_whistle.events.ToggleMount;
import com.lucab.mounts_whistle.items.ItemsRegistry;
import io.wispforest.accessories.api.AccessoriesAPI;
import io.wispforest.accessories.api.AccessoriesCapability;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;

import java.util.Map;
import java.util.Optional;

public record ToggleMountKeyBind() implements CustomPacketPayload {
    public static final Type<ToggleMountKeyBind> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(MountsWhistle.MOD_ID, "toggle_mount_keybind"));

    public static final StreamCodec<FriendlyByteBuf, ToggleMountKeyBind> STREAM_CODEC = StreamCodec.of(
            (buf, packet) -> {
            },
            buf -> new ToggleMountKeyBind()
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(ToggleMountKeyBind packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                ICuriosItemHandler curiosInventory = CuriosApi.getCuriosInventory(player).get();
                Map<String, ICurioStacksHandler> curios = curiosInventory.getCurios();

                curiosInventory.getStacksHandler("mounts_whistle").ifPresent(stacks -> {
                    ItemStack stack = stacks.getStacks().getStackInSlot(0);
                    if (stack.getItem() == ItemsRegistry.MOUNTS_WHISTLE.get()) {
                        ToggleMount.toggleMount(player, stack, false);
                    }
                });

                AccessoriesCapability capability = AccessoriesCapability.get(player);
                if (capability != null) {
                    capability.getAllEquipped().forEach(slot -> {
                        if (slot.reference().slotName().equals("mounts_whistle") && slot.stack().getItem() == ItemsRegistry.MOUNTS_WHISTLE.get()) {
                            ToggleMount.toggleMount(player, slot.stack(), false);
                            return;
                        }
                    });
                }
            }
        });
    }
}
