package de.joernott.c0de.network;

import de.joernott.c0de.C0de;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** The binding a player chose in the dialog of a display or I/O block; an empty program unbinds it. */
public record BindPayload(BlockPos pos, String program, String function) implements CustomPacketPayload {
    public static final Type<BindPayload> TYPE = new Type<>(C0de.id("bind"));

    public static final StreamCodec<RegistryFriendlyByteBuf, BindPayload> CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC,
            BindPayload::pos,
            ByteBufCodecs.STRING_UTF8,
            BindPayload::program,
            ByteBufCodecs.STRING_UTF8,
            BindPayload::function,
            BindPayload::new);

    @Override
    public Type<BindPayload> type() {
        return TYPE;
    }
}
