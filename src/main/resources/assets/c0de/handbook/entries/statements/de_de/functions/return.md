```json
{
  "title": "RETURN",
  "icon": "c0de:return_block",
  "category": "functions",
  "ordinal": 3
}
```

RETURN beendet die Funktion sofort.

Es darf eine Variable vom Typ ERROR nennen, um dem Aufrufer ein Problem zu melden. Ohne Namen gibt die Funktion „kein Fehler“ zurück. Die Ergebnisse behalten die Werte, die sie in diesem Moment haben.

Der Code eines Fehlers, den du selbst erzeugst, muss 1000 oder höher sein, sonst löst RETURN INVALID\_ARGUMENT aus. Einen Fehler, den deine Funktion von einem CALL bekommen hat, kannst du mit seinem Code weitergeben.

<recipe;c0de:return_block>

Lege eine Fichtentür in die Mitte und acht NOOP-Blöcke darum herum. Du bekommst acht RETURN-Blöcke.

```c0de
FUNCTION fail
BEGIN
  VARIABLE e ERROR
  ASSIGN e.code 1000 ;
  RETURN e
END FUNCTION
```
