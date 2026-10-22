package de.joernott.c0de.block;

import de.joernott.c0de.content.CodeBlockType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

/**
 * A language block that shows whether its statement is valid by its color. The block state {@code valid} is
 * set by the validation of the sheet (sprint 7); until then, placed blocks are invalid. The block state
 * {@code incomplete} marks the lines of an invalid statement that is not finished yet; the line numbers on the
 * floor show it (task 11.3).
 */
public class ValidatedCodeBlock extends CodeBlock {
    public static final BooleanProperty VALID = BooleanProperty.create("valid");
    public static final BooleanProperty INCOMPLETE = BooleanProperty.create("incomplete");

    public ValidatedCodeBlock(Properties properties, CodeBlockType type) {
        super(properties, type);
        registerDefaultState(stateDefinition.any().setValue(VALID, false).setValue(INCOMPLETE, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(VALID, INCOMPLETE);
    }
}
