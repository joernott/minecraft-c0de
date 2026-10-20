package de.joernott.c0de.network;

import de.joernott.c0de.C0de;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** The configuration a player chose in the dialog of a trigger block. */
public record TriggerConfigPayload(BlockPos pos, String program, String function, int interval)
        implements CustomPacketPayload {
    public static final Type<TriggerConfigPayload> TYPE = new Type<>(C0de.id("trigger_config"));

    public static final StreamCodec<RegistryFriendlyByteBuf, TriggerConfigPayload> CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC,
            TriggerConfigPayload::pos,
            ByteBufCodecs.STRING_UTF8,
            TriggerConfigPayload::program,
            ByteBufCodecs.STRING_UTF8,
            TriggerConfigPayload::function,
            ByteBufCodecs.VAR_INT,
            TriggerConfigPayload::interval,
            TriggerConfigPayload::new);

    @Override
    public Type<TriggerConfigPayload> type() {
        return TYPE;
    }
}
