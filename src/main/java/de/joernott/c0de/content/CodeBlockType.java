package de.joernott.c0de.content;

import de.joernott.c0de.core.line.Block;
import de.joernott.c0de.core.line.Keyword;
import java.util.Locale;

/**
 * The language blocks of c0de (docs/concept/18-block-reference.md). The block IDs, textures, recipes and
 * translations in the resources are generated from the same list by {@code scripts/generate_content.py}.
 */
public enum CodeBlockType {
    NOOP(Keyword.NOOP, Shape.TRANSPARENT, false),
    COMMENT(null, Shape.TRANSPARENT, false),
    LABEL(null, Shape.SOLID, true),
    PROGRAM(Keyword.PROGRAM),
    INCLUDE(Keyword.INCLUDE),
    EXIT(Keyword.EXIT),
    BEGIN(Keyword.BEGIN),
    END(Keyword.END),
    BREAK(Keyword.BREAK),
    TYPE_BOOLEAN(Keyword.BOOLEAN, Shape.SOLID, false),
    TYPE_INTEGER(Keyword.INTEGER, Shape.SOLID, false),
    TYPE_FLOAT(Keyword.FLOAT, Shape.SOLID, false),
    TYPE_CHAR(Keyword.CHAR, Shape.SOLID, false),
    TYPE_STRING(Keyword.STRING, Shape.SOLID, false),
    TYPE_ARRAY(Keyword.ARRAY, Shape.SOLID, false),
    TYPE_HASH(Keyword.HASH, Shape.SOLID, false),
    TYPE_STRUCT(Keyword.STRUCT, Shape.SOLID, false),
    TYPE_TIME(Keyword.TIME, Shape.SOLID, false),
    TYPE_ERROR(Keyword.ERROR, Shape.SOLID, false),
    VARIABLE(Keyword.VARIABLE),
    CONSTANT(Keyword.CONSTANT),
    LITERAL(null, Shape.SOLID, true),
    TAG(null, Shape.SOLID, true),
    OPERATOR(null, Shape.SOLID, true),
    ASSIGN(Keyword.ASSIGN),
    FUNCTION(Keyword.FUNCTION),
    PARAMETER(Keyword.PARAMETER),
    RESULT(Keyword.RESULT),
    RETURN(Keyword.RETURN),
    CALL(Keyword.CALL),
    IF(Keyword.IF),
    ELSE(Keyword.ELSE),
    FOR(Keyword.FOR),
    EACH(Keyword.EACH),
    WHILE(Keyword.WHILE),
    REPEAT(Keyword.REPEAT),
    PRINT(Keyword.PRINT),
    WRITE(Keyword.WRITE),
    INPUT(Keyword.INPUT);

    private final Keyword keyword;
    private final Shape shape;
    private final boolean validated;

    CodeBlockType(Keyword keyword) {
        this(keyword, Shape.SOLID, true);
    }

    CodeBlockType(Keyword keyword, Shape shape, boolean validated) {
        this.keyword = keyword;
        this.shape = shape;
        this.validated = validated;
    }

    /** The ID of the block, e.g. {@code type_boolean_block}. */
    public String id() {
        return name().toLowerCase(Locale.ROOT) + "_block";
    }

    /** The keyword of a keyword or type block, null for the other blocks. */
    public Keyword keyword() {
        return keyword;
    }

    /** True for the transparent NOOP and COMMENT blocks. */
    public boolean isTransparent() {
        return shape == Shape.TRANSPARENT;
    }

    /** True if the block has the block state {@code valid}, which selects its valid or invalid color. */
    public boolean isValidated() {
        return validated;
    }

    /** The block in the line model of {@code core} as it is placed, before its properties are set. */
    public Block placed() {
        return switch (this) {
            case LABEL -> new Block.LabelBlock(null);
            case LITERAL -> new Block.LiteralBlock(null);
            case OPERATOR -> new Block.OperatorBlock(null);
            case COMMENT -> new Block.CommentBlock("");
            case TAG -> new Block.TagBlock(null, null);
            default -> new Block.KeywordBlock(keyword);
        };
    }

    private enum Shape {
        SOLID,
        TRANSPARENT
    }
}
