package de.joernott.c0de.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * A block entity renderer of the mod that draws on a {@link Canvas} on all Minecraft versions (task 12.1): since
 * 1.21.11, block entity renderers extract a render state and submit it later; the render state of the mod keeps the
 * block entity ({@link EntityState}).
 */
//? if >=1.21.11 {
/*public abstract class C0deBlockEntityRenderer<T extends BlockEntity>
        implements BlockEntityRenderer<T, EntityState<T>> {
*///?} else {
public abstract class C0deBlockEntityRenderer<T extends BlockEntity> implements BlockEntityRenderer<T> {
//?}
    /** Draws the block entity, with the pose at the origin of its block. */
    protected abstract void draw(T entity, float partialTick, PoseStack pose, Canvas canvas, int light, int overlay);

    //? if >=1.21.11 {
    /*@Override
    public EntityState<T> createRenderState() {
        return new EntityState<>();
    }

    @Override
    public void extractRenderState(
            T entity,
            EntityState<T> state,
            float partialTick,
            net.minecraft.world.phys.Vec3 camera,
            net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(entity, state, partialTick, camera, breakProgress);
        state.entity = entity;
        state.partialTick = partialTick;
    }

    @Override
    public void submit(
            EntityState<T> state,
            PoseStack pose,
            net.minecraft.client.renderer.SubmitNodeCollector collector,
            net.minecraft.client.renderer.state.CameraRenderState camera) {
        draw(state.entity, state.partialTick, pose, Canvas.of(collector), state.lightCoords,
                net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY);
    }
    *///?} else {
    @Override
    public void render(
            T entity,
            float partialTick,
            PoseStack pose,
            net.minecraft.client.renderer.MultiBufferSource buffers,
            int light,
            int overlay) {
        draw(entity, partialTick, pose, Canvas.of(buffers), light, overlay);
    }
    //?}
}
