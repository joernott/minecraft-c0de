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
 * A literal item (docs/concept/19-input-output.md): a type and a value, but no name. INPUT takes the first literal
 * item of the type it needs from an I/O block. An empty literal item has a type but no value. Right-clicking it in
 * the main hand opens the dialog of variable items without the name.
 */
public class LiteralItem extends Item {
    public LiteralItem(Properties properties) {
        super(properties);
    }

    /**
     * The properties of a literal item.
     *
     * @param type the type in its text form, e.g. {@code INTEGER}
     * @param value the value as JSON, empty for an empty literal item
     */
    public record Contents(String type, String value) {
        public boolean isEmpty() {
            return value.isEmpty();
        }

        public static Contents of(ItemStack stack) {
            CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
            return new Contents(tag.getString("type"), tag.getString("value"));
        }

        public void writeTo(ItemStack stack) {
            if (type.isEmpty() && value.isEmpty()) {
                stack.remove(DataComponents.CUSTOM_DATA);
                return;
            }
            CompoundTag tag = new CompoundTag();
            tag.putString("type", type);
            tag.putString("value", value);
            stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        }
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
     * Checks the type and value entered in the dialog and stores them in the item.
     *
     * @return the problem if they are not valid
     */
    public Optional<String> apply(ItemStack stack, String typeText, String valueText) {
        if (typeText.isBlank()) {
            if (!valueText.isEmpty()) {
                return Optional.of("A literal needs a type");
            }
            new Contents("", "").writeTo(stack);
            return Optional.empty();
        }
        // A literal has no name; the rules of variable items are checked with a placeholder name
        ItemRules.Result result = ItemRules.check("literal", typeText, valueText, ZoneId.systemDefault());
        if (result.problem().isPresent()) {
            return result.problem();
        }
        new Contents(TypeText.format(TypeText.parse(typeText)), result.json()).writeTo(stack);
        return Optional.empty();
    }

    @Override
    public Component getName(ItemStack stack) {
        Contents contents = Contents.of(stack);
        return contents.isEmpty()
                ? super.getName(stack)
                : Component.translatable(
                        getDescriptionId() + ".valued", ItemRules.display(contents.type(), contents.value()));
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        Contents contents = Contents.of(stack);
        if (!contents.type().isEmpty()) {
            tooltip.add(Component.translatable("item.c0de.value_item.type", contents.type())
                    .withStyle(ChatFormatting.GRAY));
        }
    }
}
