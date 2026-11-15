```json
{
  "title": "SWAP",
  "icon": "minecraft:book",
  "category": "built_in_functions",
  "ordinal": 5
}
```

Swaps two values.

Both values must have the same type, otherwise TYPE\_MISMATCH is raised. To swap two variables, use them as parameters and as result targets.

- `a` any type: the first value
- `b` the type of `a`: the second value
- result `a`: the value of `b`
- result `b`: the value of `a`

```c0de
CALL SWAP
  PARAMETER a left ;
  PARAMETER b right ;
  RESULT a left
  RESULT b right
```
