package de.joernott.c0de.platform.neoforge;

//? if neoforge {
/*import de.joernott.c0de.client.C0deClient;
import de.joernott.c0de.client.DisplayRenderer;
import de.joernott.c0de.client.ExtrudedModel;
import de.joernott.c0de.client.LineNumbers;
import de.joernott.c0de.client.ProcessRenderers;
import de.joernott.c0de.client.PropertiesRenderer;
import de.joernott.c0de.client.TriggerScreen;
import de.joernott.c0de.content.C0deEntities;
import de.joernott.c0de.content.C0deItems;
import de.joernott.c0de.content.C0deMenus;
import de.joernott.c0de.content.C0deBlockEntities;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

// Client initialization for NeoForge. Only loaded on the physical client.
// The render types of the transparent blocks are set in their block models.
public final class C0deNeoForgeClient {
    private C0deNeoForgeClient() {}

    public static void init(IEventBus modEventBus) {
        C0deClient.init();
        //? if <1.21.11 {
        modEventBus.addListener(net.neoforged.neoforge.client.event.RegisterDimensionSpecialEffectsEvent.class,
                event -> event.register(
                        de.joernott.c0de.client.C0deDimensionEffects.ID,
                        new de.joernott.c0de.client.C0deDimensionEffects()));
        //?}
        modEventBus.addListener(EntityRenderersEvent.RegisterRenderers.class, event -> {
            C0deBlockEntities.all().forEach(type -> event.registerBlockEntityRenderer(type, PropertiesRenderer::new));
            event.registerBlockEntityRenderer(C0deBlockEntities.TRIGGER.get(), ProcessRenderers.Camouflage::new);
            event.registerBlockEntityRenderer(C0deBlockEntities.PID.get(), ProcessRenderers.Pid::new);
            event.registerBlockEntityRenderer(C0deBlockEntities.DISPLAY.get(), DisplayRenderer::new);
            event.registerEntityRenderer(C0deEntities.CURSOR.get(), ProcessRenderers.Cursor::new);
            event.registerEntityRenderer(C0deEntities.MARK.get(), ProcessRenderers.Mark::new);
        });
        //? if >=1.21.11 {
        /^// The text marker is colored by the tint of its item definition (assets/c0de/items/marker.json); the
        // models of the language blocks name the loader of the extruded models (src/main/versioned/block-models)
        modEventBus.addListener(net.neoforged.neoforge.client.event.ModelEvent.RegisterLoaders.class,
                event -> event.register(de.joernott.c0de.C0de.id("extruded"), ExtrudedModel.LOADER));
        ^///?} else {
        modEventBus.addListener(net.neoforged.neoforge.client.event.RegisterColorHandlersEvent.Item.class,
                event -> event.register(C0deClient::itemColor, C0deItems.MARKER.get()));
        modEventBus.addListener(net.neoforged.neoforge.client.event.ModelEvent.ModifyBakingResult.class,
                event -> event.getModels().replaceAll(ExtrudedModel::wrap));
        //?}
        modEventBus.addListener(net.neoforged.neoforge.client.event.RegisterMenuScreensEvent.class,
                event -> {
                    event.register(C0deMenus.TRIGGER.get(), TriggerScreen::new);
                    event.register(C0deMenus.IO.get(), de.joernott.c0de.client.IoScreen::new);
                    event.register(C0deMenus.DISPLAY.get(), de.joernott.c0de.client.DisplayScreen::new);
                    event.register(C0deMenus.DISK_DRIVE.get(), de.joernott.c0de.client.DiskDriveScreen::new);
                });
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(
                net.neoforged.neoforge.client.event.InputEvent.InteractionKeyMappingTriggered.class, event -> {
                    if (event.isAttack() && C0deClient.attack()) {
                        event.setSwingHand(false);
                        event.setCanceled(true);
                    }
                });
        //? if >=26.1 {
        /^net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(
                net.neoforged.neoforge.client.event.RenderLevelStageEvent.AfterOpaqueFeatures.class,
                event -> LineNumbers.renderImmediately(
                        event.getLevelRenderState().cameraRenderState.pos, event.getPoseStack()));
        ^///?} elif >=1.21.11 {
        /^net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(
                net.neoforged.neoforge.client.event.RenderLevelStageEvent.AfterEntities.class,
                event -> LineNumbers.renderImmediately(
                        event.getLevelRenderState().cameraRenderState.pos, event.getPoseStack()));
        ^///?} else {
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(
                net.neoforged.neoforge.client.event.RenderLevelStageEvent.class, event -> {
                    if (event.getStage() == net.neoforged.neoforge.client.event.RenderLevelStageEvent.Stage.AFTER_ENTITIES) {
                        LineNumbers.render(event.getCamera());
                    }
                });
        //?}
    }
}
*///?}
