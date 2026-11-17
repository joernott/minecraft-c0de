```json
{
  "title": "LITERAL",
  "icon": "c0de:literal_block",
  "category": "values_and_names",
  "ordinal": 1
}
```

Ein LITERAL-Block enthält einen festen Wert.

Klicke ihn mit leerer Hand mit rechts an. Wähle den Typ und gib den Wert ein. Ein Literal kann ein BOOLEAN, INTEGER, FLOAT, CHAR, STRING oder TIME sein. Der Block ist ein Achteck, so kannst du ihn von einem LABEL unterscheiden.

<recipe;c0de:literal_block>

Lege einen weißen Wollblock in die Mitte und acht NOOP-Blöcke darum herum. Du bekommst acht LITERAL-Blöcke.

Die Textschreibweise schreibt Literale so: `TRUE`, `42`, `3.14`, `'a'`, `"Hello"` und `@2026-10-19T12:05:09Z`. Im Dialog tippst du einen Text ohne Anführungszeichen, und ein `"` ist einfach ein Zeichen. Die Textschreibweise schreibt es als `\"`, einen Backslash als `\\` und eine neue Zeile als `\n`: `"He said \"hi\""`. Ein Zeilenumbruch lässt sich im Dialog nicht tippen\; nimm CHR aus c0de/strings mit dem Code 10.

```c0de
ASSIGN text "Hello, " ⊕ name ;
```
