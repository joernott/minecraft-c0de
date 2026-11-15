```json
{
  "title": "VARIABLE",
  "icon": "c0de:variable_block",
  "category": "values_and_names",
  "ordinal": 2
}
```

Eine VARIABLE-Zeile erzeugt eine Variable: einen benannten Platz für einen Wert, der sich ändern kann.

Auf den Block folgen ein Name und ein Typ. Ein Literal kann den Startwert angeben. Ohne Literal enthält die Variable den Standardwert ihres Typs. Arrays, Hashes und Strukturen bekommen ihre Startwerte in einem Codeblock.

Eine Variable, die in einem Codeblock erzeugt wird, lebt nur in diesem Block. Eine Variable bei x=5 ist global.

<recipe;c0de:variable_block>

<recipe;c0de:variable_block_from_constant_block>

Stelle eine Typbasis und einen Knochenblock zusammen her, oder verwandle einen CONSTANT-Block in einen VARIABLE-Block.

```c0de
VARIABLE i INTEGER
VARIABLE name STRING "Steve"
VARIABLE list ARRAY INTEGER
BEGIN
  1 2 3
END VARIABLE
```
