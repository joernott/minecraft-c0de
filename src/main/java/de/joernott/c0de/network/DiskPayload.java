package de.joernott.c0de.network;

import de.joernott.c0de.C0de;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * A choice in a dialog of the c0de disk.
 *
 * @param action {@code overwrite} to export a program over an existing file, {@code import} to import a file
 * @param pos the PROGRAM block (export) or the floor of the empty sheet (import)
 * @param program the name of the program of the file
 */
public record DiskPayload(String action, BlockPos pos, String program) implements CustomPacketPayload {
    public static final Type<DiskPayload> TYPE = new Type<>(C0de.id("disk"));

    public static final StreamCodec<RegistryFriendlyByteBuf, DiskPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8,
            DiskPayload::action,
            BlockPos.STREAM_CODEC,
            DiskPayload::pos,
            ByteBufCodecs.STRING_UTF8,
            DiskPayload::program,
            DiskPayload::new);

    @Override
    public Type<DiskPayload> type() {
        return TYPE;
    }
}
