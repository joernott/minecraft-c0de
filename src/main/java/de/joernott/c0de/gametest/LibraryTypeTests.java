package de.joernott.c0de.gametest;

import de.joernott.c0de.block.ValidatedCodeBlock;
import de.joernott.c0de.blockentity.LabelBlockEntity;
import de.joernott.c0de.compat.Nbt;
import de.joernott.c0de.core.cursor.Cursor;
import de.joernott.c0de.core.cursor.CursorSettings;
import de.joernott.c0de.core.cursor.CursorSnapshot;
import de.joernott.c0de.core.edit.LabelMode;
import de.joernott.c0de.core.line.Notation;
import de.joernott.c0de.core.library.Library;
import de.joernott.c0de.core.library.NativeFunction;
import de.joernott.c0de.core.library.NativeResult;
import de.joernott.c0de.core.program.Program;
import de.joernott.c0de.core.program.ProgramParser;
import de.joernott.c0de.core.value.IntegerValue;
import de.joernott.c0de.core.value.LibraryType;
import de.joernott.c0de.core.value.LibraryValue;
import de.joernott.c0de.core.value.Value;
import de.joernott.c0de.dimension.C0deDimension;
import de.joernott.c0de.library.ModLibraries;
import de.joernott.c0de.process.SnapshotNbt;
import de.joernott.c0de.sheet.SheetValidation;
import java.util.List;
import java.util.Map;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;

/** Types defined by the libraries of other mods (task 19.5, docs/concept/04-data.md "Library type"). */
public final class LibraryTypeTests {
    /** A value of the test type: an immutable counter, so its copies are trivially cheap. */
    record Counter(long count) implements LibraryValue {
        @Override
        public LibraryType libraryType() {
            return COUNTER;
        }

        @Override
        public Value copy() {
            return this;
        }

        @Override
        public String text() {
            return "COUNTER " + count;
        }
    }

    static final LibraryType COUNTER = new LibraryType() {
        @Override
        public String name() {
            return "c0detest/memo/COUNTER";
        }

        @Override
        public LibraryValue defaultValue() {
            return new Counter(0);
        }

        @Override
        public String save(LibraryValue value) {
            return Long.toString(((Counter) value).count());
        }

        @Override
        public LibraryValue load(String data) {
            return new Counter(Long.parseLong(data));
        }
    };

    /** The library of another mod with a library type, as c0de paint has one. */
    static final Library MEMO = new Library() {
        @Override
        public String name() {
            return "c0detest/memo";
        }

        @Override
        public Map<String, LibraryType> libraryTypes() {
            return Map.of("COUNTER", COUNTER);
        }

        @Override
        public Map<String, NativeFunction> functions() {
            return Map.of(
                    "ADD",
                    new NativeFunction(
                            "ADD",
                            List.of(NativeFunction.Parameter.reference("counter", "TYPE c0detest/memo/COUNTER")),
                            List.of(),
                            call -> {
                                var counter = call.reference("counter").orElseThrow();
                                counter.set(new Counter(((Counter) counter.value()).count() + 1));
                                return NativeResult.none();
                            }),
                    "COUNT",
                    new NativeFunction(
                            "COUNT",
                            List.of(NativeFunction.Parameter.required(
                                    "counter", "TYPE c0detest/memo/COUNTER", value -> value instanceof Counter)),
                            List.of("result"),
                            call -> NativeResult.of(new IntegerValue(((Counter) call.argument("counter")).count()))));
        }
    };

    private static final int SHEET = 547;

    private LibraryTypeTests() {}

    /** Registers the test library once; GameTests of both kinds use it. */
    static synchronized void registerMemo() {
        if (ModLibraries.registry().find(MEMO.name()).isEmpty()) {
            ModLibraries.register(MEMO);
        }
    }

    /** A TYPE line is valid, and the LABEL dialog after the TYPE block lists the types of the included libraries. */
    public static void libraryTypesAreValidatedAndListed(GameTestHelper helper) {
        registerMemo();
        ServerLevel level = helper.getLevel().getServer().getLevel(C0deDimension.LEVEL);
        helper.assertTrue(level != null, "The c0de dimension doesn't exist");
        SheetBuilder sheet = new SheetBuilder(level, SHEET).place("""
                PROGRAM librarytypes
                NOOP
                INCLUDE c0detest/memo
                NOOP
                FUNCTION main
                BEGIN
                    VARIABLE c TYPE c0detest/memo/COUNTER
                END FUNCTION
                """);
        try {
            SheetValidation.validate(level, SHEET);
            BlockState variable = level.getBlockState(sheet.pos(1, 6));
            helper.assertTrue(
                    variable.hasProperty(ValidatedCodeBlock.VALID) && variable.getValue(ValidatedCodeBlock.VALID),
                    "The VARIABLE line with a library type is not valid");
            CompoundTag label = ((LabelBlockEntity) level.getBlockEntity(sheet.pos(4, 6))).dialogData(level);
            helper.assertTrue(
                    Nbt.string(label, "mode").equals(LabelMode.LIBRARY_TYPE.name()),
                    "The label after TYPE doesn't list library types: " + label);
            String options = Nbt.list(label, "options", Tag.TAG_STRING).toString();
            helper.assertTrue(
                    options.contains("\"c0detest/memo/COUNTER\""), "The library type is not listed: " + options);
        } finally {
            sheet.clear();
            SheetValidation.validate(level, SHEET);
        }
        helper.succeed();
    }

    /** A cursor stored as NBT keeps its values of library types (task 19.4). */
    @SuppressWarnings("unchecked")
    public static void libraryValuesSurviveSavingAsNbt(GameTestHelper helper) {
        registerMemo();
        Program program = ProgramParser.parse(Notation.parse(
                """
                PROGRAM keeping
                INCLUDE c0detest/memo
                NOOP
                FUNCTION count
                    RESULT total INTEGER
                BEGIN
                    VARIABLE c TYPE c0detest/memo/COUNTER
                    FOR i INTEGER 1 ; 5 ;
                    BEGIN
                        CALL c0detest/memo/ADD
                            PARAMETER counter c ;
                    END FOR
                    CALL c0detest/memo/COUNT
                        PARAMETER counter c ;
                        RESULT result total
                END FUNCTION
                """));
        CursorSettings settings = CursorSettings.of(ModLibraries.registry());
        Cursor cursor = new Cursor(program, "count", Map.of(), settings);
        cursor.run(12);
        Object stored = SnapshotNbt.fromNbt(SnapshotNbt.toNbt(CursorSnapshot.save(cursor)));
        Cursor restored = CursorSnapshot.restore((Map<String, Object>) stored, program, settings);
        restored.run(1000);
        Value total = restored.results().get("total");
        helper.assertTrue(new IntegerValue(5).equals(total), "The total is " + total + ", " + restored.error());
        helper.succeed();
    }
}
