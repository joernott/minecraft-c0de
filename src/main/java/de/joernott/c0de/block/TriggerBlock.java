package de.joernott.c0de.block;

import de.joernott.c0de.core.sheet.SheetGeometry;
import de.joernott.c0de.dimension.C0deDimension;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * The trigger block. For now it only serves as the entrance to the c0de dimension (right-click with a
 * wrench); processes are started by it from sprint 8 on.
 */
public class TriggerBlock extends Block {
    public TriggerBlock(Properties properties) {
        super(properties);
    }

    /** In the c0de dimension, trigger blocks can only be placed outside of the sheets. */
    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        if (level instanceof net.minecraft.world.level.Level world && world.dimension().equals(C0deDimension.LEVEL)) {
            return !SheetGeometry.isOnSheet(pos.getX(), pos.getZ());
        }
        return true;
    }
}
