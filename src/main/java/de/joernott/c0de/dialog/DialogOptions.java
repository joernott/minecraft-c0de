package de.joernott.c0de.dialog;

import de.joernott.c0de.core.edit.LabelMode;
import de.joernott.c0de.core.edit.SheetView;
import de.joernott.c0de.core.library.Library;
import de.joernott.c0de.core.library.LibraryRegistry;
import de.joernott.c0de.core.line.Block;
import de.joernott.c0de.core.line.Keyword;
import de.joernott.c0de.core.line.Sheet;
import de.joernott.c0de.core.sheet.SheetGeometry;
import de.joernott.c0de.core.validation.Symbol;
import de.joernott.c0de.core.validation.Validation;
import de.joernott.c0de.core.validation.Validator;
import de.joernott.c0de.library.ModLibraries;
import de.joernott.c0de.sheet.LevelSheet;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

/**
 * The entries of the lists of the LABEL dialog. On a sheet, they come from the program: the functions that can
 * be called in the line, the parameters and results of the called function, the struct types and the variables
 * and constants in scope. Elsewhere, only the libraries and their functions are listed.
 */
public final class DialogOptions {
    private DialogOptions() {}

    /** The names of the built-in functions. */
    public static Set<String> builtinFunctions() {
        return ModLibraries.registry().builtins().functions().keySet();
    }

    /** The entries of the list of the LABEL dialog for a label at a position. */
    public static List<String> forLabel(LabelMode mode, Level level, BlockPos pos) {
        if (mode == LabelMode.LIBRARY) {
            return libraries();
        }
        Optional<Sheet> sheet = LevelSheet.isOnSheet(level, pos)
                ? LevelSheet.read(level, LevelSheet.sheetOf(pos))
                : Optional.empty();
        if (sheet.isEmpty()) {
            return mode == LabelMode.FUNCTION ? allFunctions() : List.of();
        }
        Validator.Visible visible = Validator.visibleAt(sheet.get(), ModLibraries.registry(), pos.getZ());
        return switch (mode) {
            case FUNCTION -> visible.functions().stream().map(Validation.Function::name).toList();
            case CALL_PARAMETER, CALL_RESULT ->
                calledFunction(LevelSheet.view(level, pos), SheetGeometry.relativeX(pos.getX()), pos.getZ())
                        .flatMap(visible::function)
                        .map(function ->
                                mode == LabelMode.CALL_PARAMETER ? function.parameters() : function.results())
                        .orElse(List.of());
            case STRUCT_TYPE -> visible.structTypes();
            case REFERENCE -> {
                // The innermost names first, the built-in constants last
                List<String> names = new ArrayList<>(
                        visible.symbols().stream().map(Symbol::name).toList());
                java.util.Collections.reverse(names);
                yield names;
            }
            default -> List.of();
        };
    }

    /**
     * The name of the function called by the CALL line that a PARAMETER or RESULT line at x-1 belongs to: the
     * CALL block is at x-2 in a line above the PARAMETER and RESULT lines, followed by the function label.
     */
    static Optional<String> calledFunction(SheetView sheet, int x, int z) {
        for (int line = z - 1; line >= 0; line--) {
            Block owner = sheet.at(x - 2, line);
            if (owner instanceof Block.KeywordBlock(Keyword keyword) && keyword == Keyword.CALL) {
                return sheet.at(x - 1, line) instanceof Block.LabelBlock label && label.name() != null
                        ? Optional.of(label.name())
                        : Optional.empty();
            }
            if (!(sheet.at(x - 1, line) instanceof Block.KeywordBlock(Keyword keyword))
                    || (keyword != Keyword.PARAMETER && keyword != Keyword.RESULT)) {
                return Optional.empty();
            }
        }
        return Optional.empty();
    }

    private static List<String> libraries() {
        TreeSet<String> names = new TreeSet<>();
        for (Library library : ModLibraries.registry().all()) {
            if (!library.name().isEmpty()) {
                names.add(library.name());
            }
        }
        return List.copyOf(names);
    }

    private static List<String> allFunctions() {
        LibraryRegistry registry = ModLibraries.registry();
        List<String> names = new ArrayList<>(new TreeSet<>(builtinFunctions()));
        for (Library library : registry.all()) {
            if (!library.name().isEmpty()) {
                new TreeSet<>(library.functions().keySet()).forEach(name -> names.add(library.name() + "/" + name));
            }
        }
        return names;
    }
}
