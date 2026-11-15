```json
{
  "title": "PRINT",
  "icon": "minecraft:book",
  "category": "input_and_output",
  "ordinal": 0
}
```

Schreibt einen Wert auf die Bildschirme und beginnt eine neue Zeile.

Der Text erscheint auf jeder Anzeige, jedem Laptop und jedem Tablet, das an das Programm und die Funktion gebunden ist. Strings werden ohne Anführungszeichen ausgegeben, BOOLEAN-Werte als `TRUE` und `FALSE`. Arrays, Hashes und Strukturen werden als JSON ausgegeben. Um mehrere Werte in einer Zeile auszugeben, verbinde sie mit ⊕. Ohne gebundenen Bildschirm macht PRINT nichts.

- `value` beliebiger Typ: der Wert, der ausgegeben wird

```c0de
CALL PRINT
  PARAMETER value "Hello" ;
```
