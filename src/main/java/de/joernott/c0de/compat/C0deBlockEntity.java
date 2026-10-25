package de.joernott.c0de.compat;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * A block entity of the mod that saves its data as a {@link CompoundTag} on all Minecraft versions (task 12.1): the
 * subclasses implement {@link #save} and {@link #load} instead of {@code saveAdditional} and {@code loadAdditional},
 * whose parameters changed in 1.21.6.
 */
public abstract class C0deBlockEntity extends BlockEntity {
    protected C0deBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    /** Writes the data of the block entity into the tag. */
    protected abstract void save(CompoundTag tag, HolderLookup.Provider registries);

    /** Reads the data of the block entity from the tag. */
    protected abstract void load(CompoundTag tag, HolderLookup.Provider registries);

    /** Called before the block is removed, e.g. to drop the items of the block entity. */
    protected void removed() {}

    /** Calls {@link #removed} for the blocks of the versions before 1.21.5, from their {@code onRemove}. */
    public final void removedBeforeRemoval() {
        removed();
    }

    //? if >=1.21.6 {
    /*@Override
    protected void saveAdditional(net.minecraft.world.level.storage.ValueOutput output) {
        super.saveAdditional(output);
        CompoundTag tag = new CompoundTag();
        save(tag, level != null ? level.registryAccess() : net.minecraft.core.RegistryAccess.EMPTY);
        output.store(Persist.ALL, tag);
    }

    @Override
    protected void loadAdditional(net.minecraft.world.level.storage.ValueInput input) {
        super.loadAdditional(input);
        load(input.read(Persist.ALL).orElseGet(CompoundTag::new), input.lookup());
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        super.preRemoveSideEffects(pos, state);
        removed();
    }
    *///?} else {
    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        save(tag, registries);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        load(tag, registries);
    }
    //?}
}
