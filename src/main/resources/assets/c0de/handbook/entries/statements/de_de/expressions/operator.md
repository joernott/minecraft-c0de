```json
{
  "title": "OPERATOR",
  "icon": "c0de:operator_block",
  "category": "expressions",
  "ordinal": 1
}
```

Ein OPERATOR-Block enthält ein Symbol eines Ausdrucks.

Klicke ihn mit leerer Hand mit rechts an und klicke auf ein Symbol. Das kann ein Operator sein, `(`, `)` oder das `;`, das jeden Ausdruck beendet. Ein neuer Block zeigt ein Fragezeichen.

- Rechnen: `+` `-` `*` `/` `^` (Potenz) `\` (Rest)
- Vergleichen: `=` `≠` `<` `>` `≤` `≥`
- Logik: `NOT` `AND` `OR` `XOR`, Text: `⊕`, Bits: `&` `|` `⊻` `¬` `←` `→`

<recipe;c0de:operator_block>

Lege rote Wolle in die Mitte und acht NOOP-Blöcke darum herum. Du bekommst acht OPERATOR-Blöcke.

`*` kommt vor `+`, und `(` und `)` fassen zusammen. `=` vergleicht nur\; um einen Wert zu speichern, verwende ASSIGN.

`\` bindet schwächer als `*` und `/`, `⊕` schwächer als `+`, und AND stärker als OR. Ein `-` vor einem Namen oder einer `(` dreht das Vorzeichen um: `- x`. `\` nimmt nur INTEGERs und behält das Vorzeichen der linken Zahl: `-7 \ 2` ist `-1`. Eine Division durch null löst DIVISION\_BY\_ZERO aus, auch bei FLOATs.

```c0de
ASSIGN arithmetic 1 + 2 * ( 3 + 3 ) \ 10 * 2 + 10 ;
```
