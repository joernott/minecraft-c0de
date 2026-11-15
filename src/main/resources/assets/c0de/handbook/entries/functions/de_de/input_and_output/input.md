```json
{
  "title": "INPUT",
  "icon": "minecraft:book",
  "category": "input_and_output",
  "ordinal": 2
}
```

Liest einen Wert in eine Variable ein.

`variable` muss der Name einer deklarierten Variablen sein, ohne Index. Eine Konstante oder Schleifenvariable geht nicht. Die Funktion schreibt direkt in die Variable. Sie sucht zuerst in den gebundenen E/A-Blöcken nach einer Karte mit demselben Namen und Typ oder nach einer Literalkarte des passenden Typs. Sonst wartet sie, bis ein Spieler auf einem gebundenen Laptop oder Tablet eine Zeile eintippt. Die getippte Zeile erscheint auf dem Bildschirm dieses Laptops oder Tablets.

- `variable` Variable: die Variable, die den Wert bekommt
- `hide` BOOLEAN: zeigt auf Laptops und Tablets `*` für das Getippte (optional, Standard FALSE)
- keine Ergebnisse

```c0de
PROGRAM typing
NOOP
NOOP
FUNCTION ask
BEGIN
  VARIABLE count INTEGER
  CALL INPUT
    PARAMETER variable count ;
  CALL PRINT
    PARAMETER value count * 2 ;
END FUNCTION
```
