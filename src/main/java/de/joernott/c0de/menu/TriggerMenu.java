package de.joernott.c0de.menu;

import de.joernott.c0de.block.TriggerBlock;
import de.joernott.c0de.blockentity.TriggerBlockEntity;
import de.joernott.c0de.content.C0deMenus;
import de.joernott.c0de.item.ValueItem;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;

/**
 * The dialog of a trigger block: the input inventory (only variable and constant items), the output inventory
 * (only taking), the camouflage slot (one block) and the player inventory. The configuration is sent with
 * {@link de.joernott.c0de.network.TriggerConfigPayload}; the PID and exit code are synchronized while the
 * dialog is open.
 */
public class TriggerMenu extends AbstractContainerMenu {
    public static final int SLOT = 18;
    public static final int INPUT_Y = 18;
    public static final int OUTPUT_Y = INPUT_Y + 3 * SLOT + 14;
    public static final int PLAYER_Y = OUTPUT_Y + 3 * SLOT + 14;
    public static final int HOTBAR_Y = PLAYER_Y + 3 * SLOT + 4;
    public static final int CAMOUFLAGE_X = 186;
    public static final int CAMOUFLAGE_Y = HOTBAR_Y;
    private static final int CONTAINER_SLOTS = 2 * TriggerBlockEntity.INVENTORY_SIZE + 1;

    private final TriggerBlockEntity trigger;
    private final Container input;
    private final Container output;
    private final Container camouflage;
    private final ContainerData state;
    private final CompoundTag data;
    private final BlockPos pos;

    /** The menu on the server. */
    public TriggerMenu(int id, Inventory inventory, TriggerBlockEntity trigger) {
        this(id, inventory, trigger, trigger.input(), trigger.output(), trigger.camouflage(), new ContainerData() {
            @Override
            public int get(int index) {
                return index == 0 ? trigger.pid() : trigger.exitCode();
            }

            @Override
            public void set(int index, int value) {
                // Only read
            }

            @Override
            public int getCount() {
                return 2;
            }
        }, new CompoundTag());
    }

    /** The menu on the client, with the data sent by the server. */
    public TriggerMenu(int id, Inventory inventory, CompoundTag data) {
        this(
                id,
                inventory,
                null,
                new SimpleContainer(TriggerBlockEntity.INVENTORY_SIZE),
                new SimpleContainer(TriggerBlockEntity.INVENTORY_SIZE),
                new SimpleContainer(1),
                new SimpleContainerData(2),
                data);
    }

    private TriggerMenu(
            int id,
            Inventory inventory,
            TriggerBlockEntity trigger,
            Container input,
            Container output,
            Container camouflage,
            ContainerData state,
            CompoundTag data) {
        super(C0deMenus.TRIGGER.get(), id);
        this.trigger = trigger;
        this.input = input;
        this.output = output;
        this.camouflage = camouflage;
        this.state = state;
        this.data = data;
        this.pos = trigger != null ? trigger.getBlockPos() : BlockPos.of(data.getLong("pos"));
        for (int i = 0; i < TriggerBlockEntity.INVENTORY_SIZE; i++) {
            addSlot(new Slot(input, i, 8 + (i % 9) * SLOT, INPUT_Y + (i / 9) * SLOT) {
                @Override
                public boolean mayPlace(ItemStack stack) {
                    return stack.getItem() instanceof ValueItem;
                }
            });
        }
        for (int i = 0; i < TriggerBlockEntity.INVENTORY_SIZE; i++) {
            addSlot(new Slot(output, i, 8 + (i % 9) * SLOT, OUTPUT_Y + (i / 9) * SLOT) {
                @Override
                public boolean mayPlace(ItemStack stack) {
                    return false;
                }
            });
        }
        addSlot(new Slot(camouflage, 0, CAMOUFLAGE_X, CAMOUFLAGE_Y) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.getItem() instanceof BlockItem item && !(item.getBlock() instanceof TriggerBlock);
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
        addDataSlots(state);
    }

    /** The data sent by the server when the dialog was opened (only on the client). */
    public CompoundTag data() {
        return data;
    }

    public BlockPos pos() {
        return pos;
    }

    /** The PID of the running process, or -1. */
    public int pid() {
        return state.get(0);
    }

    /** The exit code of the last process, or -1. */
    public int exitCode() {
        return state.get(1);
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
        } else if (stack.getItem() instanceof ValueItem) {
            if (!moveItemStackTo(stack, 0, TriggerBlockEntity.INVENTORY_SIZE, false)) {
                return ItemStack.EMPTY;
            }
        } else if (slots.get(CONTAINER_SLOTS - 1).mayPlace(stack) && !slots.get(CONTAINER_SLOTS - 1).hasItem()) {
            if (!moveItemStackTo(stack, CONTAINER_SLOTS - 1, CONTAINER_SLOTS, false)) {
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
        return trigger == null || Container.stillValidBlockEntity(trigger, player);
    }
}
