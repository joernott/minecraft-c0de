package de.joernott.c0de.network;

import de.joernott.c0de.C0de;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * The properties a player entered in the dialog of a block. The server checks them again before it changes
 * the block.
 */
public record SavePropertiesPayload(BlockPos pos, CompoundTag properties) implements CustomPacketPayload {
    public static final Type<SavePropertiesPayload> TYPE = new Type<>(C0de.id("save_properties"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SavePropertiesPayload> CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC,
            SavePropertiesPayload::pos,
            ByteBufCodecs.COMPOUND_TAG,
            SavePropertiesPayload::properties,
            SavePropertiesPayload::new);

    @Override
    public Type<SavePropertiesPayload> type() {
        return TYPE;
    }
}
