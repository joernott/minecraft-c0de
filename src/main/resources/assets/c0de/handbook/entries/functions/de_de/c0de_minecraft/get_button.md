```json
{
  "title": "GET_BUTTON",
  "icon": "minecraft:book",
  "category": "c0de_minecraft",
  "ordinal": 9
}
```

Prüft, ob ein Knopf gedrückt ist.

Ein Block, der kein Knopf ist, löst INVALID\_ARGUMENT aus. Eine leere `dimension` in der Position bedeutet die Dimension des Auslöserblocks.

- `position` STRUCT c0de/minecraft/POSITION: wo der Knopf ist
- Ergebnis `result` BOOLEAN: wahr, wenn der Knopf gedrückt ist

```c0de
CALL c0de/minecraft/GET_BUTTON
  PARAMETER position button ;
  RESULT result pressed
```
