```json
{
  "title": "c0de/security",
  "icon": "c0de:function_block",
  "ordinal": 6
}
```

Die Sicherheits-Bibliothek bewahrt Geheimnisse in deinem Tresor auf und braucht INCLUDE c0de/security.

Jeder Spieler hat einen Tresor, solange er online ist. Er verschwindet, wenn der Spieler geht. Ein Programm verwendet den Tresor des Spielers, der es gestartet hat, und nur, wenn diesem Spieler das Blatt gehört. Sonst lösen ihre Funktionen VAULT\_UNAVAILABLE aus. Programme lesen Geheimnisse mit SECRET-Zeilen. Ein dauerhafter Tresor bewahrt Geheimnisse in einer KeePass-Datei auf, die nur dein Spiel mit deinem Passwort öffnen kann.
