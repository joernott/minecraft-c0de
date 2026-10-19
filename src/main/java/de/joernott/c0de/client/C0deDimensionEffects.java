package de.joernott.c0de.client;

import de.joernott.c0de.C0de;
import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;

/**
 * How the c0de dimension looks: a cloudless blue sky without sun, moon and stars. With the sky type
 * {@link SkyType#NONE} nothing is drawn into the sky and the background shows the fog color, which is
 * the blue fog color of the biome {@code c0de:greenbar}.
 */
public class C0deDimensionEffects extends DimensionSpecialEffects {
    /** The ID used as {@code effects} in the dimension type. */
    public static final ResourceLocation ID = C0de.id("c0de");

    public C0deDimensionEffects() {
        super(Float.NaN, false, SkyType.NONE, false, false);
    }

    @Override
    public Vec3 getBrightnessDependentFogColor(Vec3 fogColor, float brightness) {
        return fogColor;
    }

    @Override
    public boolean isFoggyAt(int x, int y) {
        return false;
    }
}
