package de.joernott.c0de.network;

import de.joernott.c0de.C0de;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * A part of a program a player loads from their folder {@code c0de} onto the disk in a disk drive. Packets from
 * clients are small, so a program is sent in parts.
 *
 * @param first true for the first part, which starts a new program
 * @param last true for the last part, after which the program is written onto the disk
 * @param chunk the text of the part
 */
public record DiskUploadPayload(boolean first, boolean last, String chunk) implements CustomPacketPayload {
    /** The longest part, in characters; four bytes per character stay below the limit of 32 KiB. */
    public static final int CHUNK_LENGTH = 7_000;

    public static final Type<DiskUploadPayload> TYPE = new Type<>(C0de.id("disk_upload"));

    public static final StreamCodec<RegistryFriendlyByteBuf, DiskUploadPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL,
            DiskUploadPayload::first,
            ByteBufCodecs.BOOL,
            DiskUploadPayload::last,
            ByteBufCodecs.stringUtf8(CHUNK_LENGTH * 4),
            DiskUploadPayload::chunk,
            DiskUploadPayload::new);

    @Override
    public Type<DiskUploadPayload> type() {
        return TYPE;
    }
}
