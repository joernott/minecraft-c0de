package de.joernott.c0de.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestServer;
import net.minecraft.server.WorldLoader;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.WorldDimensions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * The GameTest server only creates the dimensions of the flat world preset, because it bakes the world
 * dimensions with an empty registry. This mixin passes the dimensions of the data packs instead, so that
 * the c0de dimension exists in GameTests. It only affects the GameTest server.
 */
@Mixin(GameTestServer.class)
public class GameTestServerMixin {
    @WrapOperation(
            method = "*",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lnet/minecraft/world/level/levelgen/WorldDimensions;bake(Lnet/minecraft/core/Registry;)Lnet/minecraft/world/level/levelgen/WorldDimensions$Complete;"))
    private static WorldDimensions.Complete c0de$bakeWithDataPackDimensions(
            WorldDimensions dimensions,
            Registry<LevelStem> emptyRegistry,
            Operation<WorldDimensions.Complete> original,
            @Local(argsOnly = true) WorldLoader.DataLoadContext context) {
        return original.call(dimensions, context.datapackDimensions().registryOrThrow(Registries.LEVEL_STEM));
    }
}
