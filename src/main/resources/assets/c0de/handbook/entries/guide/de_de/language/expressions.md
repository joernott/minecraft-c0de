```json
{
  "title": "Ausdrücke",
  "icon": "c0de:program_block",
  "category": "language",
  "ordinal": 8
}
```

Ein Ausdruck berechnet einen Wert aus Literalen, Variablen, Konstanten und Operatoren. Er endet immer mit einem OPERATOR-Block, der `;` zeigt. Ein Ausdruck darf in der nächsten Zeile weitergehen, einen Block weiter östlich.

Rechnungen folgen den üblichen Regeln: `*` vor `+`. Verwende `(` und `)` zum Gruppieren. Eine Division mit `/` ergibt immer ein FLOAT.

Ein Minuszeichen vor einem Namen oder einer `(` dreht das Vorzeichen um: `- x * 2 ;`. Das Minuszeichen ist ein eigener OPERATOR-Block, deshalb schreibt die Textschreibweise `- x`, nicht `-x`. `-7` in einem Stück ist ein LITERAL mit einer negativen Zahl.

Manche Regeln sind weniger üblich. `\` bindet schwächer als `*` und `/`: `7 \ 3 * 2` ist `7 \ 6`, also setze Klammern. `⊕` bindet schwächer als `+`, und AND bindet stärker als OR: `a OR b AND c` bedeutet `a OR ( b AND c )`.

Du kannst in einem Ausdruck keine Funktion aufrufen. Verwende stattdessen eine CALL-Anweisung.

**Rezepte**

<item;c0de:operator_block>

<recipe;c0de:operator_block>

<item;c0de:type_float_block>

<recipe;c0de:type_float_block>

<item;c0de:literal_block>

<recipe;c0de:literal_block>

<item;c0de:call_block>

<recipe;c0de:call_block>
