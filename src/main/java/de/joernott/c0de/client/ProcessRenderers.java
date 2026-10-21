package de.joernott.c0de.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import de.joernott.c0de.blockentity.PidBlockEntity;
import de.joernott.c0de.blockentity.TriggerBlockEntity;
import de.joernott.c0de.entity.CursorEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Matrix4f;

/** The renderers of running processes: the cursor entity, the PID block and camouflaged trigger blocks. */
public final class ProcessRenderers {
    private ProcessRenderers() {}

    /**
     * Shows a cursor as a cuboid that slowly pulses between dark green and lime, or between dark red and red for
     * debug cursors.
     */
    public static class Cursor extends EntityRenderer<CursorEntity> {
        private static final int[] REGULAR = {0x00AA00, 0x55FF55};
        private static final int[] DEBUG = {0xAA0000, 0xFF5555};

        /** Ticks of one pulse, from the dark to the bright color and back. */
        private static final float PULSE_TICKS = 40.0F;

        private static final float HALF_WIDTH = 0.4F;
        private static final float HEIGHT = 0.4F;

        public Cursor(EntityRendererProvider.Context context) {
            super(context);
        }

        @Override
        public void render(
                CursorEntity entity,
                float yaw,
                float partialTick,
                PoseStack pose,
                MultiBufferSource buffers,
                int light) {
            int[] colors = entity.isDebug() ? DEBUG : REGULAR;
            float time = (entity.tickCount + partialTick) / PULSE_TICKS;
            float mix = 0.5F - 0.5F * (float) Math.cos(time * 2.0 * Math.PI);
            int color = blend(colors[0], colors[1], mix);
            float r = ((color >> 16) & 0xFF) / 255.0F;
            float g = ((color >> 8) & 0xFF) / 255.0F;
            float b = (color & 0xFF) / 255.0F;
            VertexConsumer quads = buffers.getBuffer(RenderType.debugQuads());
            Matrix4f matrix = pose.last().pose();
            float x0 = -HALF_WIDTH;
            float x1 = HALF_WIDTH;
            float z0 = -HALF_WIDTH;
            float z1 = HALF_WIDTH;
            float y0 = 0.0F;
            float y1 = HEIGHT;
            // The sides are slightly darker than the top, like the sides of blocks
            face(quads, matrix, r, g, b, x0, y1, z0, x0, y1, z1, x1, y1, z1, x1, y1, z0);
            face(quads, matrix, r * 0.5F, g * 0.5F, b * 0.5F, x0, y0, z0, x1, y0, z0, x1, y0, z1, x0, y0, z1);
            face(quads, matrix, r * 0.8F, g * 0.8F, b * 0.8F, x0, y0, z0, x0, y1, z0, x1, y1, z0, x1, y0, z0);
            face(quads, matrix, r * 0.8F, g * 0.8F, b * 0.8F, x0, y0, z1, x1, y0, z1, x1, y1, z1, x0, y1, z1);
            face(quads, matrix, r * 0.6F, g * 0.6F, b * 0.6F, x0, y0, z0, x0, y0, z1, x0, y1, z1, x0, y1, z0);
            face(quads, matrix, r * 0.6F, g * 0.6F, b * 0.6F, x1, y0, z0, x1, y1, z0, x1, y1, z1, x1, y0, z1);
            super.render(entity, yaw, partialTick, pose, buffers, light);
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

        @Override
        public ResourceLocation getTextureLocation(CursorEntity entity) {
            return InventoryMenu.BLOCK_ATLAS;
        }
    }

    /** Shows the PID in green on the north and south sides, the trigger block on the east and west sides. */
    public static class Pid implements BlockEntityRenderer<PidBlockEntity> {
        private static final int PID_COLOR = 0xFF55FF55;
        private static final int TRIGGER_COLOR = 0xFFAAAAAA;

        private final Font font;

        public Pid(BlockEntityRendererProvider.Context context) {
            font = context.getFont();
        }

        @Override
        public void render(
                PidBlockEntity entity,
                float partialTick,
                PoseStack pose,
                MultiBufferSource buffers,
                int light,
                int overlay) {
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
                    font.drawInBatch(
                            line,
                            -font.width(line) / 2.0F,
                            y,
                            pid ? PID_COLOR : TRIGGER_COLOR,
                            false,
                            pose.last().pose(),
                            buffers,
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
    public static class Camouflage implements BlockEntityRenderer<TriggerBlockEntity> {
        public Camouflage(BlockEntityRendererProvider.Context context) {}

        @Override
        public void render(
                TriggerBlockEntity entity,
                float partialTick,
                PoseStack pose,
                MultiBufferSource buffers,
                int light,
                int overlay) {
            BlockState state = entity.camouflageState();
            if (state != null) {
                Minecraft.getInstance().getBlockRenderer().renderSingleBlock(state, pose, buffers, light, overlay);
            }
        }
    }
}
