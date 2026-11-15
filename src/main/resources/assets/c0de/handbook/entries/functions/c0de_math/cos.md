```json
{
  "title": "COS",
  "icon": "minecraft:book",
  "category": "c0de_math",
  "ordinal": 7
}
```

Returns the cosine of an angle.

The angle is in radians. A full circle is 2 × `c0de/math/PI`.

- `value` INTEGER|FLOAT: the angle in radians
- result `result` FLOAT: the cosine

```c0de
CALL c0de/math/COS
  PARAMETER value angle ;
  RESULT result cosine
```
