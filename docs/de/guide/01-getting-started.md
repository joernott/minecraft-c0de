# Erste Schritte

c0de ist eine kleine Programmiersprache in Minecraft. Du schreibst Programme, indem du Blöcke platzierst, und du startest sie mit Auslöserblöcken. Dieses Kapitel zeigt dir alles und führt dich durch dein erstes Programm.

## Was ist c0de?

c0de ist von der alten Sprache BASIC und von den frühen Lego Mindstorms inspiriert. Statt Text zu tippen, legst du bunte Sprachblöcke in eine Reihe. Jede Reihe ist eine Zeile deines Programms.

Programme leben in einer eigenen Dimension, der c0de-Dimension. Die Auslöserblöcke, die sie starten, können überall stehen: in deiner Basis, im Nether oder direkt neben dem Programm selbst.

Alle Blöcke und Gegenstände von c0de werden aus Vanilla-Materialien hergestellt. Du musst nicht programmieren können. Diese Anleitung erklärt alles Schritt für Schritt.

## Die Dimension betreten

Stelle einen **Auslöserblock** und einen **Schraubenschlüssel** her (siehe das Kapitel über Werkzeuge). Platziere den Auslöserblock irgendwo. Dann klicke ihn mit dem Schraubenschlüssel mit rechts an.

Du wirst in die c0de-Dimension teleportiert. Wenn der Auslöserblock noch nicht eingerichtet ist, landest du vor dem ersten freien Blatt. Sonst landest du vor dem Programm, das er ausführt.

Die Dimension hat keine Mobs, kein Wetter und keine Nacht. Der Himmel ist immer blau. Um sie zu verlassen, klicke mit leerer Hand mit rechts auf den schwarzen Boden. Du kehrst an den Ort zurück, an dem du sie betreten hast.

## Blätter

Der Boden sieht aus wie altes Computerpapier mit grünen und weißen Streifen. Jeder Papierstreifen ist ein **Blatt**. Ein Blatt ist 32 Blöcke breit und reicht endlos nach Süden.

Jedes Blatt enthält höchstens ein Programm. Der erste Spieler, der auf einem leeren Blatt baut, besitzt es: Auf einem Server können nur du und die Administratoren dein Blatt ändern. Wenn du die erste Zeile entfernst, ist das Blatt wieder frei. Nördlich der Ecke jedes Blatts zeigt der Boden seine Nummer, seine Koordinaten und seinen Besitzer. Um dein Blatt einem anderen Spieler zu geben, klicke diesen Text mit dem Schraubenschlüssel mit rechts an und wähle den Spieler. Die schwarze Fläche außerhalb der Blätter gehört niemandem. Dort kannst du Auslöserblöcke platzieren, um deine Programme zu starten.

## Der Aufbau eines Blatts

Ein Programm beginnt am Nordende eines Blatts und verläuft nach Süden. Jede Reihe von Blöcken quer über das Blatt ist eine **Zeile**. Die erste Zeile ist Zeile 0.

Innerhalb eines Blatts zählt x vom Westrand aus. Sprachblöcke kommen zwischen x=5 und x=28, also hat eine Zeile höchstens 24 Blöcke. So bleiben Zeilen kurz und gut lesbar.

Bei x=4 liegt eine graue Schiene: die **Cursorschiene**. Wenn ein Programm läuft, bewegt sich ein leuchtender Cursor darauf entlang und zeigt, welche Zeile gerade arbeitet.

## Zeilennummern

Bei x=2 zeigt der Boden die Nummer jeder Zeile, die Blöcke enthält. Ihre Farbe sagt dir, wie es um die Zeile steht:

- blass: eine NOOP- oder COMMENT-Zeile
- dunkelgrün: die Zeile ist korrekt
- dunkelgelb: bis hierher korrekt, aber die Anweisung ist noch nicht fertig
- dunkelrot: etwas stimmt nicht

Dunkelgelb ist beim Bauen normal. Ein BEGIN ohne sein END bleibt dunkelgelb, bis du das END platzierst. Das Debugging-Werkzeug sagt dir, was mit einer roten Zeile nicht stimmt.

## Blöcke platzieren

Alle Sprachblöcke sind so hoch wie eine Stufe. Du platzierst sie auf dem Boden zwischen x=5 und x=28. Der erste Block einer Zeile ist der **Schlüsselwortblock**. Er bestimmt, was die Zeile tut.

Manche Fehler werden sofort verhindert. Wenn ein Block irgendwo nicht hin darf, wird er nicht platziert, und du bekommst eine Nachricht mit dem Grund.

Manche Blöcke brauchen mehr Informationen. Klicke einen LABEL-, LITERAL-, OPERATOR-, COMMENT- oder TAG-Block mit **leerer Hand** mit rechts an, um seinen Dialog zu öffnen. Klicke auf das grüne Häkchen, um zu speichern.

Ein **LABEL** enthält einen Namen, zum Beispiel den Namen einer Funktion. Ein **LITERAL** enthält einen Wert, zum Beispiel 42 oder "Hello". Ein **OPERATOR** enthält ein Zeichen, zum Beispiel + oder das `;`, das eine Rechnung beendet.

## Die Beispiele lesen

Diese Anleitung schreibt Programme als Text. Jede Textzeile ist eine Zeile auf dem Blatt. Vier Leerzeichen bedeuten einen Block weiter östlich (nach rechts).

Wörter in Großbuchstaben wie `FUNCTION` sind Schlüsselwortblöcke. Kleingeschriebene Namen sind LABEL-Blöcke. Zahlen und Text in Anführungszeichen sind LITERAL-Blöcke. Zeichen wie `+`, `⊕` und `;` sind OPERATOR-Blöcke.

`//` steht für einen COMMENT-Block. Der Text danach ist der Kommentar.

## Dein erstes Programm

Bauen wir eine Funktion, die die Zahl 42 zurückgibt. Hier ist das ganze Programm:

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

## Schritt für Schritt

- Zeile 0: ein PROGRAM-Block bei x=5, dann ein LABEL mit dem Namen `first`.
- Zeile 1: ein NOOP-Block bei x=5. Jedes Programm braucht ihn hier.
- Zeile 2: noch ein NOOP. Eine FUNCTION braucht immer direkt darüber eine NOOP-Zeile.
- Zeile 3: ein FUNCTION-Block bei x=5, dann ein LABEL mit dem Namen `answer`.

- Zeile 4: ein RESULT-Block bei x=6, ein LABEL mit dem Namen `result` und ein INTEGER-Typblock.
- Zeile 5: ein BEGIN-Block bei x=5.
- Zeile 6: ein ASSIGN-Block bei x=6, ein LABEL `result`, ein LITERAL 42 und ein OPERATOR `;`.
- Zeile 7: ein END-Block bei x=5, gefolgt von einem FUNCTION-Block.

Wenn alle Zeilennummern dunkelgrün oder blass sind, ist das Programm fertig. Lass die Zeile nach dem END leer. Die erste leere Zeile beendet das Programm.

## Den Auslöser einrichten

Verlasse die Dimension und geh zurück zu deinem Auslöserblock. Klicke ihn mit leerer Hand mit rechts an, um seinen Dialog zu öffnen.

Wähle dein Programm `first` in der Programmliste. Dann wähle die Funktion `answer` in der Funktionsliste.

## Variablenkarten

Programme bekommen und liefern Daten als **Variablenkarten**. Unsere Funktion hat ein Ergebnis und keine Parameter, also braucht sie eine leere Karte, in die sie ihr Ergebnis schreibt.

Stelle eine nicht initialisierte Variablenkarte aus einer **Datenkarte** und **hellblauem Farbstoff** her. Lege sie in das Eingabeinventar des Auslöserblocks.

## Ausführen

Gib dem Auslöserblock einen Redstone-Impuls, zum Beispiel mit einem Knopf. Das Programm läuft auf seinem Blatt in der c0de-Dimension. Während es läuft, stehen ein PID-Block und ein Cursor auf der Cursorschiene.

Wenn es fertig ist, enthält das Ausgabeinventar eine Variablenkarte. Nimm sie heraus und klicke mit rechts, während du sie hältst. Der Dialog zeigt den Namen `result`, den Typ INTEGER und den Wert 42.

Glückwunsch, du hast dein erstes c0de-Programm geschrieben!
