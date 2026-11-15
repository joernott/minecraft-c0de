```json
{
  "title": "c0de/security",
  "icon": "c0de:include_block",
  "category": "libraries",
  "ordinal": 21
}
```

Programs sometimes need a password or a key, e.g. for a web service. c0de keeps them out of sight in three ways:

- INPUT with `PARAMETER hide TRUE ;` shows `*` on the laptop while you type.
- Variable, constant and literal items can hide their value for good (**Hide the value** in their dialog).
- The tag `security: "sensitive"` on a variable or constant shows `***` in the debugging tool.

Hiding protects against people who look at your screen. Real secrets belong into your vault.

**Recipes**

<item;c0de:laptop>

<recipe;c0de:laptop>

<item;c0de:literal_item>

<recipe;c0de:literal_item>

<item;c0de:debug_tool>

<recipe;c0de:debug_tool>
