package de.joernott.c0de.core.library.minecraft;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import de.joernott.c0de.core.error.C0deException;
import de.joernott.c0de.core.error.ErrorCode;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

/** The filter of MOVE_ITEMS (task 16.12). */
class ItemFilterTest {
    private record TestItem(String id, String name, Collection<String> tags) implements ItemFilter.Item {}

    private static final TestItem OAK_LOG =
            new TestItem("minecraft:oak_log", "Oak Log", Set.of("minecraft:logs", "minecraft:logs_that_burn"));
    private static final TestItem RAW_IRON = new TestItem("minecraft:raw_iron", "Raw Iron", Set.of("c:raw_materials"));
    private static final TestItem IRON_ORE =
            new TestItem("minecraft:iron_ore", "Iron Ore", Set.of("minecraft:iron_ores"));
    private static final TestItem RED_WOOL = new TestItem("minecraft:red_wool", "Red Wool", Set.of("minecraft:wool"));
    private static final TestItem COPPER =
            new TestItem("create:copper_casing", "Copper Casing", Set.of("create:casing"));
    private static final TestItem RENAMED = new TestItem("minecraft:diamond", "Treasure", Set.of());
    private static final List<TestItem> ITEMS = List.of(OAK_LOG, RAW_IRON, IRON_ORE, RED_WOOL, COPPER, RENAMED);

    private static Set<String> matching(String... filters) {
        ItemFilter filter = ItemFilter.of(
                List.of(filters),
                id -> ITEMS.stream().anyMatch(item -> item.id().equals(id)));
        return ITEMS.stream().filter(filter).map(TestItem::id).collect(Collectors.toSet());
    }

    private static Set<String> ids(TestItem... items) {
        return java.util.Arrays.stream(items).map(TestItem::id).collect(Collectors.toSet());
    }

    @Test
    void emptyFiltersMatchEverything() {
        assertEquals(ids(ITEMS.toArray(TestItem[]::new)), matching());
        assertEquals(ids(ITEMS.toArray(TestItem[]::new)), matching(""));
        assertEquals(ids(ITEMS.toArray(TestItem[]::new)), matching("iron", " "));
    }

    @Test
    void itemIdsMatchExactly() {
        assertEquals(ids(RAW_IRON), matching("minecraft:raw_iron"));
        assertEquals(ids(RAW_IRON, OAK_LOG), matching("minecraft:raw_iron", "minecraft:oak_log"));
        C0deException e = assertThrows(C0deException.class, () -> matching("minecraft:no_such_item"));
        assertEquals(ErrorCode.INVALID_ARGUMENT, e.code());
    }

    @Test
    void wordsSearchNamesModsTagsAndIds() {
        assertEquals(ids(RAW_IRON, IRON_ORE), matching("iron"));
        assertEquals(ids(RAW_IRON, IRON_ORE), matching("IRON"));
        assertEquals(ids(COPPER), matching("@create"));
        assertEquals(ids(OAK_LOG), matching("$logs"));
        assertEquals(ids(IRON_ORE), matching("$ores"));
        assertEquals(ids(RAW_IRON), matching("&raw_"));
        // The custom name, not the item
        assertEquals(ids(RENAMED), matching("treasure"));
        assertEquals(ids(), matching("diamond"));
    }

    @Test
    void wordsAndAlternativesCombine() {
        assertEquals(ids(IRON_ORE), matching("iron ore"));
        assertEquals(ids(), matching("\"iron raw\""));
        assertEquals(ids(RED_WOOL), matching("\"red wool\""));
        assertEquals(ids(IRON_ORE, RAW_IRON), matching("$ores | &raw_"));
        assertEquals(ids(RAW_IRON), matching("iron -ore"));
        assertEquals(ids(COPPER), matching("-@minecraft"));
    }

    @Test
    void invalidExpressions() {
        for (String filter : new String[] {"#tooltip", "%building", "^red", "iron |", "@", "\"open"}) {
            C0deException e = assertThrows(C0deException.class, () -> matching(filter), filter);
            assertEquals(ErrorCode.INVALID_ARGUMENT, e.code(), filter);
        }
    }
}
