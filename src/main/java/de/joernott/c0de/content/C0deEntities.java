package de.joernott.c0de.content;

import de.joernott.c0de.entity.CursorEntity;
import de.joernott.c0de.entity.MarkEntity;
import de.joernott.c0de.platform.Platform;
import java.util.function.Supplier;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

/** The entity types of the mod. */
public final class C0deEntities {
    public static Supplier<EntityType<CursorEntity>> CURSOR;
    public static Supplier<EntityType<MarkEntity>> MARK;

    private C0deEntities() {}

    public static void register() {
        CURSOR = Platform.INSTANCE.register(BuiltInRegistries.ENTITY_TYPE, "cursor", () -> EntityType.Builder.<
                        CursorEntity>of(CursorEntity::new, MobCategory.MISC)
                .sized(0.75F, 1.0F)
                .noSave()
                .fireImmune()
                .clientTrackingRange(10)
                //? if >=1.21.2 {
                /*.build(net.minecraft.resources.ResourceKey.create(
                        net.minecraft.core.registries.Registries.ENTITY_TYPE, de.joernott.c0de.C0de.id("cursor"))));
                *///?} else {
                .build("cursor"));
                //?}
        // As wide as the code area of a line, so that marks are drawn whenever a part of the line is visible
        MARK = Platform.INSTANCE.register(BuiltInRegistries.ENTITY_TYPE, "mark", () -> EntityType.Builder.<
                        MarkEntity>of(MarkEntity::new, MobCategory.MISC)
                .sized(24.0F, 1.0F)
                .fireImmune()
                .clientTrackingRange(10)
                //? if >=1.21.2 {
                /*.build(net.minecraft.resources.ResourceKey.create(
                        net.minecraft.core.registries.Registries.ENTITY_TYPE, de.joernott.c0de.C0de.id("mark"))));
                *///?} else {
                .build("mark"));
                //?}
    }
}
