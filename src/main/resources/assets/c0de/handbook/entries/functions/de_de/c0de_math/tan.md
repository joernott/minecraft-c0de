```json
{
  "title": "TAN",
  "icon": "minecraft:book",
  "category": "c0de_math",
  "ordinal": 8
}
```

Gibt den Tangens eines Winkels zurück.

Der Winkel ist im Bogenmaß. Ein voller Kreis ist 2 × `c0de/math/PI`.

- `value` INTEGER|FLOAT: der Winkel im Bogenmaß
- Ergebnis `result` FLOAT: der Tangens

```c0de
CALL c0de/math/TAN
  PARAMETER value angle ;
  RESULT result tangent
```
