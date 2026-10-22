package de.joernott.c0de.blockentity;

import de.joernott.c0de.block.TriggerBlock;
import de.joernott.c0de.content.C0deBlockEntities;
import de.joernott.c0de.core.validation.Validation;
import de.joernott.c0de.dimension.C0deDimension;
import de.joernott.c0de.item.ValueItem;
import de.joernott.c0de.menu.TriggerMenu;
import de.joernott.c0de.platform.Platform;
import de.joernott.c0de.process.ProcessManager;
import de.joernott.c0de.sheet.ProgramRegistry;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
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
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HopperBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * The configuration, inventories and state of a trigger block: owner, program, function, timer interval, the
 * running process, the exit code and error of the last process, the input and output inventories and the
 * camouflage slot.
 *
 * <p>For hoppers, pipes and cables, the block is a container of the input (slots 0 to 26) and output (slots 27 to
 * 53) inventories on all sides: variable and constant items can only be put into the input, and items can only be
 * taken out of the output. A hopper above or beside the block fills the input, a hopper below it empties the
 * output, so trigger blocks can be chained.
 */
public class TriggerBlockEntity extends BlockEntity implements MenuProvider, WorldlyContainer {
    public static final int INVENTORY_SIZE = 27;
    public static final int MIN_INTERVAL = 1;
    public static final int MAX_INTERVAL = 72_000;

    private final SimpleContainer input = container(INVENTORY_SIZE);
    private final SimpleContainer output = container(INVENTORY_SIZE);
    private final SimpleContainer camouflage = new SimpleContainer(1) {
        @Override
        public int getMaxStackSize() {
            return 1;
        }

        @Override
        public void setChanged() {
            super.setChanged();
            camouflageChanged();
        }
    };

    /** The items taken from the input inventory when the running process started. */
    private final List<ItemStack> held = new ArrayList<>();

    private UUID owner;
    private String ownerName = "";
    private String program = "";
    private String function = "";
    private int interval = 20;
    private int countdown;
    private UUID process;
    private int pid = -1;
    private int exitCode = -1;
    private CompoundTag error = new CompoundTag();
    private String status = "";

    public TriggerBlockEntity(BlockPos pos, BlockState state) {
        super(C0deBlockEntities.TRIGGER.get(), pos, state);
    }

    private SimpleContainer container(int size) {
        return new SimpleContainer(size) {
            @Override
            public void setChanged() {
                super.setChanged();
                TriggerBlockEntity.this.setChanged();
            }
        };
    }

    // ------------------------------------------------------------------------- container for hoppers and pipes

    private static final int[] SLOTS = java.util.stream.IntStream.range(0, 2 * INVENTORY_SIZE).toArray();

    private SimpleContainer part(int slot) {
        return slot < INVENTORY_SIZE ? input : output;
    }

    @Override
    public int getContainerSize() {
        return 2 * INVENTORY_SIZE;
    }

    @Override
    public boolean isEmpty() {
        return input.isEmpty() && output.isEmpty();
    }

    @Override
    public ItemStack getItem(int slot) {
        return part(slot).getItem(slot % INVENTORY_SIZE);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        return part(slot).removeItem(slot % INVENTORY_SIZE, amount);
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return part(slot).removeItemNoUpdate(slot % INVENTORY_SIZE);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        part(slot).setItem(slot % INVENTORY_SIZE, stack);
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return slot < INVENTORY_SIZE && stack.getItem() instanceof ValueItem;
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
        return slot >= INVENTORY_SIZE;
    }

    public TriggerBlock.Kind kind() {
        return getBlockState().getBlock() instanceof TriggerBlock block ? block.kind() : TriggerBlock.Kind.TRIGGER;
    }

    public SimpleContainer input() {
        return input;
    }

    public SimpleContainer output() {
        return output;
    }

    public SimpleContainer camouflage() {
        return camouflage;
    }

    public List<ItemStack> held() {
        return held;
    }

    public UUID owner() {
        return owner;
    }

    public String program() {
        return program;
    }

    public String function() {
        return function;
    }

    public int interval() {
        return interval;
    }

    /** The PID of the running process, or -1. */
    public int pid() {
        return pid;
    }

    /** The ID of the running process, or null. */
    public UUID processId() {
        return process;
    }

    public boolean isRunning() {
        return process != null;
    }

    public int exitCode() {
        return exitCode;
    }

    public CompoundTag error() {
        return error;
    }

    public String status() {
        return status;
    }

    // ------------------------------------------------------------------------------------------ dialog

    /** Opens the dialog with the data it needs: the configuration, the state and the programs. */
    public void openMenu(ServerPlayer player) {
        CompoundTag data = new CompoundTag();
        data.putLong("pos", worldPosition.asLong());
        data.putString("kind", kind().name());
        data.putString("program", program);
        data.putString("function", function);
        data.putInt("interval", interval);
        data.putString("owner", ownerName);
        data.putInt("exitCode", exitCode);
        data.put("error", error.copy());
        data.putString("status", status);
        CompoundTag programs = new CompoundTag();
        ServerLevel c0de = player.server.getLevel(C0deDimension.LEVEL);
        if (c0de != null) {
            ProgramRegistry registry = ProgramRegistry.get(c0de);
            for (String name : registry.programs()) {
                ListTag functions = new ListTag();
                for (Validation.Function f : registry.functions(name)) {
                    functions.add(StringTag.valueOf(f.name()));
                }
                programs.put(name, functions);
            }
        }
        data.put("programs", programs);
        Platform.INSTANCE.openMenu(player, this, data);
    }

    @Override
    public Component getDisplayName() {
        return getBlockState().getBlock().getName();
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new TriggerMenu(id, inventory, this);
    }

    /**
     * Changes the configuration. The player who configures the block becomes its owner.
     *
     * @return the reason if the configuration can't be changed
     */
    public Optional<String> configure(ServerPlayer player, String newProgram, String newFunction, int newInterval) {
        if (isRunning()) {
            return Optional.of("A process is running");
        }
        if (newInterval < MIN_INTERVAL || newInterval > MAX_INTERVAL) {
            return Optional.of("The interval must be between " + MIN_INTERVAL + " and " + MAX_INTERVAL + " ticks");
        }
        program = newProgram;
        function = newFunction;
        interval = newInterval;
        countdown = interval;
        owner = player.getUUID();
        ownerName = player.getGameProfile().getName();
        status = "";
        changed();
        return Optional.empty();
    }

    // ------------------------------------------------------------------------------------------ processes

    /** Starts a process, if all preconditions are met; otherwise the reason is stored as status. */
    public void start() {
        start(false);
    }

    /**
     * Starts a process with a debug cursor, which starts paused.
     *
     * @return the reason if the process didn't start
     */
    public Optional<String> startDebug() {
        start(true);
        return status.isEmpty() ? Optional.empty() : Optional.of(status);
    }

    private void start(boolean debug) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        Optional<String> problem = ProcessManager.get(serverLevel.getServer()).start(serverLevel, this, debug);
        status = problem.orElse("");
        changed();
    }

    /** Counts down the interval of a timer block; a redstone signal pauses it. */
    public void tickTimer() {
        if (getBlockState().getValue(TriggerBlock.POWERED) || program.isEmpty()) {
            return;
        }
        if (--countdown <= 0) {
            countdown = interval;
            if (!isRunning()) {
                start();
            }
        }
    }

    /** Called by the process manager when a process of this block has started. */
    public void started(UUID processId, int newPid, List<ItemStack> taken) {
        process = processId;
        pid = newPid;
        exitCode = -1;
        error = new CompoundTag();
        held.clear();
        held.addAll(taken);
        changed();
        updateComparator();
    }

    /**
     * Called by the process manager when the process has ended: stores the exit code and error, puts the items
     * into the output inventory and moves them to the only adjacent inventory, if there is one with space.
     */
    public void finished(int code, CompoundTag processError, List<ItemStack> items) {
        process = null;
        pid = -1;
        exitCode = code;
        error = processError.copy();
        held.clear();
        for (ItemStack item : items) {
            ItemStack rest = output.addItem(item);
            if (!rest.isEmpty() && level != null) {
                Containers.dropItemStack(
                        level, worldPosition.getX(), worldPosition.getY() + 1, worldPosition.getZ(), rest);
            }
        }
        transferOutput();
        changed();
        updateComparator();
    }

    /**
     * Moves the output to the inventory of the only adjacent block with an inventory, if it fits completely. An
     * adjacent trigger block counts with its input inventory.
     */
    private void transferOutput() {
        if (level == null || output.isEmpty()) {
            return;
        }
        Container target = null;
        for (Direction side : Direction.values()) {
            BlockEntity neighbor = level.getBlockEntity(worldPosition.relative(side));
            if (neighbor instanceof HopperBlockEntity
                    && neighbor.getBlockState().getValue(HopperBlock.FACING) == side.getOpposite()) {
                // A hopper that fills this block is not a target, or the items would go round in circles
                continue;
            }
            // Trigger blocks can be chained: the output of one goes into the input of the next
            Container container = neighbor instanceof TriggerBlockEntity trigger
                    ? trigger.input()
                    : neighbor instanceof Container other ? other : null;
            if (container != null) {
                if (target != null) {
                    return;
                }
                target = container;
            }
        }
        if (target == null) {
            return;
        }
        SimpleContainer copy = new SimpleContainer(target.getContainerSize());
        for (int i = 0; i < target.getContainerSize(); i++) {
            copy.setItem(i, target.getItem(i).copy());
        }
        for (int i = 0; i < output.getContainerSize(); i++) {
            if (!copy.addItem(output.getItem(i).copy()).isEmpty()) {
                return;
            }
        }
        for (int i = 0; i < target.getContainerSize(); i++) {
            target.setItem(i, copy.getItem(i));
        }
        target.setChanged();
        output.clearContent();
    }

    /** The comparator signal: the exit code of the last process, 0 while a process runs. */
    public int comparatorSignal() {
        return isRunning() || exitCode < 0 ? 0 : Math.min(exitCode, 15);
    }

    private void updateComparator() {
        if (level != null) {
            level.updateNeighbourForOutputSignal(worldPosition, getBlockState().getBlock());
        }
    }

    /** The block was removed: ends the process and drops the items. */
    public void removed() {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        if (process != null) {
            ProcessManager.get(serverLevel.getServer()).stop(serverLevel.getServer(), process);
        }
        Containers.dropContents(level, worldPosition, input);
        Containers.dropContents(level, worldPosition, output);
        Containers.dropContents(level, worldPosition, camouflage);
        held.forEach(item -> Containers.dropItemStack(
                level, worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), item));
        held.clear();
    }

    // ------------------------------------------------------------------------------------------ camouflage

    /** The block state shown instead of the trigger block, or null. */
    public BlockState camouflageState() {
        return camouflage.getItem(0).getItem() instanceof BlockItem item ? item.getBlock().defaultBlockState() : null;
    }

    private void camouflageChanged() {
        if (level == null || level.isClientSide()) {
            return;
        }
        boolean camouflaged = camouflageState() != null;
        BlockState state = getBlockState();
        if (state.getBlock() instanceof TriggerBlock && state.getValue(TriggerBlock.CAMOUFLAGED) != camouflaged) {
            level.setBlock(worldPosition, state.setValue(TriggerBlock.CAMOUFLAGED, camouflaged), Block.UPDATE_ALL);
        }
        changed();
    }

    // ------------------------------------------------------------------------------------------ storage

    private void changed() {
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (owner != null) {
            tag.putUUID("owner", owner);
        }
        tag.putString("ownerName", ownerName);
        tag.putString("program", program);
        tag.putString("function", function);
        tag.putInt("interval", interval);
        tag.putInt("countdown", countdown);
        if (process != null) {
            tag.putUUID("process", process);
        }
        tag.putInt("pid", pid);
        tag.putInt("exitCode", exitCode);
        tag.put("error", error);
        tag.putString("status", status);
        tag.put("input", input.createTag(registries));
        tag.put("output", output.createTag(registries));
        tag.put("camouflage", camouflage.createTag(registries));
        ListTag heldItems = new ListTag();
        held.forEach(item -> heldItems.add(item.save(registries)));
        tag.put("held", heldItems);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        owner = tag.hasUUID("owner") ? tag.getUUID("owner") : null;
        ownerName = tag.getString("ownerName");
        program = tag.getString("program");
        function = tag.getString("function");
        interval = tag.contains("interval") ? tag.getInt("interval") : 20;
        countdown = tag.getInt("countdown");
        process = tag.hasUUID("process") ? tag.getUUID("process") : null;
        pid = tag.contains("pid") ? tag.getInt("pid") : -1;
        exitCode = tag.contains("exitCode") ? tag.getInt("exitCode") : -1;
        error = tag.getCompound("error");
        status = tag.getString("status");
        input.fromTag(tag.getList("input", Tag.TAG_COMPOUND), registries);
        output.fromTag(tag.getList("output", Tag.TAG_COMPOUND), registries);
        camouflage.fromTag(tag.getList("camouflage", Tag.TAG_COMPOUND), registries);
        held.clear();
        for (Tag item : tag.getList("held", Tag.TAG_COMPOUND)) {
            ItemStack.parse(registries, item).ifPresent(held::add);
        }
    }

    /** Clients only get the camouflage, which they render. */
    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        tag.put("camouflage", camouflage.createTag(registries));
        return tag;
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
