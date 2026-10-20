package de.joernott.c0de.platform.neoforge;

//? if neoforge {
/*import de.joernott.c0de.client.C0deClient;
import de.joernott.c0de.client.C0deDimensionEffects;
import de.joernott.c0de.client.PropertiesRenderer;
import de.joernott.c0de.content.C0deBlockEntities;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterDimensionSpecialEffectsEvent;

// Client initialization for NeoForge. Only loaded on the physical client.
// The render types of the transparent blocks are set in their block models.
public final class C0deNeoForgeClient {
    private C0deNeoForgeClient() {}

    public static void init(IEventBus modEventBus) {
        C0deClient.init();
        modEventBus.addListener(RegisterDimensionSpecialEffectsEvent.class,
                event -> event.register(C0deDimensionEffects.ID, new C0deDimensionEffects()));
        modEventBus.addListener(EntityRenderersEvent.RegisterRenderers.class,
                event -> C0deBlockEntities.all().forEach(
                        type -> event.registerBlockEntityRenderer(type, PropertiesRenderer::new)));
    }
}
*///?}
