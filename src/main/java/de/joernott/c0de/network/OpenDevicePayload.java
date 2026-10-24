package de.joernott.c0de.network;

import de.joernott.c0de.C0de;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** A player asks for the binding dialog of a device, e.g. with the button in the dialog of an I/O block. */
public record OpenDevicePayload(BlockPos pos) implements CustomPacketPayload {
    public static final Type<OpenDevicePayload> TYPE = new Type<>(C0de.id("open_device"));

    public static final StreamCodec<RegistryFriendlyByteBuf, OpenDevicePayload> CODEC =
            StreamCodec.composite(BlockPos.STREAM_CODEC, OpenDevicePayload::pos, OpenDevicePayload::new);

    @Override
    public Type<OpenDevicePayload> type() {
        return TYPE;
    }
}
