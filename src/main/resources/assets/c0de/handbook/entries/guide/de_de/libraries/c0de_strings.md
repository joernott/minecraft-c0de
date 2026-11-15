```json
{
  "title": "c0de/strings",
  "icon": "c0de:include_block",
  "category": "libraries",
  "ordinal": 6
}
```

Text und Zeichen:

- LEFT, RIGHT, MID: Teile eines Texts
- REPLACE: einen Teil eines Texts überschreiben
- INSTR: einen Text in einem Text finden, -1, wenn er nicht gefunden wird
- UCASE, LCASE: Groß- und Kleinbuchstaben
- SPACE, FILL: ein Text aus Leerzeichen oder aus einem Zeichen
- ASC, CHR: Zeichen und ihre Codenummern

Umwandlungen:

- STR: ein beliebiger einfacher Wert als Text
- [FORMAT](^libraries/format): ein Wert als Text in einem Format deiner Wahl
- VAL: ein Text als Zahl
- HEX, OCT: eine Zahl hexadezimal oder oktal

Reguläre Ausdrücke:

- REGEX\_MATCH: passt ein Text zu einem Muster wie `^[0-9]+$`?
- REGEX\_REPLACE: ersetzen, was ein Muster findet, wie sed
