package de.joernott.c0de.core.edit;

/** The layout of the LABEL dialog, which depends on the position of the label. */
public enum LabelMode {
    /** An input field for a new name. */
    NEW_NAME,
    /** A list of the available libraries (after INCLUDE). */
    LIBRARY,
    /** A list of the available functions (after CALL and END FUNCTION). */
    FUNCTION,
    /** A list of the parameters of the called function (PARAMETER line of a CALL). */
    CALL_PARAMETER,
    /** A list of the results of the called function (RESULT line of a CALL). */
    CALL_RESULT,
    /** A list of the known struct types (after a STRUCT type block). */
    STRUCT_TYPE,
    /** A list of the variables and constants in scope, with an editor for the index. */
    REFERENCE;

    /** True if the label can have an index in this position. */
    public boolean allowsIndex() {
        return this == REFERENCE;
    }
}
