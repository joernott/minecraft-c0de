package de.joernott.c0de.client;

import de.joernott.c0de.C0de;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
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
 * The model of the BEGIN and END blocks: a cube whose west and east sides are moved inwards at one end, so that
 * the block is a trapezoid seen from above, with real diagonals. BEGIN is narrow in the north and wide in the
 * south, where its code block follows; END is wide in the north and narrow in the south.
 *
 * <p>The baked cube model of the block is wrapped and the vertices of its quads are moved. The textures of the
 * top, bottom, north and south faces are cut along the diagonals instead of being squeezed. The outline and
 * collision shapes in {@link de.joernott.c0de.block.CodeBlockShapes} approximate the trapezoid.
 */
public final class TrapezoidModel implements BakedModel {
    /** How far the narrow end is moved inwards on each side, in blocks. */
    public static final float INSET = 0.25F;

    /** The ints of a vertex in the block vertex format: position (3), color, texture (2), light, normal. */
    private static final int STRIDE = 8;

    private final BakedModel cube;
    private final boolean narrowInNorth;
    /** The quads by the side that hides them, computed once: chunks are built by several threads. */
    private final Map<Direction, List<BakedQuad>> sides = new EnumMap<>(Direction.class);
    private final List<BakedQuad> unculled;

    private TrapezoidModel(BakedModel cube, boolean narrowInNorth) {
        this.cube = cube;
        this.narrowInNorth = narrowInNorth;
        // The cube is a plain block model, its quads don't depend on the block state or the random source
        RandomSource random = RandomSource.create(0);
        List<BakedQuad> quads = new ArrayList<>(transform(cube.getQuads(null, null, random)));
        for (Direction side : Direction.values()) {
            List<BakedQuad> moved = transform(cube.getQuads(null, side, random));
            if (side.getAxis() == Direction.Axis.X) {
                // The diagonal sides are not on the border of the block any more, so neighbors don't hide them
                quads.addAll(moved);
            } else {
                sides.put(side, moved);
            }
        }
        unculled = List.copyOf(quads);
    }

    /** Wraps the models of the BEGIN and END blocks and their items; other models are returned unchanged. */
    public static BakedModel wrap(ModelResourceLocation id, BakedModel model) {
        if (!id.id().getNamespace().equals(C0de.MOD_ID)) {
            return model;
        }
        return switch (id.id().getPath()) {
            case "begin_block" -> new TrapezoidModel(model, true);
            case "end_block" -> new TrapezoidModel(model, false);
            default -> model;
        };
    }

    /** How far the sides are moved inwards at a z coordinate (0 = north, 1 = south of the block). */
    private float inset(float z) {
        return INSET * (narrowInNorth ? 1 - z : z);
    }

    @Override
    public List<BakedQuad> getQuads(BlockState state, Direction side, RandomSource random) {
        return side == null ? unculled : sides.getOrDefault(side, List.of());
    }

    private List<BakedQuad> transform(List<BakedQuad> quads) {
        List<BakedQuad> result = new ArrayList<>(quads.size());
        for (BakedQuad quad : quads) {
            result.add(transform(quad));
        }
        return List.copyOf(result);
    }

    /**
     * Moves the vertices of a quad: x is scaled into the trapezoid at the z coordinate of the vertex. On the faces
     * whose texture runs along x (top, bottom, north and south), u follows the new x, so the texture is cut.
     */
    private BakedQuad transform(BakedQuad quad) {
        int[] data = quad.getVertices().clone();
        Direction face = quad.getDirection();
        boolean cut = face.getAxis() != Direction.Axis.X;
        // u as a linear function of x, from two vertices with different x
        float a = 0;
        float b = 0;
        if (cut) {
            for (int i = 1; i < 4; i++) {
                float x0 = x(data, 0);
                float xi = x(data, i);
                if (Math.abs(xi - x0) > 1.0E-4F) {
                    b = (u(data, i) - u(data, 0)) / (xi - x0);
                    a = u(data, 0) - b * x0;
                    break;
                }
            }
        }
        for (int i = 0; i < 4; i++) {
            float x = x(data, i);
            float inset = inset(data, i);
            float moved = inset + x * (1 - 2 * inset);
            data[i * STRIDE] = Float.floatToRawIntBits(moved);
            if (cut && b != 0) {
                data[i * STRIDE + 4] = Float.floatToRawIntBits(a + b * moved);
            }
        }
        return new BakedQuad(data, quad.getTintIndex(), face, quad.getSprite(), quad.isShade());
    }

    private float inset(int[] data, int vertex) {
        return inset(Float.intBitsToFloat(data[vertex * STRIDE + 2]));
    }

    private static float x(int[] data, int vertex) {
        return Float.intBitsToFloat(data[vertex * STRIDE]);
    }

    private static float u(int[] data, int vertex) {
        return Float.intBitsToFloat(data[vertex * STRIDE + 4]);
    }

    @Override
    public boolean useAmbientOcclusion() {
        return cube.useAmbientOcclusion();
    }

    @Override
    public boolean isGui3d() {
        return cube.isGui3d();
    }

    @Override
    public boolean usesBlockLight() {
        return cube.usesBlockLight();
    }

    @Override
    public boolean isCustomRenderer() {
        return false;
    }

    @Override
    public TextureAtlasSprite getParticleIcon() {
        return cube.getParticleIcon();
    }

    @Override
    public ItemTransforms getTransforms() {
        return cube.getTransforms();
    }

    @Override
    public ItemOverrides getOverrides() {
        return cube.getOverrides();
    }
}
