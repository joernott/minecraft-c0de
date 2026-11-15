```json
{
  "title": "SIN",
  "icon": "minecraft:book",
  "category": "c0de_math",
  "ordinal": 6
}
```

Gibt den Sinus eines Winkels zurück.

Der Winkel ist im Bogenmaß. Ein voller Kreis ist 2 × `c0de/math/PI`.

- `value` INTEGER|FLOAT: der Winkel im Bogenmaß
- Ergebnis `result` FLOAT: der Sinus

```c0de
CALL c0de/math/SIN
  PARAMETER value angle ;
  RESULT result sine
```
