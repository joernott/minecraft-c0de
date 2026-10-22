package de.joernott.c0de.gametest.neoforge;

//? if neoforge {
/*import de.joernott.c0de.C0de;
import de.joernott.c0de.gametest.CodeBlockTests;
import de.joernott.c0de.gametest.DimensionTests;
import de.joernott.c0de.gametest.DiskTests;
import de.joernott.c0de.gametest.LibraryTests;
import de.joernott.c0de.gametest.ProcessTests;
import de.joernott.c0de.gametest.SmokeTests;
import de.joernott.c0de.gametest.ToolTests;
import de.joernott.c0de.gametest.ValidationTests;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

// Registers the GameTests with NeoForge, which finds this class by its annotation
@GameTestHolder(C0de.MOD_ID)
@PrefixGameTestTemplate(false)
public class NeoForgeGameTests {
    @GameTest(template = "empty")
    public static void modIsInitialized(GameTestHelper helper) {
        SmokeTests.modIsInitialized(helper);
    }

    @GameTest(template = "empty")
    public static void dimensionHasTheRequiredProperties(GameTestHelper helper) {
        DimensionTests.dimensionHasTheRequiredProperties(helper);
    }

    @GameTest(template = "empty")
    public static void floorHasTheGreenbarPattern(GameTestHelper helper) {
        DimensionTests.floorHasTheGreenbarPattern(helper);
    }

    @GameTest(template = "empty")
    public static void floorIsUnbreakable(GameTestHelper helper) {
        DimensionTests.floorIsUnbreakable(helper);
    }

    @GameTest(template = "empty")
    public static void noMobsSpawn(GameTestHelper helper) {
        DimensionTests.noMobsSpawn(helper);
    }

    @GameTest(template = "empty")
    public static void playerEntersAndLeavesTheDimension(GameTestHelper helper) {
        DimensionTests.playerEntersAndLeavesTheDimension(helper);
    }

    @GameTest(template = "empty")
    public static void allBlocksHaveItemsLootTablesAndRecipes(GameTestHelper helper) {
        CodeBlockTests.allBlocksHaveItemsLootTablesAndRecipes(helper);
    }

    @GameTest(template = "empty")
    public static void literalPropertiesAreCheckedSavedAndSynchronized(GameTestHelper helper) {
        CodeBlockTests.literalPropertiesAreCheckedSavedAndSynchronized(helper);
    }

    @GameTest(template = "empty")
    public static void operatorAndTagProperties(GameTestHelper helper) {
        CodeBlockTests.operatorAndTagProperties(helper);
    }

    @GameTest(template = "empty")
    public static void blocksFarAwayCantBeChanged(GameTestHelper helper) {
        CodeBlockTests.blocksFarAwayCantBeChanged(helper);
    }

    @GameTest(template = "empty")
    public static void labelsAndTagsUseTheirPositionOnTheSheet(GameTestHelper helper) {
        CodeBlockTests.labelsAndTagsUseTheirPositionOnTheSheet(helper);
    }

    @GameTest(template = "empty")
    public static void programsAreValidatedAndRegistered(GameTestHelper helper) {
        ValidationTests.programsAreValidatedAndRegistered(helper);
    }

    @GameTest(template = "empty")
    public static void changesAreValidatedAtTheEndOfTheTick(GameTestHelper helper) {
        ValidationTests.changesAreValidatedAtTheEndOfTheTick(helper);
    }

    @GameTest(template = "empty")
    public static void programNamesAreUnique(GameTestHelper helper) {
        ValidationTests.programNamesAreUnique(helper);
    }

    @GameTest(template = "empty")
    public static void placementRulesPreventMistakes(GameTestHelper helper) {
        ValidationTests.placementRulesPreventMistakes(helper);
    }

    @GameTest(template = "empty")
    public static void labelDialogsListTheNamesOfTheProgram(GameTestHelper helper) {
        ValidationTests.labelDialogsListTheNamesOfTheProgram(helper);
    }

    @GameTest(template = "empty")
    public static void existingProgramsAreRegisteredWhenTheirChunksLoad(GameTestHelper helper) {
        ValidationTests.existingProgramsAreRegisteredWhenTheirChunksLoad(helper);
    }

    @GameTest(template = "empty")
    public static void crashedProcessesReturnTheirInputs(GameTestHelper helper) {
        ProcessTests.crashedProcessesReturnTheirInputs(helper);
    }

    @GameTest(template = "empty")
    public static void processesReturnTheirResults(GameTestHelper helper) {
        ProcessTests.processesReturnTheirResults(helper);
    }

    @GameTest(template = "empty")
    public static void processesTakeOnlyTheItemsTheyUse(GameTestHelper helper) {
        ProcessTests.processesTakeOnlyTheItemsTheyUse(helper);
    }

    @GameTest(template = "empty")
    public static void triggerBlocksCanBeChained(GameTestHelper helper) {
        ProcessTests.triggerBlocksCanBeChained(helper);
    }

    @GameTest(template = "empty")
    public static void hoppersChainTriggerBlocks(GameTestHelper helper) {
        ProcessTests.hoppersChainTriggerBlocks(helper);
    }

    @GameTest(template = "empty")
    public static void missingInputsPreventTheStart(GameTestHelper helper) {
        ProcessTests.missingInputsPreventTheStart(helper);
    }

    @GameTest(template = "empty")
    public static void removingTheTriggerStopsTheProcess(GameTestHelper helper) {
        ProcessTests.removingTheTriggerStopsTheProcess(helper);
    }

    @GameTest(template = "empty")
    public static void cursorsSurviveSavingAsNbt(GameTestHelper helper) {
        ProcessTests.cursorsSurviveSavingAsNbt(helper);
    }

    @GameTest(template = "empty")
    public static void valueItemsAreEdited(GameTestHelper helper) {
        ProcessTests.valueItemsAreEdited(helper);
    }

    @GameTest(template = "empty")
    public static void debugCursorsArePausedAndStepped(GameTestHelper helper) {
        ToolTests.debugCursorsArePausedAndStepped(helper);
    }

    @GameTest(template = "empty")
    public static void linesAreMarkedMovedAndDeleted(GameTestHelper helper) {
        ToolTests.linesAreMarkedMovedAndDeleted(helper);
    }

    @GameTest(template = "empty")
    public static void onlyC0deBlocksAreInspected(GameTestHelper helper) {
        ToolTests.onlyC0deBlocksAreInspected(helper);
    }

    @GameTest(template = "empty")
    public static void toolsWorkAfterMovingLines(GameTestHelper helper) {
        ToolTests.toolsWorkAfterMovingLines(helper);
    }

    @GameTest(template = "empty")
    public static void toolCooldown(GameTestHelper helper) {
        ToolTests.toolCooldown(helper);
    }

    @GameTest(template = "empty")
    public static void programsSwitchLevers(GameTestHelper helper) {
        LibraryTests.programsSwitchLevers(helper);
    }

    @GameTest(template = "empty")
    public static void disksExportAndImportPrograms(GameTestHelper helper) {
        DiskTests.disksExportAndImportPrograms(helper);
    }
}
*///?}
