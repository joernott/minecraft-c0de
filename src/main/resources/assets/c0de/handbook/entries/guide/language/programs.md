```json
{
  "title": "Programs",
  "icon": "c0de:program_block",
  "category": "language",
  "ordinal": 0
}
```

A program starts at line 0 with a PROGRAM block and a LABEL with its name. The name must be unique in the whole c0de dimension. Line 1 must be a [NOOP line](^language/noop_and_comment).

After that, [INCLUDE lines](^libraries/including_libraries) may follow. They load libraries, like `INCLUDE c0de/math`. Then come [struct](^language/structs) types, global variables, constants and functions, in any order.

A name can only be used after it was defined. So put everything a function needs above it. The program ends at the first empty line.

**Recipes**

<item;c0de:program_block>

<recipe;c0de:program_block>

<item;c0de:label_block>

<recipe;c0de:label_block>

<item;c0de:noop_block>

<recipe;c0de:noop_block>

<item;c0de:include_block>

<recipe;c0de:include_block>
