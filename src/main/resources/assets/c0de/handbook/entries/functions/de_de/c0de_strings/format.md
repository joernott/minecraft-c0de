```json
{
  "title": "FORMAT",
  "icon": "minecraft:book",
  "category": "c0de_strings",
  "ordinal": 12
}
```

Formatiert einen Wert mit einem Format-String als Text.

Umwandlungen beginnen mit `%`, z. B. `%5d` (ganze Zahl, 5 Zeichen breit), `%.2f` (2 Nachkommastellen), `%s` (beliebiger Wert), `%tF` und `%tT` (Datum und Uhrzeit). Ein falscher Typ löst TYPE\_MISMATCH aus, ein ungültiger Format-String INVALID\_ARGUMENT.

Alle Umwandlungen verwenden denselben Wert, ein Wert kann also mehrere von ihnen füllen: `"%tF %tT"` zeigt Datum und Uhrzeit, und `"%d = 0x%X"` macht aus 255 den Text `255 = 0xFF`. Für zwei Werte rufst du FORMAT zweimal auf und verbindest die Texte mit `⊕`.

`%f`, `%e` und `%g` nehmen INTEGERs und FLOATs. `%d`, `%x` und `%o` nehmen nur INTEGERs, runde ein FLOAT also zuerst mit ROUND oder INT aus c0de/math.

Der Text ist immer englisch, egal welche Sprache das Spiel oder der Server verwendet: `.` ist das Dezimalzeichen, `,` das Tausendertrennzeichen (`%,d` macht aus 1234567 den Text `1,234,567`), und die Namen der Wochentage und Monate sind englisch (`%tA` ergibt `Monday`). Die `%t`-Codes verwenden die Zeitzone des Servers.

- `value` BOOLEAN|INTEGER|FLOAT|CHAR|STRING|TIME: der Wert
- `format` STRING: der Format-String
- Ergebnis `result` STRING: der formatierte Text

```c0de
CALL c0de/strings/FORMAT
  PARAMETER value now ;
  PARAMETER format "%tF %tT" ;
  RESULT result text
```
