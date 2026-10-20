package de.joernott.c0de;

import de.joernott.c0de.content.C0deBlockEntities;
import de.joernott.c0de.content.C0deBlocks;
import de.joernott.c0de.content.C0deEntities;
import de.joernott.c0de.content.C0deMenus;
import de.joernott.c0de.content.C0deItems;
import de.joernott.c0de.core.config.C0deConfig;
import de.joernott.c0de.core.library.standard.StandardLibraries;
import de.joernott.c0de.dialog.DialogService;
import de.joernott.c0de.dialog.SheetProtection;
import de.joernott.c0de.dialog.TriggerService;
import de.joernott.c0de.process.ProcessManager;
import de.joernott.c0de.dimension.C0deDimension;
import de.joernott.c0de.dimension.SettingsPack;
import de.joernott.c0de.platform.Platform;
import de.joernott.c0de.sheet.SheetValidation;
import java.io.IOException;
import net.minecraft.resources.ResourceLocation;
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
        checkLibraries();
        Platform.INSTANCE.addServerDataPackSource(SettingsPack.source());
        C0deBlocks.register();
        C0deBlockEntities.register();
        C0deItems.register();
        C0deEntities.register();
        C0deMenus.register();
        DialogService.register();
        TriggerService.register();
        SheetValidation.register();
        ProcessManager.register();
        SheetProtection.register();
        C0deDimension.register();
    }

    /** The configuration of the mod. */
    public static C0deConfig config() {
        return config;
    }

    /** An identifier in the namespace of the mod. */
    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }

    private static void checkLibraries() {
        try {
            LOGGER.info("c0de data formats available: {}", StandardLibraries.selfTest().replace('\n', ' '));
        } catch (LinkageError | RuntimeException e) {
            LOGGER.error("The c0de data formats JSON and YAML are not available", e);
        }
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
