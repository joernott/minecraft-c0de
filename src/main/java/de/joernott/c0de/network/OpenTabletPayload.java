package de.joernott.c0de.network;

import de.joernott.c0de.C0de;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** A player left-clicked with a tablet in the main hand: the server opens its terminal. */
public record OpenTabletPayload() implements CustomPacketPayload {
    public static final OpenTabletPayload INSTANCE = new OpenTabletPayload();
    public static final Type<OpenTabletPayload> TYPE = new Type<>(C0de.id("open_tablet"));
    public static final StreamCodec<ByteBuf, OpenTabletPayload> CODEC = StreamCodec.unit(INSTANCE);

    @Override
    public Type<OpenTabletPayload> type() {
        return TYPE;
    }
}
