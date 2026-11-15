```json
{
  "title": "TAG",
  "icon": "c0de:tag_block",
  "category": "values_and_names",
  "ordinal": 5
}
```

Ein TAG-Block legt den Namen eines Werts in JSON- oder YAML-Text fest oder verbirgt einen Wert vor dem Debugging-Werkzeug.

Klicke ihn mit leerer Hand mit rechts an. Wähle die Sprache, `json`, `yaml` oder `security`, und gib den Namen ein. Tags kommen ans Ende einer Zeile mit einem Strukturelement oder einer VARIABLE- oder CONSTANT-Zeile. Pro Sprache passt nur ein Tag in eine Zeile.

Bei einem Strukturelement ersetzt der Tag den Namen des Elements. Der Name `-` überspringt das Element. Bei einer Variable ist der Name ein Pfad mit Punkten, zum Beispiel `server.port`.

Der Tag `security: "sensitive"` hat nur diesen einen Namen, den der Dialog einträgt. Das Debugging-Werkzeug zeigt `***` statt des Werts der Variable, der Konstante oder des Strukturelements. Dein Programm kann den Wert trotzdem verwenden.

<recipe;c0de:tag_block>

Lege einen braunen Wollblock in die Mitte und acht NOOP-Blöcke darum herum. Du bekommst acht TAG-Blöcke.

```c0de
STRUCT player
BEGIN
  playerName STRING TAG(json: "name")
END STRUCT
VARIABLE port INTEGER 25565 TAG(json: "server.port")
```

TO\_JSON macht aus `port` den Text `{"server": {"port": 25565}}`.
