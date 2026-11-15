```json
{
  "title": "TYPE_STRING",
  "icon": "c0de:type_string_block",
  "category": "types",
  "ordinal": 5
}
```

TYPE\_STRING steht für den Typ STRING: einen Text.

Ein neuer STRING ist leer. Du kannst hinter den Typ ein INTEGER-Literal setzen, um die Länge zu begrenzen. Ein einzelnes Zeichen liest du mit einem Index, der bei 0 beginnt, zum Beispiel `name[0]`.

<recipe;c0de:type_string_block>

Stelle eine Typbasis und einen Faden zusammen her.

```c0de
VARIABLE greeting STRING "Hello"
VARIABLE short STRING 3
```

Die zweite Zeile erzeugt einen Text mit höchstens 3 Zeichen.
