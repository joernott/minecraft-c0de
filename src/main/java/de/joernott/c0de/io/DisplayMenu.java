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
 * The dialog of a display block, laid out like that of the trigger block: the player inventory on the left, the
 * binding of the wall and the camouflage slot of the block on the right.
 */
public class DisplayMenu extends AbstractContainerMenu {
    public static final int SLOT = 18;
    public static final int PLAYER_Y = 40;
    public static final int HOTBAR_Y = PLAYER_Y + 3 * SLOT + 4;
    public static final int CAMOUFLAGE_X = 186;
    public static final int CAMOUFLAGE_Y = HOTBAR_Y;

    private final DisplayBlockEntity block;
    private final CompoundTag data;
    private final BlockPos pos;

    /** The menu on the server. */
    public DisplayMenu(int id, Inventory inventory, DisplayBlockEntity block) {
        this(id, inventory, block, block.camouflage(), new CompoundTag());
    }

    /** The menu on the client, with the data sent by the server. */
    public DisplayMenu(int id, Inventory inventory, CompoundTag data) {
        this(id, inventory, null, new SimpleContainer(1), data);
    }

    private DisplayMenu(int id, Inventory inventory, DisplayBlockEntity block, Container camouflage, CompoundTag data) {
        super(C0deMenus.DISPLAY.get(), id);
        this.block = block;
        this.data = data;
        this.pos = block != null ? block.getBlockPos() : BlockPos.of(data.getLong("pos"));
        addSlot(new Slot(camouflage, 0, CAMOUFLAGE_X, CAMOUFLAGE_Y) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return DisplayBlockEntity.canCamouflage(stack);
            }

            @Override
            public int getMaxStackSize() {
                return 1;
            }
        });
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

    /** The binding when the dialog was opened and the programs to choose from. */
    public CompoundTag data() {
        return data;
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
        } else if (DisplayBlockEntity.canCamouflage(stack) && !slots.get(0).hasItem()) {
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
        return block == null || Container.stillValidBlockEntity(block, player);
    }
}
