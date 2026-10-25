package de.joernott.c0de.platform;

import de.joernott.c0de.C0de;
import de.joernott.c0de.compat.Compat;
import de.joernott.c0de.network.ClientReceivers;
import java.nio.file.Path;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.Level;
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

    /**
     * Makes the container of a block entity type available to pipes and cables of other mods. Fabric's transfer
     * API finds containers by itself; NeoForge needs an item handler capability.
     */
    <T extends BlockEntity & net.minecraft.world.WorldlyContainer> void exposeContainer(
            Supplier<BlockEntityType<T>> type);

    /** Calls the listener at the end of every server tick. */
    void onServerTickEnd(java.util.function.Consumer<net.minecraft.server.MinecraftServer> listener);

    /** Calls the listener when a chunk of a server level has been loaded. */
    void onChunkLoad(BiConsumer<net.minecraft.server.level.ServerLevel, net.minecraft.world.level.ChunkPos> listener);

    /** Creates a menu type whose menus get data from the server when they are opened. */
    <T extends AbstractContainerMenu> MenuType<T> menuType(MenuFactory<T> factory);

    /** Opens a menu for a player and sends the data the menu needs. */
    void openMenu(ServerPlayer player, MenuProvider provider, CompoundTag data);

    /**
     * Calls the check before a player breaks or places a block. If it returns a reason, the action is cancelled
     * and the player gets the reason.
     */
    void onBlockChange(BlockChangeCheck check);

    /**
     * Calls the handler when a player starts to break a block (left click), on the client and the server. If it
     * returns true, the action is handled and the block is not broken.
     */
    void onAttackBlock(AttackBlockHandler handler);

    /**
     * Calls the handler when a player attacks an entity (left click), on the client and the server. If it returns
     * true, the attack is handled and doesn't hurt the entity.
     */
    void onAttackEntity(AttackEntityHandler handler);

    /** Calls the listener when a player joins the server. */
    void onPlayerJoin(java.util.function.Consumer<ServerPlayer> listener);

    /** Calls the listener on the server when a player right-clicks a block, before the block reacts. */
    void onUseBlock(UseBlockListener listener);

    /** A listener of players right-clicking blocks. */
    @FunctionalInterface
    interface UseBlockListener {
        void used(ServerPlayer player, Level level, BlockPos pos);
    }

    /** A handler of a player starting to break a block. */
    @FunctionalInterface
    interface AttackBlockHandler {
        /** @param face the clicked face of the block, or null if it is not known */
        boolean handle(Player player, Level level, BlockPos pos, net.minecraft.core.Direction face);
    }

    /** A handler of a player attacking an entity. */
    @FunctionalInterface
    interface AttackEntityHandler {
        boolean handle(Player player, Level level, net.minecraft.world.entity.Entity entity);
    }

    /** Creates the menus of a menu type on the client and the server. */
    @FunctionalInterface
    interface MenuFactory<T extends AbstractContainerMenu> {
        T create(int id, Inventory inventory, CompoundTag data);
    }

    /** A check of a block that a player is going to break or replace. */
    @FunctionalInterface
    interface BlockChangeCheck {
        Optional<Component> check(Player player, Level level, BlockPos pos);
    }

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
            //? if >=26.1 {
            /*net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry.serverboundPlay().register(type, codec);
            *///?} else {
            net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry.playC2S().register(type, codec);
            //?}
            net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.registerGlobalReceiver(
                    type, (payload, context) -> handler.accept(payload, context.player()));
        }

        @Override
        public <T extends CustomPacketPayload> void registerClientbound(
                CustomPacketPayload.Type<T> type, StreamCodec<? super RegistryFriendlyByteBuf, T> codec) {
            //? if >=26.1 {
            /*net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry.clientboundPlay().register(type, codec);
            *///?} else {
            net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry.playS2C().register(type, codec);
            //?}
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
        public <T extends BlockEntity & net.minecraft.world.WorldlyContainer> void exposeContainer(
                Supplier<BlockEntityType<T>> type) {
            // The transfer API wraps block entities that are containers by itself
        }

        @Override
        public void onServerTickEnd(java.util.function.Consumer<net.minecraft.server.MinecraftServer> listener) {
            net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents.END_SERVER_TICK.register(listener::accept);
        }

        @Override
        public void onChunkLoad(
                BiConsumer<net.minecraft.server.level.ServerLevel, net.minecraft.world.level.ChunkPos> listener) {
            //? if >=26.1 {
            /*net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents.CHUNK_LOAD.register(
                    (level, chunk, generated) -> listener.accept(level, chunk.getPos()));
            *///?} else {
            net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents.CHUNK_LOAD.register(
                    (level, chunk) -> listener.accept(level, chunk.getPos()));
            //?}
        }

        @Override
        public <T extends AbstractContainerMenu> MenuType<T> menuType(MenuFactory<T> factory) {
            //? if >=26.1 {
            /*return new net.fabricmc.fabric.api.menu.v1.ExtendedMenuType<>(
                    factory::create, net.minecraft.network.codec.ByteBufCodecs.COMPOUND_TAG);
            *///?} else {
            return new net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType<>(
                    factory::create, net.minecraft.network.codec.ByteBufCodecs.COMPOUND_TAG);
            //?}
        }

        @Override
        public void openMenu(ServerPlayer player, MenuProvider provider, CompoundTag data) {
            //? if >=26.1 {
            /*player.openMenu(new net.fabricmc.fabric.api.menu.v1.ExtendedMenuProvider<CompoundTag>() {
            *///?} else {
            player.openMenu(new net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory<CompoundTag>() {
            //?}
                @Override
                public CompoundTag getScreenOpeningData(ServerPlayer target) {
                    return data;
                }

                @Override
                public Component getDisplayName() {
                    return provider.getDisplayName();
                }

                @Override
                public AbstractContainerMenu createMenu(int id, Inventory inventory, Player target) {
                    return provider.createMenu(id, inventory, target);
                }
            });
        }

        @Override
        public void onBlockChange(BlockChangeCheck check) {
            net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents.BEFORE.register(
                    (level, player, pos, state, blockEntity) -> {
                        Optional<Component> reason = check.check(player, level, pos);
                        reason.ifPresent(message -> Compat.message(player, message, true));
                        return reason.isEmpty();
                    });
            net.fabricmc.fabric.api.event.player.UseBlockCallback.EVENT.register((player, level, hand, hit) -> {
                // Placing a block replaces the clicked block or the block in front of the clicked side
                if (!(player.getItemInHand(hand).getItem() instanceof net.minecraft.world.item.BlockItem)) {
                    return net.minecraft.world.InteractionResult.PASS;
                }
                for (BlockPos pos : java.util.List.of(hit.getBlockPos(), hit.getBlockPos().relative(hit.getDirection()))) {
                    Optional<Component> reason = check.check(player, level, pos);
                    if (reason.isPresent()) {
                        if (!level.isClientSide()) {
                            Compat.message(player, reason.get(), true);
                        }
                        return net.minecraft.world.InteractionResult.FAIL;
                    }
                }
                return net.minecraft.world.InteractionResult.PASS;
            });
        }

        @Override
        public void onAttackBlock(AttackBlockHandler handler) {
            net.fabricmc.fabric.api.event.player.AttackBlockCallback.EVENT.register(
                    (player, level, hand, pos, direction) -> handler.handle(player, level, pos, direction)
                            ? net.minecraft.world.InteractionResult.SUCCESS
                            : net.minecraft.world.InteractionResult.PASS);
        }

        @Override
        public void onAttackEntity(AttackEntityHandler handler) {
            net.fabricmc.fabric.api.event.player.AttackEntityCallback.EVENT.register(
                    (player, level, hand, entity, hit) -> handler.handle(player, level, entity)
                            ? net.minecraft.world.InteractionResult.SUCCESS
                            : net.minecraft.world.InteractionResult.PASS);
        }

        @Override
        public void onUseBlock(UseBlockListener listener) {
            net.fabricmc.fabric.api.event.player.UseBlockCallback.EVENT.register((player, level, hand, hit) -> {
                if (player instanceof ServerPlayer serverPlayer) {
                    listener.used(serverPlayer, level, hit.getBlockPos());
                }
                return net.minecraft.world.InteractionResult.PASS;
            });
        }

        @Override
        public void onPlayerJoin(java.util.function.Consumer<ServerPlayer> listener) {
            net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents.JOIN.register(
                    (connection, sender, server) -> listener.accept(connection.getPlayer()));
        }

        @Override
        public void sendToPlayer(ServerPlayer player, CustomPacketPayload payload) {
            // Players without the mod (or GameTest mock players) can't receive the payloads of the mod
            if (net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.canSend(player, payload.type())) {
                net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.send(player, payload);
            }
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
            //? if >=1.21.11 {
            /^return net.neoforged.fml.loading.FMLEnvironment.getDist().isClient();
            ^///?} else {
            return net.neoforged.fml.loading.FMLEnvironment.dist.isClient();
            //?}
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
            //? if >=1.21.2 {
            /^return new BlockEntityType<>(factory::apply, java.util.Set.of(blocks));
            ^///?} else {
            return BlockEntityType.Builder.of(factory::apply, blocks).build(null);
            //?}
        }

        @Override
        public <T extends BlockEntity & net.minecraft.world.WorldlyContainer> void exposeContainer(
                Supplier<BlockEntityType<T>> type) {
            modEventBus.addListener(
                    net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent.class,
                    //? if >=1.21.11 {
                    /^event -> event.registerBlockEntity(
                            net.neoforged.neoforge.capabilities.Capabilities.Item.BLOCK,
                            type.get(),
                            (entity, side) -> side == null
                                    ? net.neoforged.neoforge.transfer.item.VanillaContainerWrapper.of(entity)
                                    : new net.neoforged.neoforge.transfer.item.WorldlyContainerWrapper(entity, side)));
                    ^///?} else {
                    event -> event.registerBlockEntity(
                            net.neoforged.neoforge.capabilities.Capabilities.ItemHandler.BLOCK,
                            type.get(),
                            (entity, side) -> side == null
                                    ? new net.neoforged.neoforge.items.wrapper.InvWrapper(entity)
                                    : new net.neoforged.neoforge.items.wrapper.SidedInvWrapper(entity, side)));
                    //?}
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
        public <T extends AbstractContainerMenu> MenuType<T> menuType(MenuFactory<T> factory) {
            return net.neoforged.neoforge.common.extensions.IMenuTypeExtension.create(
                    (id, inventory, buffer) -> factory.create(id, inventory, buffer.readNbt()));
        }

        @Override
        public void openMenu(ServerPlayer player, MenuProvider provider, CompoundTag data) {
            player.openMenu(provider, buffer -> buffer.writeNbt(data));
        }

        @Override
        public void onBlockChange(BlockChangeCheck check) {
            net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(
                    net.neoforged.neoforge.event.level.BlockEvent.BreakEvent.class, event -> {
                        if (event.getLevel() instanceof Level level) {
                            check.check(event.getPlayer(), level, event.getPos()).ifPresent(message -> {
                                Compat.message(event.getPlayer(), message, true);
                                event.setCanceled(true);
                            });
                        }
                    });
            net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(
                    net.neoforged.neoforge.event.level.BlockEvent.EntityPlaceEvent.class, event -> {
                        if (event.getEntity() instanceof Player player && event.getLevel() instanceof Level level) {
                            check.check(player, level, event.getPos()).ifPresent(message -> {
                                Compat.message(player, message, true);
                                event.setCanceled(true);
                            });
                        }
                    });
        }

        @Override
        public void onAttackBlock(AttackBlockHandler handler) {
            net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(
                    net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.LeftClickBlock.class, event -> {
                        if (event.getAction()
                                        == net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.LeftClickBlock
                                                .Action.START
                                && handler.handle(event.getEntity(), event.getLevel(), event.getPos(), event.getFace())) {
                            event.setCanceled(true);
                        }
                    });
        }

        @Override
        public void onAttackEntity(AttackEntityHandler handler) {
            net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(
                    net.neoforged.neoforge.event.entity.player.AttackEntityEvent.class, event -> {
                        if (handler.handle(event.getEntity(), event.getEntity().level(), event.getTarget())) {
                            event.setCanceled(true);
                        }
                    });
        }

        @Override
        public void onUseBlock(UseBlockListener listener) {
            net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(
                    net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.RightClickBlock.class, event -> {
                        if (event.getEntity() instanceof ServerPlayer player) {
                            listener.used(player, event.getLevel(), event.getPos());
                        }
                    });
        }

        @Override
        public void onPlayerJoin(java.util.function.Consumer<ServerPlayer> listener) {
            net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(
                    net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent.class, event -> {
                        if (event.getEntity() instanceof ServerPlayer player) {
                            listener.accept(player);
                        }
                    });
        }

        @Override
        public void sendToPlayer(ServerPlayer player, CustomPacketPayload payload) {
            // Players without the mod (or GameTest mock players) can't receive the payloads of the mod
            if (player.connection != null && player.connection.hasChannel(payload)) {
                net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(player, payload);
            }
        }

        @Override
        public void sendToServer(CustomPacketPayload payload) {
            //? if >=1.21.11 {
            /^net.neoforged.neoforge.client.network.ClientPacketDistributor.sendToServer(payload);
            ^///?} else {
            net.neoforged.neoforge.network.PacketDistributor.sendToServer(payload);
            //?}
        }
    }
    *///?}
}
