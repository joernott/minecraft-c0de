```json
{
  "title": "Variablen und Konstanten",
  "icon": "c0de:program_block",
  "category": "language",
  "ordinal": 5
}
```

Eine VARIABLE-Zeile hat einen Namen, einen Typ und einen optionalen Startwert. Ohne Startwert enthält sie den Standardwert: false, 0, 0.0, ein Leerzeichen, einen leeren Text oder eine leere Liste.

Eine CONSTANT-Zeile sieht genauso aus, muss aber einen Wert haben. Du kannst ihn später nicht ändern. Der Wert ist ein LITERAL, keine Rechnung und keine andere Konstante.

```c0de
VARIABLE b BOOLEAN TRUE
VARIABLE i INTEGER
CONSTANT BASE INTEGER 42
```

Listen, Hashes und Strukturen bekommen ihre Startwerte in einem [Codeblock](^language/lines_and_indentation). Jede Zeile enthält [Literale](^language/literals) oder einen Schlüssel und einen Wert:

```c0de
VARIABLE list ARRAY INTEGER
BEGIN
  1 2 3
END VARIABLE
VARIABLE stock HASH INTEGER
BEGIN
  gold 1
END VARIABLE
```

**Rezepte**

<item;c0de:variable_block>

<recipe;c0de:variable_block>

<item;c0de:constant_block>

<recipe;c0de:constant_block>

<item;c0de:literal_block>

<recipe;c0de:literal_block>
