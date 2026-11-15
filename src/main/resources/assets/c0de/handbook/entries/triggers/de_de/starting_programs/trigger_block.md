```json
{
  "title": "Auslöserblock",
  "icon": "c0de:trigger_block",
  "category": "starting_programs",
  "ordinal": 0
}
```

Der Auslöserblock startet einen Prozess, wenn er einen Redstone-Impuls bekommt.

Nur ein Signal, das angeht, startet ihn. Einen Hebel auszuschalten startet ihn nicht, und ein Signal, das an bleibt, startet ihn nicht noch einmal.

Klicke ihn mit leerer Hand mit rechts an, um seinen Dialog zu öffnen. Wähle ein Programm und eine Funktion. Lege Variablen- und Konstantenkarten in das Eingabe-Inventar. Ergebnisse landen im Ausgabe-Inventar. Gegenstände, die der Prozess nicht verwendet, wandern unverändert in die Ausgabe.

Der Knopf **Starten durch** legt fest, wer ihn starten darf: \*Nur mich\*, \*Spieler\*, \*Mobs\* oder \*Alle\*. Der Block weiß, wer einen Knopf oder Hebel an ihm gedrückt hat oder wer auf einer Druckplatte neben ihm steht. Andere Redstone-Signale starten ihn nur bei \*Alle\*. Nur du und Administratoren können einen Auslöserblock ändern, den du eingerichtet hast. Wenn ein Spieler, der kein Administrator ist, ihn startet, halten Funktionen, die mehr Rechte brauchen, das Programm an.

Ein Trichter über oder neben ihm füllt die Eingabe, ein Trichter unter ihm leert die Ausgabe. Ein angrenzender Auslöserblock bekommt die Ausgabe in seine Eingabe, so kannst du Ketten bauen. Ein Komparator liest den Exit-Code des letzten Prozesses als Signalstärke, und 0, solange ein Prozess läuft. Zeitgeberblöcke und Debug-Auslöserblöcke liest er genauso aus.

<recipe;c0de:trigger_block>

Kupferbarren in den Ecken, Glas an den Kanten und ein Goldbarren in der Mitte ergeben acht Auslöserblöcke.

Platziere einen Auslöserblock mit einem Knopf daran. Stelle ihn auf die Funktion `answer` ein und lege eine nicht initialisierte Variablenkarte in seine Eingabe. Drücke den Knopf. Die Ausgabe enthält jetzt eine Karte namens `result` mit dem Wert 42.

```c0de
PROGRAM first
NOOP
NOOP
FUNCTION answer
  RESULT result INTEGER
BEGIN
  ASSIGN result 42 ;
END FUNCTION
```
