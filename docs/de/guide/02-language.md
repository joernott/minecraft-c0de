# Die Sprache

Dieses Kapitel erklärt die Sprache c0de in Kürze. Jeder Teil ist ein Block, den du auf ein Blatt setzt. Die Beispiele verwenden die Textschreibweise aus dem ersten Kapitel.

## Programme

Ein Programm beginnt in Zeile 0 mit einem PROGRAM-Block und einem LABEL mit seinem Namen. Der Name muss in der ganzen c0de-Dimension eindeutig sein. Zeile 1 muss eine NOOP-Zeile sein.

Danach dürfen INCLUDE-Zeilen folgen. Sie laden Bibliotheken, zum Beispiel `INCLUDE c0de/math`. Dann kommen Strukturtypen, globale Variablen, Konstanten und Funktionen, in beliebiger Reihenfolge.

Ein Name kann erst verwendet werden, nachdem er definiert wurde. Setze also alles, was eine Funktion braucht, über sie. Das Programm endet an der ersten leeren Zeile.

## Zeilen und Einrückung

Globale Zeilen beginnen bei x=5: PROGRAM, INCLUDE, STRUCT, FUNCTION sowie globale VARIABLE- und CONSTANT-Zeilen. Zeilen in einem Codeblock stehen einen Block weiter östlich als sein BEGIN.

Ein Codeblock beginnt mit BEGIN und endet mit END. Auf END folgt das Schlüsselwort, das den Block geöffnet hat, zum Beispiel `END FUNCTION` oder `END WHILE`. Ein Codeblock darf nicht leer sein.

Variablen und Konstanten, die in einem Codeblock angelegt werden, leben nur darin. Sie werden vergessen, wenn der Block endet.

## NOOP und COMMENT

NOOP bedeutet „no operation“ (keine Operation). Eine NOOP-Zeile tut nichts. Verwende sie, um Teile deines Programms zu trennen. Eine NOOP-Zeile darf keinen anderen Block enthalten. Jede FUNCTION braucht direkt darüber eine NOOP-Zeile.

Ein COMMENT-Block enthält eine Notiz für Menschen. Setze ihn ans Ende einer Zeile. Der Cursor ignoriert ihn und jeden Block danach. Ein COMMENT vor Blöcken schaltet sie also ab.

Eine Zeile, die mit einem COMMENT-Block beginnt, ist eine Kommentarzeile. Sie wird wie eine NOOP-Zeile übersprungen.

```c0de
// Eine Kommentarzeile wird ganz übersprungen
VARIABLE time INTEGER 1 // ein Kommentar am Ende
ASSIGN count time ; COMMENT("disabled") + 100 ;
```

In der letzten Zeile ist `COMMENT("disabled")` ein COMMENT-Block mit dem Text "disabled". Das `+ 100 ;` danach ist abgeschaltet.

## Namen

LABEL-Blöcke enthalten Namen. Ein Name besteht aus den Buchstaben a-z und A-Z, den Ziffern 0-9 und dem Unterstrich. Er muss mit einem Buchstaben beginnen.

Schlüsselwörter in Großbuchstaben sind reserviert: `RESULT` ist kein gültiger Name, aber `result` und `Result` sind es. Bibliotheksnamen verwenden Schrägstriche, zum Beispiel `c0de/math/PI`.

Ein Label kann mit einem **Index** in einen Wert hineinzeigen, zum Beispiel `list[0]`, `stock["gold"]` oder `problem.code`. Den Index gibst du als Text im LABEL-Dialog ein.

## Typen

Ein Typblock folgt auf den Namen einer Variablen, einer Konstanten, eines Parameters oder eines Ergebnisses. Einfache Typen:

- BOOLEAN: wahr oder falsch
- INTEGER: ganze Zahlen
- FLOAT: Zahlen mit Nachkommastellen
- CHAR: ein Zeichen
- TIME: ein Zeitpunkt

Komplexe Typen:

- STRING: Text, wahlweise mit einer Längenbegrenzung
- ARRAY: eine Liste, von 0 an nummeriert
- HASH: Werte, die unter Textschlüsseln gespeichert sind
- STRUCT: dein eigener Typ mit benannten Elementen
- ERROR: ein Fehler mit Code, Meldung, Kontext und Zeile

Auf ARRAY und HASH folgt der Typ ihres Inhalts, zum Beispiel `ARRAY INTEGER`. Auf einen STRUCT-Typ folgt ein LABEL mit dem Namen der Struktur. Arrays und Hashes wachsen und schrumpfen von selbst.

## Variablen und Konstanten

Eine VARIABLE-Zeile hat einen Namen, einen Typ und einen optionalen Startwert. Ohne Startwert enthält sie den Standardwert: false, 0, 0.0, ein Leerzeichen, einen leeren Text oder eine leere Liste.

Eine CONSTANT-Zeile sieht genauso aus, muss aber einen Wert haben. Du kannst ihn später nicht ändern.

```c0de
VARIABLE b BOOLEAN TRUE
VARIABLE i INTEGER
CONSTANT BASE INTEGER 42
```

Listen, Hashes und Strukturen bekommen ihre Startwerte in einem Codeblock. Jede Zeile enthält Literale oder einen Schlüssel und einen Wert:

```c0de
VARIABLE list ARRAY INTEGER
BEGIN
    1 2 3
END VARIABLE
VARIABLE stock HASH INTEGER
BEGIN
    gold 1
END VARIABLE
```

## Strukturen

Ein STRUCT deklariert deinen eigenen Typ. Jede Zeile in seinem Codeblock ist ein Element: ein Name und ein Typ.

```c0de
STRUCT point
BEGIN
    x INTEGER
    y INTEGER
END STRUCT
```

Eine Variable dieses Typs wird als `VARIABLE p STRUCT point` deklariert. Ihre Elemente erreichst du mit einem Index, zum Beispiel `p.x`.

Jedes Programm hat außerdem ein paar Konstanten, die es nicht deklariert. `SHEET` beschreibt sein Blatt, z. B. `SHEET.number` und `SHEET.owner`. `PID` beschreibt den laufenden Prozess, z. B. `PID.pid`, `PID.activator` (wer ihn gestartet hat) und `PID.x`, `PID.y`, `PID.z` (wo sein Auslöserblock steht). `CURSOR` beschreibt den Cursor im Moment: `CURSOR.line`, `CURSOR.function`, `CURSOR.depth` und wie viel von seinem Stack belegt ist, `CURSOR.stack_used` von `CURSOR.stack_size`. `C0DE_VERSION` enthält die Version von c0de, z. B. "0.4.0". Auch die Namen der Fehler und der Farben sind Konstanten.

## Literale

Ein LITERAL-Block enthält einen festen Wert. In seinem Dialog wählst du den Typ und gibst den Wert ein. Die Textschreibweise schreibt sie so:

- `TRUE` und `FALSE`
- `42` und `-7` für ganze Zahlen, `3.14` für Gleitkommazahlen
- `'a'` für ein CHAR, `"Hello"` für einen STRING
- `@2026-10-19T12:05:09Z` für eine TIME

## Ausdrücke

Ein Ausdruck berechnet einen Wert aus Literalen, Variablen, Konstanten und Operatoren. Er endet immer mit einem OPERATOR-Block, der `;` zeigt. Ein Ausdruck darf in der nächsten Zeile weitergehen, einen Block weiter östlich.

Rechnungen folgen den üblichen Regeln: `*` vor `+`. Verwende `(` und `)` zum Gruppieren. Eine Division mit `/` ergibt immer ein FLOAT.

Du kannst in einem Ausdruck keine Funktion aufrufen. Verwende stattdessen eine CALL-Anweisung.

## Operatoren

- Rechnen: `+` `-` `*` `/` `^` (Potenz) `\` (Rest)
- Vergleichen: `=` `≠` `<` `>` `≤` `≥`
- Logik: `NOT` `AND` `OR` `XOR`
- Text: `⊕` verbindet zwei Werte zu einem Text
- Bits: `&` `|` `⊻` `¬` `←` `→`

`=` vergleicht nur. Um einen Wert zu speichern, verwende ASSIGN. `⊕` macht aus Zahlen und Listen Text, also ergibt `'a' ⊕ 1 ⊕ "b"` den Wert `"a1b"`.

## ASSIGN

ASSIGN speichert den Wert eines Ausdrucks in einer Variablen. Der Wert muss zum Typ der Variablen passen. Ein INTEGER darf in ein FLOAT und ein CHAR in einen STRING.

```c0de
ASSIGN arithmetic 1 + 2 * ( 3 + 3 ) \ 10 * 2 + 10 ;
ASSIGN list[0] 10 ;
ASSIGN list[1] 20 ;
ASSIGN list[2] list[0] + list[1] ;
```

Eine Zuweisung an den Index direkt nach dem Ende einer Liste fügt ein neues Element hinzu. Ein neuer Schlüssel fügt einem Hash ein neues Element hinzu.

## Funktionen

Eine FUNCTION-Zeile beginnt bei x=5 mit dem Namen der Funktion. PARAMETER-Zeilen darunter benennen die Eingaben, RESULT-Zeilen die Ausgaben. Dann folgt der Codeblock.

Ein Parameter darf einen Standardwert haben. Ein Aufrufer kann einen solchen Parameter weglassen. Ergebnisse beginnen mit dem Standardwert ihres Typs.

```c0de
FUNCTION greet
    PARAMETER name STRING "World"
    PARAMETER mark CHAR '!'
    RESULT result STRING
BEGIN
    ASSIGN result "Hello, " ⊕ name ⊕ mark ;
END FUNCTION
```

Wenn die Funktion endet, gehen die letzten Werte ihrer Ergebnisse an den Aufrufer zurück.

## CALL

CALL führt eine Funktion aus. PARAMETER-Zeilen geben ihr Werte, RESULT-Zeilen sagen, welche Variable welches Ergebnis bekommt. Ergebnisse, die du nicht aufführst, werden verworfen.

```c0de
CALL greet
    PARAMETER name "c0de" ;
    RESULT result greeting
```

Hier wird `greeting` zu "Hello, c0de!". Eine Funktion kann nur Funktionen aufrufen, die über ihr definiert sind, oder sich selbst.

## RETURN

RETURN beendet die Funktion sofort. Es kann eine Variable vom Typ ERROR nennen, um dem Aufrufer ein Problem zu melden.

```c0de
FUNCTION sum
    PARAMETER n INTEGER
    RESULT result INTEGER
BEGIN
    IF n = 0 ;
    BEGIN
        RETURN
    END IF
    CALL sum
        PARAMETER n n - 1 ;
        RESULT result result
    ASSIGN result result + n ;
END FUNCTION
```

Diese Funktion ruft sich selbst auf. Sie addiert alle Zahlen von 1 bis n.

## IF und ELSE

IF führt seinen Codeblock nur aus, wenn sein Ausdruck wahr ist. Sonst läuft ein ELSE-Block. Mit einem ELSE steht das erste END allein. Auf das letzte END folgt IF.

```c0de
IF prime ;
BEGIN
    ASSIGN primes primes ⊕ candidate ⊕ " " ;
END
ELSE
BEGIN
    NOOP
END IF
```

Es gibt kein ELSE IF. Setze stattdessen ein weiteres IF in den ELSE-Block.

## FOR-Schleifen

FOR zählt. Es hat eine Schleifenvariable, ihren Typ (INTEGER oder FLOAT), einen Startwert und einen Endwert. Ein optionaler dritter Wert ist die Schrittweite, die 1 ist, wenn du sie weglässt.

Der Endwert ist eingeschlossen. Du kannst die Schleifenvariable nicht selbst ändern.

```c0de
FOR i INTEGER 1 ; 10 ; 2 ;
BEGIN
    NOOP
END FOR
```

## EACH-Schleifen

EACH geht durch ein Array oder einen Hash. Bei einem Array bekommt die Schleifenvariable jedes Element. Bei einem Hash bekommt sie jeden Schlüssel in alphabetischer Reihenfolge.

```c0de
EACH key h
BEGIN
    ASSIGN keys keys ⊕ key ⊕ "=" ⊕ h[key] ⊕ " " ;
END EACH
```

## WHILE und REPEAT

WHILE prüft seine Bedingung zuerst. Ist sie gleich zu Beginn falsch, läuft der Block nie. REPEAT prüft am Ende, also läuft sein Block mindestens einmal. Seine Bedingung kommt nach `END REPEAT`.

```c0de
WHILE n > 0 ;
BEGIN
    ASSIGN countdown countdown ⊕ n ⊕ " " ;
    ASSIGN n n - 1 ;
END WHILE
REPEAT
BEGIN
    ASSIGN repeats repeats + 1 ;
END REPEAT n > 0 ;
```

## BREAK

BREAK verlässt sofort die innerste Schleife. Es kann eine Bedingung haben: Dann verlässt es die Schleife nur, wenn die Bedingung wahr ist.

## EXIT

EXIT beendet sofort den ganzen Prozess, egal wie tief der Cursor steckt. Es braucht eine Zahl von 0 bis 15: den **Exit-Code**. Ein Komparator neben dem Auslöserblock kann ihn lesen.

```c0de
EXIT 3 ;
```

Exit-Code 15 bedeutet, dass das Programm abgestürzt ist. Du darfst `EXIT 15 ;` auch für ein schweres Problem verwenden.

## Fehler

Wenn etwas schiefgeht, zum Beispiel eine Division durch null, hält die Funktion an und **löst** einen Fehler **aus**. Der Fehler geht an den Aufrufer und dann weiter nach oben an dessen Aufrufer.

Wenn der Fehler die Einstiegsfunktion erreicht, **stürzt** der Prozess **ab**. Fehler im Programm selbst, zum Beispiel eine falsche Einrückung, lassen ihn sofort abstürzen.

Um einen Fehler abzufangen, füge der CALL-Zeile eine Variable vom Typ ERROR hinzu. Der Aufrufer macht dann weiter, und du kannst `code`, `message`, `context` und `line` des Fehlers prüfen.

```c0de
VARIABLE problem ERROR
CALL fail problem
ASSIGN code problem.code ;
```

## Eigene Fehler

Du kannst eigene Fehler auslösen. Ihre Codes müssen 1000 oder höher sein. Setze den Code einer ERROR-Variablen und gib sie zurück, oder verwende die eingebaute Funktion RAISE.

```c0de
FUNCTION fail
BEGIN
    VARIABLE e ERROR
    ASSIGN e.code 1000 ;
    RETURN e
END FUNCTION
```
