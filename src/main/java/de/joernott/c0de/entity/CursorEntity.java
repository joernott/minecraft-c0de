package de.joernott.c0de.entity;

import de.joernott.c0de.compat.C0deEntity;
import de.joernott.c0de.compat.Compat;
import de.joernott.c0de.compat.Nbt;
import de.joernott.c0de.item.DebugToolItem;
import de.joernott.c0de.tool.Inspector;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/**
 * The cursor of a running process (docs/concept/15-cursor.md). Its y coordinate is the PID and its z coordinate
 * the line it processes. The entity only shows the cursor: the process manager runs the process and moves the
 * entity. It is not saved with the chunk; the process manager creates it again when the process is restored.
 */
public class CursorEntity extends C0deEntity {
    /** True for debug cursors, which are drawn in red instead of green. */
    private static final EntityDataAccessor<Boolean> DEBUG =
            SynchedEntityData.defineId(CursorEntity.class, EntityDataSerializers.BOOLEAN);

    /** True while the process waits for a value or for room (WAITING): the cursor pulses from black. */
    private static final EntityDataAccessor<Boolean> WAITING =
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

    public boolean isWaiting() {
        return entityData.get(WAITING);
    }

    public void setWaiting(boolean waiting) {
        if (isWaiting() != waiting) {
            entityData.set(WAITING, waiting);
        }
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DEBUG, false);
        builder.define(WAITING, false);
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

    /** Right-clicking a cursor with the debugging tool shows its properties and stack. */
    //? if >=26.1 {
    /*@Override
    public InteractionResult interact(Player player, InteractionHand hand, net.minecraft.world.phys.Vec3 location) {
    *///?} else {
    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
    //?}
        if (!(player.getItemInHand(hand).getItem() instanceof DebugToolItem)) {
            return InteractionResult.PASS;
        }
        if (player instanceof ServerPlayer serverPlayer) {
            Inspector.inspectCursor(serverPlayer, this);
        }
        return Compat.success(level());
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    protected void loadData(CompoundTag tag) {
        process = Nbt.hasUuid(tag, "process") ? Nbt.uuid(tag, "process") : null;
    }

    @Override
    protected void saveData(CompoundTag tag) {
        if (process != null) {
            Nbt.putUuid(tag, "process", process);
        }
    }
}
