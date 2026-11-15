```json
{
  "title": "Hello with defaults",
  "icon": "c0de:function_block",
  "category": "examples",
  "ordinal": 0
}
```

```c0de
PROGRAM greeting
NOOP
NOOP
FUNCTION greet
  PARAMETER name STRING "World"
  PARAMETER mark CHAR '!'
  RESULT result STRING
BEGIN
  ASSIGN result "Hello, " ⊕ name ⊕ mark ;
END FUNCTION
```

The function joins three parts into one text with `⊕`. Both parameters have default values, so a caller may leave them out. Called with the name "c0de", the result is "Hello, c0de\!".

To try it, set the [trigger block](^running_programs/three_trigger_blocks) to `greet`. Put in a STRING [variable item](^tools/variable_items) named `name` with your name as its value.

**Recipes**

<item;c0de:trigger_block>

<recipe;c0de:trigger_block>

<item;c0de:type_string_block>

<recipe;c0de:type_string_block>

<item;c0de:variable_item>

<recipe;c0de:variable_item_uninitialized>
