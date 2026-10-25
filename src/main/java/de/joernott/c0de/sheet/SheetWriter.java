package de.joernott.c0de.sheet;

import de.joernott.c0de.blockentity.PropertiesBlockEntity;
import de.joernott.c0de.content.C0deBlocks;
import de.joernott.c0de.content.CodeBlockType;
import de.joernott.c0de.core.line.Block;
import de.joernott.c0de.core.line.IndexText;
import de.joernott.c0de.core.line.Line;
import de.joernott.c0de.core.line.Sheet;
import de.joernott.c0de.core.value.BooleanValue;
import de.joernott.c0de.core.value.CharValue;
import de.joernott.c0de.core.value.FloatValue;
import de.joernott.c0de.core.value.IntegerValue;
import de.joernott.c0de.core.value.StringValue;
import de.joernott.c0de.core.value.TimeValue;
import de.joernott.c0de.core.value.Value;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;

/**
 * Places a program of the line model on a sheet: the c0de blocks with their properties. Used by the c0de disk
 * and by the GameTests.
 */
public final class SheetWriter {
    private SheetWriter() {}

    /** The language block of a block of the line model. */
    public static CodeBlockType type(Block block) {
        return switch (block) {
            case Block.KeywordBlock keyword -> {
                for (CodeBlockType type : CodeBlockType.values()) {
                    if (type.keyword() == keyword.keyword()) {
                        yield type;
                    }
                }
                throw new IllegalArgumentException("No block for " + keyword);
            }
            case Block.LabelBlock label -> CodeBlockType.LABEL;
            case Block.LiteralBlock literal -> CodeBlockType.LITERAL;
            case Block.OperatorBlock operator -> CodeBlockType.OPERATOR;
            case Block.CommentBlock comment -> CodeBlockType.COMMENT;
            case Block.TagBlock tag -> CodeBlockType.TAG;
        };
    }

    /** The properties of a block as they are entered in its dialog, or empty for keyword blocks. */
    public static Optional<CompoundTag> properties(Block block) {
        CompoundTag tag = new CompoundTag();
        switch (block) {
            case Block.KeywordBlock keyword -> {
                return Optional.empty();
            }
            case Block.LabelBlock label -> {
                tag.putString("name", label.name() == null ? "" : label.name());
                tag.putString("index", IndexText.format(label.index()));
            }
            case Block.LiteralBlock literal -> {
                Value value = literal.value();
                tag.putString("type", value == null ? "" : literalType(value));
                tag.putString("value", value == null ? "" : value.text());
            }
            case Block.OperatorBlock operator ->
                tag.putString("operator", operator.operator() == null ? "" : operator.operator().name());
            case Block.CommentBlock comment -> tag.putString("text", comment.text());
            case Block.TagBlock tagBlock -> {
                tag.putString("language", tagBlock.language() == null ? "" : tagBlock.language());
                tag.putString("name", tagBlock.name() == null ? "" : tagBlock.name());
            }
        }
        return Optional.of(tag);
    }

    private static String literalType(Value value) {
        return switch (value) {
            case BooleanValue b -> "BOOLEAN";
            case IntegerValue i -> "INTEGER";
            case FloatValue f -> "FLOAT";
            case CharValue c -> "CHAR";
            case StringValue string -> "STRING";
            case TimeValue time -> "TIME";
            default -> throw new IllegalArgumentException("A literal can't be " + value);
        };
    }

    /** The items of the blocks of a program, which an import takes from the inventory in survival mode. */
    public static Map<Item, Integer> items(Sheet lines) {
        Map<Item, Integer> items = new LinkedHashMap<>();
        for (Line line : lines.lines()) {
            for (Line.Cell cell : line.cells()) {
                items.merge(C0deBlocks.get(type(cell.block())).asItem(), 1, Integer::sum);
            }
        }
        return items;
    }

    /**
     * Places the blocks of a program on a sheet and sets their properties. All blocks are placed first, because the
     * meaning of a label depends on the blocks around it.
     *
     * @return the positions of the placed blocks
     */
    public static List<BlockPos> place(ServerLevel level, int sheet, Sheet lines) {
        List<BlockPos> placed = new ArrayList<>();
        for (Line line : lines.lines()) {
            for (Line.Cell cell : line.cells()) {
                BlockPos pos = LevelSheet.codePos(sheet, cell.x(), line.z());
                level.setBlockAndUpdate(pos, C0deBlocks.get(type(cell.block())).defaultBlockState());
                placed.add(pos);
            }
        }
        for (Line line : lines.lines()) {
            for (Line.Cell cell : line.cells()) {
                BlockPos pos = LevelSheet.codePos(sheet, cell.x(), line.z());
                properties(cell.block()).ifPresent(properties -> {
                    if (level.getBlockEntity(pos) instanceof PropertiesBlockEntity entity
                            && entity.apply(properties, level).isPresent()) {
                        // Properties that don't fit their position are kept as they are; the validation shows them
                        de.joernott.c0de.compat.Compat.loadBlockEntity(entity, properties, level.registryAccess());
                        entity.setChanged();
                        level.sendBlockUpdated(pos, entity.getBlockState(), entity.getBlockState(), 2);
                    }
                });
            }
        }
        return placed;
    }
}
