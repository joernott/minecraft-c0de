package de.joernott.c0de.network;

import de.joernott.c0de.C0de;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * What a player does in the terminal they have open.
 *
 * @param action {@code line} (a line typed for INPUT), {@code key} (a key typed for INKEY), {@code bind} (a new
 *     binding) or {@code close}
 * @param text the line, the key, the program or (when closing) the ID of the terminal
 * @param function the function of a new binding
 */
public record TerminalActionPayload(String action, String text, String function) implements CustomPacketPayload {
    public static final Type<TerminalActionPayload> TYPE = new Type<>(C0de.id("terminal_action"));

    public static final StreamCodec<RegistryFriendlyByteBuf, TerminalActionPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8,
            TerminalActionPayload::action,
            ByteBufCodecs.stringUtf8(1024),
            TerminalActionPayload::text,
            ByteBufCodecs.STRING_UTF8,
            TerminalActionPayload::function,
            TerminalActionPayload::new);

    @Override
    public Type<TerminalActionPayload> type() {
        return TYPE;
    }
}
