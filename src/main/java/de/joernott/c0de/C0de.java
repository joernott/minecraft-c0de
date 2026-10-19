package de.joernott.c0de;

import de.joernott.c0de.core.config.C0deConfig;
import de.joernott.c0de.platform.Platform;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Loader-independent entry point of the mod. The loader-specific entry points call {@link #init()}. */
public final class C0de {
    public static final String MOD_ID = "c0de";
    public static final String VERSION = /*$ mod_version*/ "0.1.0";
    public static final String MINECRAFT = /*$ minecraft*/ "1.21.1";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private static C0deConfig config = C0deConfig.defaults();

    private C0de() {}

    /** Initializes the mod. Called once by the loader-specific entry point. */
    public static void init() {
        LOGGER.info("c0de {} for Minecraft {} on {}", VERSION, MINECRAFT, Platform.INSTANCE.loaderName());
        config = loadConfig();
    }

    /** The configuration of the mod. */
    public static C0deConfig config() {
        return config;
    }

    private static C0deConfig loadConfig() {
        try {
            C0deConfig loaded = C0deConfig.load(Platform.INSTANCE.configDir());
            for (String problem : loaded.problems()) {
                LOGGER.warn("Configuration file {}: {}", C0deConfig.FILE_NAME, problem);
            }
            return loaded;
        } catch (IOException e) {
            LOGGER.error("Can't read the configuration file {}, using the default values", C0deConfig.FILE_NAME, e);
            return C0deConfig.defaults();
        }
    }
}
