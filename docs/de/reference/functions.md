# Funktionen

Ein Funktionsaufruf führt eine Funktion mit einer CALL-Zeile aus. Die PARAMETER-Zeilen darunter geben die Werte an, die die Funktion braucht. Die RESULT-Zeilen nennen die Variablen, die bekommen, was die Funktion zurückgibt. Ergebnisse, die du nicht aufführst, werden verworfen.

Eingebaute Funktionen funktionieren überall. Bibliotheksfunktionen brauchen zuerst eine INCLUDE-Zeile für ihre Bibliothek, z. B. `INCLUDE c0de/math`. Dann rufst du sie mit ihrem vollen Namen auf, z. B. `c0de/math/SIN`. Wenn ein Aufruf fehlschlägt und kein Fehler-Label hat, wird der Fehler ausgelöst.

## Eingebaute Funktionen

Eingebaute Funktionen arbeiten mit Arrays, Hashes und Fehlern und brauchen kein INCLUDE.

### LEN

Zählt die Zeichen eines Strings oder die Elemente eines Arrays oder Hashs.

**Parameter und Ergebnisse**

- `value` STRING|ARRAY|HASH: der String, das Array oder der Hash, der gezählt wird
- Ergebnis `result` INTEGER: die Anzahl der Zeichen oder Elemente

**Beispiel**

```c0de
CALL LEN
    PARAMETER value names ;
    RESULT result count
```

### HAS_KEY

Prüft, ob ein Hash einen Schlüssel enthält.

**Parameter und Ergebnisse**

- `hash` HASH: der Hash, in dem gesucht wird
- `key` STRING: der gesuchte Schlüssel
- Ergebnis `result` BOOLEAN: wahr, wenn es den Schlüssel gibt

**Beispiel**

```c0de
CALL HAS_KEY
    PARAMETER hash stock ;
    PARAMETER key "gold" ;
    RESULT result hasGold
```

### REMOVE

Entfernt ein Element aus einem Array oder einem Hash.

Nimm `index` für ein Array und `key` für einen Hash. Die folgenden Elemente des Arrays rücken um eins nach unten. Ein fehlender Index löst INDEX_OUT_OF_BOUNDS aus, ein fehlender Schlüssel löst UNKNOWN_KEY aus. Wenn du `index` oder `key` weglässt, wird INVALID_ARGUMENT ausgelöst.

**Parameter und Ergebnisse**

- `container` ARRAY|HASH: das Array oder der Hash
- `index` INTEGER: Position des Elements, das entfernt wird (optional, nötig für Arrays)
- `key` STRING: Schlüssel des Elements, das entfernt wird (optional, nötig für Hashes)
- Ergebnis `result` wie `container`: das Array oder der Hash ohne das Element

**Beispiel**

```c0de
CALL REMOVE
    PARAMETER container list ;
    PARAMETER index 1 ;
    RESULT result list
```

### INSERT

Fügt einen Wert in ein Array ein.

Die folgenden Elemente rücken um eins nach oben. `index` muss zwischen 0 und der Länge des Arrays liegen, sonst wird INDEX_OUT_OF_BOUNDS ausgelöst. Nimm die Länge, um den Wert am Ende anzuhängen.

**Parameter und Ergebnisse**

- `array` ARRAY: das Array
- `index` INTEGER: die Position des neuen Elements
- `value` der Typ der Elemente: der Wert, der eingefügt wird
- Ergebnis `result` wie `array`: das Array mit dem neuen Element

**Beispiel**

```c0de
CALL INSERT
    PARAMETER array list ;
    PARAMETER index 0 ;
    PARAMETER value 42 ;
    RESULT result list
```

### CLEAR

Gibt ein leeres Array oder einen leeren Hash desselben Typs zurück.

**Parameter und Ergebnisse**

- `container` ARRAY|HASH: das Array oder der Hash
- Ergebnis `result` wie `container`: ein leeres Array oder ein leerer Hash

**Beispiel**

```c0de
CALL CLEAR
    PARAMETER container stock ;
    RESULT result stock
```

### SWAP

Vertauscht zwei Werte.

Beide Werte müssen denselben Typ haben, sonst wird TYPE_MISMATCH ausgelöst. Um zwei Variablen zu vertauschen, nimm sie als Parameter und als Ziele der Ergebnisse.

**Parameter und Ergebnisse**

- `a` beliebiger Typ: der erste Wert
- `b` der Typ von `a`: der zweite Wert
- Ergebnis `a`: der Wert von `b`
- Ergebnis `b`: der Wert von `a`

**Beispiel**

```c0de
CALL SWAP
    PARAMETER a left ;
    PARAMETER b right ;
    RESULT a left
    RESULT b right
```

### RAISE

Gibt deinen eigenen Fehler an den Aufrufer der Funktion zurück.

Der Aufrufer löst den Fehler aus oder speichert ihn im Fehler-Label seiner CALL-Zeile. `code` muss 1000 oder größer sein, sonst wird INVALID_ARGUMENT ausgelöst. Der Fehler bekommt den Kontext und die Zeile der CALL-Anweisung.

**Parameter und Ergebnisse**

- `code` INTEGER: der Fehlercode, 1000 oder größer
- `message` STRING: die Fehlermeldung (optional, Standard leer)

**Beispiel**

```c0de
CALL RAISE
    PARAMETER code 1001 ;
    PARAMETER message "too big" ;
```

## Eingabe und Ausgabe

Diese eingebauten Funktionen sprechen über Anzeigen, Laptops, Tablets und E/A-Blöcke mit den Spielern. Sie brauchen kein INCLUDE. Die Geräte sind an das Programm und die Funktion gebunden, siehe das Handbuch Geräte. Die Bildschirmfunktionen stammen aus AmigaBASIC. Zeilen und Spalten beginnen bei 0.

### PRINT

Schreibt einen Wert auf die Bildschirme und beginnt eine neue Zeile.

Der Text erscheint auf jeder Anzeige, jedem Laptop und jedem Tablet, das an das Programm und die Funktion gebunden ist. Strings werden ohne Anführungszeichen ausgegeben, BOOLEAN-Werte als `TRUE` und `FALSE`. Arrays, Hashes und Strukturen werden als JSON ausgegeben. Um mehrere Werte in einer Zeile auszugeben, verbinde sie mit ⊕. Ohne gebundenen Bildschirm macht PRINT nichts.

**Parameter und Ergebnisse**

- `value` beliebiger Typ: der Wert, der ausgegeben wird

**Beispiel**

```c0de
CALL PRINT
    PARAMETER value "Hello" ;
```

### WRITE

Legt einen Wert als Variablenkarte in die E/A-Blöcke.

Jeder gebundene E/A-Block mit einer nicht initialisierten Variablenkarte in seiner Eingabe bekommt den Wert. Die Karte wandert in die Ausgabe. Sie wird nach `name` benannt. Ohne `name` wird sie nach der Variablen benannt, z. B. `count` bei `PARAMETER value count ;`. Sonst heißt sie `output`.

Wenn kein gebundener E/A-Block eine nicht initialisierte Variablenkarte hat, wartet der Cursor, bis ein Spieler eine hineinlegt. Laptops und Tablets nehmen an WRITE nicht teil.

**Parameter und Ergebnisse**

- `value` beliebiger Typ: der Wert, der geschrieben wird
- `name` STRING: der Name der Karte (optional)

**Beispiel**

```c0de
CALL WRITE
    PARAMETER value count + step ;
    PARAMETER name "total" ;
```

### INPUT

Liest einen Wert in eine Variable ein.

`variable` muss der Name einer deklarierten Variablen sein, ohne Index. Eine Konstante oder Schleifenvariable geht nicht. Die Funktion schreibt direkt in die Variable. Sie sucht zuerst in den gebundenen E/A-Blöcken nach einer Karte mit demselben Namen und Typ oder nach einer Literalkarte des passenden Typs. Sonst wartet sie, bis ein Spieler auf einem gebundenen Laptop oder Tablet eine Zeile eintippt. Die getippte Zeile erscheint auf dem Bildschirm dieses Laptops oder Tablets.

**Parameter und Ergebnisse**

- `variable` Variable: die Variable, die den Wert bekommt
- keine Ergebnisse

**Beispiel**

```c0de
PROGRAM typing
NOOP
NOOP
FUNCTION ask
BEGIN
    VARIABLE count INTEGER
    CALL INPUT
        PARAMETER variable count ;
    CALL PRINT
        PARAMETER value count * 2 ;
END FUNCTION
```

### CLS

Löscht die Bildschirme.

Der Cursor der Bildschirme springt auf Zeile 0, Spalte 0. CLS wirkt auf alle Bildschirme, die an das Programm und die Funktion gebunden sind.

**Parameter und Ergebnisse**

- keine Parameter und keine Ergebnisse

**Beispiel**

```c0de
CALL CLS
```

### LOCATE

Bewegt den Cursor der Bildschirme.

Das nächste PRINT schreibt an diese Stelle. Eine Zeile oder Spalte außerhalb des Bildschirms setzt den Cursor an den nächsten Rand.

**Parameter und Ergebnisse**

- `row` INTEGER: die Zeile
- `column` INTEGER: die Spalte (optional, Standard 0)

**Beispiel**

```c0de
CALL LOCATE
    PARAMETER row 3 ;
    PARAMETER column 5 ;
```

### CSRLIN

Gibt die Zeile des Cursors zurück.

Es gilt der Bildschirm des zuerst gebundenen Geräts. Ohne Bildschirm ist das Ergebnis 0.

**Parameter und Ergebnisse**

- Ergebnis `result` INTEGER: die Zeile des Cursors

**Beispiel**

```c0de
CALL CSRLIN
    RESULT result row
```

### POS

Gibt die Spalte des Cursors zurück.

Es gilt der Bildschirm des zuerst gebundenen Geräts. Ohne Bildschirm ist das Ergebnis 0.

**Parameter und Ergebnisse**

- Ergebnis `result` INTEGER: die Spalte des Cursors

**Beispiel**

```c0de
CALL POS
    RESULT result column
```

### COLOR

Legt die Farben für den Text fest, der ab jetzt ausgegeben wird.

Die Farben sind die 16 Farben von Minecraft-Text, von 0 (Schwarz) bis 15 (Weiß). Hintergrund 0 ist die Farbe des Bildschirms. Eine Zahl außerhalb von 0 bis 15 löst INVALID_ARGUMENT aus.

Jedes Programm hat für jede Farbe eine Konstante: COLOR_BLACK, COLOR_DARK_BLUE, COLOR_DARK_GREEN, COLOR_DARK_AQUA, COLOR_DARK_RED, COLOR_DARK_PURPLE, COLOR_GOLD, COLOR_GRAY, COLOR_DARK_GRAY, COLOR_BLUE, COLOR_GREEN, COLOR_AQUA, COLOR_RED, COLOR_LIGHT_PURPLE, COLOR_YELLOW und COLOR_WHITE.

**Parameter und Ergebnisse**

- `foreground` INTEGER: die Farbe der Buchstaben, 0 bis 15
- `background` INTEGER: die Farbe hinter den Buchstaben, 0 bis 15 (optional, Standard 0)

**Beispiel**

```c0de
CALL COLOR
    PARAMETER foreground COLOR_YELLOW ;
    PARAMETER background COLOR_DARK_BLUE ;
```

### INKEY

Gibt die älteste Taste zurück, die ein Spieler auf einem Laptop oder Tablet gedrückt hat.

Es zählen nur Tasten, die gedrückt wurden, während kein INPUT wartet. Enter kommt als CHR 13 an. Wenn keine Taste wartet, ist das Ergebnis der leere String. INKEY wartet nicht.

**Parameter und Ergebnisse**

- Ergebnis `result` STRING: die Taste oder der leere String

**Beispiel**

```c0de
CALL INKEY
    RESULT result key
```

## c0de/math

Die Mathematik-Bibliothek hat Zahlenfunktionen und Zufallszahlen und braucht INCLUDE c0de/math.

### ABS

Gibt den Betrag einer Zahl zurück.

Ein Ergebnis, das nicht in einen INTEGER passt, löst NUMERIC_OVERFLOW aus.

**Parameter und Ergebnisse**

- `value` INTEGER|FLOAT: die Zahl
- Ergebnis `result` wie `value`: die Zahl ohne ihr Vorzeichen

**Beispiel**

```c0de
CALL c0de/math/ABS
    PARAMETER value distance ;
    RESULT result steps
```

### SGN

Gibt das Vorzeichen einer Zahl zurück.

Das Ergebnis ist -1 für negative Zahlen, 0 für 0 und 1 für positive Zahlen.

**Parameter und Ergebnisse**

- `value` INTEGER|FLOAT: die Zahl
- Ergebnis `result` INTEGER: -1, 0 oder 1

**Beispiel**

```c0de
CALL c0de/math/SGN
    PARAMETER value balance ;
    RESULT result sign
```

### SQR

Gibt die Quadratwurzel einer Zahl zurück.

Eine negative Zahl löst INVALID_ARGUMENT aus.

**Parameter und Ergebnisse**

- `value` INTEGER|FLOAT: die Zahl, nicht negativ
- Ergebnis `result` FLOAT: die Quadratwurzel

**Beispiel**

```c0de
CALL c0de/math/SQR
    PARAMETER value area ;
    RESULT result side
```

### EXP

Gibt e hoch eine Zahl zurück.

**Parameter und Ergebnisse**

- `value` INTEGER|FLOAT: der Exponent
- Ergebnis `result` FLOAT: e hoch `value`

**Beispiel**

```c0de
CALL c0de/math/EXP
    PARAMETER value rate ;
    RESULT result growth
```

### LOG

Gibt den natürlichen Logarithmus einer Zahl zurück.

Die Zahl muss größer als 0 sein, sonst wird INVALID_ARGUMENT ausgelöst.

**Parameter und Ergebnisse**

- `value` INTEGER|FLOAT: die Zahl, größer als 0
- Ergebnis `result` FLOAT: der natürliche Logarithmus

**Beispiel**

```c0de
CALL c0de/math/LOG
    PARAMETER value amount ;
    RESULT result logarithm
```

### SIN

Gibt den Sinus eines Winkels zurück.

Der Winkel ist im Bogenmaß. Ein voller Kreis ist 2 × `c0de/math/PI`.

**Parameter und Ergebnisse**

- `value` INTEGER|FLOAT: der Winkel im Bogenmaß
- Ergebnis `result` FLOAT: der Sinus

**Beispiel**

```c0de
CALL c0de/math/SIN
    PARAMETER value angle ;
    RESULT result sine
```

### COS

Gibt den Kosinus eines Winkels zurück.

Der Winkel ist im Bogenmaß. Ein voller Kreis ist 2 × `c0de/math/PI`.

**Parameter und Ergebnisse**

- `value` INTEGER|FLOAT: der Winkel im Bogenmaß
- Ergebnis `result` FLOAT: der Kosinus

**Beispiel**

```c0de
CALL c0de/math/COS
    PARAMETER value angle ;
    RESULT result cosine
```

### TAN

Gibt den Tangens eines Winkels zurück.

Der Winkel ist im Bogenmaß. Ein voller Kreis ist 2 × `c0de/math/PI`.

**Parameter und Ergebnisse**

- `value` INTEGER|FLOAT: der Winkel im Bogenmaß
- Ergebnis `result` FLOAT: der Tangens

**Beispiel**

```c0de
CALL c0de/math/TAN
    PARAMETER value angle ;
    RESULT result tangent
```

### ATN

Gibt den Arkustangens einer Zahl zurück.

Das Ergebnis ist ein Winkel im Bogenmaß.

**Parameter und Ergebnisse**

- `value` INTEGER|FLOAT: der Tangens
- Ergebnis `result` FLOAT: der Winkel im Bogenmaß

**Beispiel**

```c0de
CALL c0de/math/ATN
    PARAMETER value slope ;
    RESULT result angle
```

### INT

Rundet eine Zahl auf eine ganze Zahl ab.

Aus -12.85 wird -13. Eine Zahl, die für einen INTEGER zu groß ist, löst NUMERIC_OVERFLOW aus.

**Parameter und Ergebnisse**

- `value` INTEGER|FLOAT: die Zahl
- Ergebnis `result` INTEGER: die größte ganze Zahl, die nicht größer als `value` ist

**Beispiel**

```c0de
CALL c0de/math/INT
    PARAMETER value height ;
    RESULT result blocks
```

### FIX

Schneidet die Nachkommastellen einer Zahl ab.

Es wird nicht gerundet: Aus -12.85 wird -12. Eine Zahl, die für einen INTEGER zu groß ist, löst NUMERIC_OVERFLOW aus.

**Parameter und Ergebnisse**

- `value` INTEGER|FLOAT: die Zahl
- Ergebnis `result` INTEGER: die Zahl ohne Nachkommastellen

**Beispiel**

```c0de
CALL c0de/math/FIX
    PARAMETER value price ;
    RESULT result coins
```

### ROUND

Rundet eine Zahl auf die nächste ganze Zahl.

Ein .5 geht zur nächsten geraden Zahl: Aus 12.5 wird 12, aus 13.5 wird 14. Eine Zahl, die für einen INTEGER zu groß ist, löst NUMERIC_OVERFLOW aus.

**Parameter und Ergebnisse**

- `value` INTEGER|FLOAT: die Zahl
- Ergebnis `result` INTEGER: die gerundete Zahl

**Beispiel**

```c0de
CALL c0de/math/ROUND
    PARAMETER value 12.5 ;
    RESULT result rounded
```

### TO_FLOAT

Wandelt eine Zahl in einen FLOAT um.

**Parameter und Ergebnisse**

- `value` INTEGER|FLOAT: die Zahl
- Ergebnis `result` FLOAT: dieselbe Zahl als FLOAT

**Beispiel**

```c0de
CALL c0de/math/TO_FLOAT
    PARAMETER value count ;
    RESULT result amount
```

### DIV

Teilt zwei ganze Zahlen und rundet in Richtung 0.

-7 geteilt durch 2 ist -3. Ein Teiler von 0 löst DIVISION_BY_ZERO aus.

**Parameter und Ergebnisse**

- `dividend` INTEGER: die Zahl, die geteilt wird
- `divisor` INTEGER: die Zahl, durch die geteilt wird, nicht 0
- Ergebnis `result` INTEGER: der Quotient ohne Rest

**Beispiel**

```c0de
CALL c0de/math/DIV
    PARAMETER dividend items ;
    PARAMETER divisor 64 ;
    RESULT result stacks
```

### RANDOM

Gibt eine Zufallszahl zwischen 0 und 1 zurück.

0 ist möglich, 1 nicht. Jeder Prozess hat seinen eigenen Zufallszahlengenerator.

**Parameter und Ergebnisse**

- Ergebnis `result` FLOAT: eine Zufallszahl von 0 bis unter 1

**Beispiel**

```c0de
CALL c0de/math/RANDOM
    RESULT result chance
```

### RANDOMIZE

Setzt den Startwert des Zufallszahlengenerators.

Derselbe Startwert ergibt immer dieselbe Folge von Zufallszahlen. Das betrifft nur den aktuellen Prozess.

**Parameter und Ergebnisse**

- `seed` INTEGER: der Startwert

**Beispiel**

```c0de
CALL c0de/math/RANDOMIZE
    PARAMETER seed 42 ;
```

## c0de/strings

Die String-Bibliothek hat Textfunktionen, Zahlenumwandlungen, JSON und YAML und braucht INCLUDE c0de/strings.

### LEFT

Gibt die ersten Zeichen eines Texts zurück.

Wenn der Text kürzer ist, bekommst du den ganzen Text. Eine negative Länge löst INVALID_ARGUMENT aus.

**Parameter und Ergebnisse**

- `text` STRING: der Text
- `length` INTEGER: die Anzahl der Zeichen
- Ergebnis `result` STRING: die ersten `length` Zeichen

**Beispiel**

```c0de
CALL c0de/strings/LEFT
    PARAMETER text name ;
    PARAMETER length 3 ;
    RESULT result short
```

### RIGHT

Gibt die letzten Zeichen eines Texts zurück.

Wenn der Text kürzer ist, bekommst du den ganzen Text. Eine negative Länge löst INVALID_ARGUMENT aus.

**Parameter und Ergebnisse**

- `text` STRING: der Text
- `length` INTEGER: die Anzahl der Zeichen
- Ergebnis `result` STRING: die letzten `length` Zeichen

**Beispiel**

```c0de
CALL c0de/strings/RIGHT
    PARAMETER text name ;
    PARAMETER length 3 ;
    RESULT result ending
```

### MID

Gibt einen Teil eines Texts zurück.

Positionen beginnen bei 0. Kurz vor dem Ende bekommst du weniger Zeichen. Ein Start hinter dem Ende ergibt einen leeren Text. Ein negativer Start oder eine negative Länge löst INVALID_ARGUMENT aus.

**Parameter und Ergebnisse**

- `text` STRING: der Text
- `start` INTEGER: die Position des ersten Zeichens
- `length` INTEGER: die Anzahl der Zeichen (optional, Standard der Rest des Texts)
- Ergebnis `result` STRING: der Teil des Texts

**Beispiel**

```c0de
CALL c0de/strings/MID
    PARAMETER text name ;
    PARAMETER start 2 ;
    PARAMETER length 3 ;
    RESULT result part
```

### REPLACE

Überschreibt Zeichen eines Texts mit einem anderen Text.

Die Länge des Texts bleibt gleich. Zeichen von `insert`, die ihn länger machen würden, fallen weg. Ein Start hinter dem Ende des Texts löst INVALID_ARGUMENT aus.

**Parameter und Ergebnisse**

- `text` STRING: der Text
- `start` INTEGER: die Position, an der das Überschreiben beginnt
- `insert` STRING: die neuen Zeichen
- Ergebnis `result` STRING: der geänderte Text

**Beispiel**

```c0de
CALL c0de/strings/REPLACE
    PARAMETER text "Hello Alex" ;
    PARAMETER start 6 ;
    PARAMETER insert "Stev" ;
    RESULT result greeting
```

### INSTR

Findet einen Text in einem anderen Text.

Das Ergebnis ist die Position des ersten Treffers oder -1, wenn es keinen gibt.

**Parameter und Ergebnisse**

- `text` STRING: der Text, in dem gesucht wird
- `search` STRING: der gesuchte Text
- `start` INTEGER: wo die Suche beginnt (optional, Standard 0)
- Ergebnis `result` INTEGER: die Position oder -1

**Beispiel**

```c0de
CALL c0de/strings/INSTR
    PARAMETER text message ;
    PARAMETER search "diamond" ;
    RESULT result position
```

### UCASE

Wandelt einen Text in Großbuchstaben um.

**Parameter und Ergebnisse**

- `text` STRING: der Text
- Ergebnis `result` STRING: der Text in Großbuchstaben

**Beispiel**

```c0de
CALL c0de/strings/UCASE
    PARAMETER text command ;
    RESULT result command
```

### LCASE

Wandelt einen Text in Kleinbuchstaben um.

**Parameter und Ergebnisse**

- `text` STRING: der Text
- Ergebnis `result` STRING: der Text in Kleinbuchstaben

**Beispiel**

```c0de
CALL c0de/strings/LCASE
    PARAMETER text answer ;
    RESULT result answer
```

### SPACE

Gibt einen Text aus Leerzeichen zurück.

`count` muss zwischen 0 und 1.000.000 liegen, sonst wird INVALID_ARGUMENT ausgelöst.

**Parameter und Ergebnisse**

- `count` INTEGER: die Anzahl der Leerzeichen
- Ergebnis `result` STRING: die Leerzeichen

**Beispiel**

```c0de
CALL c0de/strings/SPACE
    PARAMETER count 8 ;
    RESULT result indent
```

### FILL

Gibt einen Text zurück, der ein Zeichen wiederholt.

`count` muss zwischen 0 und 1.000.000 liegen, sonst wird INVALID_ARGUMENT ausgelöst.

**Parameter und Ergebnisse**

- `count` INTEGER: wie oft das Zeichen wiederholt wird
- `char` CHAR: das Zeichen
- Ergebnis `result` STRING: das wiederholte Zeichen

**Beispiel**

```c0de
CALL c0de/strings/FILL
    PARAMETER count 20 ;
    PARAMETER char '-' ;
    RESULT result line
```

### ASC

Gibt den Unicode-Codepunkt eines Zeichens zurück.

**Parameter und Ergebnisse**

- `char` CHAR: das Zeichen
- Ergebnis `result` INTEGER: sein Codepunkt, z. B. 65 für A

**Beispiel**

```c0de
CALL c0de/strings/ASC
    PARAMETER char letter ;
    RESULT result code
```

### CHR

Gibt das Zeichen zu einem Unicode-Codepunkt zurück.

Eine Zahl, die kein Codepunkt ist, löst INVALID_ARGUMENT aus.

**Parameter und Ergebnisse**

- `code` INTEGER: der Codepunkt
- Ergebnis `result` CHAR: das Zeichen

**Beispiel**

```c0de
CALL c0de/strings/CHR
    PARAMETER code 65 ;
    RESULT result letter
```

### STR

Wandelt einen Wert in Text um.

Der Text ist derselbe, den der Operator ⊕ erzeugt.

**Parameter und Ergebnisse**

- `value` BOOLEAN|INTEGER|FLOAT|CHAR|TIME: der Wert
- Ergebnis `result` STRING: der Wert als Text

**Beispiel**

```c0de
CALL c0de/strings/STR
    PARAMETER value score ;
    RESULT result scoreText
```

### FORMAT

Formatiert einen Wert mit einem Format-String als Text.

Umwandlungen beginnen mit `%`, z. B. `%5d` (ganze Zahl, 5 Zeichen breit), `%.2f` (2 Nachkommastellen), `%s` (beliebiger Wert), `%tF` und `%tT` (Datum und Uhrzeit). Ein falscher Typ löst TYPE_MISMATCH aus, ein ungültiger Format-String INVALID_ARGUMENT.

**Parameter und Ergebnisse**

- `value` BOOLEAN|INTEGER|FLOAT|CHAR|STRING|TIME: der Wert
- `format` STRING: der Format-String
- Ergebnis `result` STRING: der formatierte Text

**Beispiel**

```c0de
CALL c0de/strings/FORMAT
    PARAMETER value now ;
    PARAMETER format "%tF %tT" ;
    RESULT result text
```

### VAL

Wandelt einen Text in eine Zahl um.

`isInteger` ist wahr, wenn der Text eine ganze Zahl ist. Nimm dann FIX, um den Wert in einen INTEGER zu verwandeln. Ein Text, der keine Zahl ist, löst INVALID_ARGUMENT aus.

**Parameter und Ergebnisse**

- `text` STRING: der Text, z. B. "42" oder "3.5"
- Ergebnis `value` FLOAT: die Zahl
- Ergebnis `isInteger` BOOLEAN: wahr, wenn der Text eine ganze Zahl ist

**Beispiel**

```c0de
CALL c0de/strings/VAL
    PARAMETER text input ;
    RESULT value number
    RESULT isInteger whole
```

### HEX

Gibt den Hexadezimaltext einer ganzen Zahl zurück.

Buchstaben sind groß. Negative Zahlen verwenden das Zweierkomplement, also wird -1 zu FFFFFFFFFFFFFFFF.

**Parameter und Ergebnisse**

- `value` INTEGER: die Zahl
- Ergebnis `result` STRING: der Hexadezimaltext

**Beispiel**

```c0de
CALL c0de/strings/HEX
    PARAMETER value 255 ;
    RESULT result hexText
```

### OCT

Gibt den Oktaltext einer ganzen Zahl zurück.

Negative Zahlen verwenden das Zweierkomplement.

**Parameter und Ergebnisse**

- `value` INTEGER: die Zahl
- Ergebnis `result` STRING: der Oktaltext

**Beispiel**

```c0de
CALL c0de/strings/OCT
    PARAMETER value 8 ;
    RESULT result octText
```

### TO_JSON

Wandelt einen Wert in ein JSON-Dokument um.

Mit `indent` 0 ist das Dokument eine Zeile. Wenn `value` eine einzelne Variable mit einem json-Tag ist, wird der Wert am Pfad des Tags abgelegt. `indent` außerhalb von 0 bis 16 löst INVALID_ARGUMENT aus.

**Parameter und Ergebnisse**

- `value` beliebiger Typ: der Wert, der umgewandelt wird
- `indent` INTEGER: Leerzeichen pro Ebene, 0 bis 16 (optional, Standard 0)
- Ergebnis `result` STRING: das JSON-Dokument

**Beispiel**

```c0de
CALL c0de/strings/TO_JSON
    PARAMETER value config ;
    PARAMETER indent 2 ;
    RESULT result json
```

### TO_YAML

Wandelt einen Wert in ein YAML-Dokument um.

Das Dokument verwendet den Blockstil mit zwei Leerzeichen Einrückung. Wenn `value` eine einzelne Variable mit einem yaml-Tag ist, wird der Wert am Pfad des Tags abgelegt.

**Parameter und Ergebnisse**

- `value` beliebiger Typ: der Wert, der umgewandelt wird
- Ergebnis `result` STRING: das YAML-Dokument

**Beispiel**

```c0de
CALL c0de/strings/TO_YAML
    PARAMETER value config ;
    RESULT result yaml
```

### FROM_JSON

Liest ein JSON-Dokument in eine Variable ein.

`destination` muss ein einzelner Variablenname sein; die Funktion schreibt direkt in die Variable. Das Dokument muss zum Typ der Variablen passen. Bei einem Fehler (TYPE_MISMATCH, INVALID_ARGUMENT, UNKNOWN_KEY, STRING_TOO_LONG) bleibt die Variable unverändert.

**Parameter und Ergebnisse**

- `source` STRING: das JSON-Dokument
- `destination` Variable: die Variable, die die Werte bekommt
- keine Ergebnisse

**Beispiel**

```c0de
CALL c0de/strings/FROM_JSON readError
    PARAMETER source config ;
    PARAMETER destination port ;
```

### FROM_YAML

Liest ein YAML-Dokument in eine Variable ein.

`destination` muss ein einzelner Variablenname sein; die Funktion schreibt direkt in die Variable. Nur das erste Dokument wird gelesen. Bei einem Fehler (TYPE_MISMATCH, INVALID_ARGUMENT, UNKNOWN_KEY, STRING_TOO_LONG) bleibt die Variable unverändert.

**Parameter und Ergebnisse**

- `source` STRING: das YAML-Dokument
- `destination` Variable: die Variable, die die Werte bekommt
- keine Ergebnisse

**Beispiel**

```c0de
CALL c0de/strings/FROM_YAML readError
    PARAMETER source config ;
    PARAMETER destination settings ;
```

## c0de/time

Die Zeit-Bibliothek hat Funktionen für die echte Uhrzeit und das Datum sowie SLEEP und braucht INCLUDE c0de/time.

### NOW

Gibt die aktuelle Zeit zurück.

Der TIME-Wert ist ein Unix-Zeitstempel in Millisekunden. Nimm FORMAT, um ihn in Text umzuwandeln.

**Parameter und Ergebnisse**

- Ergebnis `result` TIME: die aktuelle Zeit

**Beispiel**

```c0de
CALL c0de/time/NOW
    RESULT result now
```

### MILLISECOND

Gibt die Millisekunden einer Zeit zurück (0 bis 999).

Ohne `time` nimmt die Funktion die aktuelle Zeit. Sie verwendet die Zeitzone des Servers.

**Parameter und Ergebnisse**

- `time` TIME: die Zeit (optional, Standard jetzt)
- Ergebnis `result` INTEGER: die Millisekunde, 0 bis 999

**Beispiel**

```c0de
CALL c0de/time/MILLISECOND
    PARAMETER time start ;
    RESULT result millis
```

### SECOND

Gibt die Sekunde einer Zeit zurück (0 bis 59).

Ohne `time` nimmt die Funktion die aktuelle Zeit. Sie verwendet die Zeitzone des Servers.

**Parameter und Ergebnisse**

- `time` TIME: die Zeit (optional, Standard jetzt)
- Ergebnis `result` INTEGER: die Sekunde, 0 bis 59

**Beispiel**

```c0de
CALL c0de/time/SECOND
    PARAMETER time start ;
    RESULT result second
```

### MINUTE

Gibt die Minute einer Zeit zurück (0 bis 59).

Ohne `time` nimmt die Funktion die aktuelle Zeit. Sie verwendet die Zeitzone des Servers.

**Parameter und Ergebnisse**

- `time` TIME: die Zeit (optional, Standard jetzt)
- Ergebnis `result` INTEGER: die Minute, 0 bis 59

**Beispiel**

```c0de
CALL c0de/time/MINUTE
    RESULT result minute
```

### HOUR

Gibt die Stunde einer Zeit zurück (0 bis 23).

Ohne `time` nimmt die Funktion die aktuelle Zeit. Sie verwendet die Zeitzone des Servers.

**Parameter und Ergebnisse**

- `time` TIME: die Zeit (optional, Standard jetzt)
- Ergebnis `result` INTEGER: die Stunde, 0 bis 23

**Beispiel**

```c0de
CALL c0de/time/HOUR
    RESULT result hour
```

### DAY

Gibt den Tag des Monats zurück (1 bis 31).

Ohne `time` nimmt die Funktion die aktuelle Zeit. Sie verwendet die Zeitzone des Servers.

**Parameter und Ergebnisse**

- `time` TIME: die Zeit (optional, Standard jetzt)
- Ergebnis `result` INTEGER: der Tag, 1 bis 31

**Beispiel**

```c0de
CALL c0de/time/DAY
    PARAMETER time birthday ;
    RESULT result day
```

### MONTH

Gibt den Monat einer Zeit zurück (1 bis 12).

Ohne `time` nimmt die Funktion die aktuelle Zeit. Sie verwendet die Zeitzone des Servers.

**Parameter und Ergebnisse**

- `time` TIME: die Zeit (optional, Standard jetzt)
- Ergebnis `result` INTEGER: der Monat, 1 bis 12

**Beispiel**

```c0de
CALL c0de/time/MONTH
    PARAMETER time birthday ;
    RESULT result month
```

### YEAR

Gibt das Jahr einer Zeit zurück, z. B. 2026.

Ohne `time` nimmt die Funktion die aktuelle Zeit. Sie verwendet die Zeitzone des Servers.

**Parameter und Ergebnisse**

- `time` TIME: die Zeit (optional, Standard jetzt)
- Ergebnis `result` INTEGER: das Jahr

**Beispiel**

```c0de
CALL c0de/time/YEAR
    RESULT result year
```

### WEEKDAY

Gibt den Wochentag zurück, von 1 (Montag) bis 7 (Sonntag).

Ohne `time` nimmt die Funktion die aktuelle Zeit. Sie verwendet die Zeitzone des Servers.

**Parameter und Ergebnisse**

- `time` TIME: die Zeit (optional, Standard jetzt)
- Ergebnis `result` INTEGER: der Wochentag, 1 bis 7

**Beispiel**

```c0de
CALL c0de/time/WEEKDAY
    PARAMETER time t ;
    RESULT result weekday
```

### WEEK

Gibt die Kalenderwoche nach ISO 8601 zurück (1 bis 53).

Woche 1 enthält den ersten Donnerstag des Jahres. Deshalb können Tage um Neujahr zur Woche 52 oder 53 des alten Jahres oder zur Woche 1 des neuen Jahres gehören. Ohne `time` wird die aktuelle Zeit genommen.

**Parameter und Ergebnisse**

- `time` TIME: die Zeit (optional, Standard jetzt)
- Ergebnis `result` INTEGER: die Woche, 1 bis 53

**Beispiel**

```c0de
CALL c0de/time/WEEK
    RESULT result week
```

### TIMER

Gibt die Sekunden seit Mitternacht zurück.

Das ist die echte Uhrzeit des Servers, nicht die Spielzeit. Nimm TICKS aus c0de/minecraft für die Spielzeit.

**Parameter und Ergebnisse**

- Ergebnis `result` FLOAT: die Sekunden seit Mitternacht

**Beispiel**

```c0de
CALL c0de/time/TIMER
    RESULT result seconds
```

### SLEEP

Hält das Programm für eine Anzahl von Ticks an.

20 Ticks sind etwa eine Sekunde. `ticks` muss zwischen 1 und 72000 (eine Stunde) liegen, sonst wird INVALID_ARGUMENT ausgelöst.

**Parameter und Ergebnisse**

- `ticks` INTEGER: die Anzahl der Ticks, 1 bis 72000

**Beispiel**

```c0de
CALL c0de/time/SLEEP
    PARAMETER ticks 20 ;
```

## c0de/minecraft

Die Minecraft-Bibliothek handelt in der Welt für den Besitzer des Auslöserblocks, verwendet den Strukturtyp c0de/minecraft/POSITION (x, y, z, dimension) für Orte und braucht INCLUDE c0de/minecraft.

### TICKS

Gibt die Spielzeit in Ticks zurück.

Es ist die Spielzeit der Dimension des Auslöserblocks.

**Parameter und Ergebnisse**

- Ergebnis `result` INTEGER: die Spielzeit in Ticks

**Beispiel**

```c0de
CALL c0de/minecraft/TICKS
    RESULT result ticks
```

### BEEP

Spielt am Auslöserblock einen kurzen Glockenton.

**Parameter und Ergebnisse**

- keine Parameter und keine Ergebnisse

**Beispiel**

```c0de
CALL c0de/minecraft/BEEP
```

### SOUND

Spielt am Auslöserblock einen Ton wie ein Notenblock.

`note` ist 0 bis 24, wie die Klicks auf einem Notenblock. Instrumente sind Notenblock-Instrumente wie harp, bass, bell oder flute. Ein falscher Ton oder ein falsches Instrument löst INVALID_ARGUMENT aus.

**Parameter und Ergebnisse**

- `note` INTEGER: der Ton, 0 bis 24
- `instrument` STRING: das Instrument (optional, Standard "harp")

**Beispiel**

```c0de
CALL c0de/minecraft/SOUND
    PARAMETER note 12 ;
    PARAMETER instrument "bell" ;
```

### SAY

Schreibt eine Nachricht in den Chat.

Die Nachricht beginnt mit dem Programmnamen und der PID. Mit `player` bekommt nur dieser Spieler sie; wenn er nicht online ist, wird INVALID_ARGUMENT ausgelöst. Sonst bekommen sie alle Spieler in der Nähe des Auslöserblocks (standardmäßig 64 Blöcke).

**Parameter und Ergebnisse**

- `message` STRING: die Nachricht
- `player` STRING: der Name eines Spielers (optional, Standard leer für alle Spieler in der Nähe)

**Beispiel**

```c0de
CALL c0de/minecraft/SAY
    PARAMETER message "The farm is full!" ;
    PARAMETER player "Alex" ;
```

### GET_BLOCK

Liest den Block an einer Position.

Du bekommst seine ID, seine Blockzustände als Text, die Redstone-Stärke, die er empfängt, und ob er ein Inventar hat. Ein nicht geladener Chunk löst CHUNK_NOT_LOADED aus. Eine leere `dimension` in der Position bedeutet die Dimension des Auslöserblocks.

**Parameter und Ergebnisse**

- `position` STRUCT c0de/minecraft/POSITION: wo der Block ist
- Ergebnis `id` STRING: die Block-ID, z. B. minecraft:oak_door
- Ergebnis `properties` HASH STRING: die Blockzustände, z. B. facing ist north
- Ergebnis `power` INTEGER: die Redstone-Stärke, die er empfängt, 0 bis 15
- Ergebnis `has_inventory` BOOLEAN: wahr für Blöcke mit Inventar, z. B. Truhen; GET_INVENTORY, MOVE_ITEMS und DELETE_ITEMS funktionieren mit ihnen

**Beispiel**

```c0de
CALL c0de/minecraft/GET_BLOCK
    PARAMETER position spot ;
    RESULT id blockId
    RESULT properties states
    RESULT power power
    RESULT has_inventory isChest
```

### GET_INVENTORY

Liest das Inventar eines Blocks, z. B. einer Truhe.

Du bekommst einen ITEM_STACK (id, count) pro Slot. Ein leerer Slot hat die id "" und die Anzahl 0. Ein Block ohne Inventar löst INVALID_ARGUMENT aus.

**Parameter und Ergebnisse**

- `position` STRUCT c0de/minecraft/POSITION: wo der Block ist
- Ergebnis `result` ARRAY STRUCT c0de/minecraft/ITEM_STACK: die Slots

**Beispiel**

```c0de
CALL c0de/minecraft/GET_INVENTORY
    PARAMETER position chest ;
    RESULT result slots
```

### GET_LEVER

Prüft, ob ein Hebel an ist.

Ein Block, der kein Hebel ist, löst INVALID_ARGUMENT aus.

**Parameter und Ergebnisse**

- `position` STRUCT c0de/minecraft/POSITION: wo der Hebel ist
- Ergebnis `result` BOOLEAN: wahr, wenn der Hebel an ist

**Beispiel**

```c0de
CALL c0de/minecraft/GET_LEVER
    PARAMETER position lever ;
    RESULT result isOn
```

### SET_LEVER

Schaltet einen Hebel an oder aus.

Ein Block, der kein Hebel ist, löst INVALID_ARGUMENT aus. Hebel auf den Blättern der c0de-Dimension können nicht verändert werden (PERMISSION_DENIED).

**Parameter und Ergebnisse**

- `position` STRUCT c0de/minecraft/POSITION: wo der Hebel ist
- `on` BOOLEAN: wahr zum Anschalten, falsch zum Ausschalten

**Beispiel**

```c0de
CALL c0de/minecraft/SET_LEVER
    PARAMETER position lever ;
    PARAMETER on TRUE ;
```

### GET_BUTTON

Prüft, ob ein Knopf gedrückt ist.

Ein Block, der kein Knopf ist, löst INVALID_ARGUMENT aus.

**Parameter und Ergebnisse**

- `position` STRUCT c0de/minecraft/POSITION: wo der Knopf ist
- Ergebnis `result` BOOLEAN: wahr, wenn der Knopf gedrückt ist

**Beispiel**

```c0de
CALL c0de/minecraft/GET_BUTTON
    PARAMETER position button ;
    RESULT result pressed
```

### PRESS_BUTTON

Drückt einen Knopf, wie es ein Spieler tun würde.

Ein Block, der kein Knopf ist, löst INVALID_ARGUMENT aus. Knöpfe auf den Blättern der c0de-Dimension können nicht gedrückt werden (PERMISSION_DENIED).

**Parameter und Ergebnisse**

- `position` STRUCT c0de/minecraft/POSITION: wo der Knopf ist

**Beispiel**

```c0de
CALL c0de/minecraft/PRESS_BUTTON
    PARAMETER position button ;
```

### GET_PRESSURE_PLATE

Liest die Signalstärke einer Druckplatte.

Druckplatten können nur gelesen werden; Lebewesen und Objekte drücken sie. Ein Block, der keine Druckplatte ist, löst INVALID_ARGUMENT aus.

**Parameter und Ergebnisse**

- `position` STRUCT c0de/minecraft/POSITION: wo die Druckplatte ist
- Ergebnis `result` INTEGER: die Signalstärke, 0 bis 15

**Beispiel**

```c0de
CALL c0de/minecraft/GET_PRESSURE_PLATE
    PARAMETER position plate ;
    RESULT result strength
```

### GET_OPEN

Prüft, ob eine Tür, Falltür oder ein Zauntor offen ist.

Jeder andere Block löst INVALID_ARGUMENT aus.

**Parameter und Ergebnisse**

- `position` STRUCT c0de/minecraft/POSITION: wo die Tür ist
- Ergebnis `result` BOOLEAN: wahr, wenn sie offen ist

**Beispiel**

```c0de
CALL c0de/minecraft/GET_OPEN
    PARAMETER position door ;
    RESULT result isOpen
```

### SET_OPEN

Öffnet oder schließt eine Tür, Falltür oder ein Zauntor.

Jeder andere Block löst INVALID_ARGUMENT aus. Eisentüren und Eisenfalltüren brauchen die Berechtigung allowBlockChanges oder einen Administrator als Besitzer, sonst wird PERMISSION_DENIED ausgelöst.

**Parameter und Ergebnisse**

- `position` STRUCT c0de/minecraft/POSITION: wo die Tür ist
- `open` BOOLEAN: wahr zum Öffnen, falsch zum Schließen

**Beispiel**

```c0de
CALL c0de/minecraft/SET_OPEN
    PARAMETER position door ;
    PARAMETER open TRUE ;
```

### MOVE_ITEMS

Verschiebt Gegenstände von einem Inventar in ein anderes.

Die Funktion verschiebt bis zu `count` Gegenstände und gibt zurück, wie viele verschoben wurden. Das braucht allowInventoryChanges oder einen Administrator als Besitzer, sonst wird PERMISSION_DENIED ausgelöst. Blöcke ohne Inventar lösen INVALID_ARGUMENT aus.

**Parameter und Ergebnisse**

- `from` STRUCT c0de/minecraft/POSITION: der Block, aus dem Gegenstände genommen werden
- `to` STRUCT c0de/minecraft/POSITION: der Block, in den Gegenstände gelegt werden
- `id` STRING: die Gegenstands-ID, z. B. minecraft:wheat
- `count` INTEGER: die höchste Anzahl an Gegenständen, die verschoben wird
- Ergebnis `result` INTEGER: die Anzahl der verschobenen Gegenstände

**Beispiel**

```c0de
CALL c0de/minecraft/MOVE_ITEMS
    PARAMETER from farmChest ;
    PARAMETER to storage ;
    PARAMETER id "minecraft:wheat" ;
    PARAMETER count 64 ;
    RESULT result moved
```

### DELETE_ITEMS

Entfernt Gegenstände aus einem Inventar.

Die Funktion entfernt bis zu `count` Gegenstände und gibt zurück, wie viele entfernt wurden. Das braucht allowInventoryChanges oder einen Administrator als Besitzer, sonst wird PERMISSION_DENIED ausgelöst.

**Parameter und Ergebnisse**

- `position` STRUCT c0de/minecraft/POSITION: der Block mit dem Inventar
- `id` STRING: die Gegenstands-ID, z. B. minecraft:rotten_flesh
- `count` INTEGER: die höchste Anzahl an Gegenständen, die entfernt wird
- Ergebnis `result` INTEGER: die Anzahl der entfernten Gegenstände

**Beispiel**

```c0de
CALL c0de/minecraft/DELETE_ITEMS
    PARAMETER position trashChest ;
    PARAMETER id "minecraft:rotten_flesh" ;
    PARAMETER count 64 ;
    RESULT result removed
```

### PLACE_BLOCK

Setzt einen Block an eine Position.

An der Position muss Luft oder ein ersetzbarer Block wie Gras oder Wasser sein, sonst wird INVALID_ARGUMENT ausgelöst. Ohne `inventory` braucht das allowBlockChanges oder einen Administrator als Besitzer, sonst wird PERMISSION_DENIED ausgelöst.

Mit `inventory` wird der Block aus diesem Inventar genommen, so wie ein Spieler einen Block aus seinem Inventar setzt, und das darf jeder Besitzer. Wenn das Inventar den Block nicht enthält, wird ITEM_MISSING ausgelöst.

**Parameter und Ergebnisse**

- `position` STRUCT c0de/minecraft/POSITION: wohin der Block kommt
- `id` STRING: die Block-ID, z. B. minecraft:stone
- `properties` HASH STRING: Blockzustände wie facing (optional, Standard leer)
- `inventory` STRUCT c0de/minecraft/POSITION: eine Truhe oder ein anderer Block mit Inventar, aus dem der Block genommen wird (optional)

**Beispiel**

```c0de
CALL c0de/minecraft/PLACE_BLOCK
    PARAMETER position spot ;
    PARAMETER id "minecraft:torch" ;
    PARAMETER inventory chest ;
```

### REMOVE_BLOCK

Entfernt den Block an einer Position.

Mit `drop` fällt der Block als Gegenstand heraus, wie beim Abbauen. Das braucht allowBlockChanges oder einen Administrator als Besitzer, sonst wird PERMISSION_DENIED ausgelöst.

**Parameter und Ergebnisse**

- `position` STRUCT c0de/minecraft/POSITION: wo der Block ist
- `drop` BOOLEAN: wahr, damit der Block als Gegenstand herausfällt (optional, Standard TRUE)

**Beispiel**

```c0de
CALL c0de/minecraft/REMOVE_BLOCK
    PARAMETER position spot ;
    PARAMETER drop FALSE ;
```

### MINE_BLOCK

Baut einen Block ab wie ein Spieler, mit einem Werkzeug aus einem Inventar.

Die Funktion nimmt das passendste Werkzeug aus `tool_inventory`: zuerst ein Werkzeug, das den Block droppen lässt und schneller ist (eine Spitzhacke für Bruchstein, eine Schere für Laub), dann einen Gegenstand, der den Block droppen lässt, dann ein schnelleres Werkzeug und sonst die bloßen Hände. Von mehreren gleich passenden Werkzeugen nimmt sie das erste, z. B. bei Bruchstein die Eisenspitzhacke in Slot 0 vor der Diamantspitzhacke in Slot 2; bei Obsidian nimmt sie die Diamantspitzhacke.

Das Werkzeug nutzt sich ab, als hätte ein Spieler es benutzt, und das Programm wartet so lange, wie ein Spieler zum Abbauen des Blocks bräuchte. Die Drops kommen in `output_inventory`; ohne dieses fallen sie auf den Boden. Wenn sie nicht in das Ausgabeinventar passen, wird INVENTORY_FULL ausgelöst und der Block bleibt stehen. Grundgestein und Flüssigkeiten können nicht abgebaut werden (INVALID_ARGUMENT). Jeder Besitzer darf MINE_BLOCK verwenden, weil es nur tut, was Spieler auch können.

**Parameter und Ergebnisse**

- `position` STRUCT c0de/minecraft/POSITION: wo der Block ist
- `tool_inventory` STRUCT c0de/minecraft/POSITION: der Block, dessen Inventar die Werkzeuge enthält, z. B. eine Truhe
- `output_inventory` STRUCT c0de/minecraft/POSITION: der Block, dessen Inventar die Drops bekommt (optional)

**Beispiel**

```c0de
CALL c0de/minecraft/MINE_BLOCK
    PARAMETER position spot ;
    PARAMETER tool_inventory tools ;
    PARAMETER output_inventory chest ;
```

### SPAWN_ITEM

Erzeugt Gegenstände auf dem Boden an einer Position.

`count` muss 1 bis 64 sein. Das Ergebnis ist die UUID des neuen Gegenstandsobjekts. Das braucht allowSpawning oder einen Administrator als Besitzer, sonst wird PERMISSION_DENIED ausgelöst.

**Parameter und Ergebnisse**

- `position` STRUCT c0de/minecraft/POSITION: wo die Gegenstände erscheinen
- `id` STRING: die Gegenstands-ID, z. B. minecraft:bread
- `count` INTEGER: die Anzahl der Gegenstände, 1 bis 64
- Ergebnis `result` STRING: die UUID des Gegenstandsobjekts

**Beispiel**

```c0de
CALL c0de/minecraft/SPAWN_ITEM
    PARAMETER position spot ;
    PARAMETER id "minecraft:bread" ;
    PARAMETER count 3 ;
    RESULT result itemId
```

### SPAWN_ENTITY

Erzeugt ein Objekt, z. B. ein Schwein, an einer Position.

Das Ergebnis ist die UUID des neuen Objekts. Das braucht allowSpawning oder einen Administrator als Besitzer, sonst wird PERMISSION_DENIED ausgelöst. Ein unbekannter Typ löst INVALID_ARGUMENT aus.

**Parameter und Ergebnisse**

- `position` STRUCT c0de/minecraft/POSITION: wo das Objekt erscheint
- `type` STRING: der Objekttyp, z. B. minecraft:pig
- Ergebnis `result` STRING: die UUID des Objekts

**Beispiel**

```c0de
CALL c0de/minecraft/SPAWN_ENTITY
    PARAMETER position pen ;
    PARAMETER type "minecraft:pig" ;
    RESULT result pigId
```

### FIND_ENTITIES

Findet alle Objekte in der Nähe einer Position.

Gegenstände zählen auch als Objekte. Jedes ENTITY hat uuid, type, name und position. `radius` muss 1 bis 64 sein, sonst wird INVALID_ARGUMENT ausgelöst.

**Parameter und Ergebnisse**

- `position` STRUCT c0de/minecraft/POSITION: die Mitte der Suche
- `radius` INTEGER: der Suchradius in Blöcken, 1 bis 64
- `type` STRING: nur dieser Objekttyp (optional, Standard leer für alle Typen)
- Ergebnis `result` ARRAY STRUCT c0de/minecraft/ENTITY: die gefundenen Objekte

**Beispiel**

```c0de
CALL c0de/minecraft/FIND_ENTITIES
    PARAMETER position pen ;
    PARAMETER radius 8 ;
    PARAMETER type "minecraft:sheep" ;
    RESULT result sheep
```

### TELEPORT

Teleportiert ein Objekt oder einen Gegenstand an eine Position.

Das braucht allowTeleporting oder einen Administrator als Besitzer. Nur Administratoren können Spieler teleportieren. Beides löst sonst PERMISSION_DENIED aus. Ein Objekt, das nicht geladen ist, löst INVALID_ARGUMENT aus.

**Parameter und Ergebnisse**

- `uuid` STRING: die UUID des Objekts
- `position` STRUCT c0de/minecraft/POSITION: das Ziel

**Beispiel**

```c0de
CALL c0de/minecraft/TELEPORT
    PARAMETER uuid pigId ;
    PARAMETER position barn ;
```
