package de.joernott.c0de.io;

import de.joernott.c0de.compat.Compat;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * The I/O block (docs/concept/19-input-output.md): a blue hopper with an input and an output inventory. Right-clicking
 * it opens its inventories; the binding is changed with a button in that dialog.
 */
public class IoBlock extends BaseEntityBlock {
    //? if <26.3
    private static final MapCodec<IoBlock> CODEC = simpleCodec(IoBlock::new);
    // The shape of a hopper pointing down
    private static final VoxelShape SHAPE = Shapes.or(
            Shapes.join(box(0, 10, 0, 16, 16, 16), box(2, 11, 2, 14, 16, 14), BooleanOp.ONLY_FIRST),
            box(4, 4, 4, 12, 10, 12),
            box(6, 0, 6, 10, 4, 10));

    public IoBlock(Properties properties) {
        super(properties);
    }

    //? if <26.3 {
    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }
    //?}

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new IoBlockEntity(pos, state);
    }

    @Override
    protected InteractionResult useWithoutItem(
            BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (player instanceof ServerPlayer serverPlayer && level.getBlockEntity(pos) instanceof IoBlockEntity entity) {
            entity.openMenu(serverPlayer);
        }
        return Compat.success(level);
    }

    /** What happens after the block was removed: its block entity has already cleaned up (removed). */
    private void afterRemoval(ServerLevel level, BlockPos pos) {
        level.updateNeighbourForOutputSignal(pos, this);
    }

    //? if >=1.21.5 {
    /*@Override
    protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean movedByPiston) {
        super.affectNeighborsAfterRemoval(state, level, pos, movedByPiston);
        afterRemoval(level, pos);
    }
    *///?} else {
    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        boolean replaced = !state.is(newState.getBlock());
        if (replaced && level.getBlockEntity(pos) instanceof de.joernott.c0de.compat.C0deBlockEntity entity) {
            entity.removedBeforeRemoval();
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
        if (replaced && level instanceof ServerLevel serverLevel) {
            afterRemoval(serverLevel, pos);
        }
    }
    //?}

    @Override
    protected boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    //? if >=1.21.11 {
    /*protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos, net.minecraft.core.Direction direction) {
    *///?} else {
    protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
    //?}
        // Like a hopper, measured on the output: a comparator shows that results are waiting
        return level.getBlockEntity(pos) instanceof IoBlockEntity entity
                ? net.minecraft.world.inventory.AbstractContainerMenu.getRedstoneSignalFromContainer(entity.output())
                : 0;
    }
}
