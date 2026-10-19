package de.joernott.c0de.gametest.neoforge;

//? if neoforge {
/*import de.joernott.c0de.C0de;
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
}
*///?}
