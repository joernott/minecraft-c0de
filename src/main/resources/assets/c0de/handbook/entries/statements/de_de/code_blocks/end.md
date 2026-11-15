```json
{
  "title": "END",
  "icon": "c0de:end_block",
  "category": "code_blocks",
  "ordinal": 1
}
```

END schließt einen Codeblock.

Er hat dasselbe x wie sein BEGIN. Danach folgt das Schlüsselwort, das den Block geöffnet hat, zum Beispiel `END FUNCTION` oder `END WHILE`. Bei einem IF mit ELSE steht das erste END allein. Nach `END REPEAT` kommt die Schleifenbedingung.

<recipe;c0de:end_block>

Verwandle einen BEGIN-Block in einen END-Block.

```c0de
FUNCTION answer
  RESULT result INTEGER
BEGIN
  ASSIGN result 42 ;
END FUNCTION
```
