package de.joernott.c0de.network;

import de.joernott.c0de.C0de;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** A player gives a sheet to another player, chosen in the dialog of the wrench. */
public record GiveSheetPayload(int sheet, String player) implements CustomPacketPayload {
    public static final Type<GiveSheetPayload> TYPE = new Type<>(C0de.id("give_sheet"));

    public static final StreamCodec<RegistryFriendlyByteBuf, GiveSheetPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT,
            GiveSheetPayload::sheet,
            ByteBufCodecs.STRING_UTF8,
            GiveSheetPayload::player,
            GiveSheetPayload::new);

    @Override
    public Type<GiveSheetPayload> type() {
        return TYPE;
    }
}
