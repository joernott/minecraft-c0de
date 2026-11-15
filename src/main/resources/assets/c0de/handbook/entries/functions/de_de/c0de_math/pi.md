```json
{
  "title": "PI",
  "icon": "minecraft:book",
  "category": "c0de_math",
  "ordinal": 0
}
```

Die Zahl π: 3.141592653589793.

PI ist eine Konstante, keine Funktion, du rufst sie also nicht mit CALL auf. Nach `INCLUDE c0de/math` verwendest du ihren vollen Namen `c0de/math/PI` in einem Ausdruck, wie eine Variable. Sie ist ein FLOAT.

SIN, COS und TAN nehmen Winkel im Bogenmaß: Ein ganzer Kreis ist 2 × PI, ein rechter Winkel PI / 2. Um Grad ins Bogenmaß umzurechnen, multipliziere mit PI / 180.

```c0de
ASSIGN circumference 2 * c0de/math/PI * radius ;
```
