package de.joernott.c0de.process;

import de.joernott.c0de.C0de;
import de.joernott.c0de.blockentity.PidBlockEntity;
import de.joernott.c0de.blockentity.TriggerBlockEntity;
import de.joernott.c0de.content.C0deBlocks;
import de.joernott.c0de.content.C0deEntities;
import de.joernott.c0de.content.C0deItems;
import de.joernott.c0de.core.config.C0deConfig;
import de.joernott.c0de.core.cursor.Cursor;
import de.joernott.c0de.core.cursor.CursorInspection;
import de.joernott.c0de.core.cursor.CursorSettings;
import de.joernott.c0de.core.cursor.CursorSnapshot;
import de.joernott.c0de.core.cursor.CursorState;
import de.joernott.c0de.core.edit.ItemRules;
import de.joernott.c0de.core.error.C0deException;
import de.joernott.c0de.core.eval.Declarations;
import de.joernott.c0de.core.eval.TypeRegistry;
import de.joernott.c0de.core.library.standard.JsonValues;
import de.joernott.c0de.core.line.Line;
import de.joernott.c0de.core.line.Sheet;
import de.joernott.c0de.core.program.Program;
import de.joernott.c0de.core.program.ProgramParser;
import de.joernott.c0de.core.program.Statement;
import de.joernott.c0de.core.program.TypeText;
import de.joernott.c0de.core.sheet.SheetGeometry;
import de.joernott.c0de.core.value.StructValue;
import de.joernott.c0de.core.value.Value;
import de.joernott.c0de.dimension.C0deDimension;
import de.joernott.c0de.entity.CursorEntity;
import de.joernott.c0de.item.ValueItem;
import de.joernott.c0de.library.ModLibraries;
import de.joernott.c0de.library.ProcessHost;
import de.joernott.c0de.platform.Platform;
import de.joernott.c0de.sheet.LevelSheet;
import de.joernott.c0de.sheet.ProgramRegistry;
import java.time.Clock;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.TicketType;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.saveddata.SavedData;

/**
 * The running processes (docs/concept/13-running-programs.md): starts them for trigger blocks, moves their
 * cursors one step per tick, ends them and stores them with the c0de dimension, so that they continue after a
 * restart. The chunks of the sheets and trigger blocks of running processes are kept loaded.
 */
public class ProcessManager extends SavedData {
    private static final String NAME = "c0de_processes";

    // Vanilla (Fabric) can't load saved data without a data fixer type; NeoForge allows null
    private static final DataFixTypes DATA_FIX_TYPE =
        /*? if fabric {*/DataFixTypes.SAVED_DATA_COMMAND_STORAGE;
        /*?} else *///null;

    private static final TicketType<ChunkPos> TICKET =
            TicketType.create("c0de_process", Comparator.comparingLong(ChunkPos::toLong));

    private final Map<UUID, Process> processes = new LinkedHashMap<>();

    /** Processes loaded from the save data, restored at the first tick, when all levels exist. */
    private final List<CompoundTag> pending = new ArrayList<>();

    private static final SavedData.Factory<ProcessManager> FACTORY =
            new SavedData.Factory<>(ProcessManager::new, ProcessManager::load, DATA_FIX_TYPE);

    /** The process manager of a server. */
    public static ProcessManager get(MinecraftServer server) {
        ServerLevel c0de = server.getLevel(C0deDimension.LEVEL);
        if (c0de == null) {
            throw new IllegalStateException("The c0de dimension doesn't exist");
        }
        return c0de.getDataStorage().computeIfAbsent(FACTORY, NAME);
    }

    /** Lets the processes run at the end of every server tick. Called once when the mod is initialized. */
    public static void register() {
        Platform.INSTANCE.onServerTickEnd(server -> {
            if (server.getLevel(C0deDimension.LEVEL) != null) {
                get(server).tick(server);
            }
        });
    }

    /**
     * A running process.
     *
     * @param parameterItems the number of items that were passed as parameters
     */
    private record Process(
            UUID id,
            int sheet,
            int pid,
            String program,
            String function,
            UUID owner,
            ResourceKey<Level> triggerLevel,
            BlockPos triggerPos,
            Cursor cursor,
            Statement.FunctionDeclaration declaration,
            List<String> parameterNames,
            int parameterItems,
            List<ChunkPos> chunks,
            UUID entity,
            boolean debug) {}

    /** The processes running on a sheet, as program name and PIDs for messages. */
    public List<Integer> pids(int sheet) {
        return processes.values().stream()
                .filter(process -> process.sheet() == sheet)
                .map(Process::pid)
                .sorted()
                .toList();
    }

    /** The name of the program on a sheet with running processes. */
    public Optional<String> program(int sheet) {
        return processes.values().stream()
                .filter(process -> process.sheet() == sheet)
                .map(Process::program)
                .findFirst();
    }

    /** The number of running processes. */
    public int count() {
        return processes.size();
    }

    // --------------------------------------------------------------------------------------------- starting

    /**
     * Starts a process for a trigger block if all preconditions are met.
     *
     * @return the reason if the process can't be started
     */
    public Optional<String> start(ServerLevel triggerLevel, TriggerBlockEntity trigger, boolean debug) {
        MinecraftServer server = triggerLevel.getServer();
        ServerLevel c0de = server.getLevel(C0deDimension.LEVEL);
        if (trigger.isRunning()) {
            return Optional.of("A process is still running");
        }
        if (trigger.program().isEmpty() || trigger.function().isEmpty()) {
            return Optional.of("The trigger block is not configured");
        }
        ProgramRegistry registry = ProgramRegistry.get(c0de);
        OptionalInt sheet = registry.sheetOf(trigger.program());
        if (sheet.isEmpty()) {
            return Optional.of("The program " + trigger.program() + " doesn't exist");
        }
        List<ChunkPos> chunks = new ArrayList<>();
        Program program = ProgramParser.parse(load(c0de, sheet.getAsInt(), chunks));
        Statement.FunctionDeclaration declaration = function(program, trigger.function());
        if (declaration == null) {
            release(c0de, chunks);
            return Optional.of("The function " + trigger.function() + " doesn't exist");
        }
        List<ItemStack> items = new ArrayList<>();
        for (int i = 0; i < trigger.input().getContainerSize(); i++) {
            if (!trigger.input().getItem(i).isEmpty()) {
                items.add(trigger.input().getItem(i));
            }
        }
        Map<String, Value> inputs = new LinkedHashMap<>();
        Optional<String> problem = inputs(program, declaration, items, inputs);
        if (problem.isPresent()) {
            release(c0de, chunks);
            return problem;
        }
        // The process returns one item per result, an empty variable item per parameter item without result, and
        // the initialized items it passes through
        int empty = declaration.results().size() - inputs.size();
        int returned = Math.max(declaration.results().size(), inputs.size())
                + passedThrough(trigger.input(), inputs.keySet(), empty);
        if (freeSlots(trigger.output()) < returned) {
            release(c0de, chunks);
            return Optional.of("The output inventory has no space for " + returned + " items");
        }
        int pid = freePid(c0de, sheet.getAsInt());
        if (pid < 0) {
            release(c0de, chunks);
            return Optional.of("No process ID is available for the program " + trigger.program());
        }

        // All preconditions are met
        UUID id = UUID.randomUUID();
        ProcessHost host = new ProcessHost(
                server,
                triggerLevel.dimension(),
                trigger.getBlockPos(),
                trigger.owner(),
                trigger.program(),
                trigger.function(),
                pid);
        CursorSettings settings = settings(c0de.getRandom().nextLong(), host);
        Cursor cursor = new Cursor(program, trigger.function(), inputs, settings);
        if (debug) {
            // A debug cursor starts paused (docs/concept/15-cursor.md)
            cursor.pause();
        }
        BlockPos pidPos = LevelSheet.pidPos(sheet.getAsInt(), pid);
        c0de.setBlockAndUpdate(pidPos, C0deBlocks.PID_BLOCK.get().defaultBlockState());
        if (c0de.getBlockEntity(pidPos) instanceof PidBlockEntity pidBlock) {
            pidBlock.setTrigger(pid, triggerLevel.dimension(), trigger.getBlockPos());
        }
        ChunkPos triggerChunk = new ChunkPos(trigger.getBlockPos());
        triggerLevel.getChunkSource().addRegionTicket(TICKET, triggerChunk, 0, triggerChunk);
        UUID entity = spawnCursor(c0de, id, sheet.getAsInt(), pid, debug);
        List<ItemStack> taken = take(trigger.input(), inputs.keySet(), empty);
        processes.put(
                id,
                new Process(
                        id,
                        sheet.getAsInt(),
                        pid,
                        trigger.program(),
                        trigger.function(),
                        trigger.owner(),
                        triggerLevel.dimension(),
                        trigger.getBlockPos(),
                        cursor,
                        declaration,
                        List.copyOf(inputs.keySet()),
                        inputs.size(),
                        chunks,
                        entity,
                        debug));
        trigger.started(id, pid, taken);
        setDirty();
        return Optional.empty();
    }

    private static int freeSlots(Container container) {
        int free = 0;
        for (int i = 0; i < container.getContainerSize(); i++) {
            if (container.getItem(i).isEmpty()) {
                free++;
            }
        }
        return free;
    }

    /** The number of stacks of initialized items that {@link #take} passes through to the output inventory. */
    private static int passedThrough(Container input, Collection<String> parameters, int empty) {
        SimpleContainer copy = new SimpleContainer(input.getContainerSize());
        for (int i = 0; i < input.getContainerSize(); i++) {
            copy.setItem(i, input.getItem(i).copy());
        }
        takeUsed(copy, parameters, empty);
        int passed = 0;
        for (int i = 0; i < copy.getContainerSize(); i++) {
            if (isInitialized(copy.getItem(i))) {
                passed++;
            }
        }
        return passed;
    }

    /**
     * Takes the items of a process out of the input inventory: one item per parameter with an item, the empty
     * variable items for the results without parameter item, and all other initialized items, which the process
     * passes through to the output inventory, so trigger blocks can be chained. Empty items that are not needed
     * stay in the input inventory, so they can be stacked there for several runs.
     */
    private static List<ItemStack> take(Container input, Collection<String> parameters, int empty) {
        List<ItemStack> taken = takeUsed(input, parameters, empty);
        // The used items come first, so the parameter names match them when the process ends
        for (int i = 0; i < input.getContainerSize(); i++) {
            if (isInitialized(input.getItem(i))) {
                taken.add(input.removeItemNoUpdate(i));
            }
        }
        input.setChanged();
        return taken;
    }

    private static boolean isInitialized(ItemStack item) {
        return item.getItem() instanceof ValueItem && !ValueItem.Contents.of(item).isEmpty();
    }

    /** Takes the parameter items and the empty variable items the results need. */
    private static List<ItemStack> takeUsed(Container input, Collection<String> parameters, int empty) {
        List<ItemStack> taken = new ArrayList<>();
        for (String name : parameters) {
            for (int i = 0; i < input.getContainerSize(); i++) {
                if (!input.getItem(i).isEmpty() && ValueItem.Contents.of(input.getItem(i)).name().equals(name)) {
                    taken.add(input.removeItem(i, 1));
                    break;
                }
            }
        }
        for (int i = 0; i < input.getContainerSize() && empty > 0; i++) {
            ItemStack item = input.getItem(i);
            if (isEmptyVariable(item)) {
                ItemStack removed = input.removeItem(i, empty);
                empty -= removed.getCount();
                taken.add(removed);
            }
        }
        input.setChanged();
        return taken;
    }

    private static boolean isEmptyVariable(ItemStack item) {
        return item.getItem() instanceof ValueItem value
                && !value.isConstant()
                && ValueItem.Contents.of(item).isEmpty();
    }

    /** The settings of a cursor; the host gives c0de/minecraft access to the world. */
    private static CursorSettings settings(long seed, ProcessHost host) {
        return new CursorSettings(
                C0de.config().get(C0deConfig.STACK_SIZE),
                ModLibraries.registry(),
                Clock.systemUTC(),
                ZoneId.systemDefault(),
                host,
                seed);
    }

    private static Statement.FunctionDeclaration function(Program program, String name) {
        for (Statement statement : program.statements()) {
            if (statement instanceof Statement.FunctionDeclaration function && function.name().equals(name)) {
                return function;
            }
        }
        return null;
    }

    /**
     * Converts the items of the input inventory to the values of the parameters of the entry function. Every
     * parameter without default value needs an item with its name and type; results that are not covered by
     * parameter items need empty variable items.
     */
    private static Optional<String> inputs(
            Program program,
            Statement.FunctionDeclaration declaration,
            List<ItemStack> items,
            Map<String, Value> inputs) {
        TypeRegistry types = structTypes(program);
        int empty = 0;
        for (ItemStack item : items) {
            if (isEmptyVariable(item)) {
                empty += item.getCount();
            }
        }
        for (Statement.FunctionDeclaration.Parameter parameter : declaration.parameters()) {
            ValueItem.Contents contents = null;
            for (ItemStack item : items) {
                ValueItem.Contents candidate = ValueItem.Contents.of(item);
                if (candidate.name().equals(parameter.name())) {
                    contents = candidate;
                    break;
                }
            }
            if (contents == null) {
                if (parameter.defaultValue() == null) {
                    return Optional.of("There is no item for the parameter " + parameter.name());
                }
                continue;
            }
            String type = TypeText.format(parameter.type());
            if (!type.equals(contents.type())) {
                return Optional.of("The parameter " + parameter.name() + " has the type " + type + ", not "
                        + contents.type());
            }
            try {
                inputs.put(parameter.name(), ItemRules.value(contents.value(), types.resolve(parameter.type())));
            } catch (C0deException e) {
                return Optional.of("The value of " + parameter.name() + " doesn't fit its type: " + e.getMessage());
            }
        }
        int missing = declaration.results().size() - inputs.size();
        if (missing > empty) {
            return Optional.of("The results need " + missing + " empty variable items, there are " + empty);
        }
        return Optional.empty();
    }

    /** The struct types of the included libraries and of the program, for the values of parameters. */
    private static TypeRegistry structTypes(Program program) {
        TypeRegistry types = new TypeRegistry();
        for (Statement statement : program.statements()) {
            if (statement instanceof Statement.Include include) {
                ModLibraries.registry()
                        .find(include.library())
                        .ifPresent(library -> library.types()
                                .forEach((name, definition) -> types.add(library.name() + "/" + name, definition)));
            }
        }
        for (Statement statement : program.statements()) {
            if (statement instanceof Statement.StructDeclaration struct && types.find(struct.name()).isEmpty()) {
                try {
                    types.add(struct.name(), Declarations.defineStruct(struct, types));
                } catch (C0deException e) {
                    // The cursor crashes when it reaches the declaration
                }
            }
        }
        return types;
    }

    /** The first free y coordinate above the PROGRAM line, which becomes the PID, or -1. */
    private static int freePid(ServerLevel c0de, int sheet) {
        int max = Math.min(C0de.config().get(C0deConfig.MAX_BUILD_HEIGHT), c0de.getMaxBuildHeight() - 1);
        for (int y = 0; y <= max; y++) {
            if (c0de.getBlockState(LevelSheet.pidPos(sheet, y)).isAir()) {
                return y;
            }
        }
        return -1;
    }

    /**
     * Loads the chunks of the program on a sheet and keeps them loaded, then reads the program.
     *
     * @param chunks receives the chunks that are kept loaded
     */
    private static Sheet load(ServerLevel c0de, int sheet, List<ChunkPos> chunks) {
        int chunkX = SheetGeometry.absoluteX(sheet, 0) >> 4;
        List<Line> lines = new ArrayList<>();
        // Line by line, so that the chunks are loaded when the program reaches them
        for (int z = 0; z < LevelSheet.MAX_LINES; z++) {
            if (z % 16 == 0) {
                for (int dx = 0; dx < 2; dx++) {
                    ChunkPos chunk = new ChunkPos(chunkX + dx, z >> 4);
                    c0de.getChunkSource().addRegionTicket(TICKET, chunk, 0, chunk);
                    c0de.getChunk(chunk.x, chunk.z);
                    chunks.add(chunk);
                }
            }
            Line line = LevelSheet.readLine(c0de, sheet, z);
            if (line.isEmpty()) {
                break;
            }
            lines.add(line);
        }
        return new Sheet(lines);
    }

    private static void release(ServerLevel level, List<ChunkPos> chunks) {
        chunks.forEach(chunk -> level.getChunkSource().removeRegionTicket(TICKET, chunk, 0, chunk));
    }

    private static UUID spawnCursor(ServerLevel c0de, UUID process, int sheet, int pid, boolean debug) {
        CursorEntity entity = new CursorEntity(C0deEntities.CURSOR.get(), c0de);
        entity.setProcess(process);
        entity.setDebug(debug);
        BlockPos start = LevelSheet.pidPos(sheet, pid);
        // The cursor starts in the NOOP line after the PROGRAM line (docs/concept/15-cursor.md)
        entity.setPos(start.getX() + 0.5, pid, 1.5);
        c0de.addFreshEntity(entity);
        return entity.getUUID();
    }

    // ---------------------------------------------------------------------------------------------- running

    private void tick(MinecraftServer server) {
        if (!pending.isEmpty()) {
            restore(server);
        }
        if (processes.isEmpty()) {
            return;
        }
        ServerLevel c0de = server.getLevel(C0deDimension.LEVEL);
        for (Process process : List.copyOf(processes.values())) {
            Cursor cursor = process.cursor();
            if (!cursor.ended()) {
                try {
                    cursor.step();
                } catch (RuntimeException e) {
                    C0de.LOGGER.error("Process {} of {} failed", process.pid(), process.program(), e);
                    end(server, process, Cursor.CRASH_EXIT_CODE, true);
                    continue;
                }
            }
            moved(server, process);
        }
        setDirty();
    }

    /** Moves the cursor entity to the line of the cursor and ends the process if the cursor has ended. */
    private void moved(MinecraftServer server, Process process) {
        ServerLevel c0de = server.getLevel(C0deDimension.LEVEL);
        Cursor cursor = process.cursor();
        if (c0de.getEntity(process.entity()) instanceof CursorEntity entity) {
            BlockPos pos = LevelSheet.pidPos(process.sheet(), process.pid());
            entity.setPos(pos.getX() + 0.5, process.pid(), cursor.line() + 0.5);
            entity.setWaiting(cursor.state() == CursorState.WAITING);
        }
        if (cursor.ended()) {
            end(server, process, cursor.exitCode(), cursor.error() != null);
        }
    }

    /**
     * Controls a debug cursor (docs/concept/15-cursor.md): switches it between PAUSED and RUNNING, or moves a
     * paused cursor by one line.
     *
     * @return the new state of the cursor for the message to the player, or the reason why nothing happened
     */
    public String debug(MinecraftServer server, UUID id, boolean toggle) {
        Process process = processes.get(id);
        if (process == null) {
            return "The process has ended";
        }
        if (!process.debug()) {
            return "Only debug cursors can be controlled";
        }
        Cursor cursor = process.cursor();
        if (toggle) {
            if (cursor.state() == CursorState.PAUSED) {
                cursor.resume();
            } else {
                cursor.pause();
            }
        } else if (cursor.state() == CursorState.PAUSED) {
            try {
                cursor.stepOnce();
            } catch (RuntimeException e) {
                C0de.LOGGER.error("Process {} of {} failed", process.pid(), process.program(), e);
                end(server, process, Cursor.CRASH_EXIT_CODE, true);
                return "CRASHED";
            }
            moved(server, process);
        } else {
            return "The cursor is running; pause it first";
        }
        setDirty();
        return cursor.ended() ? "ENDED, exit code " + cursor.exitCode() : cursor.state() + ", line " + cursor.line();
    }

    /**
     * Aborts the process with a PID on a sheet: its cursor crashes with PROCESS_ABORTED, and the process ends like
     * a crashed one, so the items it took go to the output inventory. Only the owner of the trigger block and
     * operators may abort a process.
     *
     * @return the message for the player
     */
    public Component abort(MinecraftServer server, ServerPlayer player, int sheet, int pid) {
        Process process = processes.values().stream()
                .filter(p -> p.sheet() == sheet && p.pid() == pid)
                .findFirst()
                .orElse(null);
        if (process == null) {
            return Component.translatable("message.c0de.no_process", pid);
        }
        if (!player.getUUID().equals(process.owner()) && !player.hasPermissions(2)) {
            return Component.translatable("message.c0de.not_owner", pid);
        }
        process.cursor().abort("Aborted by " + player.getGameProfile().getName());
        end(server, process, Cursor.CRASH_EXIT_CODE, true);
        return Component.translatable("message.c0de.aborted", process.program(), pid);
    }

    /** The sheet and the current line of the cursor of a process, or empty if it has ended. */
    public Optional<int[]> position(UUID id) {
        Process process = processes.get(id);
        return process == null ? Optional.empty() : Optional.of(new int[] {process.sheet(), process.cursor().line()});
    }

    /** The data of the cursor dialog of the debugging tool: properties and stack of the cursor. */
    public Optional<CompoundTag> inspect(UUID id) {
        Process process = processes.get(id);
        if (process == null) {
            return Optional.empty();
        }
        Cursor cursor = process.cursor();
        CompoundTag data = new CompoundTag();
        data.putString("program", process.program());
        data.putString("entry", process.function());
        data.putInt("pid", process.pid());
        data.putInt("line", cursor.line());
        data.putString("state", cursor.state().name());
        data.putString("function", cursor.currentFunction());
        data.putBoolean("debug", process.debug());
        data.putString("trigger", process.triggerLevel().location() + " " + process.triggerPos().getX() + " "
                + process.triggerPos().getY() + " " + process.triggerPos().getZ());
        ListTag stack = new ListTag();
        CursorInspection.stack(cursor).forEach(line -> stack.add(net.minecraft.nbt.StringTag.valueOf(line)));
        data.put("stack", stack);
        return Optional.of(data);
    }

    /** Ends a process: the results and other items go to the trigger block, the cursor and PID block are removed. */
    private void end(MinecraftServer server, Process process, int exitCode, boolean crashed) {
        processes.remove(process.id());
        ServerLevel c0de = server.getLevel(C0deDimension.LEVEL);
        ServerLevel triggerLevel = server.getLevel(process.triggerLevel());
        if (triggerLevel != null
                && triggerLevel.getBlockEntity(process.triggerPos()) instanceof TriggerBlockEntity trigger) {
            List<ItemStack> items = crashed || process.declaration() == null
                    ? copies(trigger.held())
                    : outputs(process, trigger.held());
            trigger.finished(exitCode, error(process.cursor().error()), items);
            if (exitCode == Cursor.CRASH_EXIT_CODE) {
                // A crash is the only end with a sound; timers end processes too often for more
                triggerLevel.playSound(
                        null, process.triggerPos(), SoundEvents.NOTE_BLOCK_BASS.value(), SoundSource.BLOCKS, 1.0F, 0.5F);
            }
        }
        remove(c0de, triggerLevel, process);
        setDirty();
    }

    /** Ends a process without results, e.g. because its trigger block was removed. */
    public void stop(MinecraftServer server, UUID id) {
        Process process = processes.remove(id);
        if (process != null) {
            remove(server.getLevel(C0deDimension.LEVEL), server.getLevel(process.triggerLevel()), process);
            setDirty();
        }
    }

    private static void remove(ServerLevel c0de, ServerLevel triggerLevel, Process process) {
        BlockPos pid = LevelSheet.pidPos(process.sheet(), process.pid());
        if (c0de.getBlockState(pid).is(C0deBlocks.PID_BLOCK.get())) {
            c0de.setBlock(pid, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
        }
        if (c0de.getEntity(process.entity()) != null) {
            c0de.getEntity(process.entity()).discard();
        }
        release(c0de, process.chunks());
        if (triggerLevel != null) {
            ChunkPos chunk = new ChunkPos(process.triggerPos());
            triggerLevel.getChunkSource().removeRegionTicket(TICKET, chunk, 0, chunk);
        }
    }

    private static List<ItemStack> copies(List<ItemStack> items) {
        return items.stream().map(ItemStack::copy).toList();
    }

    /**
     * The items a process returns: one variable item per result, empty variable items for parameters without a
     * result, and the initialized items that were passed through.
     */
    private static List<ItemStack> outputs(Process process, List<ItemStack> held) {
        List<ItemStack> remaining = new ArrayList<>(copies(held));
        // The parameter items are used up
        for (String name : process.parameterNames()) {
            for (ItemStack item : remaining) {
                if (ValueItem.Contents.of(item).name().equals(name)) {
                    item.shrink(1);
                    break;
                }
            }
        }
        List<Statement.FunctionDeclaration.Result> results = process.declaration().results();
        // Empty variable items make up for results that have no parameter item
        int emptyNeeded = Math.max(0, results.size() - process.parameterItems());
        for (ItemStack item : remaining) {
            while (emptyNeeded > 0 && !item.isEmpty() && isEmptyVariable(item)) {
                item.shrink(1);
                emptyNeeded--;
            }
        }
        List<ItemStack> outputs = new ArrayList<>();
        Map<String, Value> values = process.cursor().results();
        for (Statement.FunctionDeclaration.Result result : results) {
            ItemStack item = new ItemStack(C0deItems.VARIABLE_ITEM.get());
            Value value = values.get(result.name());
            new ValueItem.Contents(
                            result.name(),
                            TypeText.format(result.type()),
                            value == null ? "" : JsonValues.write(value))
                    .writeTo(item);
            outputs.add(item);
        }
        for (int i = results.size(); i < process.parameterItems(); i++) {
            ItemStack item = new ItemStack(C0deItems.VARIABLE_ITEM.get());
            new ValueItem.Contents("", "", "").writeTo(item);
            outputs.add(item);
        }
        remaining.stream().filter(item -> !item.isEmpty()).forEach(outputs::add);
        return outputs;
    }

    /** The error of a process as it is stored in the trigger block. */
    private static CompoundTag error(StructValue error) {
        CompoundTag tag = new CompoundTag();
        if (error != null) {
            for (String element : List.of("code", "message", "context", "line")) {
                tag.putString(element, error.get(element).text());
            }
        }
        return tag;
    }

    // ------------------------------------------------------------------------------------------- persistence

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
        ListTag list = new ListTag();
        for (Process process : processes.values()) {
            list.add(save(process));
        }
        // Processes that couldn't be restored yet are kept
        pending.forEach(list::add);
        tag.put("processes", list);
        return tag;
    }

    private static CompoundTag save(Process process) {
        CompoundTag tag = new CompoundTag();
        tag.putUUID("id", process.id());
        tag.putInt("sheet", process.sheet());
        tag.putInt("pid", process.pid());
        tag.putString("program", process.program());
        tag.putString("function", process.function());
        if (process.owner() != null) {
            tag.putUUID("owner", process.owner());
        }
        tag.putString("triggerLevel", process.triggerLevel().location().toString());
        tag.putLong("triggerPos", process.triggerPos().asLong());
        tag.putInt("parameterItems", process.parameterItems());
        tag.putBoolean("debug", process.debug());
        ListTag names = new ListTag();
        process.parameterNames().forEach(name -> names.add(net.minecraft.nbt.StringTag.valueOf(name)));
        tag.put("parameterNames", names);
        tag.put("cursor", SnapshotNbt.toNbt(CursorSnapshot.save(process.cursor())));
        return tag;
    }

    private static ProcessManager load(CompoundTag tag, HolderLookup.Provider registries) {
        ProcessManager manager = new ProcessManager();
        for (Tag process : tag.getList("processes", Tag.TAG_COMPOUND)) {
            manager.pending.add((CompoundTag) process);
        }
        return manager;
    }

    /** Restores the processes loaded from the save data: reads their programs and recreates their cursors. */
    @SuppressWarnings("unchecked")
    private void restore(MinecraftServer server) {
        ServerLevel c0de = server.getLevel(C0deDimension.LEVEL);
        for (CompoundTag tag : List.copyOf(pending)) {
            pending.remove(tag);
            try {
                int sheet = tag.getInt("sheet");
                List<ChunkPos> chunks = new ArrayList<>();
                Program program = ProgramParser.parse(load(c0de, sheet, chunks));
                String function = tag.getString("function");
                Statement.FunctionDeclaration declaration = function(program, function);
                ResourceKey<Level> triggerLevel = ResourceKey.create(
                        Registries.DIMENSION, ResourceLocation.parse(tag.getString("triggerLevel")));
                BlockPos triggerPos = BlockPos.of(tag.getLong("triggerPos"));
                ProcessHost host = new ProcessHost(
                        server,
                        triggerLevel,
                        triggerPos,
                        tag.hasUUID("owner") ? tag.getUUID("owner") : null,
                        tag.getString("program"),
                        function,
                        tag.getInt("pid"));
                Map<String, Object> data = (Map<String, Object>) SnapshotNbt.fromNbt(tag.get("cursor"));
                // The random number generator continues from the snapshot, so the seed doesn't matter here
                Cursor cursor = CursorSnapshot.restore(data, program, settings(0, host));
                ServerLevel trigger = server.getLevel(triggerLevel);
                if (trigger != null) {
                    ChunkPos chunk = new ChunkPos(triggerPos);
                    trigger.getChunkSource().addRegionTicket(TICKET, chunk, 0, chunk);
                }
                UUID id = tag.getUUID("id");
                int pid = tag.getInt("pid");
                List<String> parameterNames = tag.getList("parameterNames", Tag.TAG_STRING).stream()
                        .map(Tag::getAsString)
                        .toList();
                processes.put(
                        id,
                        new Process(
                                id,
                                sheet,
                                pid,
                                tag.getString("program"),
                                function,
                                tag.hasUUID("owner") ? tag.getUUID("owner") : null,
                                triggerLevel,
                                triggerPos,
                                cursor,
                                declaration,
                                parameterNames,
                                tag.getInt("parameterItems"),
                                chunks,
                                spawnCursor(c0de, id, sheet, pid, tag.getBoolean("debug")),
                                tag.getBoolean("debug")));
            } catch (RuntimeException e) {
                C0de.LOGGER.error("Can't restore the process {} of {}", tag.getInt("pid"), tag.getString("program"), e);
            }
        }
    }
}
