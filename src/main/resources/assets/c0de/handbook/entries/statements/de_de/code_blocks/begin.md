```json
{
  "title": "BEGIN",
  "icon": "c0de:begin_block",
  "category": "code_blocks",
  "ordinal": 0
}
```

BEGIN beginnt einen Codeblock.

Er hat dasselbe x wie die Zeile, die den Codeblock braucht. Die Zeilen darin stehen einen Block weiter östlich. Variablen, die in einem Codeblock angelegt werden, leben nur dort. Ein Codeblock darf nicht leer sein: Eine NOOP-Zeile reicht.

<recipe;c0de:begin_block>

<recipe;c0de:begin_block_from_end_block>

Lege gelbe Wolle in die Mitte und acht NOOP-Blöcke darum herum für acht BEGIN-Blöcke, oder verwandle einen END-Block in einen BEGIN-Block.

```c0de
WHILE n > 0 ;
BEGIN
  ASSIGN n n - 1 ;
END WHILE
```
