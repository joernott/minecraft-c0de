package de.joernott.c0de.block;

import de.joernott.c0de.blockentity.PidBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * The PID block of a running process (docs/concept/13-running-programs.md). Its y coordinate is the PID. It
 * can't be crafted or destroyed; the process manager removes it when the process ends.
 */
public class PidBlock extends Block implements EntityBlock {
    public PidBlock(Properties properties) {
        super(properties);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new PidBlockEntity(pos, state);
    }
}
