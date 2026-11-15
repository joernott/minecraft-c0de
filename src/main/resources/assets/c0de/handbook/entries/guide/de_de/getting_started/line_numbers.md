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

Dunkelgelb ist beim Bauen normal. Ein BEGIN ohne sein END bleibt dunkelgelb, bis du das END platzierst. Das Debugging-Werkzeug sagt dir, was mit einer roten Zeile nicht stimmt.
