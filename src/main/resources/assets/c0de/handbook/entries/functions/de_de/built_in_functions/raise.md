```json
{
  "title": "RAISE",
  "icon": "minecraft:book",
  "category": "built_in_functions",
  "ordinal": 6
}
```

Gibt deinen eigenen Fehler an den Aufrufer der Funktion zurück.

Der Aufrufer löst den Fehler aus oder speichert ihn im Fehler-Label seiner CALL-Zeile. `code` muss 1000 oder größer sein, sonst wird INVALID\_ARGUMENT ausgelöst. Der Fehler bekommt den Kontext und die Zeile der CALL-Anweisung.

- `code` INTEGER: der Fehlercode, 1000 oder größer
- `message` STRING: die Fehlermeldung (optional, Standard leer)

```c0de
CALL RAISE
  PARAMETER code 1001 ;
  PARAMETER message "too big" ;
```
