package de.joernott.c0de.client;

import java.util.List;
import net.minecraft.client.Camera;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.MultiLineEditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;

/** The client APIs that differ between the Minecraft versions (task 12.1). */
public final class ClientCompat {
    private ClientCompat() {}

    /** A builder of a cycle button that shows the strings as they are, starting with the initial value. */
    public static CycleButton.Builder<String> cycleButton(List<String> values, String initial) {
        //? if >=1.21.11 {
        /*return CycleButton.builder(Component::literal, initial).withValues(values);
        *///?} else {
        return CycleButton.<String>builder(Component::literal).withValues(values).withInitialValue(initial);
        //?}
    }

    /** A multi-line edit box with a placeholder. */
    public static MultiLineEditBox multiLineEditBox(
            Font font, int x, int y, int width, int height, Component placeholder, Component message) {
        //? if >=1.21.6 {
        /*return MultiLineEditBox.builder()
                .setX(x)
                .setY(y)
                .setPlaceholder(placeholder)
                .build(font, width, height, message);
        *///?} else {
        return new MultiLineEditBox(font, x, y, width, height, placeholder, message);
        //?}
    }

    /** The light at a position, packed for rendering. */
    public static int light(net.minecraft.world.level.Level level, net.minecraft.core.BlockPos pos) {
        //? if >=26.2 {
        /*return net.minecraft.util.LightCoordsUtil.getLightCoords(level, pos);
        *///?} elif >=26.1 {
        /*return net.minecraft.client.renderer.LevelRenderer.getLightCoords(level, pos);
        *///?} else {
        return net.minecraft.client.renderer.LevelRenderer.getLightColor(level, pos);
        //?}
    }

    /** Opens a screen. */
    public static void setScreen(net.minecraft.client.gui.screens.Screen screen) {
        //? if >=26.2 {
        /*net.minecraft.client.Minecraft.getInstance().gui.setScreen(screen);
        *///?} else {
        net.minecraft.client.Minecraft.getInstance().setScreen(screen);
        //?}
    }

    /** The open screen, or null. */
    public static net.minecraft.client.gui.screens.Screen screen() {
        //? if >=26.2 {
        /*return net.minecraft.client.Minecraft.getInstance().gui.screen();
        *///?} else {
        return net.minecraft.client.Minecraft.getInstance().screen;
        //?}
    }

    /** Rotates the pose. */
    public static void rotate(com.mojang.blaze3d.vertex.PoseStack pose, org.joml.Quaternionf rotation) {
        //? if >=26.3 {
        /*pose.mulPose(new org.joml.Matrix4f().rotation(rotation));
        *///?} else {
        pose.mulPose(rotation);
        //?}
    }

    /** The position of the camera. */
    public static Vec3 position(Camera camera) {
        //? if >=1.21.11 {
        /*return camera.position();
        *///?} else {
        return camera.getPosition();
        //?}
    }
}
