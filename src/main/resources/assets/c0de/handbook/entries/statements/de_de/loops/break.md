```json
{
  "title": "BREAK",
  "icon": "c0de:break_block",
  "category": "loops",
  "ordinal": 4
}
```

BREAK verlässt sofort die innerste Schleife.

Es funktioniert in FOR-, EACH-, WHILE- und REPEAT-Schleifen. Es darf eine Bedingung haben: Dann verlässt es die Schleife nur, wenn die Bedingung wahr ist. Ein BREAK außerhalb einer Schleife ist ein Fehler.

<recipe;c0de:break_block>

Stelle einen NOOP-Block und einen Stock zusammen her.

```c0de
FOR divisor INTEGER 2 ; candidate - 1 ;
BEGIN
  BREAK candidate \ divisor = 0 ;
END FOR
```

Die Schleife stoppt, sobald ein Teiler gefunden ist.
