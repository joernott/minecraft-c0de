package de.joernott.c0de.core.edit;

import de.joernott.c0de.core.line.Block;
import de.joernott.c0de.core.line.Keyword;

/**
 * What a LABEL at a position of a sheet names, as far as it can be determined from the blocks around it. The
 * game initializes the kind and parent of a placed label from it and chooses the layout of the dialog.
 *
 * @param kind what the label names or refers to
 * @param mode the layout of the dialog
 * @param parent the parent of a function name (the program name for functions declared in the program), or
 *     null if the label doesn't name a function
 */
public record LabelContext(LabelKind kind, LabelMode mode, String parent) {
    private static final LabelContext REFERENCE = new LabelContext(LabelKind.REFERENCE, LabelMode.REFERENCE, null);
    private static final LabelContext TARGET = new LabelContext(LabelKind.REFERENCE, LabelMode.TARGET, null);

    /** The context of a label at the position x, z of a sheet. */
    public static LabelContext of(SheetView sheet, int x, int z) {
        Block before = sheet.at(x - 1, z);
        if (before == null) {
            // The label starts a content line of a struct declaration or an initializer
            return new LabelContext(LabelKind.ELEMENT, LabelMode.NEW_NAME, null);
        }
        if (before instanceof Block.LabelBlock
                && sheet.at(x - 2, z) instanceof Block.KeywordBlock(Keyword owner)
                && (owner == Keyword.CALL || owner == Keyword.RESULT)) {
            // The error label of a CALL, or the variable receiving a result
            return TARGET;
        }
        if (!(before instanceof Block.KeywordBlock(Keyword keyword))) {
            // In an expression, or the iterable of an EACH
            return REFERENCE;
        }
        boolean keywordStartsLine = sheet.at(x - 2, z) == null;
        return switch (keyword) {
            case PROGRAM -> newName(LabelKind.PROGRAM);
            case INCLUDE -> new LabelContext(LabelKind.LIBRARY, LabelMode.LIBRARY, null);
            case STRUCT ->
                keywordStartsLine
                        ? newName(LabelKind.TYPE)
                        : new LabelContext(LabelKind.TYPE, LabelMode.STRUCT_TYPE, null);
            case VARIABLE, FOR, EACH -> newName(LabelKind.VARIABLE);
            case CONSTANT -> newName(LabelKind.CONSTANT);
            case FUNCTION ->
                new LabelContext(
                        LabelKind.FUNCTION,
                        // FUNCTION at the start of a line declares a function, END FUNCTION repeats its name
                        keywordStartsLine ? LabelMode.NEW_NAME : LabelMode.FUNCTION,
                        sheet.programName());
            case CALL -> new LabelContext(LabelKind.FUNCTION, LabelMode.FUNCTION, sheet.programName());
            case PARAMETER, RESULT -> parameterOrResult(sheet, x, z, keyword);
            case ASSIGN -> TARGET;
            default -> REFERENCE;
        };
    }

    private static LabelContext newName(LabelKind kind) {
        return new LabelContext(kind, LabelMode.NEW_NAME, null);
    }

    /**
     * A PARAMETER or RESULT line belongs to the FUNCTION or CALL line above the PARAMETER and RESULT lines,
     * whose keyword is one block further left.
     */
    private static LabelContext parameterOrResult(SheetView sheet, int x, int z, Keyword keyword) {
        LabelKind kind = keyword == Keyword.PARAMETER ? LabelKind.PARAMETER : LabelKind.RESULT;
        for (int line = z - 1; line >= 0; line--) {
            if (sheet.at(x - 1, line) instanceof Block.KeywordBlock(Keyword other)
                    && (other == Keyword.PARAMETER || other == Keyword.RESULT)) {
                continue;
            }
            if (sheet.at(x - 2, line) instanceof Block.KeywordBlock(Keyword owner) && owner == Keyword.CALL) {
                return new LabelContext(
                        kind, kind == LabelKind.PARAMETER ? LabelMode.CALL_PARAMETER : LabelMode.CALL_RESULT, null);
            }
            break;
        }
        return newName(kind);
    }
}
