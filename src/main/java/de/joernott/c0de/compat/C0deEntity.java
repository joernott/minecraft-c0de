package de.joernott.c0de.compat;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

/**
 * An entity of the mod (cursors and marks) that can't be hurt and saves its data as a {@link CompoundTag} on all
 * Minecraft versions (task 12.1): subclasses implement {@link #saveData} and {@link #loadData}.
 */
public abstract class C0deEntity extends Entity {
    protected C0deEntity(EntityType<?> type, Level level) {
        super(type, level);
    }

    /** Writes the data of the entity into the tag. */
    protected void saveData(CompoundTag tag) {}

    /** Reads the data of the entity from the tag. */
    protected void loadData(CompoundTag tag) {}

    //? if >=1.21.6 {
    /*@Override
    protected void readAdditionalSaveData(net.minecraft.world.level.storage.ValueInput input) {
        loadData(input.read(Persist.ALL).orElseGet(CompoundTag::new));
    }

    @Override
    protected void addAdditionalSaveData(net.minecraft.world.level.storage.ValueOutput output) {
        CompoundTag tag = new CompoundTag();
        saveData(tag);
        output.store(Persist.ALL, tag);
    }
    *///?} else {
    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        loadData(tag);
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        saveData(tag);
    }
    //?}

    //? if >=1.21.2 {
    /*@Override
    public boolean hurtServer(net.minecraft.server.level.ServerLevel level, DamageSource source, float amount) {
        return false;
    }
    *///?} else {
    @Override
    public boolean hurt(DamageSource source, float amount) {
        return false;
    }
    //?}
}
