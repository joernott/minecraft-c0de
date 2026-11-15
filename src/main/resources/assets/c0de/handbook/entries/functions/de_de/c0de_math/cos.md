```json
{
  "title": "COS",
  "icon": "minecraft:book",
  "category": "c0de_math",
  "ordinal": 7
}
```

Gibt den Kosinus eines Winkels zurück.

Der Winkel ist im Bogenmaß. Ein voller Kreis ist 2 × `c0de/math/PI`.

- `value` INTEGER|FLOAT: der Winkel im Bogenmaß
- Ergebnis `result` FLOAT: der Kosinus

```c0de
CALL c0de/math/COS
  PARAMETER value angle ;
  RESULT result cosine
```
