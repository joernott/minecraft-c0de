package de.joernott.c0de.gametest;

import de.joernott.c0de.content.C0deBlocks;
import de.joernott.c0de.content.C0deItems;
import de.joernott.c0de.core.sheet.SheetGeometry;
import de.joernott.c0de.core.yaml.ProgramYaml;
import de.joernott.c0de.dimension.C0deDimension;
import de.joernott.c0de.disk.DiskDriveBlockEntity;
import de.joernott.c0de.disk.DiskDriveMenu;
import de.joernott.c0de.disk.DiskService;
import de.joernott.c0de.item.DiskItem;
import de.joernott.c0de.sheet.LevelSheet;
import de.joernott.c0de.sheet.ProgramRegistry;
import de.joernott.c0de.sheet.SheetValidation;
import java.util.Optional;
import java.util.OptionalInt;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

/** GameTests of the c0de disk (tasks 10.6 and 11.11.9). */
public final class DiskTests {
    private static final int SOURCE = 540;
    private static final int TARGET = 541;

    private static final String PROGRAM =
            """
            PROGRAM disksource
            NOOP
            // Doubles a value until it is large enough
            NOOP
            FUNCTION grow
                PARAMETER value INTEGER
                RESULT result INTEGER
            BEGIN
                ASSIGN result value ;
                WHILE result < 1000 ;
                BEGIN
                    ASSIGN result result * 2 ;
                END WHILE
            END FUNCTION
            """;

    private DiskTests() {}

    /**
     * A program is copied onto a disk; a copy of the disk with another program name builds the program on an empty
     * sheet, which then holds the same program (task 11.11.9).
     */
    public static void disksExportAndImportPrograms(GameTestHelper helper) {
        ServerLevel c0de = helper.getLevel().getServer().getLevel(C0deDimension.LEVEL);
        SheetBuilder source = new SheetBuilder(c0de, SOURCE).place(PROGRAM);
        SheetBuilder target = new SheetBuilder(c0de, TARGET);
        try {
            SheetValidation.validate(c0de, SOURCE);
            ServerPlayer player = helper.makeMockServerPlayerInLevel();
            ItemStack disk = new ItemStack(C0deItems.DISK.get());
            Optional<String> copied = DiskService.copyToDisk(
                    player, c0de, LevelSheet.codePos(SOURCE, SheetGeometry.CODE_MIN_X, 0), disk);
            helper.assertTrue(copied.isEmpty(), "The program wasn't copied onto the disk: " + copied.orElse(""));
            DiskItem.Contents contents = DiskItem.Contents.of(disk);
            helper.assertTrue(contents.name().equals("disksource"), "The disk holds " + contents.name());
            helper.assertTrue(contents.yaml().contains("grow"), "The disk misses the function: " + contents.yaml());
            String copy = contents.yaml().replace("disksource", "diskcopy");
            // The disk drive loads a program onto the disk in its slot
            helper.setBlock(BlockPos.ZERO, C0deBlocks.DISK_DRIVE.get());
            DiskDriveBlockEntity drive = (DiskDriveBlockEntity) helper.getBlockEntity(BlockPos.ZERO);
            drive.disk().setItem(0, new ItemStack(C0deItems.DISK.get()));
            DiskDriveMenu menu = new DiskDriveMenu(0, player.getInventory(), drive);
            helper.assertTrue(menu.load(copy).isEmpty(), "The drive didn't load the program");
            helper.assertTrue(
                    DiskItem.Contents.of(drive.disk().getItem(0)).name().equals("diskcopy"),
                    "The disk in the drive holds " + DiskItem.Contents.of(drive.disk().getItem(0)).name());
            helper.assertTrue(menu.load("not: a program").isPresent(), "The drive loaded a broken program");

            BlockPos floor = new BlockPos(
                    SheetGeometry.absoluteX(TARGET, SheetGeometry.CODE_MIN_X), SheetGeometry.FLOOR_Y, 0);
            Optional<String> problem = DiskService.build(player, c0de, floor, DiskService.contents(copy));
            helper.assertTrue(problem.isEmpty(), "The program wasn't built: " + problem.orElse(""));
            OptionalInt sheet = ProgramRegistry.get(c0de).sheetOf("diskcopy");
            helper.assertTrue(
                    sheet.isPresent() && sheet.getAsInt() == TARGET, "The built program is not registered");
            String built = ProgramYaml.export(LevelSheet.read(c0de, TARGET).orElseThrow());
            helper.assertTrue(built.equals(copy), "The built program differs:\n" + built);
        } finally {
            source.clear();
            target.clearLines(32);
            SheetValidation.validate(c0de, SOURCE);
            SheetValidation.validate(c0de, TARGET);
        }
        helper.succeed();
    }
}
