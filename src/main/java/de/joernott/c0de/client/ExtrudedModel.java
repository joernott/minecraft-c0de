package de.joernott.c0de.client;

import de.joernott.c0de.C0de;
import de.joernott.c0de.block.CodeBlockShapes;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;

/**
 * The models of the language blocks with diagonal or round sides, which JSON models can't describe: a convex
 * polygon (seen from above) extruded to the height of a slab. BEGIN and END are trapezoids that are wide towards
 * their code block, PROGRAM is round in the west and LITERAL an octagon.
 *
 * <p>The JSON model of each block is a slab; this model wraps it and takes the textures of its top and side. The
 * top texture is projected from above, so it is cut along the sides; the sides show the lower half of the side
 * texture, like a slab. The outline and collision shapes are in {@link CodeBlockShapes}.
 */
public final class ExtrudedModel implements BakedModel {
    /** The ints of a vertex in the block vertex format: position (3), color, texture (2), light, normal. */
    private static final int STRIDE = 8;
    /** The number of edges of the semicircle of PROGRAM. */
    private static final int ROUND_EDGES = 12;

    private final BakedModel slab;
    private final List<BakedQuad> bottom = new ArrayList<>();
    private final List<BakedQuad> quads = new ArrayList<>();

    private ExtrudedModel(BakedModel slab, double[][] polygon) {
        this.slab = slab;
        TextureAtlasSprite top = sprite(slab, Direction.UP);
        TextureAtlasSprite side = sprite(slab, Direction.NORTH);
        float height = (float) (CodeBlockShapes.HEIGHT / 16);
        int n = polygon.length;
        // The top is a fan of triangles (quads with a repeated vertex) around the first corner
        for (int i = 1; i < n - 1; i++) {
            double[][] triangle = {polygon[0], polygon[i], polygon[i + 1], polygon[i + 1]};
            quads.add(horizontal(triangle, height, Direction.UP, top));
            double[][] reversed = {polygon[0], polygon[i + 1], polygon[i + 1], polygon[i]};
            bottom.add(horizontal(reversed, 0, Direction.DOWN, top));
        }
        double[] center = center(polygon);
        for (int i = 0; i < n; i++) {
            quads.add(side(polygon[i], polygon[(i + 1) % n], center, height, side));
        }
    }

    /** Wraps the models of the blocks with an extruded shape and their items; other models stay unchanged. */
    public static BakedModel wrap(ModelResourceLocation id, BakedModel model) {
        if (!id.id().getNamespace().equals(C0de.MOD_ID)) {
            return model;
        }
        double inset = CodeBlockShapes.TRAPEZOID_INSET;
        return switch (id.id().getPath()) {
            case "begin_block" -> new ExtrudedModel(model, new double[][] {{inset, 0}, {0, 16}, {16, 16}, {16 - inset, 0}});
            case "end_block" -> new ExtrudedModel(model, new double[][] {{0, 0}, {inset, 16}, {16 - inset, 16}, {16, 0}});
            case "program_block" -> new ExtrudedModel(model, halfRound());
            case "literal_block" -> new ExtrudedModel(model, octagon());
            default -> model;
        };
    }

    /**
     * The corners of a polygon go around in this order: north west, south west, south east, north east, which is
     * the order of the corners of the top of a block in Minecraft.
     */
    private static double[][] halfRound() {
        double[][] polygon = new double[ROUND_EDGES + 3][];
        for (int k = 0; k <= ROUND_EDGES; k++) {
            // From the north (z=0) through the west to the south (z=16)
            double angle = Math.PI * 1.5 - Math.PI * k / ROUND_EDGES;
            polygon[k] = new double[] {8 + 8 * Math.cos(angle), 8 + 8 * Math.sin(angle)};
        }
        polygon[ROUND_EDGES + 1] = new double[] {16, 16};
        polygon[ROUND_EDGES + 2] = new double[] {16, 0};
        return polygon;
    }

    private static double[][] octagon() {
        double c = CodeBlockShapes.OCTAGON_CUT;
        return new double[][] {
            {c, 0}, {0, c}, {0, 16 - c}, {c, 16}, {16 - c, 16}, {16, 16 - c}, {16, c}, {16 - c, 0}
        };
    }

    private static double[] center(double[][] polygon) {
        double x = 0;
        double z = 0;
        for (double[] corner : polygon) {
            x += corner[0];
            z += corner[1];
        }
        return new double[] {x / polygon.length, z / polygon.length};
    }

    /** The sprite of a face of the slab model. */
    private static TextureAtlasSprite sprite(BakedModel model, Direction face) {
        RandomSource random = RandomSource.create(0);
        List<BakedQuad> all = new ArrayList<>(model.getQuads(null, null, random));
        for (Direction side : Direction.values()) {
            all.addAll(model.getQuads(null, side, random));
        }
        return all.stream()
                .filter(quad -> quad.getDirection() == face)
                .findFirst()
                .map(BakedQuad::getSprite)
                .orElse(model.getParticleIcon());
    }

    /** The top or bottom: the texture is projected from above. */
    private static BakedQuad horizontal(double[][] corners, float y, Direction face, TextureAtlasSprite sprite) {
        int[] data = new int[4 * STRIDE];
        for (int i = 0; i < 4; i++) {
            float x = (float) (corners[i][0] / 16);
            float z = (float) (corners[i][1] / 16);
            vertex(data, i, x, y, z, sprite.getU(x), sprite.getV(face == Direction.UP ? z : 1 - z), face);
        }
        return new BakedQuad(data, -1, face, sprite, true);
    }

    /** A side between two corners, facing away from the center of the polygon. */
    private static BakedQuad side(double[] a, double[] b, double[] center, float height, TextureAtlasSprite sprite) {
        double nx = b[1] - a[1];
        double nz = a[0] - b[0];
        double middleX = (a[0] + b[0]) / 2 - center[0];
        double middleZ = (a[1] + b[1]) / 2 - center[1];
        if (nx * middleX + nz * middleZ < 0) {
            nx = -nx;
            nz = -nz;
        }
        Direction face = Direction.getNearest((float) nx, 0, (float) nz);
        // Seen from outside, left is the direction normal x up
        double leftX = -nz;
        double leftZ = nx;
        double[] left = a[0] * leftX + a[1] * leftZ >= b[0] * leftX + b[1] * leftZ ? a : b;
        double[] right = left == a ? b : a;
        int[] data = new int[4 * STRIDE];
        double[][] corners = {left, left, right, right};
        float[] heights = {height, 0, 0, height};
        boolean alongX = face.getAxis() == Direction.Axis.Z;
        for (int i = 0; i < 4; i++) {
            float x = (float) (corners[i][0] / 16);
            float z = (float) (corners[i][1] / 16);
            // Like the sides of a block: u runs along the side, v from the top, the slab shows the lower half
            float u = alongX ? (face == Direction.SOUTH ? x : 1 - x) : (face == Direction.WEST ? z : 1 - z);
            vertex(data, i, x, heights[i], z, sprite.getU(u), sprite.getV(1 - heights[i]), face);
        }
        return new BakedQuad(data, -1, face, sprite, true);
    }

    private static void vertex(int[] data, int i, float x, float y, float z, float u, float v, Direction face) {
        int offset = i * STRIDE;
        data[offset] = Float.floatToRawIntBits(x);
        data[offset + 1] = Float.floatToRawIntBits(y);
        data[offset + 2] = Float.floatToRawIntBits(z);
        data[offset + 3] = -1;
        data[offset + 4] = Float.floatToRawIntBits(u);
        data[offset + 5] = Float.floatToRawIntBits(v);
        data[offset + 6] = 0;
        data[offset + 7] = (face.getStepX() * 127 & 0xFF)
                | (face.getStepY() * 127 & 0xFF) << 8
                | (face.getStepZ() * 127 & 0xFF) << 16;
    }

    @Override
    public List<BakedQuad> getQuads(BlockState state, Direction side, RandomSource random) {
        if (side == null) {
            return quads;
        }
        // Only the bottom lies on the border of the block, where a neighbor can hide it
        return side == Direction.DOWN ? bottom : List.of();
    }

    @Override
    public boolean useAmbientOcclusion() {
        // Ambient occlusion is computed for the corners of block faces, not for diagonal sides
        return false;
    }

    @Override
    public boolean isGui3d() {
        return slab.isGui3d();
    }

    @Override
    public boolean usesBlockLight() {
        return slab.usesBlockLight();
    }

    @Override
    public boolean isCustomRenderer() {
        return false;
    }

    @Override
    public TextureAtlasSprite getParticleIcon() {
        return slab.getParticleIcon();
    }

    @Override
    public ItemTransforms getTransforms() {
        return slab.getTransforms();
    }

    @Override
    public ItemOverrides getOverrides() {
        return slab.getOverrides();
    }
}
