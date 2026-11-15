```json
{
  "title": "LOG",
  "icon": "minecraft:book",
  "category": "c0de_math",
  "ordinal": 5
}
```

Returns the natural logarithm of a number.

The number must be greater than 0, otherwise INVALID\_ARGUMENT is raised.

- `value` INTEGER|FLOAT: the number, greater than 0
- result `result` FLOAT: the natural logarithm

```c0de
CALL c0de/math/LOG
  PARAMETER value amount ;
  RESULT result logarithm
```
