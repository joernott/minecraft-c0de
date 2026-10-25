package de.joernott.c0de.item;

import de.joernott.c0de.compat.Compat;
import de.joernott.c0de.content.C0deBlocks;
import de.joernott.c0de.process.ProcessManager;
import de.joernott.c0de.sheet.LevelSheet;
import de.joernott.c0de.tool.Inspector;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/**
 * The debugging tool (docs/concept/14-items-and-tools.md). Right-clicking a c0de block shows its properties,
 * right-clicking a cursor shows the cursor and its stack, shift-right-clicking a PID block aborts its process. Attacking a debug trigger block starts a process with a
 * debug cursor, attacking a debug cursor controls it (see {@link de.joernott.c0de.tool.ToolActions}).
 */
public class DebugToolItem extends Item {
    public DebugToolItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        BlockPos pos = context.getClickedPos();
        if (context.getPlayer() != null
                && context.getPlayer().isShiftKeyDown()
                && context.getLevel().getBlockState(pos).is(C0deBlocks.PID_BLOCK.get())) {
            if (context.getPlayer() instanceof ServerPlayer player) {
                player.displayClientMessage(
                        ProcessManager.get(Compat.server(player))
                                .abort(Compat.server(player), player, LevelSheet.sheetOf(pos), pos.getY()),
                        true);
            }
            return InteractionResult.SUCCESS;
        }
        if (context.getPlayer() instanceof ServerPlayer player) {
            return Inspector.inspectBlock(player, context.getClickedPos())
                    ? InteractionResult.SUCCESS
                    : InteractionResult.PASS;
        }
        return Inspector.isInspectable(context.getLevel(), context.getClickedPos())
                ? InteractionResult.SUCCESS
                : InteractionResult.PASS;
    }

    /** The tool doesn't break blocks, not even in creative mode. */
    //? if >=1.21.5 {
    /*@Override
    public boolean canDestroyBlock(
            ItemStack stack, BlockState state, Level level, BlockPos pos, net.minecraft.world.entity.LivingEntity entity) {
        return false;
    }
    *///?} else {
    @Override
    public boolean canAttackBlock(BlockState state, Level level, BlockPos pos, Player player) {
        return false;
    }
    //?}
}
