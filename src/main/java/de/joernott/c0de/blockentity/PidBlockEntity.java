package de.joernott.c0de.blockentity;

import de.joernott.c0de.compat.C0deBlockEntity;
import de.joernott.c0de.compat.Nbt;
import de.joernott.c0de.content.C0deBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * The PID block of a running process: its PID and the trigger block that started it, which are shown on its
 * sides.
 */
public class PidBlockEntity extends C0deBlockEntity {
    private int pid;
    private String triggerLevel = "";
    private BlockPos triggerPos = BlockPos.ZERO;

    public PidBlockEntity(BlockPos pos, BlockState state) {
        super(C0deBlockEntities.PID.get(), pos, state);
    }

    public void setTrigger(int newPid, ResourceKey<Level> level, BlockPos pos) {
        pid = newPid;
        triggerLevel = level.location().toString();
        triggerPos = pos;
        setChanged();
        if (this.level != null) {
            this.level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 2);
        }
    }

    public int pid() {
        return pid;
    }

    /** The dimension and coordinates of the trigger block, e.g. {@code minecraft:overworld 10 64 -3}. */
    public String trigger() {
        return triggerLevel + " " + triggerPos.getX() + " " + triggerPos.getY() + " " + triggerPos.getZ();
    }

    @Override
    protected void save(CompoundTag tag, HolderLookup.Provider registries) {
        tag.putInt("pid", pid);
        tag.putString("triggerLevel", triggerLevel);
        tag.putLong("triggerPos", triggerPos.asLong());
    }

    @Override
    protected void load(CompoundTag tag, HolderLookup.Provider registries) {
        pid = Nbt.integer(tag, "pid");
        triggerLevel = Nbt.string(tag, "triggerLevel");
        triggerPos = BlockPos.of(Nbt.longValue(tag, "triggerPos"));
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
