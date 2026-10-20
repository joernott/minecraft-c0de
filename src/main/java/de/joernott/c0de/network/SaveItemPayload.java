package de.joernott.c0de.network;

import de.joernott.c0de.C0de;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** The properties a player entered in the dialog of the variable or constant item in their main hand. */
public record SaveItemPayload(CompoundTag properties) implements CustomPacketPayload {
    public static final Type<SaveItemPayload> TYPE = new Type<>(C0de.id("save_item"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SaveItemPayload> CODEC =
            StreamCodec.composite(ByteBufCodecs.COMPOUND_TAG, SaveItemPayload::properties, SaveItemPayload::new);

    @Override
    public Type<SaveItemPayload> type() {
        return TYPE;
    }
}
