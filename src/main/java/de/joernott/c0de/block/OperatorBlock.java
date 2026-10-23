package de.joernott.c0de.block;

import de.joernott.c0de.blockentity.OperatorBlockEntity;
import de.joernott.c0de.content.CodeBlockType;
import de.joernott.c0de.core.line.Operator;
import java.util.Locale;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;

/**
 * The OPERATOR block: the symbol of its operator, extruded from the floor. The operator is a property of the
 * block entity; the block state {@code operator} mirrors it, so that every symbol has its own block model
 * (scripts/block_models.py, GLYPHS).
 */
public class OperatorBlock extends PropertyBlock {
    public static final EnumProperty<Symbol> OPERATOR = EnumProperty.create("operator", Symbol.class);

    public OperatorBlock(Properties properties) {
        super(properties, CodeBlockType.OPERATOR, OperatorBlockEntity::new);
        registerDefaultState(defaultBlockState().setValue(OPERATOR, Symbol.NONE));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(OPERATOR);
    }

    /** The values of the block state: no operator yet, or one of the operators. */
    public enum Symbol implements StringRepresentable {
        NONE(null),
        BITWISE_NOT(Operator.BITWISE_NOT),
        NOT(Operator.NOT),
        POWER(Operator.POWER),
        NEGATE(Operator.NEGATE),
        MULTIPLY(Operator.MULTIPLY),
        DIVIDE(Operator.DIVIDE),
        MODULUS(Operator.MODULUS),
        ADD(Operator.ADD),
        SUBTRACT(Operator.SUBTRACT),
        SHIFT_LEFT(Operator.SHIFT_LEFT),
        SHIFT_RIGHT(Operator.SHIFT_RIGHT),
        CONCAT(Operator.CONCAT),
        EQUAL(Operator.EQUAL),
        NOT_EQUAL(Operator.NOT_EQUAL),
        LESS(Operator.LESS),
        GREATER(Operator.GREATER),
        LESS_OR_EQUAL(Operator.LESS_OR_EQUAL),
        GREATER_OR_EQUAL(Operator.GREATER_OR_EQUAL),
        BITWISE_AND(Operator.BITWISE_AND),
        BITWISE_XOR(Operator.BITWISE_XOR),
        BITWISE_OR(Operator.BITWISE_OR),
        AND(Operator.AND),
        XOR(Operator.XOR),
        OR(Operator.OR),
        OPEN(Operator.OPEN),
        CLOSE(Operator.CLOSE),
        END_OF_EXPRESSION(Operator.END_OF_EXPRESSION);

        private final Operator operator;

        Symbol(Operator operator) {
            this.operator = operator;
        }

        /** The value for an operator, or NONE for null. */
        public static Symbol of(Operator operator) {
            for (Symbol symbol : values()) {
                if (symbol.operator == operator) {
                    return symbol;
                }
            }
            return NONE;
        }

        @Override
        public String getSerializedName() {
            return name().toLowerCase(Locale.ROOT);
        }
    }
}
