```json
{
  "title": "GET_LEVER",
  "icon": "minecraft:book",
  "category": "c0de_minecraft",
  "ordinal": 7
}
```

Prüft, ob ein Hebel an ist.

Ein Block, der kein Hebel ist, löst INVALID\_ARGUMENT aus. Eine leere `dimension` in der Position bedeutet die Dimension des Auslöserblocks.

- `position` STRUCT c0de/minecraft/POSITION: wo der Hebel ist
- Ergebnis `result` BOOLEAN: wahr, wenn der Hebel an ist

```c0de
CALL c0de/minecraft/GET_LEVER
  PARAMETER position lever ;
  RESULT result isOn
```
