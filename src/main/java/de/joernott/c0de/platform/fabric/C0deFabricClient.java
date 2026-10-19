package de.joernott.c0de.platform.fabric;

//? if fabric {
import de.joernott.c0de.client.C0deDimensionEffects;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.DimensionRenderingRegistry;

/** Client entry point for Fabric. */
public class C0deFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        DimensionRenderingRegistry.registerDimensionEffects(C0deDimensionEffects.ID, new C0deDimensionEffects());
    }
}
//?}
