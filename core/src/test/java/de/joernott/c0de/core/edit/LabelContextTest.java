package de.joernott.c0de.core.edit;

import static org.junit.jupiter.api.Assertions.assertEquals;

import de.joernott.c0de.core.line.Line;
import de.joernott.c0de.core.line.Notation;
import de.joernott.c0de.core.line.Sheet;
import org.junit.jupiter.api.Test;

/** The kind, dialog layout and parent of labels in different positions. */
class LabelContextTest {
    private static final Sheet SHEET = Notation.parse("""
            PROGRAM demo
            INCLUDE c0de/math
            STRUCT point
            BEGIN
                x INTEGER TAG(json: "x")
                next STRUCT point
            END STRUCT
            NOOP
            FUNCTION main
                PARAMETER count INTEGER
                RESULT total INTEGER
            BEGIN
                VARIABLE p STRUCT point TAG(json: "a.b")
                CONSTANT limit INTEGER 10
                ASSIGN total count + p.x ;
                CALL c0de/math/ABS problem
                    PARAMETER value count ;
                    RESULT result total
                FOR i INTEGER 1 TO 3
                BEGIN
                END FOR
                EACH item list
                BEGIN
                END EACH
            END FUNCTION main
            """);

    private static LabelContext context(String name) {
        return context(name, 0);
    }

    /** The context of the label with this name, skipping the first {@code skip} labels with the name. */
    private static LabelContext context(String name, int skip) {
        SheetView view = SheetView.of(SHEET);
        for (Line line : SHEET.lines()) {
            for (Line.Cell cell : line.cells()) {
                if (cell.block() instanceof de.joernott.c0de.core.line.Block.LabelBlock label
                        && label.name().equals(name)
                        && skip-- == 0) {
                    return LabelContext.of(view, cell.x(), line.z());
                }
            }
        }
        throw new AssertionError("No label " + name);
    }

    private static void assertContext(LabelKind kind, LabelMode mode, String parent, LabelContext context) {
        assertEquals(new LabelContext(kind, mode, parent), context);
    }

    @Test
    void declarationsGetNewNames() {
        assertContext(LabelKind.PROGRAM, LabelMode.NEW_NAME, null, context("demo"));
        assertContext(LabelKind.TYPE, LabelMode.NEW_NAME, null, context("point"));
        assertContext(LabelKind.ELEMENT, LabelMode.NEW_NAME, null, context("x"));
        assertContext(LabelKind.FUNCTION, LabelMode.NEW_NAME, "demo", context("main"));
        assertContext(LabelKind.PARAMETER, LabelMode.NEW_NAME, null, context("count"));
        assertContext(LabelKind.RESULT, LabelMode.NEW_NAME, null, context("total"));
        assertContext(LabelKind.VARIABLE, LabelMode.NEW_NAME, null, context("p"));
        assertContext(LabelKind.CONSTANT, LabelMode.NEW_NAME, null, context("limit"));
        assertContext(LabelKind.VARIABLE, LabelMode.NEW_NAME, null, context("i"));
        assertContext(LabelKind.VARIABLE, LabelMode.NEW_NAME, null, context("item"));
    }

    @Test
    void referencesGetLists() {
        assertContext(LabelKind.LIBRARY, LabelMode.LIBRARY, null, context("c0de/math"));
        assertContext(LabelKind.TYPE, LabelMode.STRUCT_TYPE, null, context("point", 1));
        assertContext(LabelKind.TYPE, LabelMode.STRUCT_TYPE, null, context("point", 2));
        assertContext(LabelKind.FUNCTION, LabelMode.FUNCTION, "demo", context("c0de/math/ABS"));
        assertContext(LabelKind.FUNCTION, LabelMode.FUNCTION, "demo", context("main", 1));
        assertContext(LabelKind.PARAMETER, LabelMode.CALL_PARAMETER, null, context("value"));
        assertContext(LabelKind.RESULT, LabelMode.CALL_RESULT, null, context("result"));
    }

    @Test
    void labelsInExpressionsAndAfterOtherLabelsAreReferences() {
        assertContext(LabelKind.REFERENCE, LabelMode.TARGET, null, context("total", 1));
        assertContext(LabelKind.REFERENCE, LabelMode.REFERENCE, null, context("count", 1));
        assertContext(LabelKind.REFERENCE, LabelMode.REFERENCE, null, context("p", 1));
        assertContext(LabelKind.REFERENCE, LabelMode.TARGET, null, context("problem"));
        assertContext(LabelKind.REFERENCE, LabelMode.REFERENCE, null, context("count", 2));
        assertContext(LabelKind.REFERENCE, LabelMode.TARGET, null, context("total", 2));
        assertContext(LabelKind.REFERENCE, LabelMode.REFERENCE, null, context("list"));
    }

    @Test
    void tagsOfVariablesArePaths() {
        SheetView view = SheetView.of(SHEET);
        assertEquals(false, PropertyRules.isPathTag(view, 8, 4));
        assertEquals(true, PropertyRules.isPathTag(view, 10, 12));
    }
}
