```json
{
  "title": "NOOP",
  "icon": "c0de:noop_block",
  "category": "program_structure",
  "ordinal": 0
}
```

NOOP means "no operation": a NOOP line does nothing.

Use NOOP lines to separate parts of your program. A NOOP line may not hold any other block, not even a COMMENT. Line 1 of every program is a NOOP line, and every FUNCTION needs one right above it.

NOOP blocks are also the base of most other recipes.

<recipe;c0de:noop_block>

Put iron ingots in the four corners and sticks on the four sides, and leave the middle empty. You get 64 NOOP blocks.

```c0de
PROGRAM first
NOOP
NOOP
FUNCTION answer
  RESULT result INTEGER
BEGIN
  ASSIGN result 42 ;
END FUNCTION
```
