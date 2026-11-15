```json
{
  "title": "SIN",
  "icon": "minecraft:book",
  "category": "c0de_math",
  "ordinal": 6
}
```

Returns the sine of an angle.

The angle is in radians. A full circle is 2 × `c0de/math/PI`.

- `value` INTEGER|FLOAT: the angle in radians
- result `result` FLOAT: the sine

```c0de
CALL c0de/math/SIN
  PARAMETER value angle ;
  RESULT result sine
```
