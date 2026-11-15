```json
{
  "title": "Chaining",
  "icon": "c0de:trigger_block",
  "category": "running_programs",
  "ordinal": 10
}
```

Hoppers, pipes and cables can use a [trigger block](^running_programs/three_trigger_blocks) from all sides. A hopper above or beside it fills the input. A hopper below it empties the output.

So you can build chains: trigger block, hopper, trigger block, hopper, chest. Each process uses only the items it needs and passes on the rest. The end of the chain collects everything.

If exactly one block with an inventory touches a trigger block, the output goes there by itself. Another trigger block counts too: the items land in its input. A hopper pointing into the trigger block does not count.

**Recipes**

<item;c0de:trigger_block>

<recipe;c0de:trigger_block>
