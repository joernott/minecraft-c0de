```json
{
  "title": "WRITE",
  "icon": "minecraft:book",
  "category": "input_and_output",
  "ordinal": 1
}
```

Legt einen Wert als Variablenkarte in die E/A-Blöcke.

Jeder gebundene E/A-Block mit einer nicht initialisierten Variablenkarte in seiner Eingabe bekommt den Wert. Die Karte wandert in die Ausgabe. Sie wird nach `name` benannt. Ohne `name` wird sie nach der Variablen benannt, z. B. `count` bei `PARAMETER value count ;`. Sonst heißt sie `output`.

Wenn kein gebundener E/A-Block eine nicht initialisierte Variablenkarte hat, wartet der Cursor, bis ein Spieler eine hineinlegt. Laptops und Tablets nehmen an WRITE nicht teil. Eine Variable mit dem Tag `security: "sensitive"` oder ein SECRET wird zu einer verborgenen Karte.

- `value` beliebiger Typ: der Wert, der geschrieben wird
- `name` STRING: der Name der Karte (optional)

```c0de
CALL WRITE
  PARAMETER value count + step ;
  PARAMETER name "total" ;
```
