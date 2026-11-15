```json
{
  "title": "OPEN_VAULT",
  "icon": "minecraft:book",
  "category": "c0de_security",
  "ordinal": 4
}
```

Öffnet deinen dauerhaften Tresor und fügt seine Geheimnisse zu deinem Tresor hinzu.

Ohne `password` fragt dich dein Spiel danach\; nach einem falschen kannst du es noch einmal versuchen. Ein falsches `password` löst WRONG\_PASSWORD aus. Ohne dauerhaften Tresor löst die Funktion VAULT\_NOT\_FOUND aus. Ein offener Tresor bleibt offen, bis du gehst.

- `password` STRING: das Passwort (optional, Standard: den Spieler fragen)
- keine Ergebnisse

```c0de
CALL c0de/security/OPEN_VAULT
```
