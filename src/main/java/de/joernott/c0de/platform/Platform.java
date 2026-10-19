package de.joernott.c0de.platform;

import de.joernott.c0de.C0de;
import java.nio.file.Path;
import java.util.function.Supplier;
import net.minecraft.core.Registry;

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

    /**
     * Registers an object in a registry of Minecraft under the name {@code c0de:<name>}. The returned
     * supplier MUST NOT be called before the registration is complete (on NeoForge, before the registry
     * events have been fired).
     */
    <T> Supplier<T> register(Registry<? super T> registry, String name, Supplier<T> factory);

    /** Adds a source of data packs to the pack repository of every server. */
    void addServerDataPackSource(net.minecraft.server.packs.repository.RepositorySource source);

    //? if fabric {
    final class FabricPlatform implements Platform {
        /** Data pack sources, added to the pack repositories of servers by {@code PackRepositoryMixin}. */
        public static final java.util.List<net.minecraft.server.packs.repository.RepositorySource> SERVER_DATA_SOURCES =
                new java.util.concurrent.CopyOnWriteArrayList<>();

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

        @Override
        public <T> Supplier<T> register(Registry<? super T> registry, String name, Supplier<T> factory) {
            T value = Registry.register(registry, C0de.id(name), factory.get());
            return () -> value;
        }

        @Override
        public void addServerDataPackSource(net.minecraft.server.packs.repository.RepositorySource source) {
            SERVER_DATA_SOURCES.add(source);
        }
    }
    //?} elif neoforge {
    /*final class NeoForgePlatform implements Platform {
        private static net.neoforged.bus.api.IEventBus modEventBus;
        private final java.util.Map<net.minecraft.resources.ResourceKey<?>, net.neoforged.neoforge.registries.DeferredRegister<?>>
                registers = new java.util.HashMap<>();

        // Called by the entry point before the mod is initialized
        public static void setModEventBus(net.neoforged.bus.api.IEventBus bus) {
            modEventBus = bus;
        }

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

        @Override
        @SuppressWarnings("unchecked")
        public <T> Supplier<T> register(Registry<? super T> registry, String name, Supplier<T> factory) {
            net.neoforged.neoforge.registries.DeferredRegister<Object> deferred =
                    (net.neoforged.neoforge.registries.DeferredRegister<Object>) registers.computeIfAbsent(registry.key(), key -> {
                        var created = net.neoforged.neoforge.registries.DeferredRegister.create(
                                (net.minecraft.resources.ResourceKey<Registry<Object>>) key, C0de.MOD_ID);
                        created.register(modEventBus);
                        return created;
                    });
            return (Supplier<T>) deferred.register(name, factory);
        }

        @Override
        public void addServerDataPackSource(net.minecraft.server.packs.repository.RepositorySource source) {
            modEventBus.addListener(net.neoforged.neoforge.event.AddPackFindersEvent.class, event -> {
                if (event.getPackType() == net.minecraft.server.packs.PackType.SERVER_DATA) {
                    event.addRepositorySource(source);
                }
            });
        }
    }
    *///?}
}
