package de.joernott.c0de.core.cursor;

import de.joernott.c0de.core.eval.Variable;
import de.joernott.c0de.core.value.Type;
import java.util.ArrayList;
import java.util.List;

/** A readable description of the stack of a cursor, shown by the debugging tool (docs/concept/14-items-and-tools.md). */
public final class CursorInspection {
    /** The longest value that is shown; longer values are shortened. */
    private static final int MAX_VALUE_LENGTH = 80;

    private CursorInspection() {}

    /** The items of the stack from the top to the bottom, one line each. */
    public static List<String> stack(Cursor cursor) {
        List<StackItem> items = cursor.stack().items();
        List<String> lines = new ArrayList<>();
        for (int i = items.size() - 1; i >= 0; i--) {
            lines.add(describe(items.get(i)));
        }
        return lines;
    }

    private static String describe(StackItem item) {
        return switch (item) {
            case StackItem.VariableItem variable -> variable(variable.variable(), variable.global());
            case StackItem.FunctionFrame frame ->
                "FUNCTION " + frame.function() + " (line " + frame.functionLine() + ")"
                        + (frame.entry() ? ", entry function" : ", returns to line " + frame.returnLine());
            case StackItem.BlockMarker marker -> "BEGIN (line " + marker.block().beginLine() + ")";
            case StackItem.LoopItem loop -> "loop (line " + loop.loop().line() + ")";
        };
    }

    private static String variable(Variable variable, boolean global) {
        String kind = variable.loopVariable() ? "loop variable" : variable.constant() ? "CONSTANT" : "VARIABLE";
        String value = variable.value().text();
        if (value.length() > MAX_VALUE_LENGTH) {
            value = value.substring(0, MAX_VALUE_LENGTH - 3) + "...";
        }
        return kind + " " + variable.name() + " " + type(variable.type()) + " = " + value + (global ? " (global)" : "");
    }

    /** The name of a type, as written with type blocks. */
    public static String type(Type type) {
        return switch (type) {
            case Type.Simple simple -> simple.name();
            case Type.Str str -> str.maxLength() == null ? "STRING" : "STRING " + str.maxLength();
            case Type.Array array -> "ARRAY " + type(array.element());
            case Type.Hash hash -> "HASH " + type(hash.element());
            case Type.Struct struct ->
                struct == Type.Struct.ERROR
                        ? "ERROR"
                        : "STRUCT " + struct.definition().name();
        };
    }
}
