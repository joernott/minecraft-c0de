package de.joernott.c0de.disk;

import de.joernott.c0de.block.CodeBlock;
import de.joernott.c0de.content.CodeBlockType;
import de.joernott.c0de.core.line.Sheet;
import de.joernott.c0de.core.sheet.SheetGeometry;
import de.joernott.c0de.core.yaml.ProgramYaml;
import de.joernott.c0de.item.DiskItem;
import de.joernott.c0de.network.DiskUploadPayload;
import de.joernott.c0de.network.OpenDialogPayload;
import de.joernott.c0de.platform.Platform;
import de.joernott.c0de.sheet.LevelSheet;
import de.joernott.c0de.sheet.ProgramRegistry;
import de.joernott.c0de.sheet.SheetOwners;
import de.joernott.c0de.sheet.SheetValidation;
import de.joernott.c0de.sheet.SheetWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
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
import net.minecraft.world.item.ItemStack;

/**
 * The c0de disk (docs/concept/14-items-and-tools.md, task 11.11.9): copies the program of a sheet onto a disk, builds
 * the program of a disk on an empty sheet, and receives the programs a player loads onto the disk in a disk drive
 * from their folder {@code c0de}.
 */
public final class DiskService {
    /** The parts of the programs players are uploading to a disk drive, by player. */
    private static final Map<UUID, StringBuilder> UPLOADS = new HashMap<>();

    private DiskService() {}

    /** Registers the payload of the disk drive. Called once when the mod is initialized. */
    public static void register() {
        Platform.INSTANCE.registerServerbound(DiskUploadPayload.TYPE, DiskUploadPayload.CODEC, DiskService::upload);
    }

    /** A part of a program for the disk in the drive the player has open; the last part writes it onto the disk. */
    private static void upload(DiskUploadPayload payload, ServerPlayer player) {
        StringBuilder text = payload.first()
                ? UPLOADS.compute(player.getUUID(), (id, old) -> new StringBuilder())
                : UPLOADS.get(player.getUUID());
        if (text == null) {
            return;
        }
        text.append(payload.chunk());
        if (text.length() > DiskItem.MAX_LENGTH) {
            UPLOADS.remove(player.getUUID());
            player.displayClientMessage(Component.translatable("message.c0de.not_saved", "The program is too long"), true);
            return;
        }
        if (!payload.last()) {
            return;
        }
        UPLOADS.remove(player.getUUID());
        if (!(player.containerMenu instanceof DiskDriveMenu menu)) {
            return;
        }
        Optional<String> problem = menu.load(text.toString());
        player.displayClientMessage(
                problem.map(reason -> Component.translatable("message.c0de.not_saved", reason))
                        .orElse(Component.translatable("message.c0de.disk_loaded")),
                true);
    }

    /** The program of a YAML text for a disk, or the reason why it can't be one. */
    public static DiskItem.Contents contents(String yaml) {
        if (yaml.length() > DiskItem.MAX_LENGTH) {
            throw new IllegalArgumentException("The program is too long");
        }
        ProgramYaml.layout(yaml);
        return new DiskItem.Contents(ProgramYaml.programName(yaml), yaml);
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
     * Copies the program of the sheet whose PROGRAM block was clicked onto a disk; a program on the disk is replaced.
     *
     * @return the problem shown to the player, if there is one
     */
    public static Optional<String> copyToDisk(ServerPlayer player, ServerLevel level, BlockPos pos, ItemStack disk) {
        if (!(level.getBlockState(pos).getBlock() instanceof CodeBlock code)
                || code.type() != CodeBlockType.PROGRAM
                || !LevelSheet.isOnSheet(level, pos)) {
            return Optional.of("Not a PROGRAM block");
        }
        DiskItem.Contents contents;
        try {
            Sheet lines = LevelSheet.read(level, LevelSheet.sheetOf(pos))
                    .orElseThrow(() -> new IllegalArgumentException("A part of the sheet is not loaded"));
            contents = contents(ProgramYaml.export(lines));
        } catch (IllegalArgumentException e) {
            return show(player, pos, List.of(String.valueOf(e.getMessage())));
        }
        contents.writeTo(disk);
        level.playSound(null, pos, SoundEvents.BOOK_PAGE_TURN, SoundSource.BLOCKS, 1.0F, 1.0F);
        player.displayClientMessage(Component.translatable("message.c0de.exported", contents.name()), true);
        return Optional.empty();
    }

    /** True if the floor at the position is the PROGRAM position of a sheet (x=5, z=0). */
    public static boolean isProgramPosition(ServerLevel level, BlockPos pos) {
        return LevelSheet.isOnSheet(level, pos)
                && pos.getY() == SheetGeometry.FLOOR_Y
                && pos.getZ() == 0
                && SheetGeometry.relativeX(pos.getX()) == SheetGeometry.CODE_MIN_X;
    }

    /**
     * Builds the program of a disk on the empty sheet whose PROGRAM position was clicked. In survival mode, the
     * blocks are taken from the inventory of the player.
     *
     * @return the problem shown to the player, if there is one
     */
    public static Optional<String> build(ServerPlayer player, ServerLevel level, BlockPos pos, DiskItem.Contents disk) {
        if (!isProgramPosition(level, pos)) {
            return Optional.of("Not the PROGRAM position of a sheet");
        }
        int sheet = LevelSheet.sheetOf(pos);
        Sheet lines;
        String name;
        if (disk.isEmpty()) {
            return show(player, pos, List.of("The disk is empty"));
        }
        try {
            lines = ProgramYaml.layout(disk.yaml());
            name = ProgramYaml.programName(disk.yaml());
        } catch (IllegalArgumentException e) {
            return show(player, pos, List.of("The program can't be built:", String.valueOf(e.getMessage())));
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
        Optional<Component> owner = SheetOwners.get(level).check(player, sheet, false);
        if (owner.isPresent()) {
            return show(player, pos, List.of(owner.get().getString()));
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
        SheetOwners.get(level).check(player, sheet, true);
        SheetWriter.place(level, sheet, lines);
        level.playSound(null, pos, SoundEvents.STONE_PLACE, SoundSource.BLOCKS, 1.0F, 0.8F);
        SheetValidation.validate(level, sheet);
        player.displayClientMessage(Component.translatable("message.c0de.imported", name), true);
        return Optional.empty();
    }
}
