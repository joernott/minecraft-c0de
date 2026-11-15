```json
{
  "title": "Geheimnisse im Tresor",
  "icon": "c0de:include_block",
  "category": "libraries",
  "ordinal": 22
}
```

Jeder Spieler hat einen Tresor, solange er online ist. SET\_SECRET legt ein Geheimnis hinein, und eine SECRET-Zeile liest es, wie eine Konstante. Nur der Besitzer des Blatts kann den Tresor verwenden, und nur, wenn er das Programm selbst startet, zum Beispiel mit einem Knopf.

Ein dauerhafter Tresor behält Geheimnisse, wenn du gehst. CREATE\_VAULT erzeugt einen, OPEN\_VAULT öffnet ihn. Dein Spiel fragt nach dem Passwort, und nur dein Spiel kann die Datei öffnen\; der Server bewahrt sie auf, kann sie aber nicht lesen.

```c0de
PROGRAM keys
NOOP
INCLUDE c0de/security
NOOP
FUNCTION setup
BEGIN
  VARIABLE exists BOOLEAN
  VARIABLE key STRING
  CALL c0de/security/HAS_VAULT
    RESULT result exists
  IF exists ;
  BEGIN
    CALL c0de/security/OPEN_VAULT
  END
  ELSE
  BEGIN
    CALL c0de/security/CREATE_VAULT
  END IF
  CALL INPUT
    PARAMETER variable key ;
    PARAMETER hide TRUE ;
  CALL c0de/security/SET_SECRET
    PARAMETER name "apiKey" ;
    PARAMETER value key ;
    PARAMETER persistent TRUE ;
END FUNCTION
```

Ein anderes Programm liest den Schlüssel dann nach OPEN\_VAULT mit `SECRET apiKey STRING`.
