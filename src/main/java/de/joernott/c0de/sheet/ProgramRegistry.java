package de.joernott.c0de.sheet;

import de.joernott.c0de.core.validation.Validation;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.TreeMap;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;

/**
 * The programs on the sheets of the c0de dimension: their names, which are unique in the dimension, and their
 * functions. It is updated whenever a sheet is validated, and stored with the dimension
 * ({@code dimensions/c0de/c0de/data/c0de_programs.dat}), so that the programs of sheets in unloaded chunks are
 * known too.
 */
public class ProgramRegistry extends SavedData {
    private static final String NAME = "c0de_programs";

    // Vanilla (Fabric) can't load saved data without a data fixer type; NeoForge allows null
    private static final DataFixTypes DATA_FIX_TYPE =
        /*? if fabric {*/DataFixTypes.SAVED_DATA_COMMAND_STORAGE;
        /*?} else *///null;

    private static final SavedData.Factory<ProgramRegistry> FACTORY =
            new SavedData.Factory<>(ProgramRegistry::new, ProgramRegistry::load, DATA_FIX_TYPE);

    private final Map<Integer, Entry> sheets = new TreeMap<>();

    /** The registry of the c0de dimension. */
    public static ProgramRegistry get(ServerLevel c0de) {
        return c0de.getDataStorage().computeIfAbsent(FACTORY, NAME);
    }

    /** The sheet of the program with this name. */
    public OptionalInt sheetOf(String program) {
        return sheets.entrySet().stream()
                .filter(entry -> entry.getValue().name().equals(program))
                .mapToInt(Map.Entry::getKey)
                .findFirst();
    }

    /** The program on a sheet. */
    public Optional<Entry> program(int sheet) {
        return Optional.ofNullable(sheets.get(sheet));
    }

    /** The names of all programs, sorted. */
    public List<String> programs() {
        return sheets.values().stream().map(Entry::name).sorted().toList();
    }

    /** The functions of a program. */
    public List<Validation.Function> functions(String program) {
        return sheetOf(program).stream()
                .mapToObj(sheet -> sheets.get(sheet).functions())
                .findFirst()
                .orElse(List.of());
    }

    /** Stores the program of a sheet; a sheet without a program name is removed. */
    public void update(int sheet, String name, List<Validation.Function> functions) {
        Entry old = sheets.get(sheet);
        Entry entry = name == null ? null : new Entry(name, functions);
        if (java.util.Objects.equals(old, entry)) {
            return;
        }
        if (entry == null) {
            sheets.remove(sheet);
        } else {
            sheets.put(sheet, entry);
        }
        setDirty();
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        CompoundTag programs = new CompoundTag();
        sheets.forEach((sheet, entry) -> {
            CompoundTag program = new CompoundTag();
            program.putString("name", entry.name());
            ListTag functions = new ListTag();
            for (Validation.Function function : entry.functions()) {
                CompoundTag f = new CompoundTag();
                f.putString("name", function.name());
                f.putInt("line", function.line());
                f.put("parameters", strings(function.parameters()));
                f.put("mandatory", strings(function.mandatory()));
                f.put("results", strings(function.results()));
                functions.add(f);
            }
            program.put("functions", functions);
            programs.put(Integer.toString(sheet), program);
        });
        tag.put("sheets", programs);
        return tag;
    }

    private static ProgramRegistry load(CompoundTag tag, HolderLookup.Provider registries) {
        ProgramRegistry registry = new ProgramRegistry();
        CompoundTag programs = tag.getCompound("sheets");
        for (String key : programs.getAllKeys()) {
            CompoundTag program = programs.getCompound(key);
            List<Validation.Function> functions = new ArrayList<>();
            for (Tag element : program.getList("functions", Tag.TAG_COMPOUND)) {
                CompoundTag f = (CompoundTag) element;
                functions.add(new Validation.Function(
                        f.getString("name"),
                        f.getInt("line"),
                        strings(f.getList("parameters", Tag.TAG_STRING)),
                        strings(f.getList("mandatory", Tag.TAG_STRING)),
                        strings(f.getList("results", Tag.TAG_STRING))));
            }
            try {
                registry.sheets.put(Integer.parseInt(key), new Entry(program.getString("name"), functions));
            } catch (NumberFormatException e) {
                // Ignores entries that were not written by this class
            }
        }
        return registry;
    }

    private static ListTag strings(List<String> values) {
        ListTag list = new ListTag();
        values.forEach(value -> list.add(StringTag.valueOf(value)));
        return list;
    }

    private static List<String> strings(ListTag list) {
        return list.stream().map(Tag::getAsString).toList();
    }

    /** A program: its name and functions. */
    public record Entry(String name, List<Validation.Function> functions) {
        public Entry {
            functions = List.copyOf(functions);
        }
    }
}
