```json
{
  "title": "Verborgene Eingabe",
  "icon": "minecraft:book",
  "category": "screens",
  "ordinal": 2
}
```

Ein Laptop oder Tablet kann verbergen, was ein Spieler tippt.

Wenn ein Programm INPUT mit `PARAMETER hide TRUE ;` aufruft, zeigt die Eingabezeile für jedes Zeichen einen `*`. Die getippte Zeile erscheint auf dem Bildschirm als `***`. Verwende das für Passwörter und Schlüssel. Der Wert kommt so beim Programm an, wie er getippt wurde.

```c0de
VARIABLE password STRING
CALL INPUT
  PARAMETER variable password ;
  PARAMETER hide TRUE ;
```
