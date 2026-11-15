```json
{
  "title": "TYPE_HASH",
  "icon": "c0de:type_hash_block",
  "category": "types",
  "ordinal": 7
}
```

TYPE\_HASH stands for the type HASH: values stored under text keys.

A hash grows and shrinks by itself. After HASH comes the type of its content, like `HASH INTEGER`. A new key adds a new element.

<recipe;c0de:type_hash_block>

Craft a type base and a chest. A trapped chest works too.

```c0de
VARIABLE stock HASH INTEGER
BEGIN
  gold 10
END VARIABLE
ASSIGN stock["diamond"] 3 ;
```
