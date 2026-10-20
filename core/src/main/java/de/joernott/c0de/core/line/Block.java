package de.joernott.c0de.core.line;

import de.joernott.c0de.core.value.Value;
import java.util.List;

/**
 * A c0de block in a line, with the properties the interpreter needs. Properties that are not set yet in
 * the game (a label without name, a literal without value, an operator without symbol) are {@code null}.
 */
public sealed interface Block {
    /** A keyword block, including the type blocks. */
    record KeywordBlock(Keyword keyword) implements Block {}

    /**
     * A LABEL block.
     *
     * @param name the name, or null if the label is not initialized
     * @param index the index entries, empty if there is no index
     */
    record LabelBlock(String name, List<IndexEntry> index) implements Block {
        public LabelBlock {
            index = List.copyOf(index);
        }

        public LabelBlock(String name) {
            this(name, List.of());
        }
    }

    /** A LITERAL block. The value is null if the literal is not initialized. */
    record LiteralBlock(Value value) implements Block {}

    /** An OPERATOR block. The operator is null if no symbol has been chosen yet. */
    record OperatorBlock(Operator operator) implements Block {}

    /** A COMMENT block. */
    record CommentBlock(String text) implements Block {}

    /** A TAG block. Language and name are null if they are not set yet. */
    record TagBlock(String language, String name) implements Block {}
}
