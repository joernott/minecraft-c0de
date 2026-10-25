package de.joernott.c0de.io;

import de.joernott.c0de.compat.C0deSavedData;
import de.joernott.c0de.compat.Nbt;
import de.joernott.c0de.core.io.TextScreen;
import de.joernott.c0de.dialog.DialogService;
import de.joernott.c0de.network.TerminalPayload;
import de.joernott.c0de.platform.Platform;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

/**
 * The terminals of laptops and tablets (docs/concept/19-input-output.md), by their ID: binding, screen with
 * history, the lines typed for INPUT and the keys typed for INKEY. Stored with the overworld
 * ({@code data/c0de_terminals.dat}), so that processes reach tablets in any inventory and laptops in unloaded
 * chunks. The players who have a terminal open get its changes once per tick.
 */
public class Terminals extends C0deSavedData {
    public static final int COLUMNS = 48;
    public static final int ROWS = 20;
    public static final int HISTORY = 200;
    /** The keys typed for INKEY that are kept; older ones are dropped. */
    private static final int MAX_KEYS = 32;
    /** An INPUT that hasn't asked again for this many ticks no longer waits, e.g. because its process ended. */
    private static final int PROMPT_TICKS = 3;
    private static final String NAME = "c0de_terminals";

    private static final C0deSavedData.Kind<Terminals> KIND =
            new C0deSavedData.Kind<>(NAME, Terminals::new, Terminals::load);

    /** A terminal; the changes of its screen are sent with {@link #changed}. */
    public static final class Terminal {
        private final UUID id;
        private String program = "";
        private String function = "";
        private long order;
        private final TextScreen screen = new TextScreen(COLUMNS, ROWS, HISTORY);
        private final Deque<String> lines = new ArrayDeque<>();
        private final Deque<String> keys = new ArrayDeque<>();
        private String prompt = "";
        private long promptTick;
        private final Set<UUID> viewers = new HashSet<>();
        private boolean dirty;
        private long sentScrolled;
        private long sentClears;

        private Terminal(UUID id) {
            this.id = id;
        }

        public UUID id() {
            return id;
        }

        public String program() {
            return program;
        }

        public String function() {
            return function;
        }

        public TextScreen screen() {
            return screen;
        }

        /** What a waiting INPUT asks for, e.g. {@code count (INTEGER)}, or empty if no INPUT waits. */
        public String prompt() {
            return prompt;
        }

        /** The oldest line typed for INPUT, or null. */
        String pollLine() {
            return lines.poll();
        }

        /** The oldest key typed for INKEY, or null. */
        String pollKey() {
            return keys.poll();
        }
    }

    private final Map<UUID, Terminal> terminals = new LinkedHashMap<>();
    private final Map<UUID, UUID> openBy = new HashMap<>();
    private long next;

    public static Terminals get(MinecraftServer server) {
        return KIND.get(server.overworld().getDataStorage());
    }

    /** Sends the changes of the terminals to their viewers and ends INPUTs that no longer wait. */
    public static void register() {
        Platform.INSTANCE.onServerTickEnd(server -> get(server).tick(server));
    }

    /** The terminal with the ID; it is created if it doesn't exist. */
    public Terminal terminal(UUID id) {
        return terminals.computeIfAbsent(id, key -> {
            setDirty();
            return new Terminal(key);
        });
    }

    /** Removes a terminal, e.g. because its laptop was broken. */
    public void remove(UUID id) {
        if (terminals.remove(id) != null) {
            setDirty();
        }
    }

    /** Binds a terminal; it moves to the end of the order. */
    public void bind(MinecraftServer server, Terminal terminal, String program, String function) {
        terminal.program = program;
        terminal.function = program.isEmpty() ? "" : function;
        terminal.order = next++;
        terminal.prompt = "";
        terminal.lines.clear();
        terminal.keys.clear();
        terminal.screen.clear();
        setDirty();
        sendAll(server, terminal, false);
    }

    /** The terminals bound to a program and function, the earliest bound first. */
    public List<Terminal> terminals(String program, String function) {
        return terminals.values().stream()
                .filter(t -> !t.program.isEmpty() && t.program.equals(program) && t.function.equals(function))
                .sorted(Comparator.comparingLong(t -> t.order))
                .toList();
    }

    /** The screen of the terminal changed: it is saved and sent to the viewers at the end of the tick. */
    public void changed(Terminal terminal) {
        terminal.dirty = true;
        setDirty();
    }

    /** An INPUT waits for a line typed on the terminal. */
    void waiting(Terminal terminal, String prompt, long tick) {
        terminal.promptTick = tick;
        if (!terminal.prompt.equals(prompt)) {
            terminal.prompt = prompt;
            terminal.dirty = true;
        }
    }

    /** The INPUT got its value. */
    void answered(Terminal terminal) {
        if (!terminal.prompt.isEmpty()) {
            terminal.prompt = "";
            terminal.dirty = true;
        }
    }

    // ------------------------------------------------------------------ players

    /** Opens the terminal for a player. */
    public void open(ServerPlayer player, Terminal terminal) {
        UUID previous = openBy.put(player.getUUID(), terminal.id);
        if (previous != null && terminals.containsKey(previous)) {
            terminals.get(previous).viewers.remove(player.getUUID());
        }
        terminal.viewers.add(player.getUUID());
        CompoundTag data = data(terminal, ScreenNbt.write(terminal.screen));
        data.put("programs", DialogService.programs(player));
        Platform.INSTANCE.sendToPlayer(player, new TerminalPayload(terminal.id, true, data));
    }

    /** The terminal the player has open, or null. */
    public Terminal openBy(ServerPlayer player) {
        UUID id = openBy.get(player.getUUID());
        Terminal terminal = id == null ? null : terminals.get(id);
        return terminal != null && terminal.viewers.contains(player.getUUID()) ? terminal : null;
    }

    /** The player closed the terminal. */
    public void close(ServerPlayer player) {
        UUID id = openBy.remove(player.getUUID());
        if (id != null && terminals.containsKey(id)) {
            terminals.get(id).viewers.remove(player.getUUID());
        }
    }

    /** A player typed a line: the answer to a waiting INPUT. */
    public void line(Terminal terminal, String line) {
        if (!terminal.prompt.isEmpty()) {
            terminal.lines.add(line);
        }
    }

    /** A player typed a key while no INPUT waited. */
    public void key(Terminal terminal, String key) {
        if (terminal.prompt.isEmpty() && !terminal.program.isEmpty()) {
            terminal.keys.add(key);
            while (terminal.keys.size() > MAX_KEYS) {
                terminal.keys.poll();
            }
        }
    }

    private void tick(MinecraftServer server) {
        long tick = server.overworld().getGameTime();
        for (Terminal terminal : terminals.values()) {
            if (!terminal.prompt.isEmpty() && tick - terminal.promptTick > PROMPT_TICKS) {
                terminal.prompt = "";
                terminal.lines.clear();
                terminal.dirty = true;
            }
            if (terminal.dirty) {
                terminal.dirty = false;
                sendAll(server, terminal, true);
            }
        }
    }

    /** Sends the terminal to its viewers: an update with only the new rows of the history if possible. */
    private void sendAll(MinecraftServer server, Terminal terminal, boolean update) {
        TextScreen screen = terminal.screen;
        long newRows = screen.scrolled() - terminal.sentScrolled;
        boolean partial = update && screen.clears() == terminal.sentClears && newRows <= HISTORY;
        terminal.sentScrolled = screen.scrolled();
        terminal.sentClears = screen.clears();
        if (terminal.viewers.isEmpty()) {
            return;
        }
        CompoundTag data = data(terminal, partial ? ScreenNbt.update(screen, (int) newRows) : ScreenNbt.write(screen));
        TerminalPayload payload = new TerminalPayload(terminal.id, false, data);
        List<UUID> gone = new ArrayList<>();
        for (UUID viewer : terminal.viewers) {
            ServerPlayer player = server.getPlayerList().getPlayer(viewer);
            if (player == null) {
                gone.add(viewer);
            } else {
                Platform.INSTANCE.sendToPlayer(player, payload);
            }
        }
        gone.forEach(terminal.viewers::remove);
    }

    private static CompoundTag data(Terminal terminal, CompoundTag screen) {
        CompoundTag data = new CompoundTag();
        data.put("screen", screen);
        data.putString("program", terminal.program);
        data.putString("function", terminal.function);
        data.putString("prompt", terminal.prompt);
        return data;
    }

    private static Terminals load(CompoundTag tag, HolderLookup.Provider registries) {
        Terminals result = new Terminals();
        result.next = Nbt.longValue(tag, "next");
        for (Tag element : Nbt.list(tag, "terminals", Tag.TAG_COMPOUND)) {
            CompoundTag entry = (CompoundTag) element;
            Terminal terminal = new Terminal(Nbt.uuid(entry, "id"));
            terminal.program = Nbt.string(entry, "program");
            terminal.function = Nbt.string(entry, "function");
            terminal.order = Nbt.longValue(entry, "order");
            CompoundTag screen = Nbt.compound(entry, "screen");
            if (screen.contains("rows")) {
                ScreenNbt.apply(terminal.screen, screen);
            }
            result.terminals.put(terminal.id, terminal);
        }
        return result;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        tag.putLong("next", next);
        ListTag list = new ListTag();
        for (Terminal terminal : terminals.values()) {
            CompoundTag entry = new CompoundTag();
            Nbt.putUuid(entry, "id", terminal.id);
            entry.putString("program", terminal.program);
            entry.putString("function", terminal.function);
            entry.putLong("order", terminal.order);
            entry.put("screen", ScreenNbt.write(terminal.screen));
            list.add(entry);
        }
        tag.put("terminals", list);
        return tag;
    }
}
