```json
{
  "title": "c0de/security",
  "icon": "c0de:include_block",
  "category": "libraries",
  "ordinal": 14
}
```

Programme brauchen manchmal ein Passwort oder einen Schlüssel, zum Beispiel für einen Webdienst. c0de hält sie auf drei Arten verborgen:

- INPUT mit `PARAMETER hide TRUE ;` zeigt auf dem [Laptop](^running_programs/talking_to_players) `*`, während du tippst.
- Variablen-, Konstanten- und Literalkarten können ihren Wert endgültig verbergen (**Wert verbergen** in ihrem Dialog).
- Der Tag `security: "sensitive"` an einer Variable oder Konstante zeigt im [Debugging-Werkzeug](^tools/debugging_tool) `***`.

Verbergen schützt vor Leuten, die auf deinen Bildschirm schauen. Echte Geheimnisse gehören in deinen [Tresor](^libraries/secrets_in_the_vault).

**Rezepte**

<item;c0de:laptop>

<recipe;c0de:laptop>

<item;c0de:literal_item>

<recipe;c0de:literal_item>

<item;c0de:debug_tool>

<recipe;c0de:debug_tool>
