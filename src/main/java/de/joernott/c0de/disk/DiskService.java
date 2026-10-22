package de.joernott.c0de.disk;

import de.joernott.c0de.C0de;
import de.joernott.c0de.block.CodeBlock;
import de.joernott.c0de.content.CodeBlockType;
import de.joernott.c0de.core.line.Sheet;
import de.joernott.c0de.core.sheet.SheetGeometry;
import de.joernott.c0de.core.yaml.ProgramYaml;
import de.joernott.c0de.network.DiskPayload;
import de.joernott.c0de.network.OpenDialogPayload;
import de.joernott.c0de.platform.Platform;
import de.joernott.c0de.sheet.LevelSheet;
import de.joernott.c0de.sheet.ProgramRegistry;
import de.joernott.c0de.sheet.SheetValidation;
import de.joernott.c0de.sheet.SheetWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.Item;

/**
 * The c0de disk (docs/concept/14-items-and-tools.md): exports the program of a sheet to a YAML file of the world
 * and imports a file onto an empty sheet.
 */
public final class DiskService {
    private DiskService() {}

    /** Registers the payload of the dialogs. Called once when the mod is initialized. */
    public static void register() {
        Platform.INSTANCE.registerServerbound(DiskPayload.TYPE, DiskPayload.CODEC, DiskService::handle);
    }

    private static void handle(DiskPayload payload, ServerPlayer player) {
        if (!player.serverLevel().isLoaded(payload.pos()) || !player.canInteractWithBlock(payload.pos(), 2.0)) {
            return;
        }
        switch (payload.action()) {
            case "overwrite" -> export(player, player.serverLevel(), payload.pos(), true);
            case "import" -> importProgram(player, player.serverLevel(), payload.pos(), payload.program());
            default -> C0de.LOGGER.debug("Unknown action of the c0de disk: {}", payload.action());
        }
    }

    /** Shows an error in a read-only dialog and returns it. */
    private static Optional<String> show(ServerPlayer player, BlockPos pos, List<String> lines) {
        CompoundTag data = new CompoundTag();
        data.putString("title", Component.translatable("item.c0de.c0de_disk").getString());
        ListTag list = new ListTag();
        lines.forEach(line -> list.add(StringTag.valueOf(line)));
        data.put("lines", list);
        Platform.INSTANCE.sendToPlayer(player, new OpenDialogPayload(pos, "inspect", true, data));
        return Optional.of(String.join(" ", lines));
    }

    /**
     * Exports the program of the sheet whose PROGRAM block was clicked. If the file exists, the player is asked to
     * confirm overwriting it first.
     *
     * @return the problem shown to the player, if there is one
     */
    public static Optional<String> export(ServerPlayer player, ServerLevel level, BlockPos pos, boolean overwrite) {
        if (!(level.getBlockState(pos).getBlock() instanceof CodeBlock code)
                || code.type() != CodeBlockType.PROGRAM
                || !LevelSheet.isOnSheet(level, pos)) {
            return Optional.of("Not a PROGRAM block");
        }
        int sheet = LevelSheet.sheetOf(pos);
        String yaml;
        String name;
        try {
            Sheet lines = LevelSheet.read(level, sheet)
                    .orElseThrow(() -> new IllegalArgumentException("A part of the sheet is not loaded"));
            yaml = ProgramYaml.export(lines);
            name = ProgramYaml.programName(yaml);
        } catch (IllegalArgumentException e) {
            return show(player, pos, List.of(String.valueOf(e.getMessage())));
        }
        try {
            if (!overwrite && ProgramFiles.exists(player.server, name)) {
                CompoundTag data = new CompoundTag();
                data.putString("program", name);
                Platform.INSTANCE.sendToPlayer(player, new OpenDialogPayload(pos, "disk_overwrite", false, data));
                return Optional.empty();
            }
            ProgramFiles.write(player.server, name, yaml);
            level.playSound(null, pos, SoundEvents.BOOK_PAGE_TURN, SoundSource.BLOCKS, 1.0F, 1.0F);
            player.displayClientMessage(Component.translatable("message.c0de.exported", name), true);
            return Optional.empty();
        } catch (IOException e) {
            C0de.LOGGER.error("Can't write the program {}", name, e);
            return show(player, pos, List.of("The file can't be written: " + e.getMessage()));
        }
    }

    /** Opens the list of files to import onto the empty sheet whose PROGRAM position was clicked. */
    public static void openImport(ServerPlayer player, BlockPos pos) {
        try {
            List<String> files = ProgramFiles.list(player.server);
            CompoundTag data = new CompoundTag();
            ListTag list = new ListTag();
            files.forEach(file -> list.add(StringTag.valueOf(file)));
            data.put("files", list);
            Platform.INSTANCE.sendToPlayer(player, new OpenDialogPayload(pos, "disk_import", false, data));
        } catch (IOException e) {
            show(player, pos, List.of("The programs can't be listed: " + e.getMessage()));
        }
    }

    /** True if the floor at the position is the PROGRAM position of a sheet (x=5, z=0). */
    public static boolean isProgramPosition(ServerLevel level, BlockPos pos) {
        return LevelSheet.isOnSheet(level, pos)
                && pos.getY() == SheetGeometry.FLOOR_Y
                && pos.getZ() == 0
                && SheetGeometry.relativeX(pos.getX()) == SheetGeometry.CODE_MIN_X;
    }

    /**
     * Imports the file of a program onto the empty sheet whose PROGRAM position was clicked. In survival mode, the
     * blocks are taken from the inventory of the player.
     *
     * @return the problem shown to the player, if there is one
     */
    public static Optional<String> importProgram(ServerPlayer player, ServerLevel level, BlockPos pos, String program) {
        if (!isProgramPosition(level, pos)) {
            return Optional.of("Not the PROGRAM position of a sheet");
        }
        int sheet = LevelSheet.sheetOf(pos);
        Sheet lines;
        String name;
        try {
            String yaml = ProgramFiles.read(player.server, program);
            lines = ProgramYaml.layout(yaml);
            name = ProgramYaml.programName(yaml);
        } catch (IOException | IllegalArgumentException e) {
            return show(
                    player, pos, List.of("The file " + program + " can't be imported:", String.valueOf(e.getMessage())));
        }
        if (ProgramRegistry.get(level).sheetOf(name).isPresent()) {
            return show(player, pos, List.of("A program with the name " + name + " already exists"));
        }
        for (int z = 0; z < lines.size(); z++) {
            if (!LevelSheet.isLoaded(level, sheet, z)) {
                return show(player, pos, List.of("A part of the sheet is not loaded"));
            }
            if (!LevelSheet.readLine(level, sheet, z).isEmpty()) {
                return show(player, pos, List.of("The sheet is not empty (line " + z + ")"));
            }
        }
        Map<Item, Integer> needed = SheetWriter.items(lines);
        if (!player.isCreative()) {
            List<String> missing = new ArrayList<>();
            needed.forEach((item, count) -> {
                int available = player.getInventory().countItem(item);
                if (available < count) {
                    missing.add((count - available) + " " + item.getDescription().getString());
                }
            });
            if (!missing.isEmpty()) {
                List<String> message = new ArrayList<>(List.of("Missing blocks:"));
                message.addAll(missing);
                return show(player, pos, message);
            }
            needed.forEach((item, count) -> player.getInventory()
                    .clearOrCountMatchingItems(stack -> stack.is(item), count, player.inventoryMenu.getCraftSlots()));
        }
        SheetWriter.place(level, sheet, lines);
        level.playSound(null, pos, SoundEvents.STONE_PLACE, SoundSource.BLOCKS, 1.0F, 0.8F);
        SheetValidation.validate(level, sheet);
        player.displayClientMessage(Component.translatable("message.c0de.imported", name), true);
        return Optional.empty();
    }
}
