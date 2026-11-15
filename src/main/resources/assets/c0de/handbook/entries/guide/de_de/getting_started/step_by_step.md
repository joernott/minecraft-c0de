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
- Zeile 2: noch ein NOOP. Eine FUNCTION braucht immer direkt darüber eine NOOP-Zeile.
- Zeile 3: ein FUNCTION-Block bei x=5, dann ein LABEL mit dem Namen `answer`.

- Zeile 4: ein RESULT-Block bei x=6, ein LABEL mit dem Namen `result` und ein INTEGER-Typblock.
- Zeile 5: ein BEGIN-Block bei x=5.
- Zeile 6: ein ASSIGN-Block bei x=6, ein LABEL `result`, ein LITERAL 42 und ein OPERATOR `;`.
- Zeile 7: ein END-Block bei x=5, gefolgt von einem FUNCTION-Block.

Wenn alle Zeilennummern dunkelgrün oder blass sind, ist das Programm fertig. Lass die Zeile nach dem END leer. Die erste leere Zeile beendet das Programm.
