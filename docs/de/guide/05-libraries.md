# Bibliotheken

c0de bringt fertige Funktionen mit. Einige sind eingebaut und immer da. Die anderen sind in Bibliotheken zusammengefasst, die du mit einer INCLUDE-Zeile lädst.

## Aufrufen

Alle diese Funktionen werden mit CALL aufgerufen, wie deine eigenen Funktionen. Eingebaute Funktionen benutzen ihren einfachen Namen. Bibliotheksfunktionen benutzen ihren vollen Namen, zum Beispiel `c0de/strings/MID`.

```c0de
CALL c0de/strings/MID
    PARAMETER text name ;
    PARAMETER start 2 ;
    PARAMETER length 3 ;
    RESULT result part
```

Eine Funktion mit einem Ergebnis nennt es `result`. Positionen in Texten und Listen beginnen bei 0.

## Werte ändern

Funktionen bekommen Kopien ihrer Parameter. Eine Funktion, die einen Text, eine Liste oder einen Hash ändert, gibt die geänderte Kopie als `result` zurück.

Um eine Variable zu ändern, benutze sie sowohl als Parameter als auch als Ziel des Ergebnisses:

```c0de
CALL INSERT
    PARAMETER array list ;
    PARAMETER index 0 ;
    PARAMETER value 0 ;
    RESULT result list
```

## Bibliotheken einbinden

INCLUDE-Zeilen kommen direkt nach dem NOOP in Zeile 1, vor jeder anderen Anweisung. Sie beginnen bei x=5, wie die PROGRAM-Zeile.

```c0de
PROGRAM math
NOOP
INCLUDE c0de/math
INCLUDE c0de/strings
```

Der LABEL-Dialog neben einem INCLUDE-Block zeigt alle Bibliotheken, aus denen du wählen kannst.

## Eingebaute Funktionen

Diese funktionieren ohne INCLUDE:

- LEN: die Länge eines Texts, Arrays oder Hashs
- HAS_KEY: wahr, wenn ein Hash einen Schlüssel hat
- REMOVE: ein Array oder Hash ohne ein Element
- INSERT: ein Array mit einem neuen Element an einer Position
- CLEAR: ein leeres Array oder ein leerer Hash
- SWAP: tauscht zwei Werte
- RAISE: löst deinen eigenen Fehler aus, Code 1000 oder höher

## c0de/math

Zahlen und Zufallswerte. Sie hat auch die Konstante `c0de/math/PI`, die du in einem Ausdruck wie eine Variable verwendest, ohne CALL.

- ABS, SGN: Betrag und Vorzeichen einer Zahl
- SQR, EXP, LOG: Quadratwurzel, e hoch, Logarithmus
- SIN, COS, TAN, ATN: Winkel im Bogenmaß
- INT, FIX, ROUND: machen aus einem FLOAT einen INTEGER
- TO_FLOAT: macht aus einem INTEGER einen FLOAT, gegen NUMERIC_OVERFLOW bei großen Rechnungen
- DIV: ganzzahlige Division
- RANDOM: ein zufälliger FLOAT von 0 bis unter 1
- RANDOMIZE: legt den Startwert der Zufallszahlen fest

## Runden

INT rundet ab: -12.85 ergibt -13. FIX schneidet einfach die Nachkommastellen ab: -12.85 ergibt -12. ROUND geht zur nächsten ganzen Zahl. Ein .5 geht zum geraden Nachbarn, also ergibt 12.5 die 12 und 13.5 die 14.

```c0de
CALL c0de/math/ROUND
    PARAMETER value 12.5 ;
    RESULT result rounded
```

## c0de/strings

Text und Zeichen:

- LEFT, RIGHT, MID: Teile eines Texts
- REPLACE: einen Teil eines Texts überschreiben
- INSTR: einen Text in einem Text finden, -1, wenn er nicht gefunden wird
- UCASE, LCASE: Groß- und Kleinbuchstaben
- SPACE, FILL: ein Text aus Leerzeichen oder aus einem Zeichen
- ASC, CHR: Zeichen und ihre Codenummern

Umwandlungen:

- STR: ein beliebiger einfacher Wert als Text
- FORMAT: ein Wert als Text in einem Format deiner Wahl
- VAL: ein Text als Zahl
- HEX, OCT: eine Zahl hexadezimal oder oktal

Reguläre Ausdrücke:

- REGEX_MATCH: passt ein Text zu einem Muster wie `^[0-9]+$`?
- REGEX_REPLACE: ersetzen, was ein Muster findet, wie sed

## FORMAT

FORMAT macht aus einem Wert mit einem Formatstring einen Text. `%d` ist eine ganze Zahl, `%f` eine Dezimalzahl und `%s` ein beliebiger Wert. Eine Zahl zwischen `%` und dem Buchstaben legt die Breite fest.

- `[%4d]` macht aus 42 `[  42]`
- `%05d` macht aus 42 `00042`
- `%.2f` macht aus 3.14159 `3.14`
- `%,d` macht aus 1234567 `1,234,567`

Für TIME-Werte ergibt `%tF` das Datum und `%tT` die Uhrzeit, zum Beispiel `2026-10-19` und `14:05:09`. Es gibt noch viele weitere Codes, etwa `%tA` für den Namen des Wochentags.

Alle Codes verwenden denselben Wert, `"%tF %tT"` zeigt also Datum und Uhrzeit auf einmal. `%f` nimmt auch einen INTEGER, aber `%d` nimmt kein FLOAT. Der Text ist immer englisch: `.` und `,` in Zahlen, englische Namen der Wochentage und Monate, in der Zeitzone des Servers.

## JSON und YAML

c0de/strings kann außerdem jeden Wert mit TO_JSON und TO_YAML in JSON- oder YAML-Text verwandeln. FROM_JSON und FROM_YAML lesen solchen Text zurück in eine Variable.

TAG-Blöcke am Ende einer VARIABLE-Zeile oder eines Strukturelements legen die Schlüsselnamen fest, die in JSON oder YAML verwendet werden.

## c0de/time

Echte Uhrzeit und echtes Datum, in der Zeitzone des Servers:

- NOW: die aktuelle Zeit als TIME-Wert
- YEAR, MONTH, DAY: Teile des Datums
- HOUR, MINUTE, SECOND, MILLISECOND: Teile der Uhrzeit
- WEEKDAY: 1 für Montag bis 7 für Sonntag
- WEEK: die Kalenderwoche
- TIMER: Sekunden seit Mitternacht
- SLEEP: eine Anzahl von Spielticks warten

Die Datums- und Zeitfunktionen benutzen die aktuelle Zeit oder einen TIME-Wert, den du als `time` übergibst. SLEEP wartet zwischen 1 und 72000 Ticks.

TIMER beginnt um Mitternacht bei 0. WEEK liefert die ISO-Woche: Am 1. Januar kann noch Woche 52 oder 53 des Vorjahres sein, und keine Funktion liefert dieses Jahr.

## c0de/minecraft

Mit dieser Bibliothek können Programme die Welt rund um den Auslöserblock berühren:

- TICKS: die Spielzeit der Welt, nicht die Tageszeit
- BEEP, SOUND: Töne spielen wie ein Notenblock
- SAY: eine Nachricht in den Chat schreiben
- GET_BLOCK: einen Block, seine Redstone-Stärke und seine Daten lesen
- GET_INVENTORY: die Gegenstände in einer Truhe oder einem anderen Behälter lesen

- GET_LEVER, SET_LEVER: Hebel lesen oder umlegen
- GET_BUTTON, PRESS_BUTTON: Knöpfe lesen oder drücken
- GET_PRESSURE_PLATE: Druckplatten lesen
- GET_OPEN, SET_OPEN: Türen, Falltüren und Zauntore
- FIND_ENTITIES: Kreaturen und Gegenstände in der Nähe finden

## Die Welt verändern

Manche Funktionen verändern die Welt stärker:

- DELETE_ITEMS: Gegenstände aus Behältern entfernen
- PLACE_BLOCK, REMOVE_BLOCK: Blöcke setzen oder abbauen
- SPAWN_ITEM, SPAWN_ENTITY: Gegenstände und Kreaturen erschaffen
- TELEPORT: ein Objekt bewegen
- COMMAND: einen Befehl ausführen wie ein Befehlsblock

Positionen werden als Struktur `c0de/minecraft/POSITION` mit x, y, z und einer optionalen Dimension angegeben. Eine leere Dimension bedeutet die Dimension des Auslöserblocks. Programme können die Blöcke auf den Blättern der c0de-Dimension niemals verändern.

## Besitzer und Berechtigungen

Programme handeln im Namen des Besitzers des Auslöserblocks. Die weltverändernden Funktionen oben funktionieren nur, wenn der Besitzer ein Operator ist oder wenn der Server sie in der Datei `config/c0de.properties` erlaubt.

Sonst lösen sie PERMISSION_DENIED aus. Spieler können nur teleportiert werden, wenn der Besitzer ein Operator ist, und nur Operatoren können mit COMMAND Befehle ausführen oder Befehlsblöcken ihre Daten geben.

Drei Funktionen funktionieren für jeden Besitzer, weil sie nur tun, was auch ein Spieler kann:

- MOVE_ITEMS verschiebt Gegenstände zwischen zwei Truhen, die der Besitzer öffnen könnte. Die Gegenstände sind unterwegs, ein Stapel pro Tick, und ein Filter wie `$logs` oder `minecraft:wheat` wählt sie aus.
- MINE_BLOCK baut einen Block mit dem besten Werkzeug aus einer Truhe ab (`tool_inventory`). Das Werkzeug nutzt sich ab, und es dauert so lange wie bei einem Spieler. Was dabei herausfällt, kommt in eine andere Truhe (`output_inventory`) oder fällt auf den Boden. Ist diese Truhe voll, löst die Funktion INVENTORY_FULL aus.
- PLACE_BLOCK mit dem Parameter `inventory` nimmt den Block aus einer Truhe. Enthält die Truhe ihn nicht, löst die Funktion ITEM_MISSING aus.

## Musik machen

Ein kurzes Beispiel: einen Glockenton am Auslöserblock spielen.

```c0de
CALL c0de/minecraft/SOUND
    PARAMETER note 12 ;
    PARAMETER instrument "bell" ;
```

`note` geht von 0 bis 24, wie die Klicks auf einem Notenblock. Ohne `instrument` benutzt SOUND die Harfe.

## c0de/security

Programme brauchen manchmal ein Passwort oder einen Schlüssel, zum Beispiel für einen Webdienst. c0de hält sie auf drei Arten verborgen:

- INPUT mit `PARAMETER hide TRUE ;` zeigt auf dem Laptop `*`, während du tippst.
- Variablen-, Konstanten- und Literalkarten können ihren Wert endgültig verbergen (**Wert verbergen** in ihrem Dialog).
- Der Tag `security: "sensitive"` an einer Variable oder Konstante zeigt im Debugging-Werkzeug `***`.

Verbergen schützt vor Leuten, die auf deinen Bildschirm schauen. Echte Geheimnisse gehören in deinen Tresor.

## Geheimnisse im Tresor

Jeder Spieler hat einen Tresor, solange er online ist. SET_SECRET legt ein Geheimnis hinein, und eine SECRET-Zeile liest es, wie eine Konstante. Nur der Besitzer des Blatts kann den Tresor verwenden, und nur, wenn er das Programm selbst startet, zum Beispiel mit einem Knopf.

Ein dauerhafter Tresor behält Geheimnisse, wenn du gehst. CREATE_VAULT erzeugt einen, OPEN_VAULT öffnet ihn. Dein Spiel fragt nach dem Passwort, und nur dein Spiel kann die Datei öffnen; der Server bewahrt sie auf, kann sie aber nicht lesen.

```c0de
PROGRAM keys
NOOP
INCLUDE c0de/security
NOOP
FUNCTION setup
BEGIN
    VARIABLE exists BOOLEAN
    VARIABLE key STRING
    CALL c0de/security/HAS_VAULT
        RESULT result exists
    IF exists ;
    BEGIN
        CALL c0de/security/OPEN_VAULT
    END
    ELSE
    BEGIN
        CALL c0de/security/CREATE_VAULT
    END IF
    CALL INPUT
        PARAMETER variable key ;
        PARAMETER hide TRUE ;
    CALL c0de/security/SET_SECRET
        PARAMETER name "apiKey" ;
        PARAMETER value key ;
        PARAMETER persistent TRUE ;
END FUNCTION
```

Ein anderes Programm liest den Schlüssel dann nach OPEN_VAULT mit `SECRET apiKey STRING`.
