```json
{
  "title": "c0de/security",
  "icon": "c0de:include_block",
  "category": "libraries",
  "ordinal": 21
}
```

Programme brauchen manchmal ein Passwort oder einen Schlüssel, zum Beispiel für einen Webdienst. c0de hält sie auf drei Arten verborgen:

- INPUT mit `PARAMETER hide TRUE ;` zeigt auf dem Laptop `*`, während du tippst.
- Variablen-, Konstanten- und Literalkarten können ihren Wert endgültig verbergen (**Wert verbergen** in ihrem Dialog).
- Der Tag `security: "sensitive"` an einer Variable oder Konstante zeigt im Debugging-Werkzeug `***`.

Verbergen schützt vor Leuten, die auf deinen Bildschirm schauen. Echte Geheimnisse gehören in deinen Tresor.
