package de.joernott.c0de.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import de.joernott.c0de.blockentity.PidBlockEntity;
import de.joernott.c0de.blockentity.TriggerBlockEntity;
import de.joernott.c0de.entity.CursorEntity;
import de.joernott.c0de.entity.MarkEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Matrix4f;

/** The renderers of running processes: the cursor entity, the PID block and camouflaged trigger blocks. */
public final class ProcessRenderers {
    private ProcessRenderers() {}

    /**
     * Shows a cursor as a cuboid that slowly pulses between dark green and lime, or between dark red and red for
     * debug cursors.
     */
    //? if >=1.21.11 {
    /*public static class Cursor extends EntityRenderer<CursorEntity, Cursor.State> {
        /^* The render state of a cursor. ^/
        public static class State extends net.minecraft.client.renderer.entity.state.EntityRenderState {
            boolean waiting;
            boolean debug;
        }

    *///?} else {
    public static class Cursor extends EntityRenderer<CursorEntity> {
    //?}
        private static final int[] REGULAR = {0x00AA00, 0x55FF55};
        private static final int[] DEBUG = {0xAA0000, 0xFF5555};
        // A waiting cursor (INPUT, WRITE) pulses between black and the dark color
        private static final int[] REGULAR_WAITING = {0x000000, 0x006600};
        private static final int[] DEBUG_WAITING = {0x000000, 0x660000};

        /** Ticks of one pulse, from the dark to the bright color and back. */
        private static final float PULSE_TICKS = 40.0F;

        /** An upright cuboid, 3/4 of a block wide and one block high, in the middle of the block. */
        private static final float HALF_WIDTH = 0.375F;

        private static final float HEIGHT = 1.0F;

        public Cursor(EntityRendererProvider.Context context) {
            super(context);
        }

        //? if >=1.21.11 {
        /*@Override
        public State createRenderState() {
            return new State();
        }

        @Override
        public void extractRenderState(CursorEntity entity, State state, float partialTick) {
            super.extractRenderState(entity, state, partialTick);
            state.waiting = entity.isWaiting();
            state.debug = entity.isDebug();
        }

        @Override
        public void submit(
                State state,
                PoseStack pose,
                net.minecraft.client.renderer.SubmitNodeCollector collector,
                net.minecraft.client.renderer.state.CameraRenderState camera) {
            draw(state.waiting, state.debug, state.ageInTicks, pose, Canvas.of(collector));
            super.submit(state, pose, collector, camera);
        }
        *///?} else {
        @Override
        public void render(
                CursorEntity entity,
                float yaw,
                float partialTick,
                PoseStack pose,
                net.minecraft.client.renderer.MultiBufferSource buffers,
                int light) {
            draw(entity.isWaiting(), entity.isDebug(), entity.tickCount + partialTick, pose, Canvas.of(buffers));
            super.render(entity, yaw, partialTick, pose, buffers, light);
        }

        @Override
        public net.minecraft.resources.ResourceLocation getTextureLocation(CursorEntity entity) {
            return net.minecraft.world.inventory.InventoryMenu.BLOCK_ATLAS;
        }
        //?}

        /** Draws the cursor; ticks is the age of the cursor in ticks. */
        private static void draw(boolean waiting, boolean debug, float ticks, PoseStack pose, Canvas canvas) {
            int[] colors = waiting ? (debug ? DEBUG_WAITING : REGULAR_WAITING) : (debug ? DEBUG : REGULAR);
            float time = ticks / PULSE_TICKS;
            float mix = 0.5F - 0.5F * (float) Math.cos(time * 2.0 * Math.PI);
            int color = blend(colors[0], colors[1], mix);
            float r = ((color >> 16) & 0xFF) / 255.0F;
            float g = ((color >> 8) & 0xFF) / 255.0F;
            float b = (color & 0xFF) / 255.0F;
            float x0 = -HALF_WIDTH;
            float x1 = HALF_WIDTH;
            float z0 = -HALF_WIDTH;
            float z1 = HALF_WIDTH;
            float y0 = 0.0F;
            float y1 = HEIGHT;
            canvas.geometry(pose, Canvas.coloredQuads(), (last, matrix, quads) -> {
                // The sides are slightly darker than the top, like the sides of blocks
                face(quads, matrix, r, g, b, x0, y1, z0, x0, y1, z1, x1, y1, z1, x1, y1, z0);
                face(quads, matrix, r * 0.5F, g * 0.5F, b * 0.5F, x0, y0, z0, x1, y0, z0, x1, y0, z1, x0, y0, z1);
                face(quads, matrix, r * 0.8F, g * 0.8F, b * 0.8F, x0, y0, z0, x0, y1, z0, x1, y1, z0, x1, y0, z0);
                face(quads, matrix, r * 0.8F, g * 0.8F, b * 0.8F, x0, y0, z1, x1, y0, z1, x1, y1, z1, x0, y1, z1);
                face(quads, matrix, r * 0.6F, g * 0.6F, b * 0.6F, x0, y0, z0, x0, y0, z1, x0, y1, z1, x0, y1, z0);
                face(quads, matrix, r * 0.6F, g * 0.6F, b * 0.6F, x1, y0, z0, x1, y1, z0, x1, y1, z1, x1, y0, z1);
            });
        }

        private static int blend(int from, int to, float mix) {
            int r = Math.round(((from >> 16) & 0xFF) + (((to >> 16) & 0xFF) - ((from >> 16) & 0xFF)) * mix);
            int g = Math.round(((from >> 8) & 0xFF) + (((to >> 8) & 0xFF) - ((from >> 8) & 0xFF)) * mix);
            int b = Math.round((from & 0xFF) + ((to & 0xFF) - (from & 0xFF)) * mix);
            return (r << 16) | (g << 8) | b;
        }

        /** A side of the cuboid, drawn from both directions, so that its winding order doesn't matter. */
        private static void face(
                VertexConsumer quads,
                Matrix4f matrix,
                float r,
                float g,
                float b,
                float... corners) {
            for (int i = 0; i < corners.length; i += 3) {
                quads.addVertex(matrix, corners[i], corners[i + 1], corners[i + 2]).setColor(r, g, b, 1.0F);
            }
            for (int i = corners.length - 3; i >= 0; i -= 3) {
                quads.addVertex(matrix, corners[i], corners[i + 1], corners[i + 2]).setColor(r, g, b, 1.0F);
            }
        }

    }

    /** Shows a marked line as a translucent yellow overlay over the code area of the line. */
    //? if >=1.21.11 {
    /*public static class Mark
            extends EntityRenderer<MarkEntity, net.minecraft.client.renderer.entity.state.EntityRenderState> {
    *///?} else {
    public static class Mark extends EntityRenderer<MarkEntity> {
    //?}
        /** Half of the width of the code area (x=5 to x=28). */
        private static final float HALF_WIDTH = 12.0F;

        private static final float INFLATE = 0.01F;

        public Mark(EntityRendererProvider.Context context) {
            super(context);
        }

        //? if >=1.21.11 {
        /*@Override
        public net.minecraft.client.renderer.entity.state.EntityRenderState createRenderState() {
            return new net.minecraft.client.renderer.entity.state.EntityRenderState();
        }

        @Override
        public void submit(
                net.minecraft.client.renderer.entity.state.EntityRenderState state,
                PoseStack pose,
                net.minecraft.client.renderer.SubmitNodeCollector collector,
                net.minecraft.client.renderer.state.CameraRenderState camera) {
            draw(pose, Canvas.of(collector));
        }
        *///?} else {
        @Override
        public void render(
                MarkEntity entity,
                float yaw,
                float partialTick,
                PoseStack pose,
                net.minecraft.client.renderer.MultiBufferSource buffers,
                int light) {
            draw(pose, Canvas.of(buffers));
        }

        @Override
        public net.minecraft.resources.ResourceLocation getTextureLocation(MarkEntity entity) {
            return net.minecraft.world.inventory.InventoryMenu.BLOCK_ATLAS;
        }
        //?}

        private static void draw(PoseStack pose, Canvas canvas) {
            // The render type of the rays of the dying ender dragon: it doesn't write depth, so the text on the blocks
            // under the overlay stays visible, and the overlay is added to the colors behind it
            float x0 = -HALF_WIDTH - INFLATE;
            float x1 = HALF_WIDTH + INFLATE;
            float y0 = -INFLATE;
            float y1 = 1.0F + INFLATE;
            float z0 = -0.5F - INFLATE;
            float z1 = 0.5F + INFLATE;
            float r = 1.0F;
            float g = 0.9F;
            float b = 0.2F;
            float a = 0.3F;
            canvas.geometry(pose, Canvas.overlayTriangles(), (last, matrix, quads) -> {
                side(quads, matrix, r, g, b, a, x0, y1, z0, x0, y1, z1, x1, y1, z1, x1, y1, z0);
                side(quads, matrix, r, g, b, a, x0, y0, z0, x0, y1, z0, x1, y1, z0, x1, y0, z0);
                side(quads, matrix, r, g, b, a, x0, y0, z1, x1, y0, z1, x1, y1, z1, x0, y1, z1);
                side(quads, matrix, r, g, b, a, x0, y0, z0, x0, y0, z1, x0, y1, z1, x0, y1, z0);
                side(quads, matrix, r, g, b, a, x1, y0, z0, x1, y1, z0, x1, y1, z1, x1, y0, z1);
            });
        }

        /**
         * A side as two triangles, drawn in both directions; back faces are culled, so each side is drawn once.
         *
         * @param corners the four corners of the side, three coordinates each
         */
        private static void side(
                VertexConsumer triangles, Matrix4f matrix, float r, float g, float b, float a, float... corners) {
            for (int[] triangle : new int[][] {{0, 1, 2}, {0, 2, 3}, {2, 1, 0}, {3, 2, 0}}) {
                for (int corner : triangle) {
                    triangles
                            .addVertex(matrix, corners[3 * corner], corners[3 * corner + 1], corners[3 * corner + 2])
                            .setColor(r, g, b, a);
                }
            }
        }

        @Override
        public boolean shouldRender(
                MarkEntity entity,
                net.minecraft.client.renderer.culling.Frustum frustum,
                double cameraX,
                double cameraY,
                double cameraZ) {
            return frustum.isVisible(new net.minecraft.world.phys.AABB(
                    entity.getX() - HALF_WIDTH,
                    entity.getY(),
                    entity.getZ() - 0.5,
                    entity.getX() + HALF_WIDTH,
                    entity.getY() + 1,
                    entity.getZ() + 0.5));
        }
    }

    /** Shows the PID in green on the north and south sides, the trigger block on the east and west sides. */
    public static class Pid extends C0deBlockEntityRenderer<PidBlockEntity> {
        private static final int PID_COLOR = 0xFF55FF55;
        private static final int TRIGGER_COLOR = 0xFFAAAAAA;

        private final Font font = Minecraft.getInstance().font;

        public Pid(BlockEntityRendererProvider.Context context) {}

        @Override
        protected void draw(
                PidBlockEntity entity, float partialTick, PoseStack pose, Canvas canvas, int light, int overlay) {
            if (entity.getLevel() == null) {
                return;
            }
            for (Direction side : Direction.Plane.HORIZONTAL) {
                boolean pid = side.getAxis() == Direction.Axis.Z;
                String[] lines = pid ? new String[] {Integer.toString(entity.pid())} : entity.trigger().split(" ", 2);
                int sideLight = LevelRenderer.getLightColor(entity.getLevel(), entity.getBlockPos().relative(side));
                int width = 1;
                for (String line : lines) {
                    width = Math.max(width, font.width(line));
                }
                float scale = Math.min(pid ? 0.05F : 0.02F, 0.8F / width);
                pose.pushPose();
                pose.translate(0.5F, 0.5F, 0.5F);
                pose.mulPose(PropertiesRenderer.rotation(side));
                pose.translate(0.0F, 0.0F, 0.501F);
                pose.scale(scale, -scale, scale);
                float y = -lines.length * font.lineHeight / 2.0F;
                for (String line : lines) {
                    canvas.text(
                            pose,
                            FormattedCharSequence.forward(line, Style.EMPTY),
                            -font.width(line) / 2.0F,
                            y,
                            pid ? PID_COLOR : TRIGGER_COLOR,
                            Font.DisplayMode.POLYGON_OFFSET,
                            0,
                            sideLight);
                    y += font.lineHeight;
                }
                pose.popPose();
            }
        }
    }

    /** Shows the block in the camouflage slot instead of the trigger block. */
    public static class Camouflage extends C0deBlockEntityRenderer<TriggerBlockEntity> {
        public Camouflage(BlockEntityRendererProvider.Context context) {}

        @Override
        protected void draw(
                TriggerBlockEntity entity, float partialTick, PoseStack pose, Canvas canvas, int light, int overlay) {
            BlockState state = entity.camouflageState();
            if (state != null) {
                canvas.block(pose, state, light, overlay);
            }
        }
    }
}
