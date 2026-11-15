```json
{
  "title": "TAN",
  "icon": "minecraft:book",
  "category": "c0de_math",
  "ordinal": 8
}
```

Returns the tangent of an angle.

The angle is in radians. A full circle is 2 × `c0de/math/PI`.

- `value` INTEGER|FLOAT: the angle in radians
- result `result` FLOAT: the tangent

```c0de
CALL c0de/math/TAN
  PARAMETER value angle ;
  RESULT result tangent
```
