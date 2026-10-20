package de.joernott.c0de.block;

import de.joernott.c0de.blockentity.LabelBlockEntity;
import de.joernott.c0de.content.CodeBlockType;
import de.joernott.c0de.dialog.DialogService;
import java.util.function.BiFunction;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/**
 * A LABEL, LITERAL, OPERATOR or TAG block: a validated block with properties that are edited in a dialog,
 * which opens when the block is right-clicked with an empty hand.
 */
public class PropertyBlock extends ValidatedCodeBlock implements EntityBlock {
    private final BiFunction<BlockPos, BlockState, BlockEntity> factory;

    public PropertyBlock(
            Properties properties, CodeBlockType type, BiFunction<BlockPos, BlockState, BlockEntity> factory) {
        super(properties, type);
        this.factory = factory;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return factory.apply(pos, state);
    }

    @Override
    protected InteractionResult useWithoutItem(
            BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        return openDialog(level, pos, player);
    }

    /** Opens the dialog of a block with properties for a player with an empty main hand. */
    static InteractionResult openDialog(Level level, BlockPos pos, Player player) {
        if (!player.getMainHandItem().isEmpty()) {
            return InteractionResult.PASS;
        }
        if (player instanceof ServerPlayer serverPlayer) {
            DialogService.open(serverPlayer, pos);
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    /** Initializes the kind and parent of a placed LABEL from its position. */
    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof LabelBlockEntity label) {
            label.placed(level);
        }
    }
}
