package de.joernott.c0de.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Matrix4f;
//? if >=1.21.11 {
/*import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
*///?} else {
import net.minecraft.client.renderer.RenderType;
//?}

/**
 * Where the renderers of the mod draw (task 12.1): into the buffers before 1.21.11, into the submit node collector
 * since then. The drawing code of the renderers is the same for all versions.
 */
public interface Canvas {
    /** Text in the default font, or the font of its style. */
    void text(PoseStack pose, FormattedCharSequence text, float x, float y, int color, Font.DisplayMode mode,
            int background, int light);

    /** Vertices in the given render type, positioned by the matrix of the pose. */
    void geometry(PoseStack pose, RenderType type, Geometry geometry);

    /** A whole block, as placed in the world. */
    void block(PoseStack pose, BlockState state, int light, int overlay);

    /** A block without its faces on one side (since 26.1; before, DisplayRenderer filters the quads itself). */
    default void block(
            PoseStack pose, BlockState state, net.minecraft.core.Direction hidden, int light, int overlay) {
        throw new UnsupportedOperationException("blocks without faces are submitted");
    }

    /** A rectangle behind text, at z = -0.002 in the coordinates of the pose. */
    default void textBackground(PoseStack pose, float x0, float y0, float x1, float y1, int color, int light) {
        geometry(pose, textBackground(), (last, matrix, consumer) -> {
            consumer.addVertex(matrix, x0, y0, -0.002F).setColor(color).setLight(light);
            consumer.addVertex(matrix, x0, y1, -0.002F).setColor(color).setLight(light);
            consumer.addVertex(matrix, x1, y1, -0.002F).setColor(color).setLight(light);
            consumer.addVertex(matrix, x1, y0, -0.002F).setColor(color).setLight(light);
        });
    }

    /** Draws vertices. */
    @FunctionalInterface
    interface Geometry {
        void draw(PoseStack.Pose pose, Matrix4f matrix, VertexConsumer consumer);
    }

    /** Colored quads without texture. */
    static RenderType coloredQuads() {
        //? if >=1.21.11 {
        /*return RenderTypes.debugQuads();
        *///?} else {
        return RenderType.debugQuads();
        //?}
    }

    /**
     * Colored triangles that don't write depth and are added to the colors behind them: the render type of the rays
     * of the dying ender dragon.
     */
    static RenderType overlayTriangles() {
        //? if >=1.21.11 {
        /*return RenderTypes.dragonRays();
        *///?} else {
        return RenderType.dragonRays();
        //?}
    }

    /** The backgrounds of text (before 26.3; since then the text renderer draws them). */
    static RenderType textBackground() {
        //? if >=26.3 {
        /*throw new UnsupportedOperationException("text backgrounds are submitted");
        *///?} elif >=1.21.11 {
        /*return RenderTypes.textBackground();
        *///?} else {
        return RenderType.textBackground();
        //?}
    }

    /** The render type of a block that is drawn by a renderer (DisplayRenderer, before 26.1). */
    static RenderType block(BlockState state) {
        //? if >=26.1 {
        /*throw new UnsupportedOperationException("blocks are submitted");
        *///?} elif >=1.21.11 {
        /*return net.minecraft.client.renderer.ItemBlockRenderTypes.getRenderType(state);
        *///?} else {
        return net.minecraft.client.renderer.ItemBlockRenderTypes.getRenderType(state, false);
        //?}
    }

    //? if >=26.1 {
    /*/^* The display context of the blocks that renderers draw. ^/
    net.minecraft.client.renderer.block.model.BlockDisplayContext BLOCK_CONTEXT =
            net.minecraft.client.renderer.block.model.BlockDisplayContext.create();
    *///?}

    //? if >=1.21.11 {
    /*/^* The canvas of the submit node collector. ^/
    static Canvas of(net.minecraft.client.renderer.SubmitNodeCollector collector) {
        return new Canvas() {
            @Override
            public void text(PoseStack pose, FormattedCharSequence text, float x, float y, int color,
                    Font.DisplayMode mode, int background, int light) {
                collector.submitText(pose, x, y, text, false, mode, light, color, background, 0);
            }

            @Override
            public void geometry(PoseStack pose, RenderType type, Geometry geometry) {
                collector.submitCustomGeometry(
                        pose, type, (last, consumer) -> geometry.draw(last, last.pose(), consumer));
            }

            @Override
            public void block(PoseStack pose, BlockState state, int light, int overlay) {
                //? if >=26.1 {
                /^var model = new net.minecraft.client.renderer.block.BlockModelRenderState();
                new net.minecraft.client.renderer.block.BlockModelResolver(Minecraft.getInstance().getModelManager())
                        .update(model, state, BLOCK_CONTEXT);
                model.submit(pose, collector, light, overlay, 0);
                ^///?} else {
                collector.submitBlock(pose, state, light, overlay, 0);
                //?}
            }

            //? if >=26.3 {
            /^@Override
            public void textBackground(
                    PoseStack pose, float x0, float y0, float x1, float y1, int color, int light) {
                pose.pushPose();
                pose.translate(0, 0, -0.002F);
                collector.submitTextBackground(pose, x0, y0, x1, y1, color, Font.DisplayMode.POLYGON_OFFSET, light);
                pose.popPose();
            }
            ^///?}

            //? if >=26.1 {
            /^@Override
            public void block(
                    PoseStack pose, BlockState state, net.minecraft.core.Direction hidden, int light, int overlay) {
                Minecraft minecraft = Minecraft.getInstance();
                // The tints of the block come from its render state, the parts from its model
                var tints = new net.minecraft.client.renderer.block.BlockModelRenderState();
                new net.minecraft.client.renderer.block.BlockModelResolver(minecraft.getModelManager())
                        .update(tints, state, BLOCK_CONTEXT);
                var parts = new java.util.ArrayList<net.minecraft.client.renderer.block.dispatch.BlockStateModelPart>();
                minecraft.getModelManager().getBlockStateModelSet().get(state)
                        .collectParts(net.minecraft.util.RandomSource.create(42), parts);
                boolean translucent = false;
                for (int i = 0; i < parts.size(); i++) {
                    translucent |= (parts.get(i).materialFlags() & 1) != 0;
                    parts.set(i, without(parts.get(i), hidden));
                }
                collector.submitBlockModel(
                        pose, blockSheet(translucent), parts, tints.tintLayers().toIntArray(), light, overlay, 0);
            }
            ^///?}
        };
    }
    *///?}

    //? if >=26.1 {
    /*/^* The render type of blocks drawn by renderers. ^/
    private static RenderType blockSheet(boolean translucent) {
        //? if >=26.2 {
        /^return translucent
                ? net.minecraft.client.renderer.Sheets.translucentBlockItemSheet()
                : net.minecraft.client.renderer.Sheets.cutoutBlockItemSheet();
        ^///?} else {
        return translucent
                ? net.minecraft.client.renderer.Sheets.translucentBlockSheet()
                : net.minecraft.client.renderer.Sheets.cutoutBlockSheet();
        //?}
    }

    /^* A part of a block model without its faces on one side. ^/
    private static net.minecraft.client.renderer.block.dispatch.BlockStateModelPart without(
            net.minecraft.client.renderer.block.dispatch.BlockStateModelPart part, net.minecraft.core.Direction hidden) {
        return new net.minecraft.client.renderer.block.dispatch.BlockStateModelPart() {
            @Override
            public java.util.List<net.minecraft.client.resources.model.geometry.BakedQuad> getQuads(
                    net.minecraft.core.Direction side) {
                if (side == hidden) {
                    return java.util.List.of();
                }
                var quads = part.getQuads(side);
                return side != null ? quads : quads.stream().filter(quad -> quad.direction() != hidden).toList();
            }

            @Override
            public boolean useAmbientOcclusion() {
                return part.useAmbientOcclusion();
            }

            @Override
            public net.minecraft.client.resources.model.sprite.Material.Baked particleMaterial() {
                return part.particleMaterial();
            }

            @Override
            public int materialFlags() {
                return part.materialFlags();
            }
        };
    }
    *///?}

    // Since 26.2, there are no buffers any more
    //? if <26.2 {
    /** The canvas of buffers. */
    static Canvas of(net.minecraft.client.renderer.MultiBufferSource buffers) {
        return new Canvas() {
            @Override
            public void text(PoseStack pose, FormattedCharSequence text, float x, float y, int color,
                    Font.DisplayMode mode, int background, int light) {
                Minecraft.getInstance().font.drawInBatch(
                        text, x, y, color, false, pose.last().pose(), buffers, mode, background, light);
            }

            @Override
            public void geometry(PoseStack pose, RenderType type, Geometry geometry) {
                geometry.draw(pose.last(), pose.last().pose(), buffers.getBuffer(type));
            }

            @Override
            public void block(PoseStack pose, BlockState state, int light, int overlay) {
                //? if >=1.21.11 {
                /*throw new UnsupportedOperationException("blocks are submitted");
                *///?} else {
                Minecraft.getInstance().getBlockRenderer().renderSingleBlock(state, pose, buffers, light, overlay);
                //?}
            }
        };
    }
    //?}
}
