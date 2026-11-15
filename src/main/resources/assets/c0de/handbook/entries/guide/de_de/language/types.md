```json
{
  "title": "Typen",
  "icon": "c0de:program_block",
  "category": "language",
  "ordinal": 4
}
```

Ein [Typblock](^tools/type_blocks) folgt auf den Namen einer Variablen, einer Konstanten, eines Parameters oder eines Ergebnisses. Einfache Typen:

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

**Rezepte**

<item;c0de:type_boolean_block>

<recipe;c0de:type_boolean_block>

<item;c0de:type_integer_block>

<recipe;c0de:type_integer_block>

<item;c0de:type_float_block>

<recipe;c0de:type_float_block>

<item;c0de:type_char_block>

<recipe;c0de:type_char_block>

<item;c0de:type_time_block>

<recipe;c0de:type_time_block>

<item;c0de:type_string_block>

<recipe;c0de:type_string_block>

<item;c0de:type_array_block>

<recipe;c0de:type_array_block>

<item;c0de:type_hash_block>

<recipe;c0de:type_hash_block>

<item;c0de:type_struct_block>

<recipe;c0de:type_struct_block>

<item;c0de:type_error_block>

<recipe;c0de:type_error_block>

<item;c0de:label_block>

<recipe;c0de:label_block>
