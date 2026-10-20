package de.joernott.c0de.network;

import de.joernott.c0de.C0de;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * Tells a client to open the dialog of a block.
 *
 * @param pos the position of the block
 * @param dialog the kind of dialog: label, literal, operator, comment or tag
 * @param readOnly true if the properties can only be inspected, e.g. on a sheet with running processes
 * @param data the properties of the block and the data the dialog needs (e.g. the entries of lists)
 */
public record OpenDialogPayload(BlockPos pos, String dialog, boolean readOnly, CompoundTag data)
        implements CustomPacketPayload {
    public static final Type<OpenDialogPayload> TYPE = new Type<>(C0de.id("open_dialog"));

    public static final StreamCodec<RegistryFriendlyByteBuf, OpenDialogPayload> CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC,
            OpenDialogPayload::pos,
            ByteBufCodecs.STRING_UTF8,
            OpenDialogPayload::dialog,
            ByteBufCodecs.BOOL,
            OpenDialogPayload::readOnly,
            ByteBufCodecs.COMPOUND_TAG,
            OpenDialogPayload::data,
            OpenDialogPayload::new);

    @Override
    public Type<OpenDialogPayload> type() {
        return TYPE;
    }
}
