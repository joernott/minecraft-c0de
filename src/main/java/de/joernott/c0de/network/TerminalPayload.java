package de.joernott.c0de.network;

import de.joernott.c0de.C0de;
import java.util.UUID;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * The state of a terminal of a laptop or tablet, sent to the players who have it open.
 *
 * @param id the ID of the terminal
 * @param open true to open the terminal, false for an update of an open terminal
 * @param data the screen, the binding, the prompt of a waiting INPUT and, when opening, the programs
 */
public record TerminalPayload(UUID id, boolean open, CompoundTag data) implements CustomPacketPayload {
    public static final Type<TerminalPayload> TYPE = new Type<>(C0de.id("terminal"));

    public static final StreamCodec<RegistryFriendlyByteBuf, TerminalPayload> CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC,
            TerminalPayload::id,
            ByteBufCodecs.BOOL,
            TerminalPayload::open,
            ByteBufCodecs.COMPOUND_TAG,
            TerminalPayload::data,
            TerminalPayload::new);

    @Override
    public Type<TerminalPayload> type() {
        return TYPE;
    }
}
