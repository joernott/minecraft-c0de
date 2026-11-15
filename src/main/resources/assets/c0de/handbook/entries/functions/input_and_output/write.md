```json
{
  "title": "WRITE",
  "icon": "minecraft:book",
  "category": "input_and_output",
  "ordinal": 1
}
```

Puts a value into the I/O blocks as a variable item.

Each bound I/O block with an uninitialized variable item in its input gets the value. The item moves to the output. It is named after `name`. Without `name`, it is named after the variable, like `count` for `PARAMETER value count ;`. Otherwise it is named `output`.

If no bound I/O block has an uninitialized variable item, the cursor waits until a player puts one in. Laptops and tablets don't take part in WRITE. A variable with the tag `security: "sensitive"` or a SECRET becomes a hidden item.

- `value` any type: the value to write
- `name` STRING: the name of the item (optional)

```c0de
CALL WRITE
  PARAMETER value count + step ;
  PARAMETER name "total" ;
```
