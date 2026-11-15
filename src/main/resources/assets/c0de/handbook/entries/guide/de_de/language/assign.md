```json
{
  "title": "ASSIGN",
  "icon": "c0de:program_block",
  "category": "language",
  "ordinal": 10
}
```

ASSIGN speichert den Wert eines Ausdrucks in einer Variablen. Der Wert muss zum Typ der Variablen passen. Ein INTEGER darf in ein FLOAT und ein CHAR in einen STRING.

```c0de
ASSIGN arithmetic 1 + 2 * ( 3 + 3 ) \ 10 * 2 + 10 ;
ASSIGN list[0] 10 ;
ASSIGN list[1] 20 ;
ASSIGN list[2] list[0] + list[1] ;
```

Eine Zuweisung an den Index direkt nach dem Ende einer Liste fügt ein neues Element hinzu. Ein neuer Schlüssel fügt einem Hash ein neues Element hinzu.

**Rezepte**

<item;c0de:assign_block>

<recipe;c0de:assign_block>

<item;c0de:type_integer_block>

<recipe;c0de:type_integer_block>

<item;c0de:type_float_block>

<recipe;c0de:type_float_block>

<item;c0de:type_char_block>

<recipe;c0de:type_char_block>

<item;c0de:type_string_block>

<recipe;c0de:type_string_block>
