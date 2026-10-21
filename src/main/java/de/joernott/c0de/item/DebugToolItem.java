package de.joernott.c0de.item;

import de.joernott.c0de.tool.Inspector;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/**
 * The debugging tool (docs/concept/14-items-and-tools.md). Right-clicking a c0de block shows its properties,
 * right-clicking a cursor shows the cursor and its stack. Attacking a debug trigger block starts a process with a
 * debug cursor, attacking a debug cursor controls it (see {@link de.joernott.c0de.tool.ToolActions}).
 */
public class DebugToolItem extends Item {
    public DebugToolItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
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
    @Override
    public boolean canAttackBlock(BlockState state, Level level, BlockPos pos, Player player) {
        return false;
    }
}
