```json
{
  "title": "SET_OPEN",
  "icon": "minecraft:book",
  "category": "c0de_minecraft",
  "ordinal": 13
}
```

Öffnet oder schließt eine Tür, Falltür oder ein Zauntor.

Jeder andere Block löst INVALID\_ARGUMENT aus. Eisentüren und Eisenfalltüren brauchen die Berechtigung allowBlockChanges oder einen Administrator als Besitzer, sonst wird PERMISSION\_DENIED ausgelöst. Eine leere `dimension` in der Position bedeutet die Dimension des Auslöserblocks.

- `position` STRUCT c0de/minecraft/POSITION: wo die Tür ist
- `open` BOOLEAN: wahr zum Öffnen, falsch zum Schließen

```c0de
CALL c0de/minecraft/SET_OPEN
  PARAMETER position door ;
  PARAMETER open TRUE ;
```
