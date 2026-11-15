```json
{
  "title": "PI",
  "icon": "minecraft:book",
  "category": "c0de_math",
  "ordinal": 0
}
```

The number π: 3.141592653589793.

PI is a constant, not a function, so you don't CALL it. After `INCLUDE c0de/math`, use its full name `c0de/math/PI` in an expression, like a variable. It is a FLOAT.

SIN, COS and TAN take angles in radians: a full circle is 2 × PI, a right angle PI / 2. To turn degrees into radians, multiply by PI / 180.

```c0de
ASSIGN circumference 2 * c0de/math/PI * radius ;
```
