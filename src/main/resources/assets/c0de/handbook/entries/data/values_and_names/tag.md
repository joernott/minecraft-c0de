```json
{
  "title": "TAG",
  "icon": "c0de:tag_block",
  "category": "values_and_names",
  "ordinal": 5
}
```

A TAG block sets the name of a value in JSON or YAML text, or hides a value from the debugging tool.

Right-click it with an empty hand. Pick the language, `json`, `yaml` or `security`, and enter the name. Tags go at the end of a struct element line or a VARIABLE or CONSTANT line. Only one tag per language fits in a line.

On a struct element, the tag replaces the element name. The name `-` skips the element. On a variable, the name is a path with dots, like `server.port`.

The tag `security: "sensitive"` has only this one name, which the dialog fills in. The debugging tool shows `***` instead of the value of the variable, constant or struct element. Your program can still use the value.

<recipe;c0de:tag_block>

Put a brown wool block in the middle and eight NOOP blocks around it. You get eight TAG blocks.

```c0de
STRUCT player
BEGIN
  playerName STRING TAG(json: "name")
END STRUCT
VARIABLE port INTEGER 25565 TAG(json: "server.port")
```

TO\_JSON turns `port` into `{"server": {"port": 25565}}`.
