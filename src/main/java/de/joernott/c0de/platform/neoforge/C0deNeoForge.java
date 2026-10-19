package de.joernott.c0de.platform.neoforge;

//? if neoforge {
/*import de.joernott.c0de.C0de;
import de.joernott.c0de.platform.Platform;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;

// Entry point for NeoForge
@Mod(C0de.MOD_ID)
public class C0deNeoForge {
    public C0deNeoForge(IEventBus modEventBus, ModContainer modContainer) {
        Platform.NeoForgePlatform.setModEventBus(modEventBus);
        C0de.init();
        if (FMLEnvironment.dist.isClient()) {
            C0deNeoForgeClient.init(modEventBus);
        }
    }
}
*///?}
