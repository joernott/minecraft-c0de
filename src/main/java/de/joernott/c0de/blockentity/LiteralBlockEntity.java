package de.joernott.c0de.blockentity;

import de.joernott.c0de.compat.Nbt;
import de.joernott.c0de.content.C0deBlockEntities;
import de.joernott.c0de.core.edit.PropertyRules;
import de.joernott.c0de.core.line.Block;
import java.time.ZoneId;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;

/**
 * The properties of a LITERAL: type and value (docs/concept/04-data.md). The value is stored as it was
 * entered; TIME values without an offset are in the time zone of the server.
 */
public class LiteralBlockEntity extends PropertiesBlockEntity {
    private String type = "";
    private String value = "";

    public LiteralBlockEntity(BlockPos pos, BlockState state) {
        super(C0deBlockEntities.LITERAL.get(), pos, state);
    }

    /** The type name, e.g. {@code INTEGER}, or the empty string if the literal has no value yet. */
    public String type() {
        return type;
    }

    public String value() {
        return value;
    }

    @Override
    public String dialog() {
        return "literal";
    }

    @Override
    public Block toCore(ZoneId zone) {
        return new Block.LiteralBlock(
                PropertyRules.literalValue(type, value, zone).orElse(null));
    }

    @Override
    public String displayText() {
        return type.equals("STRING") ? "\"" + value + "\"" : value;
    }

    @Override
    protected Optional<String> check(CompoundTag properties, ServerLevel level) {
        return PropertyRules.literal(Nbt.string(properties, "type"), Nbt.string(properties, "value"), ZoneId.systemDefault());
    }

    @Override
    protected void read(CompoundTag tag) {
        type = Nbt.string(tag, "type");
        value = Nbt.string(tag, "value");
    }

    @Override
    protected void write(CompoundTag tag) {
        tag.putString("type", type);
        tag.putString("value", value);
    }
}
