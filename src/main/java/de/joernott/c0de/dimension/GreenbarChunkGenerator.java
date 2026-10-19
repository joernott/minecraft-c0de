package de.joernott.c0de.dimension;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.joernott.c0de.content.C0deBlocks;
import de.joernott.c0de.core.sheet.SheetGeometry;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.blending.Blender;

/**
 * Generates the floor of the c0de dimension: endless sheets of greenbar paper at y=-4 (see
 * {@link SheetGeometry#floorAt}). Everything else is air; there are no caves, structures or mobs.
 */
public class GreenbarChunkGenerator extends ChunkGenerator {
    public static final MapCodec<GreenbarChunkGenerator> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                    BiomeSource.CODEC.fieldOf("biome_source").forGetter(ChunkGenerator::getBiomeSource))
            .apply(instance, GreenbarChunkGenerator::new));

    public GreenbarChunkGenerator(BiomeSource biomeSource) {
        super(biomeSource);
    }

    @Override
    protected MapCodec<? extends ChunkGenerator> codec() {
        return CODEC;
    }

    @Override
    public CompletableFuture<ChunkAccess> fillFromNoise(
            Blender blender, RandomState randomState, StructureManager structureManager, ChunkAccess chunk) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        Heightmap oceanFloor = chunk.getOrCreateHeightmapUnprimed(Heightmap.Types.OCEAN_FLOOR_WG);
        Heightmap surface = chunk.getOrCreateHeightmapUnprimed(Heightmap.Types.WORLD_SURFACE_WG);
        int minX = chunk.getPos().getMinBlockX();
        int minZ = chunk.getPos().getMinBlockZ();
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                BlockState state = C0deBlocks.floorBlock(SheetGeometry.floorAt(minX + x, minZ + z));
                chunk.setBlockState(pos.set(x, SheetGeometry.FLOOR_Y, z), state, false);
                oceanFloor.update(x, SheetGeometry.FLOOR_Y, z, state);
                surface.update(x, SheetGeometry.FLOOR_Y, z, state);
            }
        }
        return CompletableFuture.completedFuture(chunk);
    }

    @Override
    public int getBaseHeight(int x, int z, Heightmap.Types type, LevelHeightAccessor level, RandomState random) {
        // The first free y coordinate above the floor
        return SheetGeometry.FLOOR_Y + 1;
    }

    @Override
    public NoiseColumn getBaseColumn(int x, int z, LevelHeightAccessor height, RandomState random) {
        BlockState[] column = new BlockState[height.getHeight()];
        for (int i = 0; i < column.length; i++) {
            int y = height.getMinBuildHeight() + i;
            column[i] = y == SheetGeometry.FLOOR_Y
                    ? C0deBlocks.floorBlock(SheetGeometry.floorAt(x, z))
                    : Blocks.AIR.defaultBlockState();
        }
        return new NoiseColumn(height.getMinBuildHeight(), column);
    }

    @Override
    public void applyCarvers(
            WorldGenRegion level,
            long seed,
            RandomState random,
            BiomeManager biomeManager,
            StructureManager structureManager,
            ChunkAccess chunk,
            GenerationStep.Carving step) {}

    @Override
    public void buildSurface(
            WorldGenRegion level, StructureManager structureManager, RandomState random, ChunkAccess chunk) {}

    @Override
    public void spawnOriginalMobs(WorldGenRegion level) {}

    @Override
    public int getGenDepth() {
        return 384;
    }

    @Override
    public int getSeaLevel() {
        return SheetGeometry.MIN_Y;
    }

    @Override
    public int getMinY() {
        return SheetGeometry.MIN_Y;
    }

    @Override
    public void addDebugScreenInfo(List<String> info, RandomState random, BlockPos pos) {
        int sheet = SheetGeometry.sheetIndex(pos.getX(), pos.getZ());
        info.add(sheet == SheetGeometry.NO_SHEET
                ? "c0de: outside of the sheets"
                : "c0de: sheet " + sheet + ", x " + SheetGeometry.relativeX(pos.getX()) + ", line " + pos.getZ());
    }
}
