package de.joernott.c0de.platform;

import de.joernott.c0de.C0de;
import de.joernott.c0de.network.ClientReceivers;
import java.nio.file.Path;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

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

    /**
     * Registers a payload that clients send to the server. The handler runs on the server thread with the
     * player who sent it.
     */
    <T extends CustomPacketPayload> void registerServerbound(
            CustomPacketPayload.Type<T> type,
            StreamCodec<? super RegistryFriendlyByteBuf, T> codec,
            BiConsumer<T, ServerPlayer> handler);

    /**
     * Registers a payload that the server sends to clients. On the client, it is passed on the client thread
     * to the receiver registered in {@link ClientReceivers}.
     */
    <T extends CustomPacketPayload> void registerClientbound(
            CustomPacketPayload.Type<T> type, StreamCodec<? super RegistryFriendlyByteBuf, T> codec);

    /** Creates the type of a block entity of some blocks. */
    <T extends BlockEntity> BlockEntityType<T> blockEntityType(
            BiFunction<BlockPos, BlockState, T> factory, Block... blocks);

    /** Calls the listener at the end of every server tick. */
    void onServerTickEnd(java.util.function.Consumer<net.minecraft.server.MinecraftServer> listener);

    /** Calls the listener when a chunk of a server level has been loaded. */
    void onChunkLoad(BiConsumer<net.minecraft.server.level.ServerLevel, net.minecraft.world.level.ChunkPos> listener);

    /** Sends a payload to a player. */
    void sendToPlayer(ServerPlayer player, CustomPacketPayload payload);

    /** Sends a payload to the server. Only called on the client. */
    void sendToServer(CustomPacketPayload payload);

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

        /** The clientbound payload types, whose receivers the client entry point registers. */
        public static final java.util.List<CustomPacketPayload.Type<?>> CLIENTBOUND =
                new java.util.concurrent.CopyOnWriteArrayList<>();

        @Override
        public <T extends CustomPacketPayload> void registerServerbound(
                CustomPacketPayload.Type<T> type,
                StreamCodec<? super RegistryFriendlyByteBuf, T> codec,
                BiConsumer<T, ServerPlayer> handler) {
            net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry.playC2S().register(type, codec);
            net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.registerGlobalReceiver(
                    type, (payload, context) -> handler.accept(payload, context.player()));
        }

        @Override
        public <T extends CustomPacketPayload> void registerClientbound(
                CustomPacketPayload.Type<T> type, StreamCodec<? super RegistryFriendlyByteBuf, T> codec) {
            net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry.playS2C().register(type, codec);
            CLIENTBOUND.add(type);
        }

        @Override
        public <T extends BlockEntity> BlockEntityType<T> blockEntityType(
                BiFunction<BlockPos, BlockState, T> factory, Block... blocks) {
            // The factory interface of the vanilla builder is not accessible on Fabric
            return net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder.create(
                            factory::apply, blocks)
                    .build();
        }

        @Override
        public void onServerTickEnd(java.util.function.Consumer<net.minecraft.server.MinecraftServer> listener) {
            net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents.END_SERVER_TICK.register(listener::accept);
        }

        @Override
        public void onChunkLoad(
                BiConsumer<net.minecraft.server.level.ServerLevel, net.minecraft.world.level.ChunkPos> listener) {
            net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents.CHUNK_LOAD.register(
                    (level, chunk) -> listener.accept(level, chunk.getPos()));
        }

        @Override
        public void sendToPlayer(ServerPlayer player, CustomPacketPayload payload) {
            net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.send(player, payload);
        }

        @Override
        public void sendToServer(CustomPacketPayload payload) {
            net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.send(payload);
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

        @Override
        public <T extends CustomPacketPayload> void registerServerbound(
                CustomPacketPayload.Type<T> type,
                StreamCodec<? super RegistryFriendlyByteBuf, T> codec,
                BiConsumer<T, ServerPlayer> handler) {
            modEventBus.addListener(net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent.class,
                    event -> event.registrar("1").playToServer(
                            type, codec, (payload, context) -> handler.accept(payload, (ServerPlayer) context.player())));
        }

        @Override
        public <T extends CustomPacketPayload> void registerClientbound(
                CustomPacketPayload.Type<T> type, StreamCodec<? super RegistryFriendlyByteBuf, T> codec) {
            modEventBus.addListener(net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent.class,
                    event -> event.registrar("1").playToClient(
                            type, codec, (payload, context) -> ClientReceivers.dispatch(payload)));
        }

        @Override
        public <T extends BlockEntity> BlockEntityType<T> blockEntityType(
                BiFunction<BlockPos, BlockState, T> factory, Block... blocks) {
            return BlockEntityType.Builder.of(factory::apply, blocks).build(null);
        }

        @Override
        public void onServerTickEnd(java.util.function.Consumer<net.minecraft.server.MinecraftServer> listener) {
            net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(
                    net.neoforged.neoforge.event.tick.ServerTickEvent.Post.class,
                    event -> listener.accept(event.getServer()));
        }

        @Override
        public void onChunkLoad(
                BiConsumer<net.minecraft.server.level.ServerLevel, net.minecraft.world.level.ChunkPos> listener) {
            net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(
                    net.neoforged.neoforge.event.level.ChunkEvent.Load.class, event -> {
                        if (event.getLevel() instanceof net.minecraft.server.level.ServerLevel level) {
                            listener.accept(level, event.getChunk().getPos());
                        }
                    });
        }

        @Override
        public void sendToPlayer(ServerPlayer player, CustomPacketPayload payload) {
            net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(player, payload);
        }

        @Override
        public void sendToServer(CustomPacketPayload payload) {
            net.neoforged.neoforge.network.PacketDistributor.sendToServer(payload);
        }
    }
    *///?}
}
