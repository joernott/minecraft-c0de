# Handbooks

[c0de concept](../concept.md) › Handbooks

c0de has its own in-game books. Until 0.6.0, the guide and the handbooks were Patchouli books, which only existed where Patchouli was installed, and Patchouli and its forks aren't available for every Minecraft version c0de supports. Since 0.6.0, the books are part of c0de and work on every Minecraft version and both loaders, without other mods.

The engine is based on [Lavender](https://github.com/wisp-forest/lavender) (MIT license): its Markdown processor [lavender-md](https://github.com/wisp-forest/lavender-md) is embedded, and the book screen started from Lavender's layout. Lavender itself builds on owo-lib, exists only for Fabric and ends at Minecraft 1.21.4, so the screen, the page layout and the recipes are written anew with the plain screen classes of Minecraft.

## The books

There are seven books, as before. Each one is an item of its own that looks like a red ring binder, with the colour of its label telling the books apart:

| Book | Item | Content | Crafted from a book and | Label |
|---|---|---|---|---|
| c0de Guide | `c0de:guide_book` | the guide (`docs/guide`) | a PROGRAM block | white |
| Statements | `c0de:statements_book` | statements (`docs/reference/statements.md`) | a structural base | yellow |
| Data | `c0de:data_book` | types, values and names | a type base | light blue |
| Devices | `c0de:devices_book` | laptops, tablets, displays and the like | a display block | green |
| Functions | `c0de:functions_book` | built-in functions and libraries | a FUNCTION block | orange |
| Tools | `c0de:tools_book` | items and tools | a wrench | grey |
| Triggers | `c0de:triggers_book` | trigger blocks | a trigger block | purple |

The binders stack to 1. Right-clicking with one opens the book. The recipes are shapeless and always loaded (before 0.6.0, only with Patchouli). The binders are in the c0de creative tab.

Every player gets the guide once, when they join a world with c0de: the player tag `c0de.handbook_given` records it. Players who got the Patchouli guide before get the binder too, because the tag is new. Patchouli books that players already have stay what they are; without Patchouli, they are plain items.

## Reading

The book opens as an open red ring binder: two pages with a small gap between them and four metal rings across the gap. It takes about 80 % of the width and height of the screen (at most 1.75 times as wide as high, so that lines stay readable, and at least 267 × 180), and its pages are laid out for that size: a larger screen shows more text per page in the same font. It shows:

  - **The landing page**: the title of the book, its landing text and, on the right, the index of its categories.
  - **A category**: its description and the list of its entries. The guide has one category per chapter, a handbook one per `## ` section.
  - **An entry**: its pages. Text that doesn't fit on a page flows onto the next one; a page break can also be set by hand.

The pages are numbered. Arrows at the bottom corners turn the pages, a back arrow returns to the previous view (the category, then the landing page), and the title of an entry in a list opens it. The keyboard works too: the arrow keys and Page Up/Down turn pages, Backspace goes back, Escape closes the book. Turning a page plays the sound of a book page.

The book remembers where it was open while the game runs, separately for each book, and opens there the next time.

## The content

The content is a resource of the client, so resource packs can change and translate it. A book belongs to a namespace, usually that of the mod it describes; its ID is `<namespace>:<book>`, e.g. `c0de:guide` (since 0.6.1, see [Handbooks of other mods](#handbooks-of-other-mods)). A book consists of:

  - `assets/<namespace>/handbook/books/<book>.json`: the name and the landing text (translation keys of the language files), the texture of the book and the colour of the label of its binder.
  - `assets/<namespace>/handbook/categories/<book>/<category>.md`: a category.
  - `assets/<namespace>/handbook/entries/<book>/<category>/<entry>.md`: an entry.

A category or entry starts with a JSON block of metadata, as in Lavender:

````markdown
```json
{
  "title": "LEN",
  "icon": "c0de:function_block",
  "category": "built_in_functions",
  "ordinal": 3
}
```

Counts the characters of a string or the elements of an array or hash.
````

  - `title`: the title shown in lists and above the first page, on several lines if it is long.
  - `icon`: the ID of the item whose icon stands next to the title.
  - `category`: (entries) the category the entry belongs to.
  - `ordinal`: the position in the list; entries without one come after those with one, sorted by title.

Translations are files with the same name in a folder named after the language, e.g. `entries/functions/de_de/built_in_functions/len.md`. The book shows the files of the language of the game and the English files where there is no translation.

## Markdown

The pages are Markdown, processed by lavender-md:

| Notation | Result |
|---|---|
| `**text**`, `*text*`, `__text__`, `~~text~~` | bold, italic, underlined, struck through |
| `{red}text{}`, `{#336699}text{}` | coloured text (Minecraft colour names or hex) |
| `- item`, `1. item` | lists, also nested |
| `> text` | quote |
| `---` | horizontal line |
| `[text](https://…)` | link to a web page (opens after confirmation, like links in the chat) |
| `[text](^entry)`, `[text](^category/entry)`, `[text](^category)` | link to an entry or a category of the same book |
| `[text](^namespace:book)`, `[text](^namespace:book/category/entry)` | link to another book (its landing page, a category or an entry), also of another mod (since 0.6.1) |
| `![description](c0de:textures/…png)` | image |
| `<keybind;key.use>` | the key bound to an action |
| `%{book.c0de.guide.name}%` | a text from the language files |

c0de adds:

| Notation | Result |
|---|---|
| `` `code` `` | code in the text (dark blue) |
| ```` ```c0de ```` … ```` ``` ```` | a program example: every line as written, with its indentation, in the colours of the website c0de.at (the colours of the blocks, for light paper), on green and white stripes like the greenbar paper of the sheets; code in other languages is dark blue |
| `<recipe;c0de:wrench>` | the crafting grid of a recipe, with the result |
| `<item;minecraft:book>` | a line with the icon and the name of an item (its tooltip under the mouse) |
| `;;;;;` on a line of its own | page break |

## Recipes

A recipe is a crafting grid in the colours of the sheets: light brown lines around and between the slots, the first and third row green and the middle row white, then an arrow and the result on white.

Since Minecraft 1.21.2, the client doesn't know the recipes of the server any more. Recipe pages therefore don't look recipes up: `scripts/generate_book.py` writes the grid of every recipe that a book shows into `assets/c0de/handbook/recipes.json` (other mods into `assets/<namespace>/handbook/recipes.json`; the books read the files of all namespaces, so every mod's recipes can be shown in every book) (the items of each of the nine slots, the result and its count). A slot that takes several items cycles through them once per second; recipes with tags as ingredients can't be shown, and the script refuses them. A recipe the file doesn't have shows its ID in red.

## Handbooks of other mods

Since 0.6.1, other mods ship handbooks of their own without anything in c0de being changed for them. Everything a book needs is a resource or data of the other mod:

  - **The book**: `assets/<namespace>/handbook/books/<book>.json` with `name`, `landing_text` and `color`, its categories and entries, translations in folders named after the language, as above. c0de reads the books of all namespaces.
  - **Crafting grids**: `assets/<namespace>/handbook/recipes.json` of the mod, merged with those of all other mods. A grid can show items of any mod; a result can name a book (`"book": "<namespace>:<book>"`) to show the binder of that book.
  - **The binder**: one item of c0de, `c0de:handbook`, serves all books of other mods. Which book it opens is part of the item, in its custom data (`minecraft:custom_data` with `book`, the ID of the book, and `color`, the colour of the label as a number). Its name is the translation `book.<namespace>.<book>.name`. The label has the colour of `color`: on 1.21.1 through the item colour of c0de, since 1.21.4 through the tint of its item model, which reads the first colour of `minecraft:custom_model_data`; a recipe for 1.21.2 and later sets both. The seven binders of c0de stay items of their own.
  - **The recipe of the binder**: an ordinary crafting recipe of the other mod whose result is `c0de:handbook` with these components, e.g. a book and an item of the mod.
  - **The creative tab**: the tab of c0de lists the binders of all books the client knows, after the seven of c0de.
  - **Links**: entries link to entries of other books with `^namespace:book/category/entry`; a link to a book that isn't installed does nothing.

Only the guide of c0de is given to players when they join; other books are crafted. The chapter "Handbooks" of the [library development guide](../library-development.md) describes the files for mod developers, and `scripts/generate_book.py` of c0de can serve as a template for generating a book from Markdown documentation.

## Generated from the documentation

The landing page of the guide links to the recipes of the other handbooks (chapter Tools, section Handbooks). As before, `scripts/generate_book.py` generates the books from `docs/guide`, `docs/reference` and their German translation in `docs/de`. The generated files are committed, and CI fails if they differ from what the script writes. The rules for categories and entries don't change: a guide chapter becomes a category and every `## ` section an entry; a handbook section becomes a category and every `### ` element an entry with its description, recipes and example. What changes is the output: Markdown files instead of Patchouli JSON. The Markdown of the documentation is kept as far as the books support it (bold, lists, code, program examples); links between documentation files become links between entries, also into other books of c0de (e.g. from the guide into a handbook), and plain text where the target is no entry. The guide refers to its other pages with links (e.g. `[Wrench](04-tools.md#wrench)`), which become links in the book; recipe images become crafting grids.

Every entry of the guide ends with the recipes of the c0de items and blocks its text names (**Recipes**: the icon and name of each item and its crafting grid), unless the entry shows the recipe already. The script finds them by their names (e.g. "trigger block", "wrench", also in the plural) and the language and type blocks by their keyword in capitals (IF, STRING); in a section about type items, the type names stand for the type items, and the chapter on libraries leaves out the keywords, which are function names and types of parameters there. Program examples and code don't count. A translation shows the recipes of the English section at the same position. Items with several recipes show their basic one (an uninitialized variable or constant item); items of Minecraft have no recipes in the books.

## Without Patchouli

c0de no longer uses Patchouli: the Patchouli books, their recipes and the development dependency are removed. Patchouli can still be installed for other mods.

## Licences

The files taken from Lavender and lavender-md keep their MIT licence notice in a comment at the top. `NOTICE.md` in the repository and the jar names Lavender, lavender-md and the licence text. The page arrows come from the red book texture of Lavender; the binder is drawn by c0de; the textures of the binders are drawn for c0de.
