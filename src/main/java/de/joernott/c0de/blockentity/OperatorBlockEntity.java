package de.joernott.c0de.blockentity;

import de.joernott.c0de.block.OperatorBlock;
import de.joernott.c0de.content.C0deBlockEntities;
import de.joernott.c0de.core.line.Block;
import de.joernott.c0de.core.line.Operator;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;

/**
 * The property of an OPERATOR: its symbol (docs/concept/05-expressions.md). The minus sign is stored as
 * {@link Operator#SUBTRACT}; whether it negates is decided by its position in the expression.
 */
public class OperatorBlockEntity extends PropertiesBlockEntity {
    /** The operators that can be chosen in the dialog, in the order of the dialog. */
    public static final List<Operator> CHOICES =
            Stream.of(Operator.values()).filter(operator -> operator != Operator.NEGATE).toList();

    private Operator operator;

    public OperatorBlockEntity(BlockPos pos, BlockState state) {
        super(C0deBlockEntities.OPERATOR.get(), pos, state);
    }

    /** The operator, or null if no symbol has been chosen yet. */
    public Operator operator() {
        return operator;
    }

    @Override
    public String dialog() {
        return "operator";
    }

    @Override
    public Block toCore(ZoneId zone) {
        return new Block.OperatorBlock(operator);
    }

    @Override
    public String displayText() {
        return operator == null ? "" : operator.symbol();
    }

    @Override
    protected Optional<String> check(CompoundTag properties, ServerLevel level) {
        return parse(properties.getString("operator")) == null
                ? Optional.of("Unknown operator: " + properties.getString("operator"))
                : Optional.empty();
    }

    private static Operator parse(String name) {
        return CHOICES.stream()
                .filter(choice -> choice.name().equals(name))
                .findFirst()
                .orElse(null);
    }

    @Override
    protected void read(CompoundTag tag) {
        operator = parse(tag.getString("operator"));
    }

    @Override
    protected void changed() {
        syncState();
        super.changed();
    }

    /**
     * Sets the block state operator to the operator of this block entity, so the block shows its symbol. Blocks
     * of older versions of the mod are updated by the validation of their sheet.
     */
    public void syncState() {
        if (level == null || level.isClientSide() || !getBlockState().hasProperty(OperatorBlock.OPERATOR)) {
            return;
        }
        OperatorBlock.Symbol symbol = OperatorBlock.Symbol.of(operator);
        if (getBlockState().getValue(OperatorBlock.OPERATOR) != symbol) {
            level.setBlock(
                    worldPosition,
                    getBlockState().setValue(OperatorBlock.OPERATOR, symbol),
                    net.minecraft.world.level.block.Block.UPDATE_CLIENTS);
        }
    }

    @Override
    protected void write(CompoundTag tag) {
        tag.putString("operator", operator == null ? "" : operator.name());
    }
}
