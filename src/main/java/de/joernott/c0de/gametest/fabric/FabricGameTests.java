package de.joernott.c0de.gametest.fabric;

//? if fabric {
import de.joernott.c0de.gametest.CodeBlockTests;
import de.joernott.c0de.gametest.DimensionTests;
import de.joernott.c0de.gametest.DiskTests;
import de.joernott.c0de.gametest.IoTests;
import de.joernott.c0de.gametest.LibraryTests;
import de.joernott.c0de.gametest.PerformanceTests;
import de.joernott.c0de.gametest.ProcessTests;
import de.joernott.c0de.gametest.SmokeTests;
import de.joernott.c0de.gametest.ToolTests;
import de.joernott.c0de.gametest.ValidationTests;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;

/**
 * Registers the GameTests with Fabric (entry point {@code fabric-gametest}). Since 1.21.5 the annotation is the one
 * of Fabric, with other attribute names: replacements in stonecutter.gradle.kts. All tests have a timeout, because
 * the default timeouts differ.
 */
public class FabricGameTests {
    @GameTest(template = SmokeTests.EMPTY_STRUCTURE, timeoutTicks = 100)
    public void modIsInitialized(GameTestHelper helper) {
        SmokeTests.modIsInitialized(helper);
    }

    @GameTest(template = SmokeTests.EMPTY_STRUCTURE, timeoutTicks = 100)
    public void dimensionHasTheRequiredProperties(GameTestHelper helper) {
        DimensionTests.dimensionHasTheRequiredProperties(helper);
    }

    @GameTest(template = SmokeTests.EMPTY_STRUCTURE, timeoutTicks = 100)
    public void floorHasTheGreenbarPattern(GameTestHelper helper) {
        DimensionTests.floorHasTheGreenbarPattern(helper);
    }

    @GameTest(template = SmokeTests.EMPTY_STRUCTURE, timeoutTicks = 100)
    public void floorIsUnbreakable(GameTestHelper helper) {
        DimensionTests.floorIsUnbreakable(helper);
    }

    @GameTest(template = SmokeTests.EMPTY_STRUCTURE, timeoutTicks = 100)
    public void noMobsSpawn(GameTestHelper helper) {
        DimensionTests.noMobsSpawn(helper);
    }

    @GameTest(template = SmokeTests.EMPTY_STRUCTURE, timeoutTicks = 100)
    public void playerEntersAndLeavesTheDimension(GameTestHelper helper) {
        DimensionTests.playerEntersAndLeavesTheDimension(helper);
    }

    @GameTest(template = SmokeTests.EMPTY_STRUCTURE, timeoutTicks = 100)
    public void allBlocksHaveItemsLootTablesAndRecipes(GameTestHelper helper) {
        CodeBlockTests.allBlocksHaveItemsLootTablesAndRecipes(helper);
    }

    @GameTest(template = SmokeTests.EMPTY_STRUCTURE, timeoutTicks = 100)
    public void literalPropertiesAreCheckedSavedAndSynchronized(GameTestHelper helper) {
        CodeBlockTests.literalPropertiesAreCheckedSavedAndSynchronized(helper);
    }

    @GameTest(template = SmokeTests.EMPTY_STRUCTURE, timeoutTicks = 100)
    public void operatorAndTagProperties(GameTestHelper helper) {
        CodeBlockTests.operatorAndTagProperties(helper);
    }

    @GameTest(template = SmokeTests.EMPTY_STRUCTURE, timeoutTicks = 100)
    public void blocksFarAwayCantBeChanged(GameTestHelper helper) {
        CodeBlockTests.blocksFarAwayCantBeChanged(helper);
    }

    @GameTest(template = SmokeTests.EMPTY_STRUCTURE, timeoutTicks = 100)
    public void labelsAndTagsUseTheirPositionOnTheSheet(GameTestHelper helper) {
        CodeBlockTests.labelsAndTagsUseTheirPositionOnTheSheet(helper);
    }

    @GameTest(template = SmokeTests.EMPTY_STRUCTURE, timeoutTicks = 100)
    public void programsAreValidatedAndRegistered(GameTestHelper helper) {
        ValidationTests.programsAreValidatedAndRegistered(helper);
    }

    @GameTest(template = SmokeTests.EMPTY_STRUCTURE, timeoutTicks = 100)
    public void changesAreValidatedAtTheEndOfTheTick(GameTestHelper helper) {
        ValidationTests.changesAreValidatedAtTheEndOfTheTick(helper);
    }

    @GameTest(template = SmokeTests.EMPTY_STRUCTURE, timeoutTicks = 100)
    public void programNamesAreUnique(GameTestHelper helper) {
        ValidationTests.programNamesAreUnique(helper);
    }

    @GameTest(template = SmokeTests.EMPTY_STRUCTURE, timeoutTicks = 100)
    public void placementRulesPreventMistakes(GameTestHelper helper) {
        ValidationTests.placementRulesPreventMistakes(helper);
    }

    @GameTest(template = SmokeTests.EMPTY_STRUCTURE, timeoutTicks = 100)
    public void labelDialogsListTheNamesOfTheProgram(GameTestHelper helper) {
        ValidationTests.labelDialogsListTheNamesOfTheProgram(helper);
    }

    @GameTest(template = SmokeTests.EMPTY_STRUCTURE, timeoutTicks = 100)
    public void existingProgramsAreRegisteredWhenTheirChunksLoad(GameTestHelper helper) {
        ValidationTests.existingProgramsAreRegisteredWhenTheirChunksLoad(helper);
    }

    @GameTest(template = SmokeTests.EMPTY_STRUCTURE, timeoutTicks = 100)
    public void crashedProcessesReturnTheirInputs(GameTestHelper helper) {
        ProcessTests.crashedProcessesReturnTheirInputs(helper);
    }

    @GameTest(template = SmokeTests.EMPTY_STRUCTURE, timeoutTicks = 100)
    public void processesReturnTheirResults(GameTestHelper helper) {
        ProcessTests.processesReturnTheirResults(helper);
    }

    @GameTest(template = SmokeTests.EMPTY_STRUCTURE, timeoutTicks = 100)
    public void processesCanBeAborted(GameTestHelper helper) {
        ProcessTests.processesCanBeAborted(helper);
    }

    @GameTest(template = SmokeTests.EMPTY_STRUCTURE, timeoutTicks = 100)
    public void displaysShowWhatProgramsPrint(GameTestHelper helper) {
        IoTests.displaysShowWhatProgramsPrint(helper);
    }

    @GameTest(template = SmokeTests.EMPTY_STRUCTURE, timeoutTicks = 300)
    public void manyProcessesRunAtTheSameTime(GameTestHelper helper) {
        PerformanceTests.manyProcessesRunAtTheSameTime(helper);
    }

    @GameTest(template = SmokeTests.EMPTY_STRUCTURE, timeoutTicks = 100)
    public void sheetsBelongToTheirOwners(GameTestHelper helper) {
        ToolTests.sheetsBelongToTheirOwners(helper);
    }

    @GameTest(template = SmokeTests.EMPTY_STRUCTURE, timeoutTicks = 100)
    public void administratorsRunOnlyTheirOwnPrograms(GameTestHelper helper) {
        LibraryTests.administratorsRunOnlyTheirOwnPrograms(helper);
    }

    @GameTest(template = SmokeTests.EMPTY_STRUCTURE, timeoutTicks = 100)
    public void triggersRunForTheRightActivators(GameTestHelper helper) {
        LibraryTests.triggersRunForTheRightActivators(helper);
    }

    @GameTest(template = SmokeTests.EMPTY_STRUCTURE, timeoutTicks = 100)
    public void ioBlocksExchangeItems(GameTestHelper helper) {
        IoTests.ioBlocksExchangeItems(helper);
    }

    @GameTest(template = SmokeTests.EMPTY_STRUCTURE, timeoutTicks = 100)
    public void laptopsTakeTypedLines(GameTestHelper helper) {
        IoTests.laptopsTakeTypedLines(helper);
    }

    @GameTest(template = SmokeTests.EMPTY_STRUCTURE, timeoutTicks = 800)
    public void longLoopsEnd(GameTestHelper helper) {
        ProcessTests.longLoopsEnd(helper);
    }

    @GameTest(template = SmokeTests.EMPTY_STRUCTURE, timeoutTicks = 100)
    public void processesTakeOnlyTheItemsTheyUse(GameTestHelper helper) {
        ProcessTests.processesTakeOnlyTheItemsTheyUse(helper);
    }

    @GameTest(template = SmokeTests.EMPTY_STRUCTURE, timeoutTicks = 100)
    public void processesKnowTheirSheetAndPid(GameTestHelper helper) {
        ProcessTests.processesKnowTheirSheetAndPid(helper);
    }

    @GameTest(template = SmokeTests.EMPTY_STRUCTURE, timeoutTicks = 120)
    public void redstonePausesTimers(GameTestHelper helper) {
        ProcessTests.redstonePausesTimers(helper);
    }

    @GameTest(template = SmokeTests.EMPTY_STRUCTURE, timeoutTicks = 100)
    public void triggerBlocksCanBeChained(GameTestHelper helper) {
        ProcessTests.triggerBlocksCanBeChained(helper);
    }

    @GameTest(template = SmokeTests.EMPTY_STRUCTURE, timeoutTicks = 100)
    public void hoppersChainTriggerBlocks(GameTestHelper helper) {
        ProcessTests.hoppersChainTriggerBlocks(helper);
    }

    @GameTest(template = SmokeTests.EMPTY_STRUCTURE, timeoutTicks = 100)
    public void missingInputsPreventTheStart(GameTestHelper helper) {
        ProcessTests.missingInputsPreventTheStart(helper);
    }

    @GameTest(template = SmokeTests.EMPTY_STRUCTURE, timeoutTicks = 100)
    public void removingTheTriggerStopsTheProcess(GameTestHelper helper) {
        ProcessTests.removingTheTriggerStopsTheProcess(helper);
    }

    @GameTest(template = SmokeTests.EMPTY_STRUCTURE, timeoutTicks = 100)
    public void cursorsSurviveSavingAsNbt(GameTestHelper helper) {
        ProcessTests.cursorsSurviveSavingAsNbt(helper);
    }

    @GameTest(template = SmokeTests.EMPTY_STRUCTURE, timeoutTicks = 100)
    public void valueItemsAreEdited(GameTestHelper helper) {
        ProcessTests.valueItemsAreEdited(helper);
    }

    @GameTest(template = SmokeTests.EMPTY_STRUCTURE, timeoutTicks = 100)
    public void debugCursorsArePausedAndStepped(GameTestHelper helper) {
        ToolTests.debugCursorsArePausedAndStepped(helper);
    }

    @GameTest(template = SmokeTests.EMPTY_STRUCTURE, timeoutTicks = 100)
    public void linesAreMarkedMovedAndDeleted(GameTestHelper helper) {
        ToolTests.linesAreMarkedMovedAndDeleted(helper);
    }

    @GameTest(template = SmokeTests.EMPTY_STRUCTURE, timeoutTicks = 100)
    public void onlyC0deBlocksAreInspected(GameTestHelper helper) {
        ToolTests.onlyC0deBlocksAreInspected(helper);
    }

    @GameTest(template = SmokeTests.EMPTY_STRUCTURE, timeoutTicks = 100)
    public void toolsWorkAfterMovingLines(GameTestHelper helper) {
        ToolTests.toolsWorkAfterMovingLines(helper);
    }

    @GameTest(template = SmokeTests.EMPTY_STRUCTURE, timeoutTicks = 100)
    public void toolCooldown(GameTestHelper helper) {
        ToolTests.toolCooldown(helper);
    }

    @GameTest(template = SmokeTests.EMPTY_STRUCTURE, timeoutTicks = 100)
    public void markerAndHammerRightClicks(GameTestHelper helper) {
        ToolTests.markerAndHammerRightClicks(helper);
    }

    @GameTest(template = SmokeTests.EMPTY_STRUCTURE, timeoutTicks = 100)
    public void programsSwitchLevers(GameTestHelper helper) {
        LibraryTests.programsSwitchLevers(helper);
    }

    @GameTest(template = SmokeTests.EMPTY_STRUCTURE, timeoutTicks = 100)
    public void disksExportAndImportPrograms(GameTestHelper helper) {
        DiskTests.disksExportAndImportPrograms(helper);
    }
}
//?}
