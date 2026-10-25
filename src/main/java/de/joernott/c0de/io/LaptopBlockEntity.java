package de.joernott.c0de.io;

import de.joernott.c0de.compat.C0deBlockEntity;
import de.joernott.c0de.compat.Nbt;
import de.joernott.c0de.content.C0deBlockEntities;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/** A laptop: the ID of its terminal, whose binding and screen are kept in {@link Terminals}. */
public class LaptopBlockEntity extends C0deBlockEntity {
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

    /** The laptop was removed: its terminal is deleted. */
    @Override
    protected void removed() {
        if (terminal != null && level instanceof net.minecraft.server.level.ServerLevel serverLevel) {
            Terminals.get(serverLevel.getServer()).remove(terminal);
        }
    }

    @Override
    protected void save(CompoundTag tag, HolderLookup.Provider registries) {
        if (terminal != null) {
            Nbt.putUuid(tag, "terminal", terminal);
        }
    }

    @Override
    protected void load(CompoundTag tag, HolderLookup.Provider registries) {
        terminal = Nbt.hasUuid(tag, "terminal") ? Nbt.uuid(tag, "terminal") : null;
    }
}
