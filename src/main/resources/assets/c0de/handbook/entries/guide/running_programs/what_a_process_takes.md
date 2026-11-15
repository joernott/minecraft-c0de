```json
{
  "title": "What a process takes",
  "icon": "c0de:trigger_block",
  "category": "running_programs",
  "ordinal": 5
}
```

When a process starts, it takes from the input inventory:

- one item for each parameter that has an item,
- one [uninitialized item](^running_programs/uninitialized_items) for each extra result,
- all other initialized variable and [constant items](^tools/constant_items).

Those other items are not used. They are passed through unchanged to the output inventory when the process ends.

**Recipes**

<item;c0de:constant_item>

<recipe;c0de:constant_item_uninitialized>
