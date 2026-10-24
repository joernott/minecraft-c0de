package de.joernott.c0de.io;

import de.joernott.c0de.content.C0deBlockEntities;
import de.joernott.c0de.dialog.DialogService;
import de.joernott.c0de.item.LiteralItem;
import de.joernott.c0de.item.ValueItem;
import de.joernott.c0de.platform.Platform;
import java.util.stream.IntStream;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * The I/O block (docs/concept/19-input-output.md): its binding and its input and output inventories of 18 slots.
 * INPUT reads variable, constant and literal items from the input inventory, WRITE takes uninitialized variable
 * items from it, and both put their items into the output inventory. Hoppers and pipes reach the inventories
 * from all sides: they put items into the input and take them out of the output.
 */
public class IoBlockEntity extends BlockEntity implements Bindable, MenuProvider, WorldlyContainer {
    public static final int SIZE = 18;
    private static final int[] SLOTS = IntStream.range(0, 2 * SIZE).toArray();

    private final SimpleContainer input = container();
    private final SimpleContainer output = container();
    private String program = "";
    private String function = "";

    public IoBlockEntity(BlockPos pos, BlockState state) {
        super(C0deBlockEntities.IO.get(), pos, state);
    }

    private SimpleContainer container() {
        return new SimpleContainer(SIZE) {
            @Override
            public void setChanged() {
                super.setChanged();
                IoBlockEntity.this.setChanged();
            }
        };
    }

    /** Variable, constant and literal items can be put into the input inventory. */
    public static boolean accepts(ItemStack stack) {
        return stack.getItem() instanceof ValueItem || stack.getItem() instanceof LiteralItem;
    }

    public SimpleContainer input() {
        return input;
    }

    public SimpleContainer output() {
        return output;
    }

    @Override
    public String program() {
        return program;
    }

    @Override
    public String function() {
        return function;
    }

    @Override
    public void bind(ServerLevel level, String newProgram, String newFunction) {
        program = newProgram;
        function = newFunction;
        setChanged();
        IoDevices devices = IoDevices.get(level.getServer());
        if (program.isEmpty()) {
            devices.remove(level.dimension(), worldPosition);
        } else {
            devices.bind(IoDevices.Kind.IO, level.dimension(), worldPosition, program, function);
        }
    }

    /** Puts an item into the output inventory; what doesn't fit is dropped on the block. */
    void output(ItemStack stack) {
        ItemStack rest = output.addItem(stack);
        if (!rest.isEmpty() && level != null) {
            Containers.dropItemStack(level, worldPosition.getX(), worldPosition.getY() + 1, worldPosition.getZ(), rest);
        }
    }

    /** Opens the inventories with the binding. */
    public void openMenu(ServerPlayer player) {
        CompoundTag data = new CompoundTag();
        data.putLong("pos", worldPosition.asLong());
        data.putString("program", program);
        data.putString("function", function);
        data.put("programs", DialogService.programs(player));
        Platform.INSTANCE.openMenu(player, this, data);
    }

    @Override
    public Component getDisplayName() {
        return getBlockState().getBlock().getName();
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new IoMenu(id, inventory, this);
    }

    /** The block was removed: the items are dropped and the device is unbound. */
    void removed() {
        if (level instanceof ServerLevel serverLevel) {
            Containers.dropContents(level, worldPosition, input);
            Containers.dropContents(level, worldPosition, output);
            IoDevices.get(serverLevel.getServer()).remove(serverLevel.dimension(), worldPosition);
        }
    }

    // ------------------------------------------------------------------ container for hoppers and pipes

    private SimpleContainer part(int slot) {
        return slot < SIZE ? input : output;
    }

    @Override
    public int getContainerSize() {
        return 2 * SIZE;
    }

    @Override
    public boolean isEmpty() {
        return input.isEmpty() && output.isEmpty();
    }

    @Override
    public ItemStack getItem(int slot) {
        return part(slot).getItem(slot % SIZE);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        return part(slot).removeItem(slot % SIZE, amount);
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return part(slot).removeItemNoUpdate(slot % SIZE);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        part(slot).setItem(slot % SIZE, stack);
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return slot < SIZE && accepts(stack);
    }

    @Override
    public boolean stillValid(Player player) {
        return Container.stillValidBlockEntity(this, player);
    }

    @Override
    public void clearContent() {
        input.clearContent();
        output.clearContent();
    }

    @Override
    public int[] getSlotsForFace(Direction side) {
        return SLOTS;
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, Direction side) {
        return canPlaceItem(slot, stack);
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction side) {
        return slot >= SIZE;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putString("program", program);
        tag.putString("function", function);
        tag.put("input", input.createTag(registries));
        tag.put("output", output.createTag(registries));
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        program = tag.getString("program");
        function = tag.getString("function");
        input.fromTag(tag.getList("input", Tag.TAG_COMPOUND), registries);
        output.fromTag(tag.getList("output", Tag.TAG_COMPOUND), registries);
    }
}
