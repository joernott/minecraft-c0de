package de.joernott.c0de.io;

import de.joernott.c0de.content.C0deBlockEntities;
import de.joernott.c0de.core.io.TextScreen;
import de.joernott.c0de.dialog.DialogService;
import de.joernott.c0de.platform.Platform;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * A display block (docs/concept/19-input-output.md): its binding, its camouflage, and for the block at the top
 * left of a display wall (the origin) the screen of the whole wall and the size of the wall. The walls are
 * maintained by {@link DisplayWalls}.
 */
public class DisplayBlockEntity extends BlockEntity implements Bindable, MenuProvider {
    /** The characters of a display block: 16 columns and 8 rows. */
    public static final int COLUMNS = 16;
    public static final int ROWS = 8;

    private String program = "";
    private String function = "";
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
    private boolean origin;
    private int width = 1;
    private int height = 1;
    /** The position of the origin in the bounding box of the wall: blocks from its left and from its top. */
    private int offsetRight;
    private int offsetDown;
    private TextScreen screen;

    public DisplayBlockEntity(BlockPos pos, BlockState state) {
        super(C0deBlockEntities.DISPLAY.get(), pos, state);
    }

    @Override
    public String program() {
        return program;
    }

    @Override
    public String function() {
        return function;
    }

    /** Binds the whole wall of this block. */
    @Override
    public void bind(ServerLevel level, String newProgram, String newFunction) {
        DisplayWalls.bind(level, worldPosition, newProgram, newFunction);
    }

    void setBinding(String newProgram, String newFunction) {
        program = newProgram;
        function = newFunction;
        changed();
    }

    public boolean isOrigin() {
        return origin;
    }

    public int wallWidth() {
        return width;
    }

    public int wallHeight() {
        return height;
    }

    public int offsetRight() {
        return offsetRight;
    }

    public int offsetDown() {
        return offsetDown;
    }

    /** The screen of the wall, if this block is its origin. */
    public TextScreen screen() {
        return screen;
    }

    /** Makes this block the origin of a wall, with the screen of the wall. */
    void makeOrigin(int newWidth, int newHeight, int newOffsetRight, int newOffsetDown, TextScreen newScreen) {
        origin = true;
        width = newWidth;
        height = newHeight;
        offsetRight = newOffsetRight;
        offsetDown = newOffsetDown;
        screen = newScreen;
        changed();
    }

    /** This block is a part of a wall whose screen another block holds. */
    void makePart() {
        if (origin || screen != null) {
            origin = false;
            screen = null;
            width = 1;
            height = 1;
            changed();
        }
    }

    /** The screen of the wall changed: it is saved and sent to the players. */
    public void screenChanged() {
        changed();
    }

    /** The slot of the camouflage block. */
    public SimpleContainer camouflage() {
        return camouflage;
    }

    /** The block shown on the sides other than the front, or null. */
    public BlockState camouflageState() {
        return camouflage.getItem(0).getItem() instanceof BlockItem item ? item.getBlock().defaultBlockState() : null;
    }

    /** Blocks that can camouflage a display: full blocks without block entity. */
    public static boolean canCamouflage(ItemStack stack) {
        return stack.getItem() instanceof BlockItem item
                && !(item.getBlock() instanceof EntityBlock)
                && item.getBlock().defaultBlockState().isCollisionShapeFullBlock(EmptyBlockGetter.INSTANCE, BlockPos.ZERO);
    }

    private void camouflageChanged() {
        if (level == null || level.isClientSide()) {
            return;
        }
        boolean camouflaged = camouflageState() != null;
        BlockState state = getBlockState();
        if (state.getBlock() instanceof DisplayBlock && state.getValue(DisplayBlock.CAMOUFLAGED) != camouflaged) {
            level.setBlock(worldPosition, state.setValue(DisplayBlock.CAMOUFLAGED, camouflaged), Block.UPDATE_ALL);
        }
        changed();
    }

    /** Opens the dialog: the binding of the wall and the camouflage slot. */
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
        return new DisplayMenu(id, inventory, this);
    }

    private void changed() {
        setChanged();
        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putString("program", program);
        tag.putString("function", function);
        tag.put("camouflage", camouflage.createTag(registries));
        tag.putBoolean("origin", origin);
        tag.putInt("width", width);
        tag.putInt("height", height);
        tag.putInt("offsetRight", offsetRight);
        tag.putInt("offsetDown", offsetDown);
        if (screen != null) {
            tag.put("screen", ScreenNbt.write(screen));
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        program = tag.getString("program");
        function = tag.getString("function");
        camouflage.fromTag(tag.getList("camouflage", Tag.TAG_COMPOUND), registries);
        origin = tag.getBoolean("origin");
        width = Math.max(1, tag.getInt("width"));
        height = Math.max(1, tag.getInt("height"));
        offsetRight = tag.getInt("offsetRight");
        offsetDown = tag.getInt("offsetDown");
        screen = origin && tag.contains("screen")
                ? ScreenNbt.read(tag.getCompound("screen"), width * COLUMNS, height * ROWS, 0)
                : null;
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
