```json
{
  "title": "PROGRAM",
  "icon": "c0de:program_block",
  "category": "program_structure",
  "ordinal": 2
}
```

Der PROGRAM-Block beginnt jedes Programm.

Er steht in Zeile 0 bei x=5, gefolgt von einem LABEL mit dem Programmnamen. Der Name muss in der ganzen c0de-Dimension eindeutig sein. Zeile 1 muss eine NOOP-Zeile sein. Das Programm endet an der ersten leeren Zeile.

<recipe;c0de:program_block>

Stelle einen NOOP-Block und ein Buch zusammen her.

```c0de
PROGRAM first
NOOP
```
