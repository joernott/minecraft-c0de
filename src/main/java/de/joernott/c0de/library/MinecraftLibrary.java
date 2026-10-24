package de.joernott.c0de.library;

import com.mojang.authlib.GameProfile;
import de.joernott.c0de.C0de;
import de.joernott.c0de.core.config.C0deConfig;
import de.joernott.c0de.core.error.C0deException;
import de.joernott.c0de.core.error.ErrorCode;
import de.joernott.c0de.core.library.Library;
import de.joernott.c0de.core.library.NativeCall;
import de.joernott.c0de.core.library.NativeFunction;
import de.joernott.c0de.core.library.NativeResult;
import de.joernott.c0de.core.sheet.SheetGeometry;
import de.joernott.c0de.core.value.ArrayValue;
import de.joernott.c0de.core.value.BooleanValue;
import de.joernott.c0de.core.value.HashValue;
import de.joernott.c0de.core.value.IntegerValue;
import de.joernott.c0de.core.value.StringValue;
import de.joernott.c0de.core.value.StructDefinition;
import de.joernott.c0de.core.value.StructValue;
import de.joernott.c0de.core.value.Type;
import de.joernott.c0de.core.value.Value;
import de.joernott.c0de.dimension.C0deDimension;
import de.joernott.c0de.entity.CursorEntity;
import de.joernott.c0de.entity.MarkEntity;
import de.joernott.c0de.sheet.LevelSheet;
import de.joernott.c0de.sheet.ProgramRegistry;
import de.joernott.c0de.sheet.SheetOwners;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BasePressurePlateBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.LeverBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * The library {@code c0de/minecraft} (docs/concept/12-lib-minecraft.md): functions that interact with the world
 * on behalf of the owner of the trigger block. Functions that change the world are restricted by the
 * configuration, unless the owner is an administrator; blocks on the sheets of the c0de dimension can't be
 * changed, and chunks are never loaded.
 */
public final class MinecraftLibrary implements Library {
    public static final String NAME = "c0de/minecraft";

    /** The largest radius of FIND_ENTITIES. */
    private static final int MAX_RADIUS = 64;

    public static final StructDefinition POSITION = new StructDefinition(
            NAME + "/POSITION",
            List.of(
                    element("x", Type.Simple.INTEGER),
                    element("y", Type.Simple.INTEGER),
                    element("z", Type.Simple.INTEGER),
                    element("dimension", Type.Str.UNLIMITED)));

    public static final StructDefinition ITEM_STACK = new StructDefinition(
            NAME + "/ITEM_STACK", List.of(element("id", Type.Str.UNLIMITED), element("count", Type.Simple.INTEGER)));

    public static final StructDefinition ENTITY = new StructDefinition(
            NAME + "/ENTITY",
            List.of(
                    element("uuid", Type.Str.UNLIMITED),
                    element("type", Type.Str.UNLIMITED),
                    element("name", Type.Str.UNLIMITED),
                    element("position", new Type.Struct(POSITION))));

    private static final Type STRINGS = new Type.Hash(Type.Str.UNLIMITED);

    private static StructDefinition.Element element(String name, Type type) {
        return new StructDefinition.Element(name, type, Map.of());
    }

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public Map<String, StructDefinition> types() {
        return Map.of("POSITION", POSITION, "ITEM_STACK", ITEM_STACK, "ENTITY", ENTITY);
    }

    @Override
    public Map<String, NativeFunction> functions() {
        return FUNCTIONS;
    }

    // ---------------------------------------------------------------------------------------- parameters

    private static final Predicate<Value> INTEGER = value -> value instanceof IntegerValue;
    private static final Predicate<Value> BOOLEAN = value -> value instanceof BooleanValue;
    private static final Predicate<Value> TEXT = value -> value instanceof StringValue;
    private static final Predicate<Value> A_POSITION =
            value -> value instanceof StructValue struct && struct.definition().equals(POSITION);
    private static final Predicate<Value> HASH_OF_STRINGS = value -> value instanceof HashValue;

    private static NativeFunction.Parameter position(String name) {
        return NativeFunction.Parameter.required(name, NAME + "/POSITION", A_POSITION);
    }

    private static NativeFunction.Parameter integer(String name) {
        return NativeFunction.Parameter.required(name, "INTEGER", INTEGER);
    }

    private static NativeFunction.Parameter text(String name) {
        return NativeFunction.Parameter.required(name, "STRING", TEXT);
    }

    private static final Map<String, NativeFunction> FUNCTIONS = functionMap(
            function("TICKS", List.of(), call -> NativeResult.of(new IntegerValue(host(call).level().getGameTime()))),
            function("BEEP", List.of(), MinecraftLibrary::beep),
            function(
                    "SOUND",
                    List.of(
                            integer("note"),
                            NativeFunction.Parameter.withDefault("instrument", "STRING", TEXT, new StringValue("harp"))),
                    MinecraftLibrary::sound),
            function(
                    "SAY",
                    List.of(
                            text("message"),
                            NativeFunction.Parameter.withDefault("player", "STRING", TEXT, new StringValue(""))),
                    MinecraftLibrary::say),
            function("GET_BLOCK", List.of(position("position")), MinecraftLibrary::getBlock),
            function("GET_INVENTORY", List.of(position("position")), MinecraftLibrary::getInventory),
            function("GET_LEVER", List.of(position("position")), call -> NativeResult.of(BooleanValue.of(
                    state(call, LeverBlock.class, "a lever").getValue(BlockStateProperties.POWERED)))),
            function(
                    "SET_LEVER",
                    List.of(position("position"), NativeFunction.Parameter.required("on", "BOOLEAN", BOOLEAN)),
                    MinecraftLibrary::setLever),
            function("GET_BUTTON", List.of(position("position")), call -> NativeResult.of(BooleanValue.of(
                    state(call, ButtonBlock.class, "a button").getValue(BlockStateProperties.POWERED)))),
            function("PRESS_BUTTON", List.of(position("position")), MinecraftLibrary::pressButton),
            function("GET_PRESSURE_PLATE", List.of(position("position")), MinecraftLibrary::getPressurePlate),
            function("GET_OPEN", List.of(position("position")), call -> NativeResult.of(BooleanValue.of(
                    openable(call).getValue(BlockStateProperties.OPEN)))),
            function(
                    "SET_OPEN",
                    List.of(position("position"), NativeFunction.Parameter.required("open", "BOOLEAN", BOOLEAN)),
                    MinecraftLibrary::setOpen),
            function(
                    "MOVE_ITEMS",
                    List.of(position("from"), position("to"), text("id"), integer("count")),
                    MinecraftLibrary::moveItems),
            function(
                    "DELETE_ITEMS",
                    List.of(position("position"), text("id"), integer("count")),
                    MinecraftLibrary::deleteItems),
            function(
                    "PLACE_BLOCK",
                    List.of(
                            position("position"),
                            text("id"),
                            NativeFunction.Parameter.withDefault(
                                    "properties", "HASH STRING", HASH_OF_STRINGS, new HashValue(Type.Str.UNLIMITED))),
                    MinecraftLibrary::placeBlock),
            function(
                    "REMOVE_BLOCK",
                    List.of(
                            position("position"),
                            NativeFunction.Parameter.withDefault("drop", "BOOLEAN", BOOLEAN, BooleanValue.TRUE)),
                    MinecraftLibrary::removeBlock),
            function(
                    "SPAWN_ITEM",
                    List.of(position("position"), text("id"), integer("count")),
                    MinecraftLibrary::spawnItem),
            function("SPAWN_ENTITY", List.of(position("position"), text("type")), MinecraftLibrary::spawnEntity),
            function(
                    "FIND_ENTITIES",
                    List.of(
                            position("position"),
                            integer("radius"),
                            NativeFunction.Parameter.withDefault("type", "STRING", TEXT, new StringValue(""))),
                    MinecraftLibrary::findEntities),
            function("TELEPORT", List.of(text("uuid"), position("position")), MinecraftLibrary::teleport));

    private static NativeFunction function(
            String name, List<NativeFunction.Parameter> parameters, NativeFunction.Implementation implementation) {
        // All functions of the library have at most one result, called result; GET_BLOCK has four
        List<String> results = switch (name) {
            case "GET_BLOCK" -> List.of("id", "properties", "power", "has_inventory");
            case "BEEP", "SOUND", "SAY", "SET_LEVER", "PRESS_BUTTON", "SET_OPEN", "PLACE_BLOCK", "REMOVE_BLOCK",
                    "TELEPORT" -> List.of();
            default -> List.of("result");
        };
        return new NativeFunction(name, parameters, results, implementation);
    }

    private static Map<String, NativeFunction> functionMap(NativeFunction... functions) {
        Map<String, NativeFunction> map = new LinkedHashMap<>();
        for (NativeFunction function : functions) {
            map.put(function.name(), function);
        }
        return Map.copyOf(map);
    }

    // ------------------------------------------------------------------------------------------- helpers

    private static ProcessHost host(NativeCall call) {
        if (call.process().host() instanceof ProcessHost host) {
            return host;
        }
        throw C0deException.of(ErrorCode.INVALID_ARGUMENT, NAME + " is only available in processes of the game");
    }

    private static C0deException invalid(String message) {
        return C0deException.of(ErrorCode.INVALID_ARGUMENT, message);
    }

    private static long integer(NativeCall call, String name) {
        return ((IntegerValue) call.argument(name)).value();
    }

    private static String text(NativeCall call, String name) {
        return ((StringValue) call.argument(name)).value();
    }

    private static boolean bool(NativeCall call, String name) {
        return ((BooleanValue) call.argument(name)).value();
    }

    /** A block position of the program: the level and the position. */
    private record Place(ServerLevel level, BlockPos pos) {}

    /**
     * The level and position of a POSITION argument. Raises CHUNK_NOT_LOADED if its chunk is not loaded, because
     * functions never load chunks.
     */
    private static Place place(NativeCall call, String name) {
        ProcessHost host = host(call);
        StructValue position = (StructValue) call.argument(name);
        String dimension = ((StringValue) position.get("dimension")).value();
        ServerLevel level;
        if (dimension.isEmpty()) {
            level = host.level();
        } else {
            ResourceLocation id = ResourceLocation.tryParse(dimension);
            level = id == null ? null : host.server().getLevel(ResourceKey.create(Registries.DIMENSION, id));
        }
        if (level == null) {
            throw invalid("There is no dimension " + dimension);
        }
        BlockPos pos = new BlockPos(
                (int) ((IntegerValue) position.get("x")).value(),
                (int) ((IntegerValue) position.get("y")).value(),
                (int) ((IntegerValue) position.get("z")).value());
        if (!level.isLoaded(pos)) {
            throw C0deException.of(ErrorCode.CHUNK_NOT_LOADED, "The chunk of " + pos.toShortString() + " is not loaded");
        }
        return new Place(level, pos);
    }

    /** A place whose block may be changed: not on a sheet of the c0de dimension. */
    private static Place changeable(NativeCall call, String name) {
        Place place = place(call, name);
        if (LevelSheet.isOnSheet(place.level(), place.pos()) && place.pos().getY() >= SheetGeometry.FLOOR_Y) {
            throw C0deException.of(ErrorCode.PERMISSION_DENIED, "Blocks on the sheets can't be changed by programs");
        }
        return place;
    }

    private static BlockState state(NativeCall call, Class<? extends Block> kind, String what) {
        Place place = place(call, "position");
        BlockState state = place.level().getBlockState(place.pos());
        if (!kind.isInstance(state.getBlock())) {
            throw invalid("The block at " + place.pos().toShortString() + " is not " + what);
        }
        return state;
    }

    private static BlockState openable(NativeCall call) {
        Place place = place(call, "position");
        BlockState state = place.level().getBlockState(place.pos());
        Block block = state.getBlock();
        if (!(block instanceof DoorBlock || block instanceof TrapDoorBlock || block instanceof FenceGateBlock)) {
            throw invalid("The block at " + place.pos().toShortString() + " is not a door, trapdoor or fence gate");
        }
        return state;
    }

    private static Container container(Place place) {
        if (place.level().getBlockEntity(place.pos()) instanceof Container container) {
            return container;
        }
        throw invalid("The block at " + place.pos().toShortString() + " has no inventory");
    }

    private static Item item(String id) {
        ResourceLocation key = ResourceLocation.tryParse(id);
        Optional<Item> item = key == null ? Optional.empty() : BuiltInRegistries.ITEM.getOptional(key);
        if (item.isEmpty() || item.get() == net.minecraft.world.item.Items.AIR) {
            throw invalid("There is no item " + id);
        }
        return item.get();
    }

    /**
     * True if the owner of the process is an administrator (permission level 2 or higher) and the program is theirs:
     * its sheet belongs to them, to another administrator or to nobody (blocks placed by commands). Otherwise a
     * player could write a program with the name of an administrator's program and run it with their trigger.
     */
    private static boolean isAdministrator(ProcessHost host) {
        if (!isAdministrator(host.server(), host.owner())) {
            return false;
        }
        ServerLevel c0de = host.server().getLevel(C0deDimension.LEVEL);
        if (c0de == null) {
            return true;
        }
        java.util.OptionalInt sheet = ProgramRegistry.get(c0de).sheetOf(host.program());
        Optional<SheetOwners.Owner> author =
                sheet.isPresent() ? SheetOwners.get(c0de).owner(sheet.getAsInt()) : Optional.empty();
        return author.map(a -> a.id().equals(host.owner()) || isAdministrator(host.server(), a.id()))
                .orElse(true);
    }

    /** True if the player is an administrator (permission level 2 or higher), online or not. */
    private static boolean isAdministrator(MinecraftServer server, UUID player) {
        if (player == null) {
            return false;
        }
        ServerPlayer online = server.getPlayerList().getPlayer(player);
        Optional<GameProfile> profile = online != null
                ? Optional.of(online.getGameProfile())
                : server.getProfileCache() == null ? Optional.empty() : server.getProfileCache().get(player);
        return profile.map(p -> server.getProfilePermissions(p) >= 2).orElse(false);
    }

    /** Raises PERMISSION_DENIED unless the group of the function is allowed or the owner is an administrator. */
    private static void requirePermission(NativeCall call, C0deConfig.BooleanSetting setting) {
        if (!C0de.config().get(setting) && !isAdministrator(host(call))) {
            throw C0deException.of(
                    ErrorCode.PERMISSION_DENIED,
                    "The owner of the trigger block may not use this function (" + setting.key() + ")");
        }
    }

    private static StructValue position(BlockPos pos, Level level, ProcessHost host) {
        StructValue position = new StructValue(POSITION);
        position.set("x", new IntegerValue(pos.getX()));
        position.set("y", new IntegerValue(pos.getY()));
        position.set("z", new IntegerValue(pos.getZ()));
        position.set(
                "dimension",
                new StringValue(level.dimension().equals(host.triggerLevel()) ? "" : level.dimension().location().toString()));
        return position;
    }

    // --------------------------------------------------------------------------------------------- functions

    private static NativeResult beep(NativeCall call) {
        ProcessHost host = host(call);
        host.level().playSound(
                null, host.triggerPos(), SoundEvents.NOTE_BLOCK_BELL.value(), SoundSource.RECORDS, 3.0F, 1.0F);
        return NativeResult.none();
    }

    private static NativeResult sound(NativeCall call) {
        ProcessHost host = host(call);
        long note = integer(call, "note");
        if (note < 0 || note > 24) {
            throw invalid("A note is between 0 and 24, not " + note);
        }
        String name = text(call, "instrument");
        NoteBlockInstrument instrument = null;
        for (NoteBlockInstrument candidate : NoteBlockInstrument.values()) {
            if (candidate.getSerializedName().equalsIgnoreCase(name)) {
                instrument = candidate;
            }
        }
        if (instrument == null) {
            throw invalid("There is no instrument " + name);
        }
        float pitch = (float) Math.pow(2.0, (note - 12) / 12.0);
        host.level().playSound(
                null, host.triggerPos(), instrument.getSoundEvent().value(), SoundSource.RECORDS, 3.0F, pitch);
        return NativeResult.none();
    }

    private static NativeResult say(NativeCall call) {
        ProcessHost host = host(call);
        Component message = Component.literal("[" + host.program() + "/" + host.pid() + "] " + text(call, "message"));
        String name = text(call, "player");
        if (!name.isEmpty()) {
            ServerPlayer player = host.server().getPlayerList().getPlayerByName(name);
            if (player == null) {
                throw invalid("The player " + name + " is not online");
            }
            player.sendSystemMessage(message);
            return NativeResult.none();
        }
        int radius = C0de.config().get(C0deConfig.SAY_RADIUS);
        Vec3 center = Vec3.atCenterOf(host.triggerPos());
        for (ServerPlayer player : host.level().players()) {
            if (player.position().distanceToSqr(center) <= (double) radius * radius) {
                player.sendSystemMessage(message);
            }
        }
        return NativeResult.none();
    }

    private static NativeResult getBlock(NativeCall call) {
        Place place = place(call, "position");
        BlockState state = place.level().getBlockState(place.pos());
        HashValue properties = new HashValue(Type.Str.UNLIMITED);
        for (Property<?> property : state.getProperties()) {
            properties.elements().put(property.getName(), new StringValue(valueName(state, property)));
        }
        Map<String, Value> results = new LinkedHashMap<>();
        results.put("id", new StringValue(BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString()));
        results.put("properties", properties);
        results.put("power", new IntegerValue(place.level().getBestNeighborSignal(place.pos())));
        // The blocks GET_INVENTORY, MOVE_ITEMS and DELETE_ITEMS work with
        results.put(
                "has_inventory", BooleanValue.of(place.level().getBlockEntity(place.pos()) instanceof Container));
        return NativeResult.of(results);
    }

    private static <T extends Comparable<T>> String valueName(BlockState state, Property<T> property) {
        return property.getName(state.getValue(property));
    }

    private static NativeResult getInventory(NativeCall call) {
        Container container = container(place(call, "position"));
        ArrayValue stacks = new ArrayValue(new Type.Struct(ITEM_STACK));
        for (int i = 0; i < container.getContainerSize(); i++) {
            ItemStack stack = container.getItem(i);
            StructValue value = new StructValue(ITEM_STACK);
            value.set(
                    "id",
                    new StringValue(stack.isEmpty() ? "" : BuiltInRegistries.ITEM.getKey(stack.getItem()).toString()));
            value.set("count", new IntegerValue(stack.getCount()));
            stacks.elements().add(value);
        }
        return NativeResult.of(stacks);
    }

    private static NativeResult setLever(NativeCall call) {
        BlockState state = state(call, LeverBlock.class, "a lever");
        Place place = changeable(call, "position");
        if (state.getValue(BlockStateProperties.POWERED) != bool(call, "on")) {
            ((LeverBlock) state.getBlock()).pull(state, place.level(), place.pos(), null);
        }
        return NativeResult.none();
    }

    private static NativeResult pressButton(NativeCall call) {
        BlockState state = state(call, ButtonBlock.class, "a button");
        Place place = changeable(call, "position");
        if (!state.getValue(BlockStateProperties.POWERED)) {
            ((ButtonBlock) state.getBlock()).press(state, place.level(), place.pos(), null);
        }
        return NativeResult.none();
    }

    private static NativeResult getPressurePlate(NativeCall call) {
        BlockState state = state(call, BasePressurePlateBlock.class, "a pressure plate");
        if (state.hasProperty(BlockStateProperties.POWER)) {
            return NativeResult.of(new IntegerValue(state.getValue(BlockStateProperties.POWER)));
        }
        return NativeResult.of(new IntegerValue(state.getValue(BlockStateProperties.POWERED) ? 15 : 0));
    }

    private static NativeResult setOpen(NativeCall call) {
        BlockState state = openable(call);
        Place place = changeable(call, "position");
        // Players can't open iron doors and trapdoors by hand
        if (state.is(Blocks.IRON_DOOR) || state.is(Blocks.IRON_TRAPDOOR)) {
            requirePermission(call, C0deConfig.ALLOW_BLOCK_CHANGES);
        }
        boolean open = bool(call, "open");
        if (state.getValue(BlockStateProperties.OPEN) == open) {
            return NativeResult.none();
        }
        if (state.getBlock() instanceof DoorBlock door) {
            door.setOpen(null, place.level(), state, place.pos(), open);
        } else {
            place.level().setBlock(place.pos(), state.setValue(BlockStateProperties.OPEN, open), Block.UPDATE_ALL);
        }
        return NativeResult.none();
    }

    private static NativeResult moveItems(NativeCall call) {
        requirePermission(call, C0deConfig.ALLOW_INVENTORY_CHANGES);
        Container from = container(place(call, "from"));
        Container to = container(place(call, "to"));
        Item item = item(text(call, "id"));
        long wanted = integer(call, "count");
        long moved = 0;
        for (int i = 0; i < from.getContainerSize() && moved < wanted; i++) {
            ItemStack source = from.getItem(i);
            if (!source.is(item)) {
                continue;
            }
            for (int j = 0; j < to.getContainerSize() && moved < wanted && !source.isEmpty(); j++) {
                ItemStack target = to.getItem(j);
                int space;
                if (target.isEmpty()) {
                    space = Math.min(source.getMaxStackSize(), to.getMaxStackSize());
                } else if (ItemStack.isSameItemSameComponents(source, target)) {
                    space = Math.min(target.getMaxStackSize(), to.getMaxStackSize()) - target.getCount();
                } else {
                    continue;
                }
                int amount = (int) Math.min(Math.min(space, source.getCount()), wanted - moved);
                if (amount <= 0) {
                    continue;
                }
                if (target.isEmpty()) {
                    to.setItem(j, source.copyWithCount(amount));
                } else {
                    target.grow(amount);
                }
                source.shrink(amount);
                moved += amount;
            }
        }
        from.setChanged();
        to.setChanged();
        return NativeResult.of(new IntegerValue(moved));
    }

    private static NativeResult deleteItems(NativeCall call) {
        requirePermission(call, C0deConfig.ALLOW_INVENTORY_CHANGES);
        Container container = container(place(call, "position"));
        Item item = item(text(call, "id"));
        long wanted = integer(call, "count");
        long removed = 0;
        for (int i = 0; i < container.getContainerSize() && removed < wanted; i++) {
            ItemStack stack = container.getItem(i);
            if (stack.is(item)) {
                int amount = (int) Math.min(stack.getCount(), wanted - removed);
                stack.shrink(amount);
                removed += amount;
            }
        }
        container.setChanged();
        return NativeResult.of(new IntegerValue(removed));
    }

    private static NativeResult placeBlock(NativeCall call) {
        requirePermission(call, C0deConfig.ALLOW_BLOCK_CHANGES);
        Place place = changeable(call, "position");
        String id = text(call, "id");
        ResourceLocation key = ResourceLocation.tryParse(id);
        Optional<Block> block = key == null ? Optional.empty() : BuiltInRegistries.BLOCK.getOptional(key);
        if (block.isEmpty()) {
            throw invalid("There is no block " + id);
        }
        BlockState state = block.get().defaultBlockState();
        HashValue properties = (HashValue) call.argument("properties");
        for (Map.Entry<String, Value> entry : properties.elements().entrySet()) {
            Property<?> property = block.get().getStateDefinition().getProperty(entry.getKey());
            if (property == null) {
                throw invalid(id + " has no property " + entry.getKey());
            }
            state = withValue(state, property, entry.getValue().text());
        }
        if (!place.level().getBlockState(place.pos()).canBeReplaced()) {
            throw invalid("The block at " + place.pos().toShortString() + " can't be replaced");
        }
        place.level().setBlockAndUpdate(place.pos(), state);
        return NativeResult.none();
    }

    private static <T extends Comparable<T>> BlockState withValue(BlockState state, Property<T> property, String text) {
        return property.getValue(text)
                .map(value -> state.setValue(property, value))
                .orElseThrow(() -> invalid(text + " is not a value of " + property.getName()));
    }

    private static NativeResult removeBlock(NativeCall call) {
        requirePermission(call, C0deConfig.ALLOW_BLOCK_CHANGES);
        Place place = changeable(call, "position");
        place.level().destroyBlock(place.pos(), bool(call, "drop"));
        return NativeResult.none();
    }

    private static NativeResult spawnItem(NativeCall call) {
        requirePermission(call, C0deConfig.ALLOW_SPAWNING);
        Place place = place(call, "position");
        long count = integer(call, "count");
        if (count < 1 || count > 64) {
            throw invalid("The count is between 1 and 64, not " + count);
        }
        Vec3 center = Vec3.atCenterOf(place.pos());
        ItemEntity entity = new ItemEntity(
                place.level(), center.x, center.y, center.z, new ItemStack(item(text(call, "id")), (int) count));
        place.level().addFreshEntity(entity);
        return NativeResult.of(new StringValue(entity.getUUID().toString()));
    }

    private static NativeResult spawnEntity(NativeCall call) {
        requirePermission(call, C0deConfig.ALLOW_SPAWNING);
        Place place = place(call, "position");
        String type = text(call, "type");
        Optional<EntityType<?>> entityType = EntityType.byString(type);
        if (entityType.isEmpty()) {
            throw invalid("There is no entity type " + type);
        }
        Entity entity = entityType.get().spawn(place.level(), place.pos(), MobSpawnType.COMMAND);
        if (entity == null) {
            throw invalid("An entity of the type " + type + " can't be spawned");
        }
        return NativeResult.of(new StringValue(entity.getUUID().toString()));
    }

    private static NativeResult findEntities(NativeCall call) {
        ProcessHost host = host(call);
        Place place = place(call, "position");
        long radius = integer(call, "radius");
        if (radius < 1 || radius > MAX_RADIUS) {
            throw invalid("The radius is between 1 and " + MAX_RADIUS + ", not " + radius);
        }
        String type = text(call, "type");
        Vec3 center = Vec3.atCenterOf(place.pos());
        List<Entity> entities = place.level().getEntities((Entity) null, AABB.ofSize(center, 2 * radius, 2 * radius, 2 * radius), entity ->
                !(entity instanceof CursorEntity || entity instanceof MarkEntity)
                        && entity.position().distanceTo(center) <= radius
                        && (type.isEmpty() || EntityType.getKey(entity.getType()).toString().equals(type)));
        ArrayValue result = new ArrayValue(new Type.Struct(ENTITY));
        for (Entity entity : entities) {
            StructValue value = new StructValue(ENTITY);
            value.set("uuid", new StringValue(entity.getUUID().toString()));
            value.set("type", new StringValue(EntityType.getKey(entity.getType()).toString()));
            value.set("name", new StringValue(entity.getName().getString()));
            value.set("position", position(entity.blockPosition(), place.level(), host));
            result.elements().add(value);
        }
        return NativeResult.of(result);
    }

    private static NativeResult teleport(NativeCall call) {
        requirePermission(call, C0deConfig.ALLOW_TELEPORTING);
        ProcessHost host = host(call);
        UUID uuid;
        try {
            uuid = UUID.fromString(text(call, "uuid"));
        } catch (IllegalArgumentException e) {
            throw invalid("Not a UUID: " + text(call, "uuid"));
        }
        Entity entity = null;
        for (ServerLevel level : host.server().getAllLevels()) {
            entity = level.getEntity(uuid);
            if (entity != null) {
                break;
            }
        }
        if (entity == null) {
            throw invalid("No loaded entity has the UUID " + uuid);
        }
        if (entity instanceof ServerPlayer && !isAdministrator(host)) {
            throw C0deException.of(ErrorCode.PERMISSION_DENIED, "Only administrators can teleport players");
        }
        Place place = place(call, "position");
        entity.teleportTo(
                place.level(),
                place.pos().getX() + 0.5,
                place.pos().getY(),
                place.pos().getZ() + 0.5,
                Set.of(),
                entity.getYRot(),
                entity.getXRot());
        return NativeResult.none();
    }
}
