```json
{
  "title": "RANDOMIZE",
  "icon": "minecraft:book",
  "category": "c0de_math",
  "ordinal": 16
}
```

Sets the seed of the random number generator.

The same seed always gives the same sequence of random numbers. This only affects the current process.

- `seed` INTEGER: the seed

```c0de
CALL c0de/math/RANDOMIZE
  PARAMETER seed 42 ;
```
