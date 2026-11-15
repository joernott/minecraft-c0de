```json
{
  "title": "Zeilen und Einrückung",
  "icon": "c0de:program_block",
  "category": "language",
  "ordinal": 1
}
```

Globale Zeilen beginnen bei x=5: PROGRAM, [INCLUDE](^libraries/including_libraries), STRUCT, [FUNCTION](^language/functions) sowie globale VARIABLE- und CONSTANT-Zeilen. Zeilen in einem Codeblock stehen einen Block weiter östlich als sein BEGIN.

Ein Codeblock beginnt mit BEGIN und endet mit END. Auf END folgt das Schlüsselwort, das den Block geöffnet hat, zum Beispiel `END FUNCTION` oder `END WHILE`. Ein Codeblock darf nicht leer sein.

Variablen und Konstanten, die in einem Codeblock angelegt werden, leben nur darin. Sie werden vergessen, wenn der Block endet.

**Rezepte**

<item;c0de:program_block>

<recipe;c0de:program_block>

<item;c0de:include_block>

<recipe;c0de:include_block>

<item;c0de:type_struct_block>

<recipe;c0de:type_struct_block>

<item;c0de:function_block>

<recipe;c0de:function_block>

<item;c0de:variable_block>

<recipe;c0de:variable_block>

<item;c0de:constant_block>

<recipe;c0de:constant_block>

<item;c0de:begin_block>

<recipe;c0de:begin_block>

<item;c0de:end_block>

<recipe;c0de:end_block>
