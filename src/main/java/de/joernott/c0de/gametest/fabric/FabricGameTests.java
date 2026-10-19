package de.joernott.c0de.gametest.fabric;

//? if fabric {
import de.joernott.c0de.gametest.DimensionTests;
import de.joernott.c0de.gametest.SmokeTests;
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
}
//?}
