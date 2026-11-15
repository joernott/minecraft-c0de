```json
{
  "title": "Operatoren",
  "icon": "c0de:program_block",
  "category": "language",
  "ordinal": 9
}
```

- Rechnen: `+` `-` `*` `/` `^` (Potenz) `\` (Rest)
- Vergleichen: `=` `≠` `<` `>` `≤` `≥`
- Logik: `NOT` `AND` `OR` `XOR`
- Text: `⊕` verbindet zwei Werte zu einem Text
- Bits: `&` `|` `⊻` `¬` `←` `→`

`=` vergleicht nur. Um einen Wert zu speichern, verwende ASSIGN. `⊕` macht aus Zahlen und Listen Text, also ergibt `'a' ⊕ 1 ⊕ "b"` den Wert `"a1b"`.

`\` geht nur mit INTEGERs. Das Ergebnis behält das Vorzeichen der linken Zahl: `-7 \ 2` ist `-1`. Eine Division durch null löst DIVISION\_BY\_ZERO aus, mit `/` und `\`, auch bei FLOATs: `1.5 / 0.0` ergibt nicht unendlich.

TIME-Werte kannst du mit `<`, `>` und den anderen vergleichen: Die frühere Zeit ist die kleinere. `⊕` schreibt eine TIME als ISO-Text in UTC mit Millisekunden, zum Beispiel `2026-10-19T12:05:09.250Z`. Für die Ortszeit verwende FORMAT aus c0de/strings.

**Rezepte**

<item;c0de:assign_block>

<recipe;c0de:assign_block>

<item;c0de:type_time_block>

<recipe;c0de:type_time_block>
