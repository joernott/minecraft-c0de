package de.joernott.c0de.gametest;

import de.joernott.c0de.C0de;
import de.joernott.c0de.content.C0deBlocks;
import de.joernott.c0de.content.C0deItems;
import de.joernott.c0de.core.line.Notation;
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
            DiskDriveBlockEntity drive = (DiskDriveBlockEntity) TestCompat.blockEntity(helper, BlockPos.ZERO);
            drive.disk().setItem(0, new ItemStack(C0deItems.DISK.get()));
            DiskDriveMenu menu = new DiskDriveMenu(0, player.getInventory(), drive);
            helper.assertTrue(menu.load(copy).isEmpty(), "The drive didn't load the program");
            helper.assertTrue(
                    DiskItem.Contents.of(drive.disk().getItem(0)).name().equals("diskcopy"),
                    "The disk in the drive holds " + DiskItem.Contents.of(drive.disk().getItem(0)).name());
            helper.assertTrue(menu.load("not: a program").isPresent(), "The drive loaded a broken program");
            // Names that hide what they are or leave the folder c0de are refused (task 15.7)
            helper.assertTrue(
                    menu.load(copy.replace("diskcopy", "\"disk\\u202Ecopy\"")).isPresent(),
                    "The drive loaded a program with a hidden character in its name");

            BlockPos floor = new BlockPos(
                    SheetGeometry.absoluteX(TARGET, SheetGeometry.CODE_MIN_X), SheetGeometry.FLOOR_Y, 0);
            // A disk of an older version with a name that isn't valid any more isn't built
            DiskItem.Contents old = new DiskItem.Contents("../evil", copy.replace("diskcopy", "\"../evil\""));
            helper.assertTrue(
                    DiskService.build(player, c0de, floor, old).isPresent(), "A program with the name ../evil was built");
            helper.assertTrue(
                    ProgramRegistry.get(c0de).sheetOf("../evil").isEmpty(), "../evil is registered");
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

    /** The sheet of the long program; the lines beyond the first chunks are loaded by nobody else. */
    private static final int LONG = 561;

    private static final int LONG_LINES = 300;

    /**
     * A program longer than the view distance is built from a disk on a sheet whose chunks aren't loaded, validated,
     * and copied onto a disk again after its chunks were unloaded (task 21.1).
     */
    public static void longProgramsGoOntoDisksAndBack(GameTestHelper helper) {
        ServerLevel c0de = helper.getLevel().getServer().getLevel(C0deDimension.LEVEL);
        StringBuilder text = new StringBuilder("PROGRAM longdisk\nNOOP\nFUNCTION count\n    RESULT result INTEGER\nBEGIN\n");
        for (int i = 0; i < LONG_LINES; i++) {
            text.append("    ASSIGN result result + 1 ;\n");
        }
        text.append("END FUNCTION\n");
        String yaml = ProgramYaml.export(Notation.parse(text.toString()));
        int last = LONG_LINES + 4;
        helper.assertTrue(
                !LevelSheet.isLoaded(c0de, LONG, last), "The end of the sheet is loaded before the test");
        SheetBuilder sheet = new SheetBuilder(c0de, LONG);
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        BlockPos floor =
                new BlockPos(SheetGeometry.absoluteX(LONG, SheetGeometry.CODE_MIN_X), SheetGeometry.FLOOR_Y, 0);
        Optional<String> problem = DiskService.build(player, c0de, floor, DiskService.contents(yaml));
        helper.assertTrue(problem.isEmpty(), "The long program wasn't built: " + problem.orElse(""));
        OptionalInt registered = ProgramRegistry.get(c0de).sheetOf("longdisk");
        helper.assertTrue(
                registered.isPresent() && registered.getAsInt() == LONG, "The long program is not registered");
        // The temporary tickets expire after 200 ticks; then the chunks of the end of the program are unloaded
        helper.runAfterDelay(400, () -> {
            try {
                C0de.LOGGER.info(
                        "Long program: its last line is {} before it is copied",
                        LevelSheet.isLoaded(c0de, LONG, last) ? "loaded" : "not loaded");
                ItemStack disk = new ItemStack(C0deItems.DISK.get());
                Optional<String> copied = DiskService.copyToDisk(
                        player, c0de, LevelSheet.codePos(LONG, SheetGeometry.CODE_MIN_X, 0), disk);
                helper.assertTrue(copied.isEmpty(), "The long program wasn't copied: " + copied.orElse(""));
                String copy = DiskItem.Contents.of(disk).yaml();
                helper.assertTrue(copy.equals(yaml), "The copy of the long program differs:\n" + copy);
            } finally {
                sheet.clearLines(last + 1);
                SheetValidation.validate(c0de, LONG);
            }
            helper.succeed();
        });
    }
}
