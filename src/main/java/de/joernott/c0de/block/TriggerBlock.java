package de.joernott.c0de.block;

import de.joernott.c0de.blockentity.TriggerBlockEntity;
import de.joernott.c0de.compat.Compat;
import de.joernott.c0de.process.Activator;
import de.joernott.c0de.core.sheet.SheetGeometry;
import de.joernott.c0de.dimension.C0deDimension;
import de.joernott.c0de.item.WrenchItem;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
//? if <1.21.2
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;

/**
 * A trigger block, timer block or debug trigger block (docs/concept/13-running-programs.md). It starts processes
 * of the program and function it is configured with: the trigger block on a redstone pulse, the timer block at
 * an interval and the debug trigger block when it is attacked with a debugging tool (sprint 9). Right-clicking
 * opens its dialog with the configuration and the inventories; right-clicking it with a wrench enters the c0de
 * dimension.
 */
public class TriggerBlock extends Block implements EntityBlock {
    /** The kinds of trigger blocks. */
    public enum Kind {
        TRIGGER,
        TIMER,
        DEBUG
    }

    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

    /** True if a block in the camouflage slot is shown instead of the trigger block. */
    public static final BooleanProperty CAMOUFLAGED = BooleanProperty.create("camouflaged");

    private final Kind kind;

    public TriggerBlock(Properties properties, Kind kind) {
        super(properties);
        this.kind = kind;
        registerDefaultState(
                stateDefinition.any().setValue(POWERED, false).setValue(CAMOUFLAGED, false));
    }

    public Kind kind() {
        return kind;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(POWERED, CAMOUFLAGED);
    }

    /** In the c0de dimension, trigger blocks can only be placed outside of the sheets. */
    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        if (level instanceof Level world && world.dimension().equals(C0deDimension.LEVEL)) {
            return !SheetGeometry.isOnSheet(pos.getX(), pos.getZ());
        }
        return true;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TriggerBlockEntity(pos, state);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide() || kind != Kind.TIMER) {
            return null;
        }
        return (tickLevel, pos, tickState, entity) -> {
            if (entity instanceof TriggerBlockEntity trigger) {
                trigger.tickTimer();
            }
        };
    }

    /** The wrench and the debugging tool are used on the block; everything else opens the dialog. */
    //? if >=1.21.2 {
    /*@Override
    protected InteractionResult useItemOn(
            ItemStack stack,
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            InteractionHand hand,
            BlockHitResult hitResult) {
        return stack.getItem() instanceof WrenchItem || stack.getItem() instanceof de.joernott.c0de.item.DebugToolItem
                ? InteractionResult.PASS
                : InteractionResult.TRY_WITH_EMPTY_HAND;
    }
    *///?} else {
    @Override
    protected ItemInteractionResult useItemOn(
            ItemStack stack,
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            InteractionHand hand,
            BlockHitResult hitResult) {
        return stack.getItem() instanceof WrenchItem || stack.getItem() instanceof de.joernott.c0de.item.DebugToolItem
                ? ItemInteractionResult.SKIP_DEFAULT_BLOCK_INTERACTION
                : ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }
    //?}

    @Override
    protected InteractionResult useWithoutItem(
            BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (player instanceof ServerPlayer serverPlayer
                && level.getBlockEntity(pos) instanceof TriggerBlockEntity trigger) {
            trigger.openMenu(serverPlayer);
        }
        return Compat.success(level);
    }

    /** A trigger block starts a process on the rising edge of a redstone signal. */
    @Override
    //? if >=1.21.2 {
    /*protected void neighborChanged(
            BlockState state,
            Level level,
            BlockPos pos,
            Block neighbor,
            net.minecraft.world.level.redstone.Orientation orientation,
            boolean movedByPiston) {
    *///?} else {
    protected void neighborChanged(
            BlockState state, Level level, BlockPos pos, Block neighbor, BlockPos neighborPos, boolean movedByPiston) {
    //?}
        boolean powered = level.hasNeighborSignal(pos);
        if (powered == state.getValue(POWERED)) {
            return;
        }
        level.setBlock(pos, state.setValue(POWERED, powered), Block.UPDATE_CLIENTS);
        if (powered
                && kind == Kind.TRIGGER
                && level instanceof ServerLevel
                && level.getBlockEntity(pos) instanceof TriggerBlockEntity trigger) {
            trigger.start(Activator.find((ServerLevel) level, pos));
        }
    }

    /**
     * A block placed next to a redstone signal that is already on is powered from the start: a timer block is
     * paused, a trigger block waits for the next rising edge.
     */
    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        boolean powered = level.hasNeighborSignal(pos);
        if (!oldState.is(state.getBlock()) && powered != state.getValue(POWERED)) {
            level.setBlock(pos, state.setValue(POWERED, powered), Block.UPDATE_CLIENTS);
        }
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return state.getValue(CAMOUFLAGED) ? RenderShape.INVISIBLE : RenderShape.MODEL;
    }

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
        return level.getBlockEntity(pos) instanceof TriggerBlockEntity trigger ? trigger.comparatorSignal() : 0;
    }

    /** What happens after the block was removed: its block entity has already cleaned up (removed). */
    private void afterRemoval(ServerLevel level, BlockPos pos) {
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
}
