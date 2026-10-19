package de.joernott.c0de.platform.neoforge;

//? if neoforge {
/*import de.joernott.c0de.client.C0deDimensionEffects;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.RegisterDimensionSpecialEffectsEvent;

// Client initialization for NeoForge. Only loaded on the physical client.
public final class C0deNeoForgeClient {
    private C0deNeoForgeClient() {}

    public static void init(IEventBus modEventBus) {
        modEventBus.addListener(RegisterDimensionSpecialEffectsEvent.class,
                event -> event.register(C0deDimensionEffects.ID, new C0deDimensionEffects()));
    }
}
*///?}
