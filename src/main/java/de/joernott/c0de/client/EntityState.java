package de.joernott.c0de.client;

//? if >=1.21.11 {
/*import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.world.level.block.entity.BlockEntity;

/^*
 * The render state of the block entity renderers of the mod since 1.21.11 (task 12.1): the block entity itself,
 * which the renderers draw from when they submit, right after the state was extracted.
 ^/
public class EntityState<T extends BlockEntity> extends BlockEntityRenderState {
    public T entity;
    public float partialTick;
}
*///?} else {
/** Only used since 1.21.11. */
final class EntityState {
    private EntityState() {}
}
//?}
