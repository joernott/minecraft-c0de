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
- one uninitialized item for each extra result,
- all other initialized variable and constant items.

Those other items are not used. They are passed through unchanged to the output inventory when the process ends.
