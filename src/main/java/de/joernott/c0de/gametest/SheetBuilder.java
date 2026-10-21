package de.joernott.c0de.gametest;

import de.joernott.c0de.blockentity.PropertiesBlockEntity;
import de.joernott.c0de.content.C0deBlocks;
import de.joernott.c0de.content.CodeBlockType;
import de.joernott.c0de.core.line.Block;
import de.joernott.c0de.core.line.IndexText;
import de.joernott.c0de.core.line.Line;
import de.joernott.c0de.core.line.Notation;
import de.joernott.c0de.core.sheet.SheetGeometry;
import de.joernott.c0de.core.value.BooleanValue;
import de.joernott.c0de.core.value.CharValue;
import de.joernott.c0de.core.value.FloatValue;
import de.joernott.c0de.core.value.IntegerValue;
import de.joernott.c0de.core.value.StringValue;
import de.joernott.c0de.core.value.TimeValue;
import de.joernott.c0de.core.value.Value;
import de.joernott.c0de.sheet.LevelSheet;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;

/** Places programs written in the notation of the examples on sheets of the c0de dimension, for GameTests. */
final class SheetBuilder {
    private final ServerLevel level;
    private final int sheet;
    private final List<BlockPos> placed = new ArrayList<>();

    /**
     * Keeps the chunks of a sheet loaded for a test. Forced chunks made the NeoForge GameTest server hang when
     * it stopped, so the chunks get a ticket that expires by itself.
     */
    private static final TicketType<ChunkPos> TICKET =
            TicketType.create("c0de_gametest", Comparator.comparingLong(ChunkPos::toLong), 200);

    /** Loads the chunks of the first 32 lines of the sheet. */
    SheetBuilder(ServerLevel level, int sheet) {
        this.level = level;
        this.sheet = sheet;
        int chunkX = SheetGeometry.absoluteX(sheet, 0) >> 4;
        for (int chunkZ = 0; chunkZ < 2; chunkZ++) {
            for (ChunkPos chunk : List.of(new ChunkPos(chunkX, chunkZ), new ChunkPos(chunkX + 1, chunkZ))) {
                level.getChunkSource().addRegionTicket(TICKET, chunk, 0, chunk);
                level.getChunk(chunk.x, chunk.z);
            }
        }
    }

    /** Places the blocks of a program and sets their properties. */
    SheetBuilder place(String program) {
        for (Line line : Notation.parse(program).lines()) {
            for (Line.Cell cell : line.cells()) {
                place(cell.block(), cell.x(), line.z());
            }
        }
        return this;
    }

    /** Places one block and sets its properties. */
    BlockPos place(Block block, int x, int z) {
        BlockPos pos = pos(x, z);
        level.setBlockAndUpdate(pos, C0deBlocks.get(type(block)).defaultBlockState());
        placed.add(pos);
        properties(block).ifPresent(properties -> {
            PropertiesBlockEntity entity = (PropertiesBlockEntity) level.getBlockEntity(pos);
            Optional<String> problem = entity.apply(properties, level);
            if (problem.isPresent()) {
                throw new IllegalArgumentException(block + " at " + x + ", " + z + ": " + problem.get());
            }
        });
        return pos;
    }

    BlockPos pos(int x, int z) {
        return LevelSheet.codePos(sheet, x, z);
    }

    /** Removes all placed blocks. */
    void clear() {
        placed.forEach(pos -> level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState()));
        placed.clear();
    }

    private static CodeBlockType type(Block block) {
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

    private static Optional<CompoundTag> properties(Block block) {
        CompoundTag tag = new CompoundTag();
        switch (block) {
            case Block.KeywordBlock keyword -> {
                return Optional.empty();
            }
            case Block.LabelBlock label -> {
                tag.putString("name", label.name());
                tag.putString("index", IndexText.format(label.index()));
            }
            case Block.LiteralBlock literal -> {
                Value value = literal.value();
                tag.putString("type", literalType(value));
                tag.putString("value", value.text());
            }
            case Block.OperatorBlock operator -> tag.putString("operator", operator.operator().name());
            case Block.CommentBlock comment -> tag.putString("text", comment.text());
            case Block.TagBlock tagBlock -> {
                tag.putString("language", tagBlock.language());
                tag.putString("name", tagBlock.name());
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
}
