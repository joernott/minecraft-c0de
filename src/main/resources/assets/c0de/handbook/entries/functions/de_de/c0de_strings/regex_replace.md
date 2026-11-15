```json
{
  "title": "REGEX_REPLACE",
  "icon": "minecraft:book",
  "category": "c0de_strings",
  "ordinal": 16
}
```

Ersetzt, was ein regulärer Ausdruck findet, wie der Befehl `s` von sed.

In `replacement` steht `&` für den ganzen Treffer und `\1` bis `\9` für die Gruppen des Musters\; `\&` und `\\` sind ein `&` und ein Backslash, `\n` ist ein Zeilenumbruch. Ohne `flags` wird nur der erste Treffer ersetzt. Die Flags sind `g` für alle Treffer, eine Zahl N für den N-ten Treffer (mit `g` den N-ten und alle danach), `i`, um Groß- und Kleinschreibung zu ignorieren, `m` für `^` und \`# Funktionen

Ein Funktionsaufruf führt eine Funktion mit einer CALL-Zeile aus. Die PARAMETER-Zeilen darunter geben die Werte an, die die Funktion braucht. Die RESULT-Zeilen nennen die Variablen, die bekommen, was die Funktion zurückgibt. Ergebnisse, die du nicht aufführst, werden verworfen.

Eingebaute Funktionen funktionieren überall. Bibliotheksfunktionen brauchen zuerst eine INCLUDE-Zeile für ihre Bibliothek, z. B. `INCLUDE c0de/math`. Dann rufst du sie mit ihrem vollen Namen auf, z. B. `c0de/math/SIN`. Wenn ein Aufruf fehlschlägt und kein Fehler-Label hat, wird der Fehler ausgelöst.
