```json
{
  "title": "Variable item",
  "icon": "c0de:variable_item",
  "category": "items",
  "ordinal": 2
}
```

A variable item carries a name, a type and a value into or out of a process.

It has a blue border. Hold it in your main hand and right-click to edit its name, type and value. The trash can button empties all fields. Saving with all fields empty makes the item uninitialized.

An uninitialized variable item has no name, type or value. A process writes a result into it. You need one for each result that has no parameter item to take its place.

<recipe;c0de:variable_item_boolean>

<recipe;c0de:variable_item_uninitialized>

A data item, a type item and light blue dye give two variable items\; a data item and light blue dye give one uninitialized item.

This function doubles a number. Put an INTEGER variable item named `n` with the value 21 into the trigger block. After the run, the output holds an item named `result` with the value 42.

```c0de
PROGRAM doubler
NOOP
NOOP
FUNCTION double
  PARAMETER n INTEGER
  RESULT result INTEGER
BEGIN
  ASSIGN result n * 2 ;
END FUNCTION
```
