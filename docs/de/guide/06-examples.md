# Beispiele

Hier sind ein paar vollständige Programme zum Bauen und Ausprobieren. Sie stammen aus der Testsuite der Mod, sie funktionieren also nachweislich. Bau sie auf einem leeren Blatt nach, oder ändere sie und schau, was passiert.

## Hallo mit Standardwerten

```c0de
PROGRAM greeting
NOOP
NOOP
FUNCTION greet
    PARAMETER name STRING "World"
    PARAMETER mark CHAR '!'
    RESULT result STRING
BEGIN
    ASSIGN result "Hello, " ⊕ name ⊕ mark ;
END FUNCTION
```

Die Funktion fügt mit `⊕` drei Teile zu einem Text zusammen. Beide Parameter haben Standardwerte, deshalb darf ein Aufrufer sie weglassen. Mit dem Namen "c0de" aufgerufen, ist das Ergebnis "Hello, c0de!".

Zum Ausprobieren stellst du den Auslöserblock auf `greet`. Leg eine STRING-Variablenkarte mit dem Namen `name` und deinem Namen als Wert hinein.

## Zahlen addieren

```c0de
PROGRAM adder
NOOP
NOOP
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

Diese Funktion addiert alle Zahlen von 1 bis `n`. Sie ruft sich selbst mit `n - 1` auf, bis `n` 0 ist, und addiert dann auf dem Rückweg `n`. Für `n` = 10 ist das Ergebnis 55.

Zum Ausprobieren legst du eine INTEGER-Variablenkarte mit dem Namen `n` hinein. Jeder Aufruf braucht Platz auf dem Stack des Cursors, deshalb können sehr große Zahlen ihn abstürzen lassen.

## Primzahlen

```c0de
PROGRAM primefinder
NOOP
NOOP
FUNCTION main
    RESULT primes STRING
BEGIN
    FOR candidate INTEGER 2 ; 20 ;
    BEGIN
        VARIABLE prime BOOLEAN TRUE
        FOR divisor INTEGER 2 ; candidate - 1 ;
        BEGIN
            IF candidate \ divisor = 0 ;
            BEGIN
                ASSIGN prime FALSE ;
                BREAK
            END IF
        END FOR
        IF prime ;
        BEGIN
            ASSIGN primes primes ⊕ candidate ⊕ " " ;
        END IF
    END FOR
END FUNCTION
```

Die äußere Schleife probiert jede Zahl von 2 bis 20 aus. Die innere Schleife prüft, ob eine kleinere Zahl sie ohne Rest (`\`) teilt. Wenn ja, ist sie keine Primzahl, und BREAK beendet die innere Schleife. Das Ergebnis ist "2 3 5 7 11 13 17 19 ".

## Mathe-Bibliothek

```c0de
PROGRAM math
NOOP
INCLUDE c0de/math
INCLUDE c0de/strings
NOOP
FUNCTION main
    RESULT rounded INTEGER
    RESULT quotient INTEGER
    RESULT root FLOAT
    RESULT circle STRING
BEGIN
    CALL c0de/math/ROUND
        PARAMETER value 12.5 ;
        RESULT result rounded
    CALL c0de/math/DIV
        PARAMETER dividend -7 ;
        PARAMETER divisor 2 ;
        RESULT result quotient
    CALL c0de/math/SQR
        PARAMETER value 9 ;
        RESULT result root
    CALL c0de/strings/FORMAT
        PARAMETER value c0de/math/PI ;
        PARAMETER format "%.2f" ;
        RESULT result circle
END FUNCTION
```

Dieses Programm lädt zwei Bibliotheken. ROUND macht aus 12.5 die 12, DIV teilt -7 durch 2 und ergibt -3, und SQR ergibt 3.0. FORMAT zeigt PI mit zwei Nachkommastellen: "3.14". Die Funktion hat vier Ergebnisse, also legst du vier nicht initialisierte Variablenkarten hinein.

## Datum und Uhrzeit

```c0de
PROGRAM time
NOOP
INCLUDE c0de/strings
INCLUDE c0de/time
NOOP
FUNCTION main
    RESULT now STRING
    RESULT week INTEGER
    RESULT weekday INTEGER
    RESULT later TIME
BEGIN
    VARIABLE t TIME
    CALL c0de/time/NOW
        RESULT result t
    CALL c0de/strings/FORMAT
        PARAMETER value t ;
        PARAMETER format "%tF %tT" ;
        RESULT result now
    CALL c0de/time/WEEK
        RESULT result week
    CALL c0de/time/WEEKDAY
        PARAMETER time t ;
        RESULT result weekday
    ASSIGN later t + 1000 ;
    CALL c0de/time/SLEEP
        PARAMETER ticks 2 ;
END FUNCTION
```

NOW liest die Uhr, FORMAT macht daraus einen Text wie "2026-10-19 12:05:09". WEEK und WEEKDAY ergeben die Kalenderwoche und den Wochentag (1 ist Montag). Wenn du 1000 zu einer TIME addierst, kommt eine Sekunde dazu. SLEEP wartet vor dem Ende zwei Spielticks.

## Ein absichtlicher Absturz

```c0de
PROGRAM errors
NOOP
NOOP
FUNCTION divide
    PARAMETER a INTEGER
    PARAMETER b INTEGER
    RESULT result INTEGER
BEGIN
    VARIABLE quotient FLOAT
    ASSIGN quotient a / b ;
END FUNCTION
NOOP
FUNCTION main
BEGIN
    CALL divide
        PARAMETER a 1 ;
        PARAMETER b 0 ;
END FUNCTION
```

Die Division durch null löst in `divide` den Fehler DIVISION_BY_ZERO aus. Der CALL in `main` hat keine ERROR-Variable, die ihn abfängt, also wandert der Fehler nach oben. In der Einstiegsfunktion lässt er den Prozess abstürzen. Der Auslöserdialog zeigt den Fehler und Zeile 9, und ein Komparator liest 15.
