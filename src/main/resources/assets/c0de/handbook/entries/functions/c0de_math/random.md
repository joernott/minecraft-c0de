```json
{
  "title": "RANDOM",
  "icon": "minecraft:book",
  "category": "c0de_math",
  "ordinal": 15
}
```

Returns a random number between 0 and 1.

0 is possible, 1 is not. Each process has its own random number generator.

- result `result` FLOAT: a random number from 0 up to below 1

```c0de
CALL c0de/math/RANDOM
  RESULT result chance
```
