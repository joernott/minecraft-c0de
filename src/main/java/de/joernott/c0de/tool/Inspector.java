package de.joernott.c0de.tool;

import de.joernott.c0de.block.CodeBlock;
import de.joernott.c0de.block.PidBlock;
import de.joernott.c0de.block.TriggerBlock;
import de.joernott.c0de.blockentity.CommentBlockEntity;
import de.joernott.c0de.blockentity.LabelBlockEntity;
import de.joernott.c0de.blockentity.LiteralBlockEntity;
import de.joernott.c0de.blockentity.OperatorBlockEntity;
import de.joernott.c0de.blockentity.PidBlockEntity;
import de.joernott.c0de.blockentity.TagBlockEntity;
import de.joernott.c0de.blockentity.TriggerBlockEntity;
import de.joernott.c0de.core.sheet.SheetGeometry;
import de.joernott.c0de.core.validation.Validation;
import de.joernott.c0de.core.validation.Validator;
import de.joernott.c0de.entity.CursorEntity;
import de.joernott.c0de.library.ModLibraries;
import de.joernott.c0de.network.OpenDialogPayload;
import de.joernott.c0de.platform.Platform;
import de.joernott.c0de.process.ProcessManager;
import de.joernott.c0de.sheet.LevelSheet;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * The read-only dialogs of the debugging tool (task 9.2): the properties of every c0de block, including the
 * validation result of its line, and the properties and stack of a cursor.
 */
public final class Inspector {
    private Inspector() {}

    /** True for the blocks the debugging tool can inspect. */
    public static boolean isInspectable(Level level, BlockPos pos) {
        Block block = level.getBlockState(pos).getBlock();
        return block instanceof CodeBlock || block instanceof TriggerBlock || block instanceof PidBlock;
    }

    /**
     * Opens the dialog with the properties of a c0de block.
     *
     * @return false if the block is not a c0de block
     */
    public static boolean inspectBlock(ServerPlayer player, BlockPos pos) {
        ServerLevel level = player.serverLevel();
        if (!isInspectable(level, pos)) {
            return false;
        }
        List<String> lines = new ArrayList<>();
        Block block = level.getBlockState(pos).getBlock();
        BlockEntity entity = level.getBlockEntity(pos);
        if (LevelSheet.isOnSheet(level, pos)) {
            lines.add("sheet " + LevelSheet.sheetOf(pos) + ", x=" + SheetGeometry.relativeX(pos.getX()) + ", line "
                    + pos.getZ());
        } else {
            lines.add(level.dimension().location() + " " + pos.getX() + " " + pos.getY() + " " + pos.getZ());
        }
        switch (entity) {
            case LabelBlockEntity label -> {
                lines.add("kind: " + label.kind());
                lines.add("parent: " + label.parent());
                lines.add("name: " + label.name());
                lines.add("index: " + label.index());
            }
            case LiteralBlockEntity literal -> {
                lines.add("type: " + literal.type());
                lines.add("value: " + literal.value());
            }
            case OperatorBlockEntity operator ->
                lines.add("operator: " + (operator.operator() == null ? "" : operator.operator().name()));
            case CommentBlockEntity comment -> lines.add("text: " + comment.text());
            case TagBlockEntity tag -> {
                lines.add("language: " + tag.language());
                lines.add("name: " + tag.name());
            }
            case PidBlockEntity pid -> {
                lines.add("PID: " + pid.pid());
                lines.add("trigger: " + pid.trigger());
            }
            case TriggerBlockEntity trigger -> {
                lines.add("program: " + trigger.program());
                lines.add("function: " + trigger.function());
                lines.add("PID: " + (trigger.isRunning() ? trigger.pid() : "-"));
                lines.add("exit code: " + (trigger.exitCode() < 0 ? "-" : trigger.exitCode()));
                if (!trigger.status().isEmpty()) {
                    lines.add("status: " + trigger.status());
                }
            }
            case null, default -> {
                if (block instanceof CodeBlock code && code.type().keyword() != null) {
                    lines.add("keyword: " + code.type().keyword());
                }
            }
        }
        if (block instanceof CodeBlock && LevelSheet.isOnSheet(level, pos) && pos.getY() == SheetGeometry.CODE_Y) {
            lines.add(validation(level, pos));
        }
        CompoundTag data = new CompoundTag();
        data.putString("title", block.getName().getString());
        data.put("lines", strings(lines));
        Platform.INSTANCE.sendToPlayer(player, new OpenDialogPayload(pos, "inspect", true, data));
        return true;
    }

    /** The validation result of the line of a block. */
    private static String validation(ServerLevel level, BlockPos pos) {
        return LevelSheet.read(level, LevelSheet.sheetOf(pos))
                .map(sheet -> {
                    Validation validation = Validator.validate(sheet, ModLibraries.registry());
                    int z = pos.getZ();
                    if (z >= validation.endLine()) {
                        return "line " + z + ": not part of the program (after an empty line)";
                    }
                    return validation
                            .problem(z)
                            .map(problem -> "line " + z + ": " + problem.code() + " (" + problem.code().code() + ") "
                                    + problem.message())
                            .orElse("line " + z + ": valid");
                })
                .orElse("The sheet is not completely loaded");
    }

    /** Opens the dialog with the properties and the stack of a cursor. */
    public static void inspectCursor(ServerPlayer player, CursorEntity cursor) {
        ProcessManager.get(player.server).inspect(cursor.process()).ifPresent(data -> {
            List<String> lines = new ArrayList<>();
            lines.add("program " + data.getString("program") + ", entry function " + data.getString("entry"));
            lines.add("PID " + data.getInt("pid") + ", line " + data.getInt("line") + ", " + data.getString("state")
                    + (data.getBoolean("debug") ? " (debug cursor)" : ""));
            lines.add("function: " + data.getString("function"));
            lines.add("trigger: " + data.getString("trigger"));
            lines.add("stack (top first):");
            data.getList("stack", StringTag.TAG_STRING).forEach(item -> lines.add("  " + item.getAsString()));
            CompoundTag dialog = new CompoundTag();
            dialog.putString("title", cursor.getName().getString());
            dialog.put("lines", strings(lines));
            Platform.INSTANCE.sendToPlayer(
                    player, new OpenDialogPayload(cursor.blockPosition(), "inspect", true, dialog));
        });
    }

    private static ListTag strings(List<String> lines) {
        ListTag list = new ListTag();
        lines.forEach(line -> list.add(StringTag.valueOf(line)));
        return list;
    }
}
