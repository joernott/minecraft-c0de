```json
{
  "title": "SECRET",
  "icon": "c0de:secret_block",
  "category": "values_and_names",
  "ordinal": 4
}
```

Eine SECRET-Zeile nennt ein Geheimnis aus deinem Tresor: ein Passwort oder einen Schlüssel, den ein Programm braucht, den aber niemand sehen soll.

Sie sieht aus wie eine CONSTANT-Zeile ohne Wert: `SECRET apiKey STRING`. Der Typ ist BOOLEAN, INTEGER, FLOAT, CHAR, STRING oder TIME. Wenn der Cursor die Zeile erreicht, nimmt sie den Wert des Geheimnisses mit diesem Namen aus dem Tresor des Spielers, der das Programm gestartet hat. Du verwendest sie wie eine Konstante. Das Debugging-Werkzeug zeigt `***` statt ihres Werts.

Der Tresor funktioniert nur für den Besitzer des Blatts, der das Programm startet, zum Beispiel mit einem Knopf. Ohne Tresor löst die Zeile VAULT\_UNAVAILABLE aus. Hat der Tresor kein Geheimnis mit diesem Namen, löst sie SECRET\_NOT\_FOUND aus. Die Funktionen von c0de/security legen Geheimnisse in den Tresor.

<recipe;c0de:secret_block>

<recipe;c0de:constant_block_from_secret_block>

Stelle einen CONSTANT-Block und einen Eisenbarren zusammen her, oder verwandle einen SECRET-Block zurück in einen CONSTANT-Block.

```c0de
SECRET apiKey STRING
VARIABLE url STRING TAG(security: "sensitive")
ASSIGN url "https://example.org/weather?key=" ⊕ apiKey ;
```
