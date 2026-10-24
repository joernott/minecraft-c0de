package de.joernott.c0de.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import de.joernott.c0de.C0de;
import de.joernott.c0de.core.io.TextScreen;
import de.joernott.c0de.io.DisplayBlock;
import de.joernott.c0de.io.DisplayBlockEntity;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Matrix4f;

/**
 * Draws display blocks (docs/concept/19-input-output.md): the camouflage, and on the block at the top left of a
 * wall the screen of the whole wall, in the monospace font Monocraft, 16 columns and 8 rows per block. Screens
 * glow, so they are drawn fully lit.
 */
public class DisplayRenderer implements BlockEntityRenderer<DisplayBlockEntity> {
    /** The font of the screens (assets/c0de/font/monocraft.json). */
    public static final ResourceLocation FONT = C0de.id("monocraft");
    public static final Style STYLE = Style.EMPTY.withFont(FONT);

    private final Font font;

    public DisplayRenderer(BlockEntityRendererProvider.Context context) {
        font = context.getFont();
    }

    /** The color of a color number 0 to 15, the colors of Minecraft text. */
    public static int color(int number) {
        ChatFormatting formatting = ChatFormatting.getById(Math.max(0, Math.min(15, number)));
        Integer rgb = formatting == null ? null : formatting.getColor();
        return 0xFF000000 | (rgb == null ? 0xFFFFFF : rgb);
    }

    @Override
    public void render(
            DisplayBlockEntity entity, float partialTick, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
        BlockState camouflage = entity.camouflageState();
        if (camouflage != null) {
            renderCamouflage(camouflage, entity.getBlockState().getValue(DisplayBlock.FACING), pose, buffers, light, overlay);
        }
        TextScreen screen = entity.screen();
        if (!entity.isOrigin() || screen == null) {
            return;
        }
        Direction facing = entity.getBlockState().getValue(DisplayBlock.FACING);
        pose.pushPose();
        pose.translate(0.5F, 0.5F, 0.5F);
        pose.mulPose(PropertiesRenderer.rotation(facing));
        // The top left corner of the wall, just in front of the faces of the blocks
        pose.translate(-0.5F - entity.offsetRight(), 0.5F + entity.offsetDown(), 0.505F);
        draw(screen, entity.wallWidth(), entity.wallHeight(), pose, buffers, font);
        pose.popPose();
    }

    /**
     * Draws the camouflage block without its face on the front of the display, so that it doesn't flicker with the
     * front from far away.
     */
    private static void renderCamouflage(
            BlockState state, Direction front, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
        Minecraft minecraft = Minecraft.getInstance();
        BakedModel model = minecraft.getBlockRenderer().getBlockModel(state);
        VertexConsumer consumer = buffers.getBuffer(ItemBlockRenderTypes.getRenderType(state, false));
        RandomSource random = RandomSource.create(42);
        List<BakedQuad> quads = new ArrayList<>();
        for (Direction side : Direction.values()) {
            if (side != front) {
                random.setSeed(42);
                quads.addAll(model.getQuads(state, side, random));
            }
        }
        random.setSeed(42);
        model.getQuads(state, null, random).stream()
                .filter(quad -> quad.getDirection() != front)
                .forEach(quads::add);
        for (BakedQuad quad : quads) {
            int color = quad.isTinted() ? minecraft.getBlockColors().getColor(state, null, null, quad.getTintIndex()) : -1;
            float red = (color >> 16 & 0xFF) / 255.0F;
            float green = (color >> 8 & 0xFF) / 255.0F;
            float blue = (color & 0xFF) / 255.0F;
            consumer.putBulkData(pose.last(), quad, red, green, blue, 1.0F, light, overlay);
        }
    }

    /**
     * Draws a screen into a rectangle of the given size (in blocks), from its top left corner at the origin of the
     * pose, with x to the right and y up. Used by the displays and the terminals.
     */
    public static void draw(
            TextScreen screen, float width, float height, PoseStack pose, MultiBufferSource buffers, Font font) {
        float cellWidth = width / screen.width();
        float cellHeight = height / screen.height();
        float advance = Math.max(1, font.width(Component.literal("M").withStyle(STYLE)));
        int light = LightTexture.FULL_BRIGHT;
        VertexConsumer background = buffers.getBuffer(RenderType.textBackground());
        Matrix4f matrix = pose.last().pose();
        for (int r = 0; r < screen.height(); r++) {
            TextScreen.Row row = screen.rows().get(r);
            float top = -r * cellHeight;
            int start = 0;
            while (start < screen.width()) {
                int foreground = row.foregrounds()[start];
                int back = row.backgrounds()[start];
                int end = start + 1;
                while (end < screen.width() && row.foregrounds()[end] == foreground && row.backgrounds()[end] == back) {
                    end++;
                }
                if (back != 0) {
                    float x0 = start * cellWidth;
                    float x1 = end * cellWidth;
                    int argb = color(back);
                    background.addVertex(matrix, x0, top, -0.002F).setColor(argb).setLight(light);
                    background.addVertex(matrix, x0, top - cellHeight, -0.002F).setColor(argb).setLight(light);
                    background.addVertex(matrix, x1, top - cellHeight, -0.002F).setColor(argb).setLight(light);
                    background.addVertex(matrix, x1, top, -0.002F).setColor(argb).setLight(light);
                }
                String text = new String(row.characters(), start, end - start);
                if (!text.isBlank()) {
                    pose.pushPose();
                    pose.translate(start * cellWidth, top, 0);
                    pose.scale(cellWidth / advance, -cellHeight / font.lineHeight, 1);
                    font.drawInBatch(
                            Component.literal(text).withStyle(STYLE),
                            0,
                            0.5F,
                            color(foreground),
                            false,
                            pose.last().pose(),
                            buffers,
                            Font.DisplayMode.POLYGON_OFFSET,
                            0,
                            light);
                    pose.popPose();
                }
                start = end;
            }
        }
    }

    /** Walls are larger than their origin block, so they are drawn even if the origin is out of sight. */
    @Override
    public boolean shouldRenderOffScreen(DisplayBlockEntity entity) {
        return entity.wallWidth() > 1 || entity.wallHeight() > 1;
    }

    @Override
    public int getViewDistance() {
        return 96;
    }

    //? if neoforge {
    /*// NeoForge culls block entities by this box; the screen of a wall covers all of its blocks
    @Override
    public net.minecraft.world.phys.AABB getRenderBoundingBox(DisplayBlockEntity entity) {
        int reach = Math.max(entity.wallWidth(), entity.wallHeight());
        return new net.minecraft.world.phys.AABB(entity.getBlockPos()).inflate(reach);
    }
    *///?}
}
