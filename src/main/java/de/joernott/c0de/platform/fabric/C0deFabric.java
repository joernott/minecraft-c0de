package de.joernott.c0de.platform.fabric;

//? if fabric {
import de.joernott.c0de.C0de;
import net.fabricmc.api.ModInitializer;

/** Entry point for Fabric. */
public class C0deFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        C0de.init();
    }
}
//?}
