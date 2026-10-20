package de.joernott.c0de.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import de.joernott.c0de.blockentity.PidBlockEntity;
import de.joernott.c0de.blockentity.TriggerBlockEntity;
import de.joernott.c0de.content.C0deItems;
import de.joernott.c0de.entity.CursorEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

/** The renderers of running processes: the cursor entity, the PID block and camouflaged trigger blocks. */
public final class ProcessRenderers {
    private ProcessRenderers() {}

    /** Shows a cursor as a spinning arrow. */
    public static class Cursor extends EntityRenderer<CursorEntity> {
        private final ItemStack look = new ItemStack(C0deItems.CURSOR.get());

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
            pose.pushPose();
            pose.translate(0.0F, 0.25F, 0.0F);
            pose.mulPose(Axis.YP.rotationDegrees((entity.tickCount + partialTick) * 4.0F));
            Minecraft.getInstance()
                    .getItemRenderer()
                    .renderStatic(
                            look,
                            ItemDisplayContext.GROUND,
                            light,
                            OverlayTexture.NO_OVERLAY,
                            pose,
                            buffers,
                            entity.level(),
                            entity.getId());
            pose.popPose();
            super.render(entity, yaw, partialTick, pose, buffers, light);
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
