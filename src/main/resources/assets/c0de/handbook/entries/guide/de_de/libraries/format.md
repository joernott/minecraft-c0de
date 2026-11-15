```json
{
  "title": "FORMAT",
  "icon": "c0de:include_block",
  "category": "libraries",
  "ordinal": 14
}
```

FORMAT macht aus einem Wert mit einem Formatstring einen Text. `%d` ist eine ganze Zahl, `%f` eine Dezimalzahl und `%s` ein beliebiger Wert. Eine Zahl zwischen `%` und dem Buchstaben legt die Breite fest.

- `[%4d]` macht aus 42 `[  42]`
- `%05d` macht aus 42 `00042`
- `%.2f` macht aus 3.14159 `3.14`
- `%,d` macht aus 1234567 `1,234,567`

Für TIME-Werte ergibt `%tF` das Datum und `%tT` die Uhrzeit, zum Beispiel `2026-10-19` und `14:05:09`. Es gibt noch viele weitere Codes, etwa `%tA` für den Namen des Wochentags.

Alle Codes verwenden denselben Wert, `"%tF %tT"` zeigt also Datum und Uhrzeit auf einmal. `%f` nimmt auch einen INTEGER, aber `%d` nimmt kein FLOAT. Der Text ist immer englisch: `.` und `,` in Zahlen, englische Namen der Wochentage und Monate, in der Zeitzone des Servers.
