```json
{
  "title": "REMOVE",
  "icon": "minecraft:book",
  "category": "built_in_functions",
  "ordinal": 2
}
```

Entfernt ein Element aus einem Array oder einem Hash.

Nimm `index` für ein Array und `key` für einen Hash. Die folgenden Elemente des Arrays rücken um eins nach unten. Ein fehlender Index löst INDEX\_OUT\_OF\_BOUNDS aus, ein fehlender Schlüssel löst UNKNOWN\_KEY aus. Wenn du `index` oder `key` weglässt, wird INVALID\_ARGUMENT ausgelöst.

- `container` ARRAY|HASH: das Array oder der Hash
- `index` INTEGER: Position des Elements, das entfernt wird (optional, nötig für Arrays)
- `key` STRING: Schlüssel des Elements, das entfernt wird (optional, nötig für Hashes)
- Ergebnis `result` wie `container`: das Array oder der Hash ohne das Element

```c0de
CALL REMOVE
  PARAMETER container list ;
  PARAMETER index 1 ;
  RESULT result list
```
