```json
{
  "title": "TYPE_STRUCT",
  "icon": "c0de:type_struct_block",
  "category": "types",
  "ordinal": 8
}
```

TYPE\_STRUCT steht für den Typ STRUCT: deinen eigenen Typ mit benannten Elementen.

Bei x=5 beginnt der Block eine Struktur-Deklaration. Ein LABEL mit dem Typnamen folgt, und ein Codeblock zählt die Elemente auf. An anderen Stellen verwendet er eine Struktur: Ein LABEL mit dem Namen der Struktur folgt ihm.

<recipe;c0de:type_struct_block>

Stelle eine Typbasis und ein Gerüst zusammen her.

```c0de
STRUCT point
BEGIN
  x INTEGER
  y INTEGER
END STRUCT
VARIABLE p STRUCT point
```

Die Elemente erreichst du mit einem Index, zum Beispiel `p.x`.
