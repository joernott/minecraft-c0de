```json
{
  "title": "INCLUDE",
  "icon": "c0de:include_block",
  "category": "program_structure",
  "ordinal": 3
}
```

An INCLUDE line loads a library.

A LABEL with the library name follows the block, like `c0de/math`. INCLUDE lines start at x=5, right after the NOOP in line 1. They come before all structs, variables, constants and functions.

<recipe;c0de:include_block>

Craft a NOOP block and an empty map together.

```c0de
PROGRAM math
NOOP
INCLUDE c0de/math
INCLUDE c0de/strings
```

Now the program can call functions like `c0de/math/ROUND`.
