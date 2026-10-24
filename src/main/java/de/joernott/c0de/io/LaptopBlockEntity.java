package de.joernott.c0de.io;

import de.joernott.c0de.content.C0deBlockEntities;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/** A laptop: the ID of its terminal, whose binding and screen are kept in {@link Terminals}. */
public class LaptopBlockEntity extends BlockEntity {
    private UUID terminal;

    public LaptopBlockEntity(BlockPos pos, BlockState state) {
        super(C0deBlockEntities.LAPTOP.get(), pos, state);
    }

    /** The ID of the terminal; a new laptop gets one when it is first used. */
    public UUID terminal() {
        if (terminal == null) {
            terminal = UUID.randomUUID();
            setChanged();
        }
        return terminal;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (terminal != null) {
            tag.putUUID("terminal", terminal);
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        terminal = tag.hasUUID("terminal") ? tag.getUUID("terminal") : null;
    }
}
