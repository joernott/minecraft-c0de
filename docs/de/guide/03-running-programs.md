# Programme ausführen

Ein Programm auf einem Blatt tut von selbst nichts. Auslöserblöcke starten es, versorgen es mit Daten und sammeln seine Ergebnisse ein. Ein laufendes Programm heißt **Prozess**.

## Drei Auslöserblöcke

- **Auslöserblock**: startet einen Prozess, wenn er einen Redstone-Impuls bekommt.
- **Zeitgeberblock**: startet einen Prozess immer wieder, in einem Abstand, den du einstellst.
- **Debug-Auslöserblock**: startet einen Prozess, wenn du ihn mit dem Debugging-Werkzeug schlägst.

Nur ein Signal, das angeht, startet einen Auslöserblock. Einen Hebel auszuschalten startet ihn nicht, und ein Signal, das an bleibt, startet ihn nicht noch einmal.

Auslöserblöcke funktionieren in jeder Dimension. In der c0de-Dimension kannst du sie nur auf den schwarzen Boden außerhalb der Blätter setzen.

Ein Redstone-Signal pausiert einen Zeitgeberblock. Verwende einen Hebel, um ihn abzuschalten. Da ein Blatt geschützt ist, solange sein Programm läuft, pausiere einen schnellen Zeitgeber, bevor du das Programm änderst; sein Dialog zeigt dann „Durch Redstone pausiert“.

## Der Auslöser-Dialog

Klicke einen Auslöserblock mit leerer Hand mit rechts an, um seinen Dialog zu öffnen. Er zeigt:

- die PID des laufenden Prozesses
- den Exit-Code des letzten Prozesses und seinen Fehler, falls er abgestürzt ist
- eine Liste zur Auswahl des Programms
- eine Liste zur Auswahl der auszuführenden Funktion
- bei Zeitgeberblöcken den Abstand in Ticks

Der Dialog hat außerdem ein Eingabeinventar, ein Ausgabeinventar und einen Platz für Tarnung. Lege einen beliebigen Block in diesen Platz, und der Auslöserblock nimmt sein Aussehen an.

Die Programm- und die Funktionsliste sind gesperrt, solange ein Prozess läuft.

## Der Besitzer

Der Spieler, der einen Auslöserblock zuletzt eingerichtet hat, ist sein **Besitzer**. Seine Programme handeln im Namen dieses Spielers. Das ist wichtig für die Bibliothek c0de/minecraft und für das Abbrechen von Prozessen.

## Variablenkarten

Daten kommen als **Variablenkarten** und **Konstantenkarten** im Eingabeinventar in einen Prozess. Nur diese Gegenstände passen dort hinein.

Jede Karte hat einen Namen, einen Typ und einen Wert. Eine Karte geht an den Parameter der Einstiegsfunktion mit demselben Namen. Ihr Typ muss zum Typ des Parameters passen.

Um eine Variablenkarte zu bearbeiten, halte sie in deiner Haupthand und klicke mit rechts. Im Dialog kannst du ihren Namen, ihren Typ und ihren Wert einstellen. Eine Konstantenkarte kann nur bearbeitet werden, bis sie einen Wert hat.

## Nicht initialisierte Karten

Eine **nicht initialisierte** Variablenkarte hat keinen Namen, keinen Typ und keinen Wert. Ein Prozess schreibt ein Ergebnis hinein.

Du brauchst eine für jedes Ergebnis, das keine Parameterkarte hat, die seinen Platz einnimmt. Eine Funktion mit einem Ergebnis und ohne Parameter braucht eine nicht initialisierte Karte.

Übrige nicht initialisierte Karten bleiben im Eingabeinventar. So kannst du dort mehrere davon für viele Durchläufe stapeln.

## Was ein Prozess nimmt

Wenn ein Prozess startet, nimmt er aus dem Eingabeinventar:

- eine Karte für jeden Parameter, für den es eine Karte gibt,
- eine nicht initialisierte Karte für jedes zusätzliche Ergebnis,
- alle anderen initialisierten Variablen- und Konstantenkarten.

Diese anderen Karten werden nicht verwendet. Sie werden unverändert in das Ausgabeinventar weitergereicht, wenn der Prozess endet.

## Ergebnisse

Wenn ein Prozess normal endet, bekommt das Ausgabeinventar eine Variablenkarte pro Ergebnis der Einstiegsfunktion. Jede Karte trägt den Namen, den Typ und den Wert ihres Ergebnisses. Ein EXIT zählt als normales Ende: Die Ergebnisse bekommen die Werte, die sie haben, wenn EXIT läuft.

Wenn die Funktion weniger Ergebnisse als Parameter hat, bekommst du auch leere Variablenkarten zurück. So ist die Zahl der Karten, die hinein- und herausgehen, gleich.

## Wenn er nicht startet

Ein Auslöserblock startet keinen Prozess, wenn:

- er noch kein Programm oder keine Funktion hat,
- sein letzter Prozess noch läuft,
- eine Karte für einen Parameter ohne Standardwert fehlt,
- eine nicht initialisierte Karte für ein Ergebnis fehlt,
- das Ausgabeinventar zu wenige freie Plätze hat.

## Exit-Codes

Jeder Prozess endet mit einem **Exit-Code** von 0 bis 15. Er ist 0, wenn das Programm normal endet, die Zahl aus einer EXIT-Anweisung oder 15, wenn der Prozess abgestürzt ist.

Ein Komparator, der den Auslöserblock ausliest, gibt ein Signal aus, das so stark ist wie der Exit-Code. Das Signal bleibt, bis der nächste Prozess startet, und ist 0, solange ein Prozess läuft. So kann deine Redstone-Schaltung auf das Ergebnis eines Programms reagieren. Ein Komparator liest Zeitgeberblöcke und Debug-Auslöserblöcke genauso aus.

## Abstürze

Wenn ein Prozess abstürzt, spielt der Auslöserblock einen tiefen Ton. Sein Dialog zeigt den Fehlercode, die Meldung, die Funktion und die Zeile, in der es passiert ist.

Ein abgestürzter Prozess erzeugt keine Ergebnisse. Alle Karten, die er genommen hat, gehen unverändert in das Ausgabeinventar.

## Verketten

Trichter, Rohre und Kabel können einen Auslöserblock von allen Seiten nutzen. Ein Trichter über oder neben ihm füllt die Eingabe. Ein Trichter unter ihm leert die Ausgabe.

So kannst du Ketten bauen: Auslöserblock, Trichter, Auslöserblock, Trichter, Truhe. Jeder Prozess verwendet nur die Karten, die er braucht, und reicht den Rest weiter. Das Ende der Kette sammelt alles.

Wenn genau ein Block mit Inventar einen Auslöserblock berührt, geht die Ausgabe von selbst dorthin. Ein anderer Auslöserblock zählt auch: Die Karten landen in seiner Eingabe. Ein Trichter, der in den Auslöserblock zeigt, zählt nicht.

## Mit Spielern sprechen

Ein Prozess kann auch mit Spielern sprechen, während er läuft. Drei eingebaute Funktionen tun das, aufgerufen mit CALL wie jede andere Funktion:

- **PRINT** schreibt Text auf Bildschirme.
- **INPUT** liest einen Wert in eine Variable.
- **WRITE** legt einen Wert als Variablenkarte in einen E/A-Block.

Die Geräte dafür sind Anzeigeblöcke, Laptops, Tablets und E/A-Blöcke. Verbinde jedes davon mit einem Programm und einer Funktion, wie einen Auslöserblock. Anzeigeblöcke nebeneinander bilden einen großen Bildschirm.

COLOR ändert die Farben des Texts, der danach kommt. Die Konstanten COLOR_BLACK bis COLOR_WHITE benennen die 16 Farben:

```c0de
PROGRAM hello
NOOP
FUNCTION greet
BEGIN
    CALL CLS
    CALL COLOR
        PARAMETER foreground COLOR_YELLOW ;
        PARAMETER background COLOR_DARK_BLUE ;
    CALL PRINT
        PARAMETER value "Process " ;
    CALL PRINT
        PARAMETER value PID.pid ;
    CALL COLOR
        PARAMETER foreground COLOR_WHITE ;
END FUNCTION
```

INPUT nimmt zuerst eine passende Karte aus einem E/A-Block. Sonst wartet es, bis ein Spieler auf einem Laptop oder Tablet eine Zeile eintippt. Die getippte Zeile erscheint auf seinem Bildschirm, du siehst also deine Antwort. Die Handbücher Funktionen und Geräte beschreiben sie alle.

## Geschützte Programme

Solange ein Prozess läuft, ist sein Blatt gesperrt. Niemand kann darauf Blöcke setzen oder abbauen, nicht einmal Kolben oder Explosionen. Der Schraubenschlüssel, der Hammer und die Zange funktionieren dort nicht.

Dialoge von Blöcken auf dem Blatt öffnen sich schreibgeschützt. Der Textmarker funktioniert weiterhin, weil er nichts ändert. Wenn du versuchst, ein gesperrtes Blatt zu ändern, bekommst du eine Nachricht, die die laufenden Prozesse nennt.

## PID-Blöcke

Jeder Prozess bekommt eine Nummer, seine **PID**. Wenn er startet, erscheint ein dunkler PID-Block auf der Cursorschiene in Zeile 0. Seine Höhe ist die PID: der erste freie Platz, von y=0 aufwärts gezählt.

Der PID-Block zeigt die PID in Grün auf seiner Nord- und Südseite. Seine Ost- und Westseite zeigen, wo der Auslöserblock steht.

Mehrere Prozesse eines Programms können gleichzeitig laufen, einer pro Auslöserblock. Jeder hat seinen eigenen PID-Block und seinen eigenen Cursor. Sie teilen sich keine Variablen.

## Der Cursor

Der Cursor ist eine leuchtende grüne Säule auf der Cursorschiene. Er steht in der Zeile, die gerade bearbeitet wird. Er rückt pro Tick um eine Zeile weiter.

Ein Debug-Cursor leuchtet rot. Du bewegst ihn selbst mit dem Debugging-Werkzeug, siehe das Kapitel über Werkzeuge.

## Einen Prozess abbrechen

Klicke einen PID-Block mit dem Debugging-Werkzeug bei gedrückter Umschalttaste mit rechts an, um seinen Prozess anzuhalten. Der Prozess stürzt mit dem Fehler PROCESS_ABORTED ab, und seine Karten gehen in das Ausgabeinventar.

Nur der Besitzer des Auslöserblocks und Server-Operatoren können das tun.
