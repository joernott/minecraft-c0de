package de.joernott.c0de.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

/**
 * The mark of a line made with the text marker (docs/concept/14-items-and-tools.md), shown as a yellow overlay
 * over the code area of the line. It stands in the middle of the line and is saved with its chunk; players can't
 * hit it, so clicks go to the blocks of the line.
 */
public class MarkEntity extends Entity {
    public MarkEntity(EntityType<? extends MarkEntity> type, Level level) {
        super(type, level);
        noPhysics = true;
        setNoGravity(true);
        setInvulnerable(true);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        // No synchronized data
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        return false;
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        // The position is all a mark needs
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        // The position is all a mark needs
    }
}
