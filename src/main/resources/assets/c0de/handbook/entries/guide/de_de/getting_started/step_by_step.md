```json
{
  "title": "Schritt für Schritt",
  "icon": "c0de:wrench",
  "category": "getting_started",
  "ordinal": 8
}
```

- Zeile 0: ein PROGRAM-Block bei x=5, dann ein LABEL mit dem Namen `first`.
- Zeile 1: ein NOOP-Block bei x=5. Jedes Programm braucht ihn hier.
- Zeile 2: noch ein [NOOP](^language/noop_and_comment). Eine [FUNCTION](^language/functions) braucht immer direkt darüber eine NOOP-Zeile.
- Zeile 3: ein FUNCTION-Block bei x=5, dann ein LABEL mit dem Namen `answer`.

- Zeile 4: ein RESULT-Block bei x=6, ein LABEL mit dem Namen `result` und ein INTEGER-Typblock.
- Zeile 5: ein BEGIN-Block bei x=5.
- Zeile 6: ein ASSIGN-Block bei x=6, ein LABEL `result`, ein LITERAL 42 und ein OPERATOR `;`.
- Zeile 7: ein END-Block bei x=5, gefolgt von einem FUNCTION-Block.

Wenn alle [Zeilennummern](^getting_started/line_numbers) dunkelgrün oder blass sind, ist das Programm fertig. Lass die Zeile nach dem END leer. Die erste leere Zeile beendet das Programm.

**Rezepte**

<item;c0de:program_block>

<recipe;c0de:program_block>

<item;c0de:label_block>

<recipe;c0de:label_block>

<item;c0de:noop_block>

<recipe;c0de:noop_block>

<item;c0de:function_block>

<recipe;c0de:function_block>

<item;c0de:result_block>

<recipe;c0de:result_block>

<item;c0de:type_integer_block>

<recipe;c0de:type_integer_block>

<item;c0de:begin_block>

<recipe;c0de:begin_block>

<item;c0de:assign_block>

<recipe;c0de:assign_block>

<item;c0de:literal_block>

<recipe;c0de:literal_block>

<item;c0de:operator_block>

<recipe;c0de:operator_block>

<item;c0de:end_block>

<recipe;c0de:end_block>
