```json
{
  "title": "CREATE_VAULT",
  "icon": "minecraft:book",
  "category": "c0de_security",
  "ordinal": 3
}
```

Erzeugt deinen dauerhaften Tresor und öffnet ihn.

Der Tresor ist eine KeePass-Datei auf dem Server, die nur dein Spiel öffnen kann. Ohne `password` fragt dich dein Spiel nach einem Passwort und lässt es dich wiederholen. Das Passwort verlässt dein Spiel dann nie. Wenn du schon einen dauerhaften Tresor hast, löst die Funktion VAULT\_ALREADY\_EXISTS aus. Schließt du den Dialog, wird VAULT\_CANCELLED ausgelöst.

- `password` STRING: das Passwort (optional, Standard: den Spieler fragen)
- keine Ergebnisse

```c0de
CALL c0de/security/CREATE_VAULT
```
