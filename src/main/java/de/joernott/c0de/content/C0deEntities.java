package de.joernott.c0de.content;

import de.joernott.c0de.entity.CursorEntity;
import de.joernott.c0de.platform.Platform;
import java.util.function.Supplier;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

/** The entity types of the mod. */
public final class C0deEntities {
    public static Supplier<EntityType<CursorEntity>> CURSOR;

    private C0deEntities() {}

    public static void register() {
        CURSOR = Platform.INSTANCE.register(BuiltInRegistries.ENTITY_TYPE, "cursor", () -> EntityType.Builder.<
                        CursorEntity>of(CursorEntity::new, MobCategory.MISC)
                .sized(0.75F, 1.0F)
                .noSave()
                .fireImmune()
                .clientTrackingRange(10)
                .build("cursor"));
    }
}
