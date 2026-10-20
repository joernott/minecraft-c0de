package de.joernott.c0de.blockentity;

import de.joernott.c0de.core.line.Block;
import de.joernott.c0de.sheet.SheetValidation;
import java.time.ZoneId;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * The properties of a LABEL, LITERAL, OPERATOR, COMMENT or TAG block. They are edited in a dialog and
 * synchronized to the clients, which show them on the block.
 */
public abstract class PropertiesBlockEntity extends BlockEntity {
    protected PropertiesBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    /** The kind of dialog: label, literal, operator, comment or tag. */
    public abstract String dialog();

    /** The block in the line model of {@code core}. */
    public abstract Block toCore(ZoneId zone);

    /** The text shown on the sides of the block, empty for none. */
    public abstract String displayText();

    /** Reads the properties. */
    protected abstract void read(CompoundTag tag);

    /** Writes the properties. */
    protected abstract void write(CompoundTag tag);

    /**
     * The data the dialog needs: the properties and, depending on the block, its context (e.g. the layout of
     * the LABEL dialog and the entries of its list).
     */
    public CompoundTag dialogData(ServerLevel level) {
        CompoundTag data = new CompoundTag();
        write(data);
        return data;
    }

    /**
     * Checks the properties entered in the dialog and changes the block if they are valid.
     *
     * @return the problem if the properties are not valid
     */
    public Optional<String> apply(CompoundTag properties, ServerLevel level) {
        Optional<String> problem = check(properties, level);
        if (problem.isEmpty()) {
            read(properties);
            changed();
        }
        return problem;
    }

    /** Checks the properties entered in the dialog. */
    protected abstract Optional<String> check(CompoundTag properties, ServerLevel level);

    /** Saves the block entity and sends the changed properties to the clients. */
    protected void changed() {
        setChanged();
        if (level != null) {
            SheetValidation.changed(level, worldPosition);
            level.sendBlockUpdated(
                    worldPosition,
                    getBlockState(),
                    getBlockState(),
                    net.minecraft.world.level.block.Block.UPDATE_CLIENTS);
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        read(tag);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        write(tag);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        write(tag);
        return tag;
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
