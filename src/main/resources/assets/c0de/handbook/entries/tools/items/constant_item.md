```json
{
  "title": "Constant item",
  "icon": "c0de:constant_item",
  "category": "items",
  "ordinal": 4
}
```

A constant item works like a variable item, but once it has a value, it can't be changed.

It has a pink border. Hold it and right-click to set its name and value. This only works as long as it has no value yet.

<recipe;c0de:constant_item_boolean>

<recipe;c0de:constant_item_uninitialized>

A data item, a type item and pink dye give two constant items\; a data item and pink dye give one uninitialized constant item.

Use a constant item for a value that must stay fixed, like a limit for a loop. Nobody can change it by accident in the dialog.

In a chain of trigger blocks, items a process does not use pass through unchanged. So a constant item can travel along the whole chain.
