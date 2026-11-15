```json
{
  "title": "PROGRAM",
  "icon": "c0de:program_block",
  "category": "program_structure",
  "ordinal": 2
}
```

The PROGRAM block starts every program.

It sits in line 0 at x=5, followed by a LABEL with the program name. The name must be unique in the whole c0de dimension. Line 1 must be a NOOP line. The program ends at the first empty line.

<recipe;c0de:program_block>

Craft a NOOP block and a book together.

```c0de
PROGRAM first
NOOP
```
