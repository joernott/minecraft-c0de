package de.joernott.c0de.network;

import de.joernott.c0de.C0de;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * A player saved the program of the disk in the disk drive they have open into their folder {@code c0de}. The
 * client writes the file itself; the server only lets the drive sound for the players around it (task 13.3).
 */
public record DiskSavedPayload() implements CustomPacketPayload {
    public static final Type<DiskSavedPayload> TYPE = new Type<>(C0de.id("disk_saved"));

    public static final StreamCodec<RegistryFriendlyByteBuf, DiskSavedPayload> CODEC =
            StreamCodec.unit(new DiskSavedPayload());

    @Override
    public Type<DiskSavedPayload> type() {
        return TYPE;
    }
}
