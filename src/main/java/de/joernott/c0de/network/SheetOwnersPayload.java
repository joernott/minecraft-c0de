package de.joernott.c0de.network;

import de.joernott.c0de.C0de;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** The names of the owners of all sheets (sheet number as key), for the sheet information on the floor. */
public record SheetOwnersPayload(CompoundTag owners) implements CustomPacketPayload {
    public static final Type<SheetOwnersPayload> TYPE = new Type<>(C0de.id("sheet_owners"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SheetOwnersPayload> CODEC =
            StreamCodec.composite(ByteBufCodecs.COMPOUND_TAG, SheetOwnersPayload::owners, SheetOwnersPayload::new);

    @Override
    public Type<SheetOwnersPayload> type() {
        return TYPE;
    }
}
