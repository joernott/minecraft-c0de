package de.joernott.c0de.client;

import java.util.List;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemStack;

/**
 * Drawing in screens that differs between the Minecraft versions: textures (since 1.21.11 with a render pipeline;
 * the overload with nine arguments draws between two corners there), items and tooltips (since 1.21.11 drawn
 * after the screen).
 */
public final class GuiDraw {
    private GuiDraw() {}

    /** Draws the part (u, v, width, height) of a texture of the size textureWidth × textureHeight at x, y. */
    public static void blit(
            GuiGraphics graphics,
            ResourceLocation texture,
            int x,
            int y,
            int u,
            int v,
            int width,
            int height,
            int textureWidth,
            int textureHeight) {
        //? if >=1.21.11 {
        /*graphics.blit(net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED, texture, x, y, u, v, width, height,
                textureWidth, textureHeight);
        *///?} else {
        graphics.blit(texture, x, y, u, v, width, height, textureWidth, textureHeight);
        //?}
    }

    /** Draws a whole texture of the size textureWidth × textureHeight, scaled to width × height. */
    public static void blitScaled(
            GuiGraphics graphics,
            ResourceLocation texture,
            int x,
            int y,
            int width,
            int height,
            int textureWidth,
            int textureHeight) {
        //? if >=1.21.11 {
        /*graphics.blit(net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED, texture, x, y, 0, 0, width, height,
                textureWidth, textureHeight, textureWidth, textureHeight);
        *///?} else {
        graphics.blit(texture, x, y, width, height, 0, 0, textureWidth, textureHeight, textureWidth, textureHeight);
        //?}
    }

    /** Draws an item with its count. */
    public static void item(GuiGraphics graphics, Font font, ItemStack stack, int x, int y) {
        //? if >=26.1 {
        /*graphics.item(stack, x, y);
        graphics.itemDecorations(font, stack, x, y);
        *///?} else {
        graphics.renderItem(stack, x, y);
        graphics.renderItemDecorations(font, stack, x, y);
        //?}
    }

    /** Shows the tooltip of an item. */
    public static void tooltip(GuiGraphics graphics, Font font, ItemStack stack, int x, int y) {
        //? if >=1.21.11 {
        /*graphics.setTooltipForNextFrame(font, stack, x, y);
        *///?} else {
        graphics.renderTooltip(font, stack, x, y);
        //?}
    }

    /** Shows a tooltip of text lines. */
    public static void tooltip(GuiGraphics graphics, Font font, List<FormattedCharSequence> lines, int x, int y) {
        //? if >=1.21.11 {
        /*graphics.setTooltipForNextFrame(font, lines, x, y);
        *///?} else {
        graphics.renderTooltip(font, lines, x, y);
        //?}
    }
}
