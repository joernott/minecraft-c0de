package de.joernott.c0de.mixin.fabric;

//? if fabric {
import de.joernott.c0de.platform.Platform;
import java.util.LinkedHashSet;
import java.util.Set;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.repository.RepositorySource;
import net.minecraft.server.packs.repository.ServerPacksSource;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Adds the data pack sources of the mod to the pack repositories of servers (Fabric only; NeoForge has
 * an event for this). Server pack repositories are recognized by their {@link ServerPacksSource}.
 */
@Mixin(PackRepository.class)
public class PackRepositoryMixin {
    @Shadow
    @Final
    @Mutable
    private Set<RepositorySource> sources;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void c0de$addServerDataPackSources(RepositorySource[] repositorySources, CallbackInfo info) {
        if (sources.stream().anyMatch(source -> source instanceof ServerPacksSource)) {
            Set<RepositorySource> extended = new LinkedHashSet<>(sources);
            extended.addAll(Platform.FabricPlatform.SERVER_DATA_SOURCES);
            sources = extended;
        }
    }
}
//?}
