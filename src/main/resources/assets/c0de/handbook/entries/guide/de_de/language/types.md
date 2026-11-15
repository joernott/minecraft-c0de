```json
{
  "title": "Typen",
  "icon": "c0de:program_block",
  "category": "language",
  "ordinal": 4
}
```

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
