package de.joernott.c0de.client;

import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import de.joernott.c0de.block.CodeBlock;
import de.joernott.c0de.block.ValidatedCodeBlock;
import de.joernott.c0de.content.CodeBlockType;
import de.joernott.c0de.core.sheet.SheetGeometry;
import de.joernott.c0de.dimension.C0deDimension;
import de.joernott.c0de.sheet.LevelSheet;
import java.util.List;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * The line numbers on the floor of the sheets (task 11.3): every line with content shows its number at x=2. The
 * color shows the state of the line, which the validation stores in the block states of the code blocks:
 * a faint shade of the floor for NOOP and COMMENT lines, dark green for a correct line, dark yellow for an
 * incomplete statement and dark red for a wrong line. North of the corner of every sheet (x=0, z=0), the floor
 * shows the number of the sheet, its coordinates and its owner (task 11.11.7).
 */
public final class LineNumbers {
    /** The distance from the camera in blocks up to which line numbers are drawn. */
    private static final int RANGE = 40;
    private static final int NUMBER_X = 2;

    private static final int VALID_COLOR = 0xFF00AA00;
    private static final int INCOMPLETE_COLOR = 0xFFB0A000;
    private static final int INVALID_COLOR = 0xFFAA0000;
    /** Faint shades of the floor: black on the white lines (the holes of the tractor feed), light green. */
    private static final int NEUTRAL_ON_BLACK = 0xFF2A2A2A;
    private static final int NEUTRAL_ON_GREEN = 0xFFB4CCB9;
    /** The sheet information: light gray on the black floor, lines half a block high. */
    private static final int INFO_COLOR = 0xFFC0C0C0;
    private static final float INFO_LINE = 0.5F;

    private static final MultiBufferSource.BufferSource BUFFERS =
            MultiBufferSource.immediate(new ByteBufferBuilder(4096));

    private LineNumbers() {}

    /** Draws the line numbers around the camera into buffers of their own. */
    public static void render(Camera camera) {
        renderImmediately(ClientCompat.position(camera), new PoseStack());
    }

    /**
     * Draws the line numbers around the camera into buffers of their own.
     *
     * @param pose the pose at the camera
     */
    public static void renderImmediately(Vec3 position, PoseStack pose) {
        render(position, pose, Canvas.of(BUFFERS));
        BUFFERS.endBatch();
    }

    /**
     * Draws the line numbers around the camera. Called after the entities of the level are rendered.
     *
     * @param pose the pose at the camera
     */
    public static void render(Vec3 position, PoseStack pose, Canvas canvas) {
        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel level = minecraft.level;
        if (level == null || !level.dimension().equals(C0deDimension.LEVEL)) {
            return;
        }
        int cameraX = (int) Math.floor(position.x);
        int cameraZ = (int) Math.floor(position.z);
        int firstSheet = Math.max(0, Math.floorDiv(cameraX - RANGE, SheetGeometry.SHEET_WIDTH));
        int lastSheet = Math.floorDiv(cameraX + RANGE, SheetGeometry.SHEET_WIDTH);
        Font font = minecraft.font;
        for (int sheet = firstSheet; sheet <= lastSheet; sheet++) {
            for (int z = Math.max(0, cameraZ - RANGE); z <= cameraZ + RANGE; z++) {
                int color = color(level, sheet, z);
                if (color == 0) {
                    continue;
                }
                BlockPos floor = new BlockPos(SheetGeometry.absoluteX(sheet, NUMBER_X), SheetGeometry.FLOOR_Y, z);
                String text = Integer.toString(z);
                float scale = Math.min(1 / 24.0F, 0.85F / font.width(text));
                pose.pushPose();
                pose.translate(floor.getX() + 0.5 - position.x, floor.getY() + 1.001 - position.y, floor.getZ() + 0.5
                        - position.z);
                // Lying on the floor, readable from the south like the text on the top of the code blocks
                pose.mulPose(Axis.XP.rotationDegrees(-90));
                pose.scale(scale, -scale, scale);
                canvas.text(
                        pose,
                        FormattedCharSequence.forward(text, Style.EMPTY),
                        -font.width(text) / 2.0F,
                        -font.lineHeight / 2.0F + 0.5F,
                        color,
                        Font.DisplayMode.POLYGON_OFFSET,
                        0,
                        LevelRenderer.getLightColor(level, floor.above()));
                pose.popPose();
            }
            if (cameraZ <= RANGE) {
                renderInfo(level, sheet, pose, canvas, font, position);
            }
        }
    }

    /** The number, coordinates and owner of a sheet, lying on the black floor north of its corner. */
    private static void renderInfo(
            ClientLevel level, int sheet, PoseStack pose, Canvas canvas, Font font, Vec3 position) {
        String owner = ClientSheets.owner(sheet);
        List<Component> lines = List.of(
                Component.translatable("sheet.c0de.info.sheet", sheet),
                Component.literal("x " + SheetGeometry.absoluteX(sheet, 0) + ", z 0"),
                owner == null
                        ? Component.translatable("sheet.c0de.info.nobody")
                        : Component.translatable("sheet.c0de.info.owner", owner));
        BlockPos corner = new BlockPos(SheetGeometry.absoluteX(sheet, 0), SheetGeometry.FLOOR_Y, 0);
        float scale = INFO_LINE * 0.8F / font.lineHeight;
        int light = LevelRenderer.getLightColor(level, corner.above());
        for (int i = 0; i < lines.size(); i++) {
            pose.pushPose();
            // Readable from the south; the last line ends at the north edge of the corner
            pose.translate(
                    corner.getX() + 0.1 - position.x,
                    corner.getY() + 1.001 - position.y,
                    corner.getZ() - (lines.size() - i) * INFO_LINE + 0.05 - position.z);
            pose.mulPose(Axis.XP.rotationDegrees(-90));
            pose.scale(scale, -scale, scale);
            canvas.text(
                    pose,
                    lines.get(i).getVisualOrderText(),
                    0,
                    0,
                    INFO_COLOR,
                    Font.DisplayMode.POLYGON_OFFSET,
                    0,
                    light);
            pose.popPose();
        }
    }

    /** The color of the number of a line, or 0 if the line is empty or not loaded. */
    private static int color(ClientLevel level, int sheet, int z) {
        BlockPos first = LevelSheet.codePos(sheet, SheetGeometry.CODE_MIN_X, z);
        if (!level.isLoaded(first)) {
            return 0;
        }
        CodeBlockType start = null;
        boolean anyValidated = false;
        boolean invalid = false;
        boolean incomplete = false;
        for (int x = SheetGeometry.CODE_MIN_X; x <= SheetGeometry.CODE_MAX_X; x++) {
            BlockState state = level.getBlockState(LevelSheet.codePos(sheet, x, z));
            if (!(state.getBlock() instanceof CodeBlock code)) {
                continue;
            }
            if (start == null) {
                start = code.type();
            }
            if (state.getBlock() instanceof ValidatedCodeBlock) {
                anyValidated = true;
                invalid |= !state.getValue(ValidatedCodeBlock.VALID);
                incomplete |= state.getValue(ValidatedCodeBlock.INCOMPLETE);
            }
        }
        if (start == null) {
            return 0;
        }
        if (start == CodeBlockType.NOOP || start == CodeBlockType.COMMENT) {
            return z % 2 == 0 ? NEUTRAL_ON_BLACK : NEUTRAL_ON_GREEN;
        }
        if (incomplete) {
            return INCOMPLETE_COLOR;
        }
        return invalid || !anyValidated ? INVALID_COLOR : VALID_COLOR;
    }
}
