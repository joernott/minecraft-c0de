```json
{
  "title": "Hidden input",
  "icon": "minecraft:book",
  "category": "screens",
  "ordinal": 2
}
```

A laptop or tablet can hide what a player types.

When a program calls INPUT with `PARAMETER hide TRUE ;`, the input line shows a `*` for every character. The typed line appears on the screen as `***`. Use it for passwords and keys. The value goes to the program as it was typed.

```c0de
VARIABLE password STRING
CALL INPUT
  PARAMETER variable password ;
  PARAMETER hide TRUE ;
```
