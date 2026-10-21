package de.joernott.c0de.entity;

import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

/**
 * The cursor of a running process (docs/concept/15-cursor.md). Its y coordinate is the PID and its z coordinate
 * the line it processes. The entity only shows the cursor: the process manager runs the process and moves the
 * entity. It is not saved with the chunk; the process manager creates it again when the process is restored.
 */
public class CursorEntity extends Entity {
    /** True for debug cursors, which are drawn in red instead of green. */
    private static final EntityDataAccessor<Boolean> DEBUG =
            SynchedEntityData.defineId(CursorEntity.class, EntityDataSerializers.BOOLEAN);

    private UUID process;

    public CursorEntity(EntityType<? extends CursorEntity> type, Level level) {
        super(type, level);
        noPhysics = true;
        setNoGravity(true);
        setInvulnerable(true);
    }

    public UUID process() {
        return process;
    }

    public void setProcess(UUID id) {
        process = id;
    }

    public boolean isDebug() {
        return entityData.get(DEBUG);
    }

    public void setDebug(boolean debug) {
        entityData.set(DEBUG, debug);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DEBUG, false);
    }

    @Override
    public boolean shouldBeSaved() {
        return false;
    }

    @Override
    public boolean isPickable() {
        // The debugging tool interacts with cursors (sprint 9)
        return true;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        return false;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        process = tag.hasUUID("process") ? tag.getUUID("process") : null;
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        if (process != null) {
            tag.putUUID("process", process);
        }
    }
}
