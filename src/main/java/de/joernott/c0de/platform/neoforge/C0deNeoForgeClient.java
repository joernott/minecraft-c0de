package de.joernott.c0de.platform.neoforge;

//? if neoforge {
/*import de.joernott.c0de.client.C0deClient;
import de.joernott.c0de.client.C0deDimensionEffects;
import de.joernott.c0de.client.LineNumbers;
import de.joernott.c0de.client.ProcessRenderers;
import de.joernott.c0de.client.PropertiesRenderer;
import de.joernott.c0de.client.TrapezoidModel;
import de.joernott.c0de.client.TriggerScreen;
import de.joernott.c0de.content.C0deEntities;
import de.joernott.c0de.content.C0deMenus;
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
        modEventBus.addListener(EntityRenderersEvent.RegisterRenderers.class, event -> {
            C0deBlockEntities.all().forEach(type -> event.registerBlockEntityRenderer(type, PropertiesRenderer::new));
            event.registerBlockEntityRenderer(C0deBlockEntities.TRIGGER.get(), ProcessRenderers.Camouflage::new);
            event.registerBlockEntityRenderer(C0deBlockEntities.PID.get(), ProcessRenderers.Pid::new);
            event.registerEntityRenderer(C0deEntities.CURSOR.get(), ProcessRenderers.Cursor::new);
            event.registerEntityRenderer(C0deEntities.MARK.get(), ProcessRenderers.Mark::new);
        });
        modEventBus.addListener(net.neoforged.neoforge.client.event.ModelEvent.ModifyBakingResult.class,
                event -> event.getModels().replaceAll(TrapezoidModel::wrap));
        modEventBus.addListener(net.neoforged.neoforge.client.event.RegisterMenuScreensEvent.class,
                event -> event.register(C0deMenus.TRIGGER.get(), TriggerScreen::new));
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(
                net.neoforged.neoforge.client.event.RenderLevelStageEvent.class, event -> {
                    if (event.getStage() == net.neoforged.neoforge.client.event.RenderLevelStageEvent.Stage.AFTER_ENTITIES) {
                        LineNumbers.render(event.getCamera());
                    }
                });
    }
}
*///?}
