package de.joernott.c0de.core.validation;

import de.joernott.c0de.core.line.Block;
import de.joernott.c0de.core.line.Keyword;
import de.joernott.c0de.core.line.Line;
import de.joernott.c0de.core.line.Sheet;
import de.joernott.c0de.core.sheet.SheetGeometry;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Finds the names of a function that are visible in a line by looking at the lines above it, without parsing
 * the function. It is used for the lists of the LABEL dialog while a function is being written and can't be
 * parsed yet, e.g. because its END line is still missing. The types of the names are not known.
 */
final class LineScope {
    private LineScope() {}

    /**
     * The function that contains a line, with the parameters, results, variables, constants and loop
     * variables visible in that line.
     */
    record Function(String name, List<Symbol> symbols) {}

    static Optional<Function> at(Sheet sheet, int z) {
        int function = functionLine(sheet, z);
        if (function < 0) {
            return Optional.empty();
        }
        List<Declared> declared = new ArrayList<>();
        int line = function + 1;
        // PARAMETER and RESULT lines of the function
        for (; line < z; line++) {
            Line row = sheet.line(line);
            Keyword keyword = keyword(row);
            if (keyword != Keyword.PARAMETER && keyword != Keyword.RESULT) {
                break;
            }
            name(row, 1)
                    .ifPresent(name -> declared.add(new Declared(
                            0,
                            new Symbol(
                                    name,
                                    keyword == Keyword.PARAMETER ? Symbol.Kind.PARAMETER : Symbol.Kind.RESULT,
                                    null))));
        }
        // Declarations in the code blocks above the line; END removes the names of its block
        for (; line < z; line++) {
            Line row = sheet.line(line);
            Keyword keyword = keyword(row);
            if (keyword == null) {
                continue;
            }
            int x = row.cells().get(0).x();
            switch (keyword) {
                case END -> declared.removeIf(d -> d.indentation() > x);
                case VARIABLE, CONSTANT ->
                    name(row, 1)
                            .ifPresent(name -> declared.add(new Declared(
                                    x,
                                    new Symbol(
                                            name,
                                            keyword == Keyword.CONSTANT ? Symbol.Kind.CONSTANT : Symbol.Kind.VARIABLE,
                                            null))));
                case FOR, EACH ->
                    // The loop variable is visible in the body, which is indented by one block
                    name(row, 1)
                            .ifPresent(name -> declared.add(
                                    new Declared(x + 1, new Symbol(name, Symbol.Kind.LOOP_VARIABLE, null))));
                default -> {
                    // Other statements declare no names
                }
            }
        }
        String name = name(sheet.line(function), 1).orElse(null);
        return Optional.of(
                new Function(name, declared.stream().map(Declared::symbol).toList()));
    }

    /**
     * The FUNCTION line above a line, or -1 if the line is not in a function: lines at x=5 other than BEGIN
     * and the FUNCTION line itself are global statements, and END at x=5 closes a function.
     */
    private static int functionLine(Sheet sheet, int z) {
        for (int line = z; line >= 0; line--) {
            Line row = sheet.line(line);
            if (row.isEmpty() || row.cells().get(0).x() != SheetGeometry.CODE_MIN_X) {
                continue;
            }
            Keyword keyword = keyword(row);
            if (keyword == Keyword.FUNCTION) {
                return line;
            }
            if (keyword != Keyword.BEGIN && keyword != Keyword.NOOP && line != z) {
                return -1;
            }
        }
        return -1;
    }

    private static Keyword keyword(Line row) {
        return !row.isEmpty() && row.cells().get(0).block() instanceof Block.KeywordBlock(Keyword keyword)
                ? keyword
                : null;
    }

    /** The name of the label at a position of a line, counted from its first block. */
    private static Optional<String> name(Line row, int index) {
        return index < row.cells().size()
                        && row.cells().get(index).block() instanceof Block.LabelBlock label
                        && label.name() != null
                ? Optional.of(label.name())
                : Optional.empty();
    }

    /** A name and the indentation of the code block it belongs to. */
    private record Declared(int indentation, Symbol symbol) {}
}
