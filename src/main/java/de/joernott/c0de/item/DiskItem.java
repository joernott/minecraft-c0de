package de.joernott.c0de.item;

import de.joernott.c0de.block.CodeBlock;
import de.joernott.c0de.content.CodeBlockType;
import de.joernott.c0de.disk.DiskService;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;

/**
 * The c0de disk (docs/concept/14-items-and-tools.md): it carries one program in the YAML format. Right-clicking the
 * PROGRAM block of a sheet copies its program onto the disk; right-clicking the floor at the PROGRAM position of an
 * empty sheet builds the program of the disk there. The disk drive copies programs between disks and the folder
 * {@code c0de} of the player's Minecraft (task 11.11.9).
 */
public class DiskItem extends Item {
    /** The longest program a disk can carry, in characters of YAML. */
    public static final int MAX_LENGTH = 1_000_000;

    public DiskItem(Properties properties) {
        super(properties);
    }

    /**
     * The program on a disk.
     *
     * @param name the name of the program, empty for an empty disk
     * @param yaml the program in the YAML format
     */
    public record Contents(String name, String yaml) {
        public static final Contents EMPTY = new Contents("", "");

        public boolean isEmpty() {
            return yaml.isEmpty();
        }

        public static Contents of(ItemStack stack) {
            CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
            return new Contents(tag.getString("program"), tag.getString("yaml"));
        }

        public void writeTo(ItemStack stack) {
            if (isEmpty()) {
                stack.remove(DataComponents.CUSTOM_DATA);
                return;
            }
            CompoundTag tag = new CompoundTag();
            tag.putString("program", name);
            tag.putString("yaml", yaml);
            stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        }
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        boolean program = context.getLevel().getBlockState(context.getClickedPos()).getBlock() instanceof CodeBlock code
                && code.type() == CodeBlockType.PROGRAM;
        if (!(context.getPlayer() instanceof ServerPlayer player)) {
            return program ? InteractionResult.SUCCESS : InteractionResult.PASS;
        }
        if (program) {
            DiskService.copyToDisk(player, player.serverLevel(), context.getClickedPos(), context.getItemInHand());
            return InteractionResult.SUCCESS;
        }
        if (DiskService.isProgramPosition(player.serverLevel(), context.getClickedPos())) {
            DiskService.build(player, player.serverLevel(), context.getClickedPos(), Contents.of(context.getItemInHand()));
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        Contents contents = Contents.of(stack);
        tooltip.add((contents.isEmpty()
                        ? Component.translatable("item.c0de.c0de_disk.empty")
                        : Component.translatable("item.c0de.c0de_disk.program", contents.name()))
                .withStyle(ChatFormatting.GRAY));
    }
}
