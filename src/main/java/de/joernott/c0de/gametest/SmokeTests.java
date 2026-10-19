package de.joernott.c0de.gametest;

import de.joernott.c0de.C0de;
import de.joernott.c0de.core.config.C0deConfig;
import net.minecraft.gametest.framework.GameTestHelper;

/**
 * GameTests that check that the mod is loaded and initialized. The loader-specific classes in
 * {@code gametest.fabric} and {@code gametest.neoforge} register them with the GameTest framework.
 */
public final class SmokeTests {
    /** Structure for tests that don't need any blocks: a single air block. */
    public static final String EMPTY_STRUCTURE = C0de.MOD_ID + ":empty";

    private SmokeTests() {}

    public static void modIsInitialized(GameTestHelper helper) {
        helper.assertTrue(C0de.config() != null, "The configuration is not loaded");
        helper.assertTrue(
                C0de.config().get(C0deConfig.STACK_SIZE) >= 2048, "The stack size is below the minimum of 2048");
        helper.succeed();
    }
}
