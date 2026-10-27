package de.joernott.c0de.disk;

import de.joernott.c0de.content.C0deMenus;
import de.joernott.c0de.content.C0deSounds;
import de.joernott.c0de.item.DiskItem;
import java.util.Optional;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/** The dialog of a disk drive: the slot of the disk and the player inventory. */
public class DiskDriveMenu extends AbstractContainerMenu {
    public static final int SLOT = 18;
    /** The width of the dialog (task 13.2), with the player inventory in the middle. */
    public static final int WIDTH = 200;
    public static final int INVENTORY_X = (WIDTH - 9 * SLOT) / 2;
    public static final int DISK_X = (WIDTH - 16) / 2;
    public static final int DISK_Y = 24;
    /** The box of the messages: two lines below the name of the disk. */
    public static final int MESSAGES_Y = 58;
    public static final int MESSAGES_HEIGHT = 24;
    public static final int PLAYER_Y = MESSAGES_Y + MESSAGES_HEIGHT + 14;
    public static final int HOTBAR_Y = PLAYER_Y + 3 * SLOT + 4;

    private final DiskDriveBlockEntity drive;
    private final Container disk;

    /** The menu on the server. */
    public DiskDriveMenu(int id, Inventory inventory, DiskDriveBlockEntity drive) {
        this(id, inventory, drive, drive.disk());
    }

    /** The menu on the client. */
    public DiskDriveMenu(int id, Inventory inventory, CompoundTag data) {
        this(id, inventory, null, new SimpleContainer(1));
    }

    private DiskDriveMenu(int id, Inventory inventory, DiskDriveBlockEntity drive, Container disk) {
        super(C0deMenus.DISK_DRIVE.get(), id);
        this.drive = drive;
        this.disk = disk;
        addSlot(new Slot(disk, 0, DISK_X, DISK_Y) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.getItem() instanceof DiskItem;
            }

            @Override
            public int getMaxStackSize() {
                return 1;
            }
        });
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(inventory, column + row * 9 + 9, INVENTORY_X + column * SLOT, PLAYER_Y + row * SLOT));
            }
        }
        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(inventory, column, INVENTORY_X + column * SLOT, HOTBAR_Y));
        }
    }

    /** The disk in the drive, or an empty stack. */
    public ItemStack disk() {
        return disk.getItem(0);
    }

    /**
     * Writes a program onto the disk in the drive (on the server).
     *
     * @return the reason if it can't be written
     */
    public Optional<String> load(String yaml) {
        ItemStack stack = disk.getItem(0);
        if (!(stack.getItem() instanceof DiskItem)) {
            return Optional.of("There is no disk in the drive");
        }
        try {
            DiskService.contents(yaml).writeTo(stack);
        } catch (IllegalArgumentException e) {
            return Optional.of(String.valueOf(e.getMessage()));
        }
        disk.setChanged();
        broadcastChanges();
        sound();
        return Optional.empty();
    }

    /** Lets the drive sound as it reads or writes its disk, for the players around it (on the server). */
    public void sound() {
        if (drive != null && drive.getLevel() != null) {
            drive.getLevel()
                    .playSound(null, drive.getBlockPos(), C0deSounds.DISK_DRIVE.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = slot.getItem();
        ItemStack original = stack.copy();
        if (index == 0) {
            if (!moveItemStackTo(stack, 1, slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else if (stack.getItem() instanceof DiskItem && !slots.get(0).hasItem()) {
            if (!moveItemStackTo(stack, 0, 1, false)) {
                return ItemStack.EMPTY;
            }
        } else {
            return ItemStack.EMPTY;
        }
        if (stack.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        return original;
    }

    @Override
    public boolean stillValid(Player player) {
        return drive == null || Container.stillValidBlockEntity(drive, player);
    }
}
