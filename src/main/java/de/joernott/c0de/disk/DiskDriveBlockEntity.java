package de.joernott.c0de.disk;

import de.joernott.c0de.compat.C0deBlockEntity;
import de.joernott.c0de.compat.Nbt;
import de.joernott.c0de.compat.Persist;
import de.joernott.c0de.content.C0deBlockEntities;
import de.joernott.c0de.platform.Platform;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/** A disk drive: the slot of its disk. */
public class DiskDriveBlockEntity extends C0deBlockEntity implements MenuProvider {
    private final SimpleContainer disk = new SimpleContainer(1) {
        @Override
        public int getMaxStackSize() {
            return 1;
        }

        @Override
        public void setChanged() {
            super.setChanged();
            DiskDriveBlockEntity.this.setChanged();
        }
    };

    public DiskDriveBlockEntity(BlockPos pos, BlockState state) {
        super(C0deBlockEntities.DISK_DRIVE.get(), pos, state);
    }

    public SimpleContainer disk() {
        return disk;
    }

    /** Opens the dialog of the drive. */
    public void openMenu(ServerPlayer player) {
        Platform.INSTANCE.openMenu(player, this, new CompoundTag());
    }

    /** The drive was removed: the disk drops. */
    @Override
    protected void removed() {
        if (level != null) {
            net.minecraft.world.Containers.dropContents(level, worldPosition, disk);
        }
    }

    @Override
    public Component getDisplayName() {
        return getBlockState().getBlock().getName();
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new DiskDriveMenu(id, inventory, this);
    }

    @Override
    protected void save(CompoundTag tag, HolderLookup.Provider registries) {
        tag.put("disk", Persist.saveItems(disk, registries));
    }

    @Override
    protected void load(CompoundTag tag, HolderLookup.Provider registries) {
        Persist.loadItems(disk, Nbt.list(tag, "disk", Tag.TAG_COMPOUND), registries);
    }
}
