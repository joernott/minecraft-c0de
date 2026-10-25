package de.joernott.c0de.client;

import de.joernott.c0de.C0de;
import de.joernott.c0de.block.CodeBlockShapes;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;

/**
 * The models of the language blocks with diagonal or round sides, which JSON models can't describe: a convex
 * polygon (seen from above) extruded to the height of a slab. BEGIN and END are trapezoids that are wide towards
 * their code block, PROGRAM is round in the west, LITERAL an octagon, ASSIGN a triangle pointing to positive x,
 * IF and ELSE diamonds, and the loops FOR, EACH, WHILE and REPEAT cylinders.
 *
 * <p>The JSON model of each block is a slab; this model replaces its quads and takes the textures of its top and
 * side. The top texture is projected from above, so it is cut along the sides; the sides show the lower half of the
 * side texture, like a slab.
 *
 * <p>The symbols of the blocks are raised half a pixel above their tops, like items in invisible item frames: the
 * bottom texture of the slab model is the symbol on a transparent background (the bottom of the block is never
 * seen). Textures of Minecraft (the book of PROGRAM, the items of the type blocks) are as large as items in an item
 * frame; the symbols of the mod cover the whole top, so their pixels stay where the texture has them. Blocks with a
 * symbol but without a shape of their own are squares. The outline and collision shapes are in
 * {@link CodeBlockShapes}.
 *
 * <p>Before 1.21.11, {@link Baked} wraps the baked slab models of the blocks and items. Since then the geometry of
 * the unbaked block models is replaced ({@link Unbaked}), which the item models inherit.
 */
public final class ExtrudedModel {
    /** The number of edges of the semicircle of PROGRAM; a full circle has twice as many. */
    private static final int ROUND_EDGES = 12;
    /** The size of a Minecraft texture lying on the top, in blocks, and the thickness of all symbols. */
    private static final float ITEM_SIZE = 10 / 16.0F;
    private static final float ITEM_THICKNESS = 0.5F / 16;

    /** The whole block: for blocks with a symbol but without a shape of their own. */
    private static final double[][] SQUARE = {{0, 0}, {0, 16}, {16, 16}, {16, 0}};

    /** The quads on the bottom of the block, where a neighbor can hide them. */
    private final List<BakedQuad> bottom = new ArrayList<>();
    /** All other quads. */
    private final List<BakedQuad> quads = new ArrayList<>();

    /**
     * @param sprites the sprites of the faces of the slab model: the top, the side (north) and the symbol (bottom)
     */
    private ExtrudedModel(Function<Direction, TextureAtlasSprite> sprites, double[][] polygon) {
        TextureAtlasSprite top = sprites.apply(Direction.UP);
        TextureAtlasSprite side = sprites.apply(Direction.NORTH);
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
        TextureAtlasSprite symbol = sprites.apply(Direction.DOWN);
        if (!symbol.contents().name().equals(top.contents().name())) {
            // Since 1.21.11, the item textures of Minecraft are in the blocks atlas as c0de:block/item/...
            ResourceLocation name = symbol.contents().name();
            boolean minecraft = name.getNamespace().equals("minecraft") || name.getPath().startsWith("block/item/");
            item(symbol, height, minecraft ? ITEM_SIZE : 1);
        }
    }

    /**
     * An item lying on the top, as in an invisible item frame: its sprite on top and a side for every edge between
     * an opaque and a transparent pixel, like the item models of Minecraft. Readable from the south.
     */
    private void item(TextureAtlasSprite sprite, float bottom, float size) {
        SpriteContents contents = sprite.contents();
        int width = contents.width();
        int height = contents.height();
        float x0 = (1 - size) / 2;
        float z0 = (1 - size) / 2;
        float top = bottom + ITEM_THICKNESS;
        float pixelX = size / width;
        float pixelZ = size / height;
        float[][] corners = {{x0, z0, 0, 0}, {x0, z0 + size, 0, 1}, {x0 + size, z0 + size, 1, 1}, {x0 + size, z0, 1, 0}};
        float[][] vertices = new float[4][];
        for (int i = 0; i < 4; i++) {
            vertices[i] = new float[] {
                corners[i][0], top, corners[i][1], sprite.getU(corners[i][2]), sprite.getV(corners[i][3])
            };
        }
        quads.add(quad(vertices, Direction.UP, sprite));
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
                    quads.add(quad(
                            new float[][] {
                                {edge[0][0], top, edge[0][1], u, v},
                                {edge[0][0], bottom, edge[0][1], u, v},
                                {edge[1][0], bottom, edge[1][1], u, v},
                                {edge[1][0], top, edge[1][1], u, v}
                            },
                            face,
                            sprite));
                }
            }
        }
    }

    /**
     * The polygon of the block with the given model name (the path of a block or item ID), or null if the block
     * keeps its JSON model.
     */
    private static double[][] polygon(String path) {
        double inset = CodeBlockShapes.TRAPEZOID_INSET;
        // The polygons MUST match the borders of the top textures (scripts/generate_textures.py, TOP_SHAPES)
        return switch (path) {
            case "begin_block" -> new double[][] {{inset, 0}, {0, 16}, {16, 16}, {16 - inset, 0}};
            case "end_block" -> new double[][] {{0, 0}, {inset, 16}, {16 - inset, 16}, {16, 0}};
            case "program_block" -> halfRound();
            case "literal_block" -> octagon();
            case "assign_block" -> new double[][] {{0, 0}, {0, 16}, {16, 8}};
            case "for_block", "each_block", "while_block", "repeat_block" -> circle();
            case "if_block", "else_block" -> new double[][] {{8, 0}, {0, 8}, {8, 16}, {16, 8}};
            case "tag_block", "exit_block", "break_block", "function_block", "return_block", "call_block" -> SQUARE;
            default -> path.startsWith("type_") && path.endsWith("_block") ? SQUARE : null;
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

    /** A circle filling the block, in the same order of corners as the other polygons. */
    private static double[][] circle() {
        int edges = 2 * ROUND_EDGES;
        double[][] polygon = new double[edges][];
        for (int k = 0; k < edges; k++) {
            // From the north through the west, the south and the east
            double angle = Math.PI * 1.5 - 2 * Math.PI * k / edges;
            polygon[k] = new double[] {8 + 8 * Math.cos(angle), 8 + 8 * Math.sin(angle)};
        }
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

    /** The sprite of a face of the slab model, given by its quads, or the fallback if no quad has the face. */
    private static TextureAtlasSprite sprite(List<BakedQuad> slab, Direction face, TextureAtlasSprite fallback) {
        for (BakedQuad quad : slab) {
            //? if >=26.1 {
            /*if (quad.direction() == face) {
                return quad.materialInfo().sprite();
            }
            *///?} elif >=1.21.5 {
            /*if (quad.direction() == face) {
                return quad.sprite();
            }
            *///?} else {
            if (quad.getDirection() == face) {
                return quad.getSprite();
            }
            //?}
        }
        return fallback;
    }

    /** The top or bottom: the texture is projected from above. */
    private static BakedQuad horizontal(double[][] corners, float y, Direction face, TextureAtlasSprite sprite) {
        float[][] vertices = new float[4][];
        for (int i = 0; i < 4; i++) {
            float x = (float) (corners[i][0] / 16);
            float z = (float) (corners[i][1] / 16);
            vertices[i] = new float[] {x, y, z, sprite.getU(x), sprite.getV(face == Direction.UP ? z : 1 - z)};
        }
        return quad(vertices, face, sprite);
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
        //? if >=1.21.5 {
        /*Direction face = Direction.getApproximateNearest((float) nx, 0, (float) nz);
        *///?} else {
        Direction face = Direction.getNearest((float) nx, 0, (float) nz);
        //?}
        // Seen from outside, left is the direction normal x up
        double leftX = -nz;
        double leftZ = nx;
        double[] left = a[0] * leftX + a[1] * leftZ >= b[0] * leftX + b[1] * leftZ ? a : b;
        double[] right = left == a ? b : a;
        double[][] corners = {left, left, right, right};
        float[] heights = {height, 0, 0, height};
        boolean alongX = face.getAxis() == Direction.Axis.Z;
        float[][] vertices = new float[4][];
        for (int i = 0; i < 4; i++) {
            float x = (float) (corners[i][0] / 16);
            float z = (float) (corners[i][1] / 16);
            // Like the sides of a block: u runs along the side, v from the top, the slab shows the lower half
            float u = alongX ? (face == Direction.SOUTH ? x : 1 - x) : (face == Direction.WEST ? z : 1 - z);
            vertices[i] = new float[] {x, heights[i], z, sprite.getU(u), sprite.getV(1 - heights[i])};
        }
        return quad(vertices, face, sprite);
    }

    /** A quad from four vertices: x, y, z, u, v. */
    private static BakedQuad quad(float[][] vertices, Direction face, TextureAtlasSprite sprite) {
        //? if >=26.1 {
        /*org.joml.Vector3f[] positions = new org.joml.Vector3f[4];
        long[] uvs = new long[4];
        for (int i = 0; i < 4; i++) {
            positions[i] = new org.joml.Vector3f(vertices[i][0], vertices[i][1], vertices[i][2]);
            uvs[i] = net.minecraft.client.model.geom.builders.UVPair.pack(vertices[i][3], vertices[i][4]);
        }
        // The textures of the language blocks have transparent pixels (the symbols, the book of PROGRAM)
        //? if >=26.3 {
        /^var material = new BakedQuad.MaterialInfo(
                sprite,
                net.minecraft.client.renderer.chunk.ChunkSectionLayer.CUTOUT,
                net.minecraft.client.renderer.Sheets.cutoutBlockItemSheet(),
                net.minecraft.client.renderer.Sheets.cutoutBlockItemGlintSheet(),
                net.minecraft.client.renderer.Sheets.cutoutBlockItemGlintSpecialSheet(),
                -1,
                null,
                0);
        ^///?} else {
        var material = new BakedQuad.MaterialInfo(
                sprite,
                net.minecraft.client.renderer.chunk.ChunkSectionLayer.CUTOUT,
                net.minecraft.client.renderer.Sheets.cutoutBlockItemSheet(),
                -1,
                true,
                0);
        //?}
        return new BakedQuad(
                positions[0], positions[1], positions[2], positions[3],
                uvs[0], uvs[1], uvs[2], uvs[3],
                face, material);
        *///?} elif >=1.21.11 {
        /*org.joml.Vector3f[] positions = new org.joml.Vector3f[4];
        long[] uvs = new long[4];
        for (int i = 0; i < 4; i++) {
            positions[i] = new org.joml.Vector3f(vertices[i][0], vertices[i][1], vertices[i][2]);
            uvs[i] = net.minecraft.client.model.geom.builders.UVPair.pack(vertices[i][3], vertices[i][4]);
        }
        return new BakedQuad(
                positions[0], positions[1], positions[2], positions[3],
                uvs[0], uvs[1], uvs[2], uvs[3],
                -1, face, sprite, true, 0);
        *///?} else {
        // The ints of a vertex in the block vertex format: position (3), color, texture (2), light, normal
        int stride = 8;
        int[] data = new int[4 * stride];
        int normal = (face.getStepX() * 127 & 0xFF)
                | (face.getStepY() * 127 & 0xFF) << 8
                | (face.getStepZ() * 127 & 0xFF) << 16;
        for (int i = 0; i < 4; i++) {
            int offset = i * stride;
            data[offset] = Float.floatToRawIntBits(vertices[i][0]);
            data[offset + 1] = Float.floatToRawIntBits(vertices[i][1]);
            data[offset + 2] = Float.floatToRawIntBits(vertices[i][2]);
            data[offset + 3] = -1;
            data[offset + 4] = Float.floatToRawIntBits(vertices[i][3]);
            data[offset + 5] = Float.floatToRawIntBits(vertices[i][4]);
            data[offset + 6] = 0;
            data[offset + 7] = normal;
        }
        return new BakedQuad(data, -1, face, sprite, true);
        //?}
    }

    //? if >=1.21.11 {
    /*/^*
     * Replaces the geometry of the block models with an extruded shape; the item models inherit it. Other models
     * stay unchanged.
     ^/
    public static net.minecraft.client.resources.model.UnbakedModel wrap(
            ResourceLocation id, net.minecraft.client.resources.model.UnbakedModel model) {
        if (!id.getNamespace().equals(C0de.MOD_ID) || !id.getPath().startsWith("block/")) {
            return model;
        }
        return wrap(id.getPath().substring("block/".length()), model);
    }

    /^* Gives the model of a block the extruded shape of the block with the given name, if it has one. ^/
    public static net.minecraft.client.resources.model.UnbakedModel wrap(
            String name, net.minecraft.client.resources.model.UnbakedModel model) {
        double[][] polygon = polygon(name);
        return polygon == null ? model : new Unbaked(model, polygon);
    }

    //? if neoforge {
    /^// NeoForge has no event to change loaded models, so the models of the language blocks name this loader
    // ("loader": "c0de:extruded", "c0de:block": name of the block; src/main/versioned/block-models)
    public static final net.neoforged.neoforge.client.model.UnbakedModelLoader<
                    net.minecraft.client.resources.model.UnbakedModel>
            LOADER = (json, context) -> {
                com.google.gson.JsonObject plain = json.deepCopy();
                plain.remove("loader");
                String name = net.minecraft.util.GsonHelper.getAsString(plain, "c0de:block");
                plain.remove("c0de:block");
                return wrap(name, context.deserialize(plain, net.minecraft.client.resources.model.UnbakedModel.class));
            };
    ^///?}

    /^* A block model whose geometry is extruded from the geometry of its slab model. ^/
    private record Unbaked(net.minecraft.client.resources.model.UnbakedModel wrapped, double[][] polygon)
            implements net.minecraft.client.resources.model.UnbakedModel {
        @Override
        public Boolean ambientOcclusion() {
            // Ambient occlusion is computed for the corners of block faces, not for diagonal sides
            return false;
        }

        @Override
        public net.minecraft.client.resources.model.UnbakedModel.GuiLight guiLight() {
            return wrapped.guiLight();
        }

        @Override
        public net.minecraft.client.renderer.block.model.ItemTransforms transforms() {
            return wrapped.transforms();
        }

        @Override
        public net.minecraft.client.renderer.block.model.TextureSlots.Data textureSlots() {
            return wrapped.textureSlots();
        }

        @Override
        public ResourceLocation parent() {
            return wrapped.parent();
        }

        //? if neoforge {
        /^// The render type of the JSON model (cutout for the book of PROGRAM)
        @Override
        public void fillAdditionalProperties(net.minecraft.util.context.ContextMap.Builder properties) {
            wrapped.fillAdditionalProperties(properties);
        }

        @Override
        public void resolveDependencies(net.minecraft.client.resources.model.ResolvableModel.Resolver resolver) {
            wrapped.resolveDependencies(resolver);
        }
        ^///?}

        @Override
        public net.minecraft.client.resources.model.UnbakedGeometry geometry() {
            return (textureSlots, baker, state, name) -> {
                net.minecraft.client.resources.model.UnbakedGeometry slab = wrapped.geometry() != null
                        ? wrapped.geometry()
                        : baker.getModel(wrapped.parent()).getTopGeometry();
                List<BakedQuad> slabQuads = slab.bake(textureSlots, baker, state, name).getAll();
                //? if >=26.1 {
                /^TextureAtlasSprite particle = baker.materials().resolveSlot(textureSlots, "particle", name).sprite();
                ^///?} else {
                TextureAtlasSprite particle = baker.sprites().resolveSlot(textureSlots, "particle", name);
                //?}
                ExtrudedModel model = new ExtrudedModel(face -> sprite(slabQuads, face, particle), polygon);
                var quads = new net.minecraft.client.resources.model.QuadCollection.Builder();
                model.quads.forEach(quads::addUnculledFace);
                model.bottom.forEach(quad -> quads.addCulledFace(Direction.DOWN, quad));
                return quads.build();
            };
        }
    }
    *///?} else {
    /** Wraps the models of the blocks with an extruded shape and their items; other models stay unchanged. */
    public static net.minecraft.client.resources.model.BakedModel wrap(
            net.minecraft.client.resources.model.ModelResourceLocation id,
            net.minecraft.client.resources.model.BakedModel model) {
        if (!id.id().getNamespace().equals(C0de.MOD_ID)) {
            return model;
        }
        double[][] polygon = polygon(id.id().getPath());
        return polygon == null ? model : new Baked(model, polygon);
    }

    /** A baked slab model with the quads of an extruded shape. */
    private static final class Baked implements net.minecraft.client.resources.model.BakedModel {
        private final net.minecraft.client.resources.model.BakedModel slab;
        private final ExtrudedModel shape;

        private Baked(net.minecraft.client.resources.model.BakedModel slab, double[][] polygon) {
            this.slab = slab;
            RandomSource random = RandomSource.create(0);
            List<BakedQuad> all = new ArrayList<>(slab.getQuads(null, null, random));
            for (Direction side : Direction.values()) {
                all.addAll(slab.getQuads(null, side, random));
            }
            shape = new ExtrudedModel(face -> sprite(all, face, slab.getParticleIcon()), polygon);
        }

        @Override
        public List<BakedQuad> getQuads(BlockState state, Direction side, RandomSource random) {
            if (side == null) {
                return shape.quads;
            }
            // Only the bottom lies on the border of the block, where a neighbor can hide it
            return side == Direction.DOWN ? shape.bottom : List.of();
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
        public net.minecraft.client.renderer.block.model.ItemTransforms getTransforms() {
            return slab.getTransforms();
        }

        @Override
        public net.minecraft.client.renderer.block.model.ItemOverrides getOverrides() {
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
    //?}
}
