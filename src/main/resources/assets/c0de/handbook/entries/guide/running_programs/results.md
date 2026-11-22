```json
{
  "title": "Results",
  "icon": "c0de:trigger_block",
  "category": "running_programs",
  "ordinal": 6
}
```

When a process ends normally, the output inventory gets one [variable item](^tools/variable_items) per result of the entry function. Each item carries the name, type and value of its result. An [EXIT](^language/exit) counts as a normal end: the results get the values they have when EXIT runs.

Constant items of parameters come back unchanged: a constant stays a constant. If the function has fewer results than parameters with variable items, you also get empty variable items back. So the number of items going in and out is the same.

**Recipes**

<item;c0de:variable_item>

<recipe;c0de:variable_item_uninitialized>

<item;c0de:exit_block>

<recipe;c0de:exit_block>

<item;c0de:constant_item>

<recipe;c0de:constant_item_uninitialized>
