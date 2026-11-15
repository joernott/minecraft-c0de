```json
{
  "title": "INSERT",
  "icon": "minecraft:book",
  "category": "built_in_functions",
  "ordinal": 3
}
```

Fügt einen Wert in ein Array ein.

Die folgenden Elemente rücken um eins nach oben. `index` muss zwischen 0 und der Länge des Arrays liegen, sonst wird INDEX\_OUT\_OF\_BOUNDS ausgelöst. Nimm die Länge, um den Wert am Ende anzuhängen.

- `array` ARRAY: das Array
- `index` INTEGER: die Position des neuen Elements
- `value` der Typ der Elemente: der Wert, der eingefügt wird
- Ergebnis `result` wie `array`: das Array mit dem neuen Element

```c0de
CALL INSERT
  PARAMETER array list ;
  PARAMETER index 0 ;
  PARAMETER value 42 ;
  RESULT result list
```
