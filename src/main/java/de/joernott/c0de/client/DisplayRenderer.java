package de.joernott.c0de.client;

import com.mojang.blaze3d.vertex.PoseStack;
import de.joernott.c0de.C0de;
import de.joernott.c0de.core.io.TextScreen;
import de.joernott.c0de.io.DisplayBlock;
import de.joernott.c0de.io.DisplayBlockEntity;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Draws display blocks (docs/concept/19-input-output.md): the camouflage, and on the block at the top left of a
 * wall the screen of the whole wall, in the monospace font Monocraft, 16 columns and 8 rows per block. Screens
 * glow, so they are drawn fully lit.
 */
public class DisplayRenderer extends C0deBlockEntityRenderer<DisplayBlockEntity> {
    /** The font of the screens (assets/c0de/font/monocraft.json). */
    public static final ResourceLocation FONT = C0de.id("monocraft");
    //? if >=1.21.11 {
    /*public static final Style STYLE =
            Style.EMPTY.withFont(new net.minecraft.network.chat.FontDescription.Resource(FONT));
    *///?} else {
    public static final Style STYLE = Style.EMPTY.withFont(FONT);
    //?}

    public DisplayRenderer(BlockEntityRendererProvider.Context context) {}

    /** The color of a color number 0 to 15, the colors of Minecraft text. */
    public static int color(int number) {
        return 0xFF000000 | COLORS[Math.max(0, Math.min(15, number))];
    }

    /** The colors of the formatting codes 0 to f of Minecraft. */
    private static final int[] COLORS = {
        0x000000, 0x0000AA, 0x00AA00, 0x00AAAA, 0xAA0000, 0xAA00AA, 0xFFAA00, 0xAAAAAA,
        0x555555, 0x5555FF, 0x55FF55, 0x55FFFF, 0xFF5555, 0xFF55FF, 0xFFFF55, 0xFFFFFF
    };

    @Override
    protected void draw(
            DisplayBlockEntity entity, float partialTick, PoseStack pose, Canvas canvas, int light, int overlay) {
        BlockState camouflage = entity.camouflageState();
        if (camouflage != null) {
            renderCamouflage(camouflage, entity.getBlockState().getValue(DisplayBlock.FACING), pose, canvas, light,
                    overlay);
        }
        TextScreen screen = entity.screen();
        if (!entity.isOrigin() || screen == null) {
            return;
        }
        Direction facing = entity.getBlockState().getValue(DisplayBlock.FACING);
        pose.pushPose();
        pose.translate(0.5F, 0.5F, 0.5F);
        ClientCompat.rotate(pose, PropertiesRenderer.rotation(facing));
        // The top left corner of the wall, just in front of the faces of the blocks
        pose.translate(-0.5F - entity.offsetRight(), 0.5F + entity.offsetDown(), 0.505F);
        draw(screen, entity.wallWidth(), entity.wallHeight(), pose, canvas, Minecraft.getInstance().font);
        pose.popPose();
    }

    /**
     * Draws the camouflage block without its face on the front of the display, so that it doesn't flicker with the
     * front from far away.
     */
    private static void renderCamouflage(
            BlockState state, Direction front, PoseStack pose, Canvas canvas, int light, int overlay) {
        //? if >=26.1 {
        /*// The quads of the block models can't be filtered any more: the camouflage is slightly smaller instead, so
        // that the front of the display stays in front of it
        pose.pushPose();
        pose.translate(0.5F, 0.5F, 0.5F);
        pose.scale(0.995F, 0.995F, 0.995F);
        pose.translate(-0.5F, -0.5F, -0.5F);
        canvas.block(pose, state, light, overlay);
        pose.popPose();
        *///?} else {
        Minecraft minecraft = Minecraft.getInstance();
        RandomSource random = RandomSource.create(42);
        List<BakedQuad> quads = new ArrayList<>();
        //? if >=1.21.11 {
        /*for (var part : minecraft.getBlockRenderer().getBlockModel(state).collectParts(random)) {
            for (Direction side : Direction.values()) {
                if (side != front) {
                    quads.addAll(part.getQuads(side));
                }
            }
            part.getQuads(null).stream().filter(quad -> quad.direction() != front).forEach(quads::add);
        }
        *///?} else {
        net.minecraft.client.resources.model.BakedModel model = minecraft.getBlockRenderer().getBlockModel(state);
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
        //?}
        canvas.geometry(pose, Canvas.block(state), (last, matrix, consumer) -> {
            for (BakedQuad quad : quads) {
                //? if >=1.21.11 {
                /*int tint = quad.tintIndex();
                *///?} else {
                int tint = quad.getTintIndex();
                //?}
                int color = quad.isTinted() ? minecraft.getBlockColors().getColor(state, null, null, tint) : -1;
                float red = (color >> 16 & 0xFF) / 255.0F;
                float green = (color >> 8 & 0xFF) / 255.0F;
                float blue = (color & 0xFF) / 255.0F;
                consumer.putBulkData(last, quad, red, green, blue, 1.0F, light, overlay);
            }
        });
        //?}
    }

    /**
     * Draws a screen into a rectangle of the given size (in blocks), from its top left corner at the origin of the
     * pose, with x to the right and y up.
     */
    public static void draw(TextScreen screen, float width, float height, PoseStack pose, Canvas canvas, Font font) {
        float cellWidth = width / screen.width();
        float cellHeight = height / screen.height();
        float advance = Math.max(1, font.width(Component.literal("M").withStyle(STYLE)));
        int light = LightTexture.FULL_BRIGHT;
        canvas.geometry(pose, Canvas.textBackground(), (last, matrix, background) -> {
            for (int r = 0; r < screen.height(); r++) {
                TextScreen.Row row = screen.rows().get(r);
                float top = -r * cellHeight;
                forEachRun(screen, row, (start, end) -> {
                    int back = row.backgrounds()[start];
                    if (back != 0) {
                        float x0 = start * cellWidth;
                        float x1 = end * cellWidth;
                        int argb = color(back);
                        background.addVertex(matrix, x0, top, -0.002F).setColor(argb).setLight(light);
                        background.addVertex(matrix, x0, top - cellHeight, -0.002F).setColor(argb).setLight(light);
                        background.addVertex(matrix, x1, top - cellHeight, -0.002F).setColor(argb).setLight(light);
                        background.addVertex(matrix, x1, top, -0.002F).setColor(argb).setLight(light);
                    }
                });
            }
        });
        for (int r = 0; r < screen.height(); r++) {
            TextScreen.Row row = screen.rows().get(r);
            float top = -r * cellHeight;
            forEachRun(screen, row, (start, end) -> {
                String text = new String(row.characters(), start, end - start);
                if (!text.isBlank()) {
                    pose.pushPose();
                    pose.translate(start * cellWidth, top, 0);
                    pose.scale(cellWidth / advance, -cellHeight / font.lineHeight, 1);
                    canvas.text(
                            pose,
                            Component.literal(text).withStyle(STYLE).getVisualOrderText(),
                            0,
                            0.5F,
                            color(row.foregrounds()[start]),
                            Font.DisplayMode.POLYGON_OFFSET,
                            0,
                            light);
                    pose.popPose();
                }
            });
        }
    }

    /** Calls the action for every run of characters of a row with the same colors: start inclusive, end exclusive. */
    private static void forEachRun(TextScreen screen, TextScreen.Row row, Run run) {
        int start = 0;
        while (start < screen.width()) {
            int foreground = row.foregrounds()[start];
            int back = row.backgrounds()[start];
            int end = start + 1;
            while (end < screen.width() && row.foregrounds()[end] == foreground && row.backgrounds()[end] == back) {
                end++;
            }
            run.accept(start, end);
            start = end;
        }
    }

    /** A run of characters with the same colors. */
    @FunctionalInterface
    private interface Run {
        void accept(int start, int end);
    }

    //? if >=1.21.11 {
    /*// The wall of a display can be larger than its origin block
    @Override
    public boolean shouldRenderOffScreen() {
        return true;
    }
    *///?} else {
    /** Walls are larger than their origin block, so they are drawn even if the origin is out of sight. */
    @Override
    public boolean shouldRenderOffScreen(DisplayBlockEntity entity) {
        return entity.wallWidth() > 1 || entity.wallHeight() > 1;
    }
    //?}

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
