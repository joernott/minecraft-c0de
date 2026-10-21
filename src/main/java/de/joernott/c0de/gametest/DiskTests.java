package de.joernott.c0de.gametest;

import de.joernott.c0de.core.sheet.SheetGeometry;
import de.joernott.c0de.core.yaml.ProgramYaml;
import de.joernott.c0de.dimension.C0deDimension;
import de.joernott.c0de.disk.DiskService;
import de.joernott.c0de.disk.ProgramFiles;
import de.joernott.c0de.sheet.LevelSheet;
import de.joernott.c0de.sheet.ProgramRegistry;
import de.joernott.c0de.sheet.SheetValidation;
import java.io.IOException;
import java.util.Optional;
import java.util.OptionalInt;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

/** GameTests of the c0de disk (task 10.6). */
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
     * A program is exported to a YAML file; a copy of the file with another program name is imported onto an empty
     * sheet, which then holds the same program.
     */
    public static void disksExportAndImportPrograms(GameTestHelper helper) {
        ServerLevel c0de = helper.getLevel().getServer().getLevel(C0deDimension.LEVEL);
        SheetBuilder source = new SheetBuilder(c0de, SOURCE).place(PROGRAM);
        SheetBuilder target = new SheetBuilder(c0de, TARGET);
        try {
            SheetValidation.validate(c0de, SOURCE);
            ServerPlayer player = helper.makeMockServerPlayerInLevel();
            Optional<String> exported =
                    DiskService.export(player, c0de, LevelSheet.codePos(SOURCE, SheetGeometry.CODE_MIN_X, 0), true);
            helper.assertTrue(exported.isEmpty(), "The export failed: " + exported.orElse(""));
            helper.assertTrue(
                    ProgramFiles.exists(c0de.getServer(), "disksource"), "The program was not exported");
            String yaml = ProgramFiles.read(c0de.getServer(), "disksource");
            helper.assertTrue(yaml.contains("grow"), "The export misses the function: " + yaml);
            String copy = yaml.replace("disksource", "diskcopy");
            ProgramFiles.write(c0de.getServer(), "diskcopy", copy);

            BlockPos floor = new BlockPos(
                    SheetGeometry.absoluteX(TARGET, SheetGeometry.CODE_MIN_X), SheetGeometry.FLOOR_Y, 0);
            Optional<String> problem = DiskService.importProgram(player, c0de, floor, "diskcopy");
            helper.assertTrue(problem.isEmpty(), "The import failed: " + problem.orElse(""));
            OptionalInt sheet = ProgramRegistry.get(c0de).sheetOf("diskcopy");
            helper.assertTrue(
                    sheet.isPresent() && sheet.getAsInt() == TARGET, "The imported program is not registered");
            String imported = ProgramYaml.export(LevelSheet.read(c0de, TARGET).orElseThrow());
            helper.assertTrue(imported.equals(copy), "The imported program differs:\n" + imported);
        } catch (IOException e) {
            throw new AssertionError("The program file can't be read", e);
        } finally {
            source.clear();
            target.clearLines(32);
            SheetValidation.validate(c0de, SOURCE);
            SheetValidation.validate(c0de, TARGET);
        }
        helper.succeed();
    }
}
