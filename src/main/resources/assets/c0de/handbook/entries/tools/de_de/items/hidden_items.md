```json
{
  "title": "Verborgene Karten",
  "icon": "minecraft:book",
  "category": "items",
  "ordinal": 3
}
```

Variablen-, Konstanten- und Literalkarten können ihren Wert endgültig verbergen.

Stelle im Dialog der Karte **Wert verbergen** auf „Ja, endgültig“, bevor du sie speicherst. Ab dann zeigen der Dialog und der Tooltip `***`. Eine Variablenkarte kannst du trotzdem umbenennen. Nur der Mülleimer-Knopf entfernt den Wert und damit auch das Verbergen. Verborgene Karten stapeln sich nicht mit Karten, die nicht verborgen sind.

Ein Prozess kann den Wert wie jeden anderen verwenden. Der Parameter, den er füllt, zeigt im Debugging-Werkzeug `***`. Verbergen schützt vor Leuten, die auf deinen Bildschirm schauen, nicht vor dem Server: Für Geheimnisse, die sicher bleiben müssen, nimm den Tresor von c0de/security.

```c0de
FUNCTION login
  PARAMETER password STRING
BEGIN
  CALL PRINT
    PARAMETER value "Logged in" ;
END FUNCTION
```
