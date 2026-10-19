package de.joernott.c0de.gametest.neoforge;

//? if neoforge {
/*import de.joernott.c0de.C0de;
import de.joernott.c0de.gametest.DimensionTests;
import de.joernott.c0de.gametest.SmokeTests;
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
}
*///?}
