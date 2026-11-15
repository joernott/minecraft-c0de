```json
{
  "title": "Besitzer und Berechtigungen",
  "icon": "c0de:include_block",
  "category": "libraries",
  "ordinal": 12
}
```

Programme handeln im Namen des Besitzers des Auslöserblocks. Die weltverändernden Funktionen oben funktionieren nur, wenn der Besitzer ein Operator ist oder wenn der Server sie in der Datei `config/c0de.properties` erlaubt.

Sonst lösen sie PERMISSION\_DENIED aus. Spieler können nur teleportiert werden, wenn der Besitzer ein Operator ist, und nur Operatoren können mit COMMAND Befehle ausführen oder Befehlsblöcken ihre Daten geben.

Drei Funktionen funktionieren für jeden Besitzer, weil sie nur tun, was auch ein Spieler kann:

- MOVE\_ITEMS verschiebt Gegenstände zwischen zwei Truhen, die der Besitzer öffnen könnte. Die Gegenstände sind unterwegs, ein Stapel pro Tick, und ein Filter wie `$logs` oder `minecraft:wheat` wählt sie aus.
- MINE\_BLOCK baut einen Block mit dem besten Werkzeug aus einer Truhe ab (`tool_inventory`). Das Werkzeug nutzt sich ab, und es dauert so lange wie bei einem Spieler. Was dabei herausfällt, kommt in eine andere Truhe (`output_inventory`) oder fällt auf den Boden. Ist diese Truhe voll, löst die Funktion INVENTORY\_FULL aus.
- PLACE\_BLOCK mit dem Parameter `inventory` nimmt den Block aus einer Truhe. Enthält die Truhe ihn nicht, löst die Funktion ITEM\_MISSING aus.

**Rezepte**

<item;c0de:trigger_block>

<recipe;c0de:trigger_block>
