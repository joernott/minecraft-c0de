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
import org.jetbrains.annotations.Nullable;

/**
 * The content of a handbook (docs/concept/21-handbooks.md), read from the resources of the client, in the
 * namespace of the book (c0de or another mod): the book {@code handbook/books/<book>.json}, its categories
 * {@code handbook/categories/<book>/<category>.md} and its entries {@code handbook/entries/<book>/<category>/<entry>.md},
 * with translations in a folder named after the language, and the recipe grids {@code handbook/recipes.json} of all
 * namespaces. It is read whenever a book is opened, so changed
 * resource packs take effect without a reload listener.
 */
public final class HandbookContent {
    private static final String ROOT = "handbook";
    private static final String FALLBACK = "en_us";
    private static final Pattern LANGUAGE = Pattern.compile("[a-z]{2,3}_[a-z]{2,4}");

    /**
     * A book with its categories in their order and its entries by ID ({@code <category>/<entry>}). Its ID is
     * {@code <namespace>:<book>}, e.g. {@code c0de:guide}; {@code color} is the colour of the label of its binder.
     */
    public record Book(
            String id,
            String name,
            String landing,
            int color,
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

    /**
     * A crafting recipe: nine slots (each with the items it cycles through, empty if the slot is empty), the result
     * and, for the binder of a book of another mod, the ID of that book and the colour of its label.
     */
    public record Recipe(List<List<String>> slots, String result, int count, @Nullable String book, int color) {}

    /** A book the client knows: its ID and the colour of the label of its binder. */
    public record Known(String id, int color) {}

    /** The colour of a label without a colour of its own. */
    public static final int WHITE = 0xF2F2F2;

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

    /** The full ID of a book: {@code guide} is {@code c0de:guide}. */
    public static String id(String book) {
        return book.contains(":") ? book : C0de.MOD_ID + ":" + book;
    }

    /**
     * Reads a book (e.g. {@code c0de:guide} or {@code c0de_net:net}; without a namespace one of c0de) in the given
     * language (e.g. de_de), with the English files where there is no translation.
     */
    public static Optional<Book> load(ResourceManager resources, String bookId, String language) {
        String bookIdFull = id(bookId);
        String namespace = bookIdFull.substring(0, bookIdFull.indexOf(':'));
        String book = bookIdFull.substring(bookIdFull.indexOf(':') + 1);
        ResourceLocation location = ResourceLocation.tryParse(namespace + ":" + ROOT + "/books/" + book + ".json");
        Optional<JsonObject> definition = location == null ? Optional.empty() : json(resources, location);
        if (definition.isEmpty()) {
            return Optional.empty();
        }
        JsonObject json = definition.get();
        Map<String, Document> categoryFiles =
                documents(resources, namespace, ROOT + "/categories/" + book, language);
        Map<String, Document> entryFiles = documents(resources, namespace, ROOT + "/entries/" + book, language);
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
                bookIdFull,
                json.has("name") ? json.get("name").getAsString() : book,
                json.has("landing_text") ? json.get("landing_text").getAsString() : "",
                color(json),
                ResourceLocation.parse(texture),
                List.copyOf(categories),
                entries,
                recipes(resources)));
    }

    /** The books of all mods, by their ID, with the colours of their labels. */
    public static List<Known> books(ResourceManager resources) {
        List<Known> books = new ArrayList<>();
        resources.listResources(ROOT + "/books", id -> id.getPath().endsWith(".json")).forEach((location, resource) -> {
            String path = location.getPath();
            String book = path.substring(ROOT.length() + "/books/".length(), path.length() - ".json".length());
            if (book.contains("/")) {
                return;
            }
            int color = read(resource).map(text -> {
                try {
                    return color(JsonParser.parseString(text).getAsJsonObject());
                } catch (RuntimeException e) {
                    return WHITE;
                }
            }).orElse(WHITE);
            books.add(new Known(location.getNamespace() + ":" + book, color));
        });
        books.sort(Comparator.comparing(Known::id));
        return books;
    }

    /** The colour of the label of a book: {@code color} as {@code "#RRGGBB"} or a number, white without it. */
    private static int color(JsonObject json) {
        JsonElement value = json.get("color");
        if (value == null || !value.isJsonPrimitive()) {
            return WHITE;
        }
        try {
            String text = value.getAsString();
            return text.startsWith("#") ? Integer.parseInt(text.substring(1), 16) & 0xFFFFFF : value.getAsInt();
        } catch (RuntimeException e) {
            return WHITE;
        }
    }

    /** The Markdown files below a folder by their ID (the path without the language folder and .md). */
    private static Map<String, Document> documents(
            ResourceManager resources, String namespace, String folder, String language) {
        Map<String, Document> english = new HashMap<>();
        Map<String, Document> translated = new HashMap<>();
        Map<ResourceLocation, Resource> found = resources.listResources(
                folder, id -> id.getNamespace().equals(namespace) && id.getPath().endsWith(".md"));
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

    /** The crafting grids of all mods ({@code handbook/recipes.json} of every namespace), merged. */
    private static Map<String, Recipe> recipes(ResourceManager resources) {
        Map<String, Recipe> recipes = new HashMap<>();
        for (String namespace : new java.util.TreeSet<>(resources.getNamespaces())) {
            ResourceLocation location = ResourceLocation.tryParse(namespace + ":" + ROOT + "/recipes.json");
            if (location != null) {
                recipes(resources, location, recipes);
            }
        }
        return recipes;
    }

    private static void recipes(ResourceManager resources, ResourceLocation location, Map<String, Recipe> recipes) {
        json(resources, location).ifPresent(json -> json.entrySet().forEach(entry -> {
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
                        recipe.has("count") ? recipe.get("count").getAsInt() : 1,
                        recipe.has("book") ? id(recipe.get("book").getAsString()) : null,
                        color(recipe)));
            } catch (RuntimeException e) {
                C0de.LOGGER.warn("Handbook recipe {} is invalid: {}", entry.getKey(), e.getMessage());
            }
        }));
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
