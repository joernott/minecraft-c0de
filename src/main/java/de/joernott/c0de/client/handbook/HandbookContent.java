package de.joernott.c0de.client.handbook;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.joernott.c0de.C0de;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;

/**
 * The content of a handbook (docs/concept/21-handbooks.md), read from the resources of the client: the book
 * {@code handbook/books/<book>.json}, its categories {@code handbook/categories/<book>/<category>.md} and its
 * entries {@code handbook/entries/<book>/<category>/<entry>.md}, with translations in a folder named after the
 * language, and the recipe grids {@code handbook/recipes.json}. It is read whenever a book is opened, so changed
 * resource packs take effect without a reload listener.
 */
public final class HandbookContent {
    private static final String ROOT = "handbook";
    private static final String FALLBACK = "en_us";
    private static final Pattern LANGUAGE = Pattern.compile("[a-z]{2,3}_[a-z]{2,4}");

    /** A book with its categories in their order and its entries by ID ({@code <category>/<entry>}). */
    public record Book(
            String id,
            String name,
            String landing,
            ResourceLocation texture,
            List<Category> categories,
            Map<String, Entry> entries,
            Map<String, Recipe> recipes) {

        /** The entry of a link: an entry ID, or just the name of the entry if it is unique in the book. */
        public Optional<Entry> entry(String link) {
            Entry exact = entries.get(link);
            if (exact != null) {
                return Optional.of(exact);
            }
            return entries.values().stream()
                    .filter(entry -> entry.id().endsWith("/" + link))
                    .findFirst();
        }

        public Optional<Category> category(String id) {
            return categories.stream().filter(category -> category.id().equals(id)).findFirst();
        }
    }

    /** A category: its title, icon, description and entries in their order. */
    public record Category(String id, String title, String icon, int ordinal, String markdown, List<Entry> entries) {}

    /** An entry: its title, icon and pages as Markdown. */
    public record Entry(String id, String category, String title, String icon, int ordinal, String markdown) {}

    /** A crafting recipe: nine slots (each with the items it cycles through, empty if the slot is empty). */
    public record Recipe(List<List<String>> slots, String result, int count) {}

    private record Document(JsonObject meta, String markdown) {
        String string(String key, String fallback) {
            JsonElement value = meta.get(key);
            return value != null && value.isJsonPrimitive() ? value.getAsString() : fallback;
        }

        int ordinal() {
            JsonElement value = meta.get("ordinal");
            return value != null && value.isJsonPrimitive() ? value.getAsInt() : Integer.MAX_VALUE;
        }
    }

    private HandbookContent() {}

    /** Reads a book in the given language (e.g. de_de), with the English files where there is no translation. */
    public static Optional<Book> load(ResourceManager resources, String book, String language) {
        Optional<JsonObject> definition = json(resources, C0de.id(ROOT + "/books/" + book + ".json"));
        if (definition.isEmpty()) {
            return Optional.empty();
        }
        JsonObject json = definition.get();
        Map<String, Document> categoryFiles = documents(resources, ROOT + "/categories/" + book, language);
        Map<String, Document> entryFiles = documents(resources, ROOT + "/entries/" + book, language);
        Comparator<Entry> entryOrder = Comparator.comparingInt(Entry::ordinal).thenComparing(Entry::title);
        Map<String, Entry> entries = new LinkedHashMap<>();
        entryFiles.forEach((id, document) -> {
            int slash = id.indexOf('/');
            String category = document.string("category", slash < 0 ? "" : id.substring(0, slash));
            String title = document.string("title", id.substring(slash + 1));
            entries.put(id, new Entry(
                    id, category, title, document.string("icon", "minecraft:book"), document.ordinal(),
                    document.markdown()));
        });
        List<Category> categories = new ArrayList<>();
        categoryFiles.forEach((id, document) -> categories.add(new Category(
                id,
                document.string("title", id),
                document.string("icon", "minecraft:book"),
                document.ordinal(),
                document.markdown(),
                entries.values().stream()
                        .filter(entry -> entry.category().equals(id))
                        .sorted(entryOrder)
                        .toList())));
        categories.sort(Comparator.comparingInt(Category::ordinal).thenComparing(Category::title));
        String texture = json.has("texture") ? json.get("texture").getAsString() : "c0de:textures/gui/handbook.png";
        return Optional.of(new Book(
                book,
                json.has("name") ? json.get("name").getAsString() : book,
                json.has("landing_text") ? json.get("landing_text").getAsString() : "",
                ResourceLocation.parse(texture),
                List.copyOf(categories),
                entries,
                recipes(resources)));
    }

    /** The Markdown files below a folder by their ID (the path without the language folder and .md). */
    private static Map<String, Document> documents(ResourceManager resources, String folder, String language) {
        Map<String, Document> english = new HashMap<>();
        Map<String, Document> translated = new HashMap<>();
        Map<ResourceLocation, Resource> found = resources.listResources(
                folder, id -> id.getNamespace().equals(C0de.MOD_ID) && id.getPath().endsWith(".md"));
        found.forEach((location, resource) -> {
            String path = location.getPath().substring(folder.length() + 1, location.getPath().length() - 3);
            String first = path.contains("/") ? path.substring(0, path.indexOf('/')) : "";
            Map<String, Document> target = english;
            if (LANGUAGE.matcher(first).matches()) {
                if (!first.equals(language) || first.equals(FALLBACK)) {
                    return;
                }
                target = translated;
                path = path.substring(first.length() + 1);
            }
            Map<String, Document> into = target;
            String id = path;
            read(resource).map(HandbookContent::document).ifPresent(document -> into.put(id, document));
        });
        Map<String, Document> result = new LinkedHashMap<>(english);
        result.putAll(translated);
        return result;
    }

    /** Splits a file into its JSON metadata (a ```json block at the start) and its Markdown. */
    static Document document(String text) {
        String content = text.startsWith("﻿") ? text.substring(1) : text;
        content = content.replace("\r\n", "\n");
        String start = content.stripLeading();
        if (start.startsWith("```json")) {
            int end = start.indexOf("\n```", 7);
            if (end > 0) {
                try {
                    JsonObject meta = JsonParser.parseString(start.substring(7, end)).getAsJsonObject();
                    return new Document(meta, start.substring(end + 4).strip());
                } catch (RuntimeException e) {
                    C0de.LOGGER.warn("Handbook page with invalid metadata: {}", e.getMessage());
                }
            }
        }
        return new Document(new JsonObject(), content.strip());
    }

    private static Map<String, Recipe> recipes(ResourceManager resources) {
        Map<String, Recipe> recipes = new HashMap<>();
        json(resources, C0de.id(ROOT + "/recipes.json")).ifPresent(json -> json.entrySet().forEach(entry -> {
            try {
                JsonObject recipe = entry.getValue().getAsJsonObject();
                List<List<String>> slots = new ArrayList<>();
                for (JsonElement slot : recipe.getAsJsonArray("slots")) {
                    List<String> items = new ArrayList<>();
                    slot.getAsJsonArray().forEach(item -> items.add(item.getAsString()));
                    slots.add(List.copyOf(items));
                }
                recipes.put(entry.getKey(), new Recipe(
                        List.copyOf(slots),
                        recipe.get("result").getAsString(),
                        recipe.has("count") ? recipe.get("count").getAsInt() : 1));
            } catch (RuntimeException e) {
                C0de.LOGGER.warn("Handbook recipe {} is invalid: {}", entry.getKey(), e.getMessage());
            }
        }));
        return recipes;
    }

    private static Optional<JsonObject> json(ResourceManager resources, ResourceLocation location) {
        return resources.getResource(location).flatMap(HandbookContent::read).flatMap(text -> {
            try {
                return Optional.of(JsonParser.parseString(text).getAsJsonObject());
            } catch (RuntimeException e) {
                C0de.LOGGER.warn("Handbook file {} is invalid: {}", location, e.getMessage());
                return Optional.empty();
            }
        });
    }

    private static Optional<String> read(Resource resource) {
        try (Reader reader = resource.openAsReader()) {
            StringBuilder text = new StringBuilder();
            char[] buffer = new char[8192];
            int length;
            while ((length = reader.read(buffer)) > 0) {
                text.append(buffer, 0, length);
            }
            return Optional.of(text.toString());
        } catch (IOException e) {
            C0de.LOGGER.warn("Handbook file can't be read: {}", e.getMessage());
            return Optional.empty();
        }
    }
}
