```json
{
  "title": "GET_OPEN",
  "icon": "minecraft:book",
  "category": "c0de_minecraft",
  "ordinal": 12
}
```

Prüft, ob eine Tür, Falltür oder ein Zauntor offen ist.

Jeder andere Block löst INVALID\_ARGUMENT aus. Eine leere `dimension` in der Position bedeutet die Dimension des Auslöserblocks.

- `position` STRUCT c0de/minecraft/POSITION: wo die Tür ist
- Ergebnis `result` BOOLEAN: wahr, wenn sie offen ist

```c0de
CALL c0de/minecraft/GET_OPEN
  PARAMETER position door ;
  RESULT result isOpen
```
