```json
{
  "title": "Zeilennummern",
  "icon": "c0de:wrench",
  "category": "getting_started",
  "ordinal": 4
}
```

Bei x=2 zeigt der Boden die Nummer jeder Zeile, die Blöcke enthält. Ihre Farbe sagt dir, wie es um die Zeile steht:

- blass: eine NOOP- oder COMMENT-Zeile
- dunkelgrün: die Zeile ist korrekt
- dunkelgelb: bis hierher korrekt, aber die Anweisung ist noch nicht fertig
- dunkelrot: etwas stimmt nicht

Dunkelgelb ist beim Bauen normal. Ein BEGIN ohne sein END bleibt dunkelgelb, bis du das END platzierst. Das [Debugging-Werkzeug](^tools/debugging_tool) sagt dir, was mit einer roten Zeile nicht stimmt. Eine Zeile wird auch rot, wenn ein CALL einer Funktion ein Literal oder eine Variable vom falschen Typ übergibt. Andere Fehler bei Typen zeigen sich erst, wenn das Programm läuft.

**Rezepte**

<item;c0de:noop_block>

<recipe;c0de:noop_block>

<item;c0de:comment_block>

<recipe;c0de:comment_block>

<item;c0de:begin_block>

<recipe;c0de:begin_block>

<item;c0de:end_block>

<recipe;c0de:end_block>

<item;c0de:debug_tool>

<recipe;c0de:debug_tool>

<item;c0de:call_block>

<recipe;c0de:call_block>
