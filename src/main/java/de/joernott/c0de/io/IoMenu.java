package de.joernott.c0de.io;

import de.joernott.c0de.content.C0deMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * The dialog of an I/O block: the input inventory (variable, constant and literal items), the output inventory
 * (only taking) and the player inventory. The binding is shown and changed with the dialog of devices.
 */
public class IoMenu extends AbstractContainerMenu {
    public static final int SLOT = 18;
    public static final int INPUT_Y = 42;
    public static final int OUTPUT_Y = INPUT_Y + SLOT + 14;
    public static final int PLAYER_Y = OUTPUT_Y + SLOT + 14;
    public static final int HOTBAR_Y = PLAYER_Y + 3 * SLOT + 4;
    private static final int CONTAINER_SLOTS = 2 * IoBlockEntity.SIZE;

    private final IoBlockEntity block;
    private final CompoundTag data;
    private final BlockPos pos;

    /** The menu on the server. */
    public IoMenu(int id, Inventory inventory, IoBlockEntity block) {
        this(id, inventory, block, block.input(), block.output(), new CompoundTag());
    }

    /** The menu on the client, with the data sent by the server. */
    public IoMenu(int id, Inventory inventory, CompoundTag data) {
        this(id, inventory, null, new SimpleContainer(IoBlockEntity.SIZE), new SimpleContainer(IoBlockEntity.SIZE), data);
    }

    private IoMenu(int id, Inventory inventory, IoBlockEntity block, Container input, Container output, CompoundTag data) {
        super(C0deMenus.IO.get(), id);
        this.block = block;
        this.data = data;
        this.pos = block != null ? block.getBlockPos() : BlockPos.of(data.getLong("pos"));
        for (int i = 0; i < IoBlockEntity.SIZE; i++) {
            addSlot(new Slot(input, i, 8 + i * SLOT, INPUT_Y) {
                @Override
                public boolean mayPlace(ItemStack stack) {
                    return IoBlockEntity.accepts(stack);
                }
            });
        }
        for (int i = 0; i < IoBlockEntity.SIZE; i++) {
            addSlot(new Slot(output, i, 8 + i * SLOT, OUTPUT_Y) {
                @Override
                public boolean mayPlace(ItemStack stack) {
                    return false;
                }
            });
        }
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(inventory, column + row * 9 + 9, 8 + column * SLOT, PLAYER_Y + row * SLOT));
            }
        }
        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(inventory, column, 8 + column * SLOT, HOTBAR_Y));
        }
    }

    public BlockPos pos() {
        return pos;
    }

    /** The binding when the dialog was opened: program and function, empty if the block is not bound. */
    public String program() {
        return data.getString("program");
    }

    public String function() {
        return data.getString("function");
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = slot.getItem();
        ItemStack original = stack.copy();
        if (index < CONTAINER_SLOTS) {
            if (!moveItemStackTo(stack, CONTAINER_SLOTS, slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else if (IoBlockEntity.accepts(stack)) {
            if (!moveItemStackTo(stack, 0, IoBlockEntity.SIZE, false)) {
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
        return block == null || Container.stillValidBlockEntity(block, player);
    }
}
