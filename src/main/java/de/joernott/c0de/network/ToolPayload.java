package de.joernott.c0de.network;

import de.joernott.c0de.C0de;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * A left click with a c0de tool on a block or an entity. The client cancels the vanilla action (breaking or
 * attacking) and sends this payload instead, because a cancelled left click doesn't reach the server on all
 * loaders.
 *
 * @param pos the clicked block
 * @param entity the ID of the clicked entity, or -1 for a block
 */
public record ToolPayload(BlockPos pos, int entity) implements CustomPacketPayload {
    public static final Type<ToolPayload> TYPE = new Type<>(C0de.id("tool"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ToolPayload> CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, ToolPayload::pos, ByteBufCodecs.VAR_INT, ToolPayload::entity, ToolPayload::new);

    @Override
    public Type<ToolPayload> type() {
        return TYPE;
    }
}
