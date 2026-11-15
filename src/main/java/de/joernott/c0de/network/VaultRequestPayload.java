package de.joernott.c0de.network;

import de.joernott.c0de.C0de;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * A request of the server to the client of a player to work on their persistent vault
 * (docs/concept/20-lib-security.md): create it, open it, or put or remove a secret. The client answers with
 * {@link VaultResponsePayload}s.
 *
 * @param request the number of the request, repeated in the answer
 * @param action {@code create}, {@code open}, {@code put} or {@code remove}
 * @param data the arguments, written by {@code VaultMessages}
 */
public record VaultRequestPayload(int request, String action, byte[] data) implements CustomPacketPayload {
    public static final Type<VaultRequestPayload> TYPE = new Type<>(C0de.id("vault_request"));

    public static final StreamCodec<RegistryFriendlyByteBuf, VaultRequestPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT,
            VaultRequestPayload::request,
            ByteBufCodecs.STRING_UTF8,
            VaultRequestPayload::action,
            ByteBufCodecs.BYTE_ARRAY,
            VaultRequestPayload::data,
            VaultRequestPayload::new);

    @Override
    public Type<VaultRequestPayload> type() {
        return TYPE;
    }
}
