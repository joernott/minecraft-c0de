package de.joernott.c0de.platform;

import java.nio.file.Path;

/**
 * Access to the functionality of the mod loader. All loader-specific code outside of the entry points
 * goes through this interface, so that the rest of the mod is the same for Fabric and NeoForge.
 */
public sealed interface Platform {
    Platform INSTANCE =
        /*? if fabric {*/new FabricPlatform();
        /*?} elif neoforge *///new NeoForgePlatform();

    /** Name of the mod loader, e.g. "Fabric". */
    String loaderName();

    /** Directory of the configuration files. */
    Path configDir();

    /** True on the physical client, false on a dedicated server. */
    boolean isClient();

    /** True if a mod with this ID is loaded. */
    boolean isModLoaded(String id);

    //? if fabric {
    final class FabricPlatform implements Platform {
        private final net.fabricmc.loader.api.FabricLoader loader = net.fabricmc.loader.api.FabricLoader.getInstance();

        @Override
        public String loaderName() {
            return "Fabric";
        }

        @Override
        public Path configDir() {
            return loader.getConfigDir();
        }

        @Override
        public boolean isClient() {
            return loader.getEnvironmentType() == net.fabricmc.api.EnvType.CLIENT;
        }

        @Override
        public boolean isModLoaded(String id) {
            return loader.isModLoaded(id);
        }
    }
    //?} elif neoforge {
    /*final class NeoForgePlatform implements Platform {
        @Override
        public String loaderName() {
            return "NeoForge";
        }

        @Override
        public Path configDir() {
            return net.neoforged.fml.loading.FMLPaths.CONFIGDIR.get();
        }

        @Override
        public boolean isClient() {
            return net.neoforged.fml.loading.FMLEnvironment.dist.isClient();
        }

        @Override
        public boolean isModLoaded(String id) {
            return net.neoforged.fml.ModList.get().isLoaded(id);
        }
    }
    *///?}
}
