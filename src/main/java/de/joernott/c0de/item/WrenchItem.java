package de.joernott.c0de.item;

import de.joernott.c0de.block.TriggerBlock;
import de.joernott.c0de.dimension.C0deDimension;
import de.joernott.c0de.sheet.LevelSheet;
import de.joernott.c0de.tool.ToolActions;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/**
 * The c0de wrench (docs/concept/14-items-and-tools.md). Right-clicking a trigger block with it teleports the
 * player into the c0de dimension. On a sheet, left-clicking a marked area selects it, and right-clicking another
 * line moves the selected lines in front of that line.
 */
public class WrenchItem extends Item {
    public WrenchItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        if (level.getBlockState(pos).getBlock() instanceof TriggerBlock) {
            if (context.getPlayer() instanceof ServerPlayer player) {
                C0deDimension.enter(player, pos);
            }
            return InteractionResult.sidedSuccess(level.isClientSide());
        }
        if (!LevelSheet.isOnSheet(level, pos)) {
            return InteractionResult.PASS;
        }
        if (context.getPlayer() instanceof ServerPlayer player) {
            return ToolActions.wrenchTarget(player, pos) ? InteractionResult.SUCCESS : InteractionResult.PASS;
        }
        return InteractionResult.SUCCESS;
    }

    /** The wrench doesn't break blocks; its left click selects marked lines. */
    @Override
    public boolean canAttackBlock(BlockState state, Level level, BlockPos pos, Player player) {
        return false;
    }
}
