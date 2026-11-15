package de.joernott.c0de.network;

import de.joernott.c0de.C0de;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * A part of the answer of a client to a {@link VaultRequestPayload}. Packets from clients are small, so the
 * answer (which may contain the encrypted file or the secrets) is sent in parts.
 *
 * @param request the number of the request
 * @param last true for the last part, after which the server reads the answer
 * @param chunk the bytes of the part
 */
public record VaultResponsePayload(int request, boolean last, byte[] chunk) implements CustomPacketPayload {
    /** The longest part, in bytes; it stays below the limit of 32 KiB of packets from clients. */
    public static final int CHUNK_LENGTH = 30_000;

    public static final Type<VaultResponsePayload> TYPE = new Type<>(C0de.id("vault_response"));

    public static final StreamCodec<RegistryFriendlyByteBuf, VaultResponsePayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT,
            VaultResponsePayload::request,
            ByteBufCodecs.BOOL,
            VaultResponsePayload::last,
            ByteBufCodecs.byteArray(CHUNK_LENGTH),
            VaultResponsePayload::chunk,
            VaultResponsePayload::new);

    @Override
    public Type<VaultResponsePayload> type() {
        return TYPE;
    }
}
