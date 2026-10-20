package de.joernott.c0de.gametest.fabric;

//? if fabric {
import de.joernott.c0de.gametest.CodeBlockTests;
import de.joernott.c0de.gametest.DimensionTests;
import de.joernott.c0de.gametest.SmokeTests;
import de.joernott.c0de.gametest.ValidationTests;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;

/** Registers the GameTests with Fabric (entry point {@code fabric-gametest}). */
public class FabricGameTests implements FabricGameTest {
    @GameTest(template = SmokeTests.EMPTY_STRUCTURE)
    public void modIsInitialized(GameTestHelper helper) {
        SmokeTests.modIsInitialized(helper);
    }

    @GameTest(template = SmokeTests.EMPTY_STRUCTURE)
    public void dimensionHasTheRequiredProperties(GameTestHelper helper) {
        DimensionTests.dimensionHasTheRequiredProperties(helper);
    }

    @GameTest(template = SmokeTests.EMPTY_STRUCTURE)
    public void floorHasTheGreenbarPattern(GameTestHelper helper) {
        DimensionTests.floorHasTheGreenbarPattern(helper);
    }

    @GameTest(template = SmokeTests.EMPTY_STRUCTURE)
    public void floorIsUnbreakable(GameTestHelper helper) {
        DimensionTests.floorIsUnbreakable(helper);
    }

    @GameTest(template = SmokeTests.EMPTY_STRUCTURE)
    public void noMobsSpawn(GameTestHelper helper) {
        DimensionTests.noMobsSpawn(helper);
    }

    @GameTest(template = SmokeTests.EMPTY_STRUCTURE)
    public void playerEntersAndLeavesTheDimension(GameTestHelper helper) {
        DimensionTests.playerEntersAndLeavesTheDimension(helper);
    }

    @GameTest(template = SmokeTests.EMPTY_STRUCTURE)
    public void allBlocksHaveItemsLootTablesAndRecipes(GameTestHelper helper) {
        CodeBlockTests.allBlocksHaveItemsLootTablesAndRecipes(helper);
    }

    @GameTest(template = SmokeTests.EMPTY_STRUCTURE)
    public void literalPropertiesAreCheckedSavedAndSynchronized(GameTestHelper helper) {
        CodeBlockTests.literalPropertiesAreCheckedSavedAndSynchronized(helper);
    }

    @GameTest(template = SmokeTests.EMPTY_STRUCTURE)
    public void operatorAndTagProperties(GameTestHelper helper) {
        CodeBlockTests.operatorAndTagProperties(helper);
    }

    @GameTest(template = SmokeTests.EMPTY_STRUCTURE)
    public void blocksFarAwayCantBeChanged(GameTestHelper helper) {
        CodeBlockTests.blocksFarAwayCantBeChanged(helper);
    }

    @GameTest(template = SmokeTests.EMPTY_STRUCTURE)
    public void labelsAndTagsUseTheirPositionOnTheSheet(GameTestHelper helper) {
        CodeBlockTests.labelsAndTagsUseTheirPositionOnTheSheet(helper);
    }

    @GameTest(template = SmokeTests.EMPTY_STRUCTURE)
    public void programsAreValidatedAndRegistered(GameTestHelper helper) {
        ValidationTests.programsAreValidatedAndRegistered(helper);
    }

    @GameTest(template = SmokeTests.EMPTY_STRUCTURE)
    public void changesAreValidatedAtTheEndOfTheTick(GameTestHelper helper) {
        ValidationTests.changesAreValidatedAtTheEndOfTheTick(helper);
    }

    @GameTest(template = SmokeTests.EMPTY_STRUCTURE)
    public void programNamesAreUnique(GameTestHelper helper) {
        ValidationTests.programNamesAreUnique(helper);
    }

    @GameTest(template = SmokeTests.EMPTY_STRUCTURE)
    public void placementRulesPreventMistakes(GameTestHelper helper) {
        ValidationTests.placementRulesPreventMistakes(helper);
    }

    @GameTest(template = SmokeTests.EMPTY_STRUCTURE)
    public void labelDialogsListTheNamesOfTheProgram(GameTestHelper helper) {
        ValidationTests.labelDialogsListTheNamesOfTheProgram(helper);
    }

    @GameTest(template = SmokeTests.EMPTY_STRUCTURE)
    public void existingProgramsAreRegisteredWhenTheirChunksLoad(GameTestHelper helper) {
        ValidationTests.existingProgramsAreRegisteredWhenTheirChunksLoad(helper);
    }
}
//?}
