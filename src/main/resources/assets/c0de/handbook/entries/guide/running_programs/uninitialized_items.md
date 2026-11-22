```json
{
  "title": "Uninitialized items",
  "icon": "c0de:trigger_block",
  "category": "running_programs",
  "ordinal": 4
}
```

An **uninitialized** [variable item](^tools/variable_items) has no name, no type and no value. A process writes a result into it.

You need one for each result that has no variable item of a parameter to take its place. A function with one result and no parameters needs one uninitialized item. [Constant items](^tools/constant_items) don't take the place of a result: they stay constants.

Spare uninitialized items stay in the input inventory. So you can stack several of them there for many runs.

**Recipes**

<item;c0de:variable_item>

<recipe;c0de:variable_item_uninitialized>

<item;c0de:constant_item>

<recipe;c0de:constant_item_uninitialized>
