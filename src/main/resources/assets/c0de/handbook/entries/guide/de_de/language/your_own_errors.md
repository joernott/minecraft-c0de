```json
{
  "title": "Eigene Fehler",
  "icon": "c0de:program_block",
  "category": "language",
  "ordinal": 21
}
```

Du kannst eigene Fehler auslösen. Ihre Codes müssen 1000 oder höher sein. Setze den Code einer ERROR-Variablen und gib sie zurück, oder verwende die eingebaute Funktion RAISE.

```c0de
FUNCTION fail
BEGIN
  VARIABLE e ERROR
  ASSIGN e.code 1000 ;
  RETURN e
END FUNCTION
```
