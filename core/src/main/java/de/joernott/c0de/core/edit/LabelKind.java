package de.joernott.c0de.core.edit;

/** What a LABEL names or refers to (docs/concept/02-language-basics.md, "Naming with labels"). */
public enum LabelKind {
    PROGRAM,
    LIBRARY,
    TYPE,
    VARIABLE,
    CONSTANT,
    FUNCTION,
    PARAMETER,
    RESULT,
    /** An element of a struct or a key of a hash initializer. */
    ELEMENT,
    /** A use of an already defined variable or constant. */
    REFERENCE
}
