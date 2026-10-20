package de.joernott.c0de.item;

import de.joernott.c0de.core.edit.ItemRules;
import de.joernott.c0de.core.program.TypeText;
import de.joernott.c0de.dialog.DialogService;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;

/**
 * A variable or constant item (docs/concept/14-items-and-tools.md). It carries a name, a type and a value,
 * which are passed to a process as parameters and come back as results. Right-clicking it in the main hand
 * opens a dialog to edit the name and value; a constant can only be edited as long as it has no value.
 */
public class ValueItem extends Item {
    private final boolean constant;

    public ValueItem(Properties properties, boolean constant) {
        super(properties);
        this.constant = constant;
    }

    public boolean isConstant() {
        return constant;
    }

    /**
     * The properties of an item.
     *
     * @param name the name, empty for an empty item
     * @param type the type in its text form, e.g. {@code ARRAY INTEGER}
     * @param value the value as JSON, empty if the item has no value
     */
    public record Contents(String name, String type, String value) {
        /** True for an item without name and value. */
        public boolean isEmpty() {
            return name.isEmpty() && value.isEmpty();
        }

        public static Contents of(ItemStack stack) {
            CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY)
                    .copyTag();
            return new Contents(tag.getString("name"), tag.getString("type"), tag.getString("value"));
        }

        public void writeTo(ItemStack stack) {
            CompoundTag tag = new CompoundTag();
            tag.putString("name", name);
            tag.putString("type", type);
            tag.putString("value", value);
            stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        }
    }

    /** True if the properties of the item can't be changed any more. */
    public boolean isReadOnly(ItemStack stack) {
        return constant && !Contents.of(stack).value().isEmpty();
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (hand != InteractionHand.MAIN_HAND) {
            return InteractionResultHolder.pass(stack);
        }
        if (player instanceof ServerPlayer serverPlayer) {
            DialogService.openItem(serverPlayer);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    /**
     * Checks the properties entered in the dialog and stores them in the item.
     *
     * @return the problem if they are not valid
     */
    public Optional<String> apply(ItemStack stack, String name, String typeText, String valueText) {
        if (isReadOnly(stack)) {
            return Optional.of("The value of a constant can't be changed");
        }
        Contents old = Contents.of(stack);
        // The kind of type comes from crafting; only the subtype or struct name can be added
        String base = old.type().split(" ")[0];
        if (!base.isEmpty() && !typeText.trim().split("\\s+")[0].equalsIgnoreCase(base)) {
            return Optional.of("The type of this item is " + base);
        }
        ItemRules.Result result = ItemRules.check(name, typeText, valueText, ZoneId.systemDefault());
        if (result.problem().isPresent()) {
            return result.problem();
        }
        String type = TypeText.format(TypeText.parse(typeText));
        new Contents(name, type, result.json()).writeTo(stack);
        return Optional.empty();
    }

    @Override
    public Component getName(ItemStack stack) {
        Contents contents = Contents.of(stack);
        return contents.name().isEmpty()
                ? super.getName(stack)
                : Component.translatable(getDescriptionId() + ".named", contents.name());
    }

    @Override
    public void appendHoverText(
            ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        Contents contents = Contents.of(stack);
        if (!contents.type().isEmpty()) {
            tooltip.add(Component.translatable("item.c0de.value_item.type", contents.type())
                    .withStyle(ChatFormatting.GRAY));
        }
        if (!contents.value().isEmpty()) {
            String value = ItemRules.display(contents.type(), contents.value());
            if (value.length() > 64) {
                value = value.substring(0, 61) + "...";
            }
            tooltip.add(Component.translatable("item.c0de.value_item.value", value).withStyle(ChatFormatting.GRAY));
        }
    }
}
