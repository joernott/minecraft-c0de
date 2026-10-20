package de.joernott.c0de.block;

import de.joernott.c0de.blockentity.CommentBlockEntity;
import de.joernott.c0de.content.CodeBlockType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/** The COMMENT block. It has no invalid color, so it is not a {@link ValidatedCodeBlock}. */
public class CommentBlock extends CodeBlock implements EntityBlock {
    public CommentBlock(Properties properties) {
        super(properties, CodeBlockType.COMMENT);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CommentBlockEntity(pos, state);
    }

    @Override
    protected InteractionResult useWithoutItem(
            BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        return PropertyBlock.openDialog(level, pos, player);
    }
}
