package de.joernott.c0de.client;

import de.joernott.c0de.C0de;
import de.joernott.c0de.block.CodeBlockShapes;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;

/**
 * The models of the language blocks with diagonal or round sides, which JSON models can't describe: a convex
 * polygon (seen from above) extruded to the height of a slab. BEGIN and END are trapezoids that are wide towards
 * their code block, PROGRAM is round in the west, LITERAL an octagon, ASSIGN a triangle pointing to positive x,
 * IF and ELSE diamonds.
 *
 * <p>The JSON model of each block is a slab; this model wraps it and takes the textures of its top and side. The
 * top texture is projected from above, so it is cut along the sides; the sides show the lower half of the side
 * texture, like a slab. PROGRAM carries a book, as if it lay in an invisible item frame: the bottom texture of
 * its slab model, extruded by half a pixel like an item. The outline and collision shapes are in
 * {@link CodeBlockShapes}.
 */
public final class ExtrudedModel implements BakedModel {
    /** The ints of a vertex in the block vertex format: position (3), color, texture (2), light, normal. */
    private static final int STRIDE = 8;
    /** The number of edges of the semicircle of PROGRAM. */
    private static final int ROUND_EDGES = 12;
    /** The size of an item lying on the top, in blocks, and its thickness. */
    private static final float ITEM_SIZE = 10 / 16.0F;
    private static final float ITEM_THICKNESS = 0.5F / 16;

    private final BakedModel slab;
    private final List<BakedQuad> bottom = new ArrayList<>();
    private final List<BakedQuad> quads = new ArrayList<>();

    private ExtrudedModel(BakedModel slab, double[][] polygon, boolean item) {
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
        if (item) {
            item(sprite(slab, Direction.DOWN), height);
        }
    }

    /**
     * An item lying on the top, as in an invisible item frame: its sprite on top and a side for every edge between
     * an opaque and a transparent pixel, like the item models of Minecraft. Readable from the south.
     */
    private void item(TextureAtlasSprite sprite, float bottom) {
        SpriteContents contents = sprite.contents();
        int width = contents.width();
        int height = contents.height();
        float x0 = (1 - ITEM_SIZE) / 2;
        float z0 = (1 - ITEM_SIZE) / 2;
        float top = bottom + ITEM_THICKNESS;
        float pixelX = ITEM_SIZE / width;
        float pixelZ = ITEM_SIZE / height;
        int[] data = new int[4 * STRIDE];
        float[][] corners = {{x0, z0, 0, 0}, {x0, z0 + ITEM_SIZE, 0, 1}, {x0 + ITEM_SIZE, z0 + ITEM_SIZE, 1, 1},
            {x0 + ITEM_SIZE, z0, 1, 0}};
        for (int i = 0; i < 4; i++) {
            vertex(data, i, corners[i][0], top, corners[i][1], sprite.getU(corners[i][2]), sprite.getV(corners[i][3]),
                    Direction.UP);
        }
        quads.add(new BakedQuad(data, -1, Direction.UP, sprite, true));
        Direction[] sides = {Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST};
        for (int px = 0; px < width; px++) {
            for (int pz = 0; pz < height; pz++) {
                if (contents.isTransparent(0, px, pz)) {
                    continue;
                }
                float u = sprite.getU((px + 0.5F) / width);
                float v = sprite.getV((pz + 0.5F) / height);
                float xa = x0 + px * pixelX;
                float xb = xa + pixelX;
                float za = z0 + pz * pixelZ;
                float zb = za + pixelZ;
                for (Direction face : sides) {
                    int nx = px + face.getStepX();
                    int nz = pz + face.getStepZ();
                    if (nx >= 0 && nx < width && nz >= 0 && nz < height && !contents.isTransparent(0, nx, nz)) {
                        continue;
                    }
                    // The ends of the side as seen from outside: left, right
                    float[][] edge = switch (face) {
                        case NORTH -> new float[][] {{xb, za}, {xa, za}};
                        case SOUTH -> new float[][] {{xa, zb}, {xb, zb}};
                        case WEST -> new float[][] {{xa, za}, {xa, zb}};
                        default -> new float[][] {{xb, zb}, {xb, za}};
                    };
                    int[] side = new int[4 * STRIDE];
                    vertex(side, 0, edge[0][0], top, edge[0][1], u, v, face);
                    vertex(side, 1, edge[0][0], bottom, edge[0][1], u, v, face);
                    vertex(side, 2, edge[1][0], bottom, edge[1][1], u, v, face);
                    vertex(side, 3, edge[1][0], top, edge[1][1], u, v, face);
                    quads.add(new BakedQuad(side, -1, face, sprite, true));
                }
            }
        }
    }

    /** Wraps the models of the blocks with an extruded shape and their items; other models stay unchanged. */
    public static BakedModel wrap(ModelResourceLocation id, BakedModel model) {
        if (!id.id().getNamespace().equals(C0de.MOD_ID)) {
            return model;
        }
        double inset = CodeBlockShapes.TRAPEZOID_INSET;
        double[][] narrowInNorth = {{inset, 0}, {0, 16}, {16, 16}, {16 - inset, 0}};
        double[][] narrowInSouth = {{0, 0}, {inset, 16}, {16 - inset, 16}, {16, 0}};
        // Items are seen from the other side in the inventory, so the trapezoids of the items are swapped
        boolean item = id.variant().equals("inventory");
        return switch (id.id().getPath()) {
            case "begin_block" -> new ExtrudedModel(model, item ? narrowInSouth : narrowInNorth, false);
            case "end_block" -> new ExtrudedModel(model, item ? narrowInNorth : narrowInSouth, false);
            case "program_block" -> new ExtrudedModel(model, halfRound(), true);
            case "literal_block" -> new ExtrudedModel(model, octagon(), false);
            case "assign_block" -> new ExtrudedModel(model, new double[][] {{0, 0}, {0, 16}, {16, 8}}, false);
            case "if_block", "else_block" ->
                new ExtrudedModel(model, new double[][] {{8, 0}, {0, 8}, {8, 16}, {16, 8}}, false);
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

    //? if neoforge {
    /*// The render type (cutout for the book of PROGRAM) comes from the JSON model on NeoForge
    @Override
    public net.neoforged.neoforge.client.ChunkRenderTypeSet getRenderTypes(
            BlockState state, RandomSource random, net.neoforged.neoforge.client.model.data.ModelData data) {
        return slab.getRenderTypes(state, random, data);
    }

    @Override
    public List<net.minecraft.client.renderer.RenderType> getRenderTypes(
            net.minecraft.world.item.ItemStack stack, boolean fabulous) {
        return slab.getRenderTypes(stack, fabulous);
    }
    *///?}
}
