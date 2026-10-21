package de.joernott.c0de.item;

import de.joernott.c0de.block.CodeBlock;
import de.joernott.c0de.content.CodeBlockType;
import de.joernott.c0de.disk.DiskService;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;

/**
 * The c0de disk (docs/concept/14-items-and-tools.md). Right-clicking the PROGRAM block of a sheet exports the
 * program to a YAML file; right-clicking the floor at the PROGRAM position of an empty sheet imports one.
 */
public class DiskItem extends Item {
    public DiskItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        boolean program = context.getLevel().getBlockState(context.getClickedPos()).getBlock() instanceof CodeBlock code
                && code.type() == CodeBlockType.PROGRAM;
        if (!(context.getPlayer() instanceof ServerPlayer player)) {
            return program ? InteractionResult.SUCCESS : InteractionResult.PASS;
        }
        if (program) {
            DiskService.export(player, player.serverLevel(), context.getClickedPos(), false);
            return InteractionResult.SUCCESS;
        }
        if (DiskService.isProgramPosition(player.serverLevel(), context.getClickedPos())) {
            DiskService.openImport(player, context.getClickedPos());
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }
}
