```json
{
  "title": "Fehler",
  "icon": "c0de:program_block",
  "category": "language",
  "ordinal": 20
}
```

Wenn etwas schiefgeht, zum Beispiel eine Division durch null, hält die Funktion an und **löst** einen Fehler **aus**. Der Fehler geht an den Aufrufer und dann weiter nach oben an dessen Aufrufer.

Wenn der Fehler die Einstiegsfunktion erreicht, **stürzt** der Prozess **ab**. Fehler im Programm selbst, zum Beispiel eine falsche Einrückung, lassen ihn sofort abstürzen.

Um einen Fehler abzufangen, füge der CALL-Zeile eine Variable vom Typ ERROR hinzu. Der Aufrufer macht dann weiter, und du kannst `code`, `message`, `context` und `line` des Fehlers prüfen.

Jeder CALL mit einer solchen Variablen schreibt in sie, auch wenn nichts schiefging: Dann ist ihr `code` wieder 0 und ihre `message` leer. Prüfe sie also direkt nach dem CALL.

Die Namen der Fehler sind Konstanten, zum Beispiel `DIVISION_BY_ZERO`. Dazu gehören die Fehler der Bibliotheken von c0de, wie PERMISSION\_DENIED, CHUNK\_NOT\_LOADED, INVENTORY\_FULL und ITEM\_MISSING. Fehler von Bibliotheken anderer Mods sind nur Konstanten, wenn die Mod sie hinzufügt. Sonst vergleiche `code` mit der Zahl aus ihrem Handbuch.

```c0de
VARIABLE problem ERROR
CALL fail problem
ASSIGN code problem.code ;
```

**Rezepte**

<item;c0de:type_error_block>

<recipe;c0de:type_error_block>

<item;c0de:call_block>

<recipe;c0de:call_block>
