package de.joernott.c0de.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import de.joernott.c0de.blockentity.CommentBlockEntity;
import de.joernott.c0de.blockentity.OperatorBlockEntity;
import de.joernott.c0de.blockentity.PropertiesBlockEntity;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.joml.Quaternionf;

/**
 * Shows the properties of blocks: the name of a LABEL, the value of a LITERAL, the symbol of an OPERATOR and
 * the language of a TAG on all six sides, and the text of a COMMENT above the block while a player looks at
 * it.
 */
public class PropertiesRenderer implements BlockEntityRenderer<PropertiesBlockEntity> {
    /** The largest part of a side the text covers. */
    private static final float MAX_TEXT_SIZE = 0.8F;

    /** The largest scale of the text, so that short texts aren't huge. */
    private static final float MAX_SCALE = 0.05F;

    private static final int TEXT_COLOR = 0xFF000000;
    private static final int OPERATOR_COLOR = 0xFFAA0000;
    private static final int COMMENT_WIDTH = 160;
    private static final int COMMENT_LINES = 8;

    private final Font font;

    public PropertiesRenderer(BlockEntityRendererProvider.Context context) {
        font = context.getFont();
    }

    @Override
    public void render(
            PropertiesBlockEntity entity,
            float partialTick,
            PoseStack pose,
            MultiBufferSource buffers,
            int light,
            int overlay) {
        if (entity instanceof CommentBlockEntity comment) {
            renderComment(comment, pose, buffers);
            return;
        }
        String text = entity.displayText();
        if (text.isEmpty() || entity.getLevel() == null) {
            return;
        }
        int color = entity instanceof OperatorBlockEntity ? OPERATOR_COLOR : TEXT_COLOR;
        float width = font.width(text);
        float scale = Math.min(MAX_SCALE, MAX_TEXT_SIZE / Math.max(width, font.lineHeight));
        for (Direction side : Direction.values()) {
            int sideLight = LevelRenderer.getLightColor(entity.getLevel(), entity.getBlockPos().relative(side));
            pose.pushPose();
            pose.translate(0.5F, 0.5F, 0.5F);
            pose.mulPose(rotation(side));
            // The text is drawn slightly in front of the south side of the rotated block, facing outwards
            pose.translate(0.0F, 0.0F, 0.501F);
            pose.scale(scale, -scale, scale);
            font.drawInBatch(
                    text,
                    -width / 2,
                    -font.lineHeight / 2.0F + 0.5F,
                    color,
                    false,
                    pose.last().pose(),
                    buffers,
                    Font.DisplayMode.POLYGON_OFFSET,
                    0,
                    sideLight);
            pose.popPose();
        }
    }

    /** The rotation that turns the south side of a block into the given side. */
    private static Quaternionf rotation(Direction side) {
        return switch (side) {
            case SOUTH -> Axis.YP.rotationDegrees(0);
            case NORTH -> Axis.YP.rotationDegrees(180);
            case EAST -> Axis.YP.rotationDegrees(90);
            case WEST -> Axis.YP.rotationDegrees(-90);
            case UP -> Axis.XP.rotationDegrees(-90);
            case DOWN -> Axis.XP.rotationDegrees(90);
        };
    }

    /** Shows the text of a comment above the block, like the name of an entity, while a player looks at it. */
    private void renderComment(CommentBlockEntity comment, PoseStack pose, MultiBufferSource buffers) {
        Minecraft minecraft = Minecraft.getInstance();
        if (comment.text().isEmpty()
                || !(minecraft.hitResult instanceof BlockHitResult hit)
                || hit.getType() != HitResult.Type.BLOCK
                || !hit.getBlockPos().equals(comment.getBlockPos())) {
            return;
        }
        List<FormattedCharSequence> lines = font.split(Component.literal(comment.text()), COMMENT_WIDTH);
        if (lines.size() > COMMENT_LINES) {
            lines = lines.subList(0, COMMENT_LINES);
        }
        float scale = 0.025F;
        pose.pushPose();
        pose.translate(0.5F, 1.25F + lines.size() * font.lineHeight * scale, 0.5F);
        pose.mulPose(minecraft.gameRenderer.getMainCamera().rotation());
        pose.scale(scale, -scale, scale);
        int background = (int) (minecraft.options.getBackgroundOpacity(0.25F) * 255.0F) << 24;
        for (int i = 0; i < lines.size(); i++) {
            FormattedCharSequence line = lines.get(i);
            font.drawInBatch(
                    line,
                    -font.width(line) / 2.0F,
                    i * font.lineHeight,
                    0xFFFFFFFF,
                    false,
                    pose.last().pose(),
                    buffers,
                    Font.DisplayMode.NORMAL,
                    background,
                    LightTexture.FULL_BRIGHT);
        }
        pose.popPose();
    }
}
