package de.joernott.c0de.network;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * The receivers of clientbound payloads. The client entry points register them; the network code of both
 * loaders passes received payloads to {@link #dispatch}. This class doesn't refer to client classes, so it
 * can be loaded on dedicated servers.
 */
public final class ClientReceivers {
    private static final Map<CustomPacketPayload.Type<?>, Consumer<CustomPacketPayload>> RECEIVERS =
            new ConcurrentHashMap<>();

    private ClientReceivers() {}

    /** Registers the receiver of a payload type. */
    @SuppressWarnings("unchecked")
    public static <T extends CustomPacketPayload> void register(
            CustomPacketPayload.Type<T> type, Consumer<T> receiver) {
        RECEIVERS.put(type, payload -> receiver.accept((T) payload));
    }

    /** Passes a received payload to its receiver. Called on the client thread. */
    public static void dispatch(CustomPacketPayload payload) {
        Consumer<CustomPacketPayload> receiver = RECEIVERS.get(payload.type());
        if (receiver != null) {
            receiver.accept(payload);
        }
    }
}
