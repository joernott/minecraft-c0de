package de.joernott.c0de.platform.fabric;

//? if fabric {
import de.joernott.c0de.blockentity.PropertiesBlockEntity;
import de.joernott.c0de.client.C0deClient;
import de.joernott.c0de.client.C0deDimensionEffects;
import de.joernott.c0de.client.ExtrudedModel;
import de.joernott.c0de.client.LineNumbers;
import de.joernott.c0de.client.ProcessRenderers;
import de.joernott.c0de.client.PropertiesRenderer;
import de.joernott.c0de.client.TriggerScreen;
import de.joernott.c0de.content.C0deEntities;
import de.joernott.c0de.content.C0deItems;
import de.joernott.c0de.content.C0deMenus;
import de.joernott.c0de.content.C0deBlockEntities;
import de.joernott.c0de.content.C0deBlocks;
import de.joernott.c0de.content.CodeBlockType;
import de.joernott.c0de.network.ClientReceivers;
import de.joernott.c0de.platform.Platform;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.DimensionRenderingRegistry;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.level.block.entity.BlockEntityType;

/** Client entry point for Fabric. */
public class C0deFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        DimensionRenderingRegistry.registerDimensionEffects(C0deDimensionEffects.ID, new C0deDimensionEffects());
        C0deClient.init();
        Platform.FabricPlatform.CLIENTBOUND.forEach(C0deFabricClient::receive);
        for (CodeBlockType type : CodeBlockType.values()) {
            // PROGRAM carries a book with transparent pixels
            if (type.isTransparent() || type == CodeBlockType.PROGRAM) {
                BlockRenderLayerMap.INSTANCE.putBlock(C0deBlocks.get(type), RenderType.cutout());
            }
        }
        // The vanilla registration method is private in 1.21.1
        C0deBlockEntities.all().forEach(C0deFabricClient::renderer);
        for (var block : java.util.List.of(
                C0deBlocks.TRIGGER_BLOCK, C0deBlocks.TIMER_BLOCK, C0deBlocks.DEBUG_TRIGGER_BLOCK)) {
            BlockRenderLayerMap.INSTANCE.putBlock(block.get(), RenderType.cutout());
        }
        BlockEntityRendererRegistry.register(C0deBlockEntities.TRIGGER.get(), ProcessRenderers.Camouflage::new);
        BlockEntityRendererRegistry.register(C0deBlockEntities.PID.get(), ProcessRenderers.Pid::new);
        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(
                C0deEntities.CURSOR.get(), ProcessRenderers.Cursor::new);
        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(
                C0deEntities.MARK.get(), ProcessRenderers.Mark::new);
        net.minecraft.client.gui.screens.MenuScreens.register(C0deMenus.TRIGGER.get(), TriggerScreen::new);
        net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin.register(plugin -> plugin
                .modifyModelAfterBake()
                .register((model, context) ->
                        context.topLevelId() == null ? model : ExtrudedModel.wrap(context.topLevelId(), model)));
        net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry.ITEM.register(
                C0deClient::itemColor, C0deItems.MARKER.get());
        net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents.AFTER_ENTITIES.register(
                context -> LineNumbers.render(context.camera()));
    }

    private static <E extends PropertiesBlockEntity> void renderer(BlockEntityType<E> type) {
        BlockEntityRendererProvider<PropertiesBlockEntity> provider = PropertiesRenderer::new;
        BlockEntityRendererRegistry.register(type, provider);
    }

    private static <T extends CustomPacketPayload> void receive(CustomPacketPayload.Type<T> type) {
        ClientPlayNetworking.registerGlobalReceiver(type, (payload, context) -> ClientReceivers.dispatch(payload));
    }
}
//?}
