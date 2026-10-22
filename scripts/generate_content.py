"""Generates the data and asset JSON files of the language blocks for both loaders.

The list of blocks matches the enum CodeBlockType in the mod. The script writes block states, block and item
models, loot tables, recipes and the language files en_us and de_de. Generated files are committed; the CI
runs the script and fails if they differ from the committed files.

Usage: python scripts/generate_content.py
"""

import json
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
RESOURCES = ROOT / "src" / "main" / "resources"
ASSETS = RESOURCES / "assets" / "c0de"
DATA = RESOURCES / "data" / "c0de"

# (name of the CodeBlockType constant, validated, transparent, English name, German name)
BLOCKS = [
    ("NOOP", False, True, "NOOP Block", "NOOP-Block"),
    ("COMMENT", False, True, "COMMENT Block", "COMMENT-Block"),
    ("LABEL", True, False, "LABEL Block", "LABEL-Block"),
    ("PROGRAM", True, False, "PROGRAM Block", "PROGRAM-Block"),
    ("INCLUDE", True, False, "INCLUDE Block", "INCLUDE-Block"),
    ("EXIT", True, False, "EXIT Block", "EXIT-Block"),
    ("BEGIN", True, False, "BEGIN Block", "BEGIN-Block"),
    ("END", True, False, "END Block", "END-Block"),
    ("BREAK", True, False, "BREAK Block", "BREAK-Block"),
    ("TYPE_BOOLEAN", False, False, "BOOLEAN Type Block", "BOOLEAN-Typblock"),
    ("TYPE_INTEGER", False, False, "INTEGER Type Block", "INTEGER-Typblock"),
    ("TYPE_FLOAT", False, False, "FLOAT Type Block", "FLOAT-Typblock"),
    ("TYPE_CHAR", False, False, "CHAR Type Block", "CHAR-Typblock"),
    ("TYPE_STRING", False, False, "STRING Type Block", "STRING-Typblock"),
    ("TYPE_ARRAY", False, False, "ARRAY Type Block", "ARRAY-Typblock"),
    ("TYPE_HASH", False, False, "HASH Type Block", "HASH-Typblock"),
    ("TYPE_STRUCT", False, False, "STRUCT Type Block", "STRUCT-Typblock"),
    ("TYPE_TIME", False, False, "TIME Type Block", "TIME-Typblock"),
    ("TYPE_ERROR", False, False, "ERROR Type Block", "ERROR-Typblock"),
    ("VARIABLE", True, False, "VARIABLE Block", "VARIABLE-Block"),
    ("CONSTANT", True, False, "CONSTANT Block", "CONSTANT-Block"),
    ("LITERAL", True, False, "LITERAL Block", "LITERAL-Block"),
    ("TAG", True, False, "TAG Block", "TAG-Block"),
    ("OPERATOR", True, False, "OPERATOR Block", "OPERATOR-Block"),
    ("ASSIGN", True, False, "ASSIGN Block", "ASSIGN-Block"),
    ("FUNCTION", True, False, "FUNCTION Block", "FUNCTION-Block"),
    ("PARAMETER", True, False, "PARAMETER Block", "PARAMETER-Block"),
    ("RESULT", True, False, "RESULT Block", "RESULT-Block"),
    ("RETURN", True, False, "RETURN Block", "RETURN-Block"),
    ("CALL", True, False, "CALL Block", "CALL-Block"),
    ("IF", True, False, "IF Block", "IF-Block"),
    ("ELSE", True, False, "ELSE Block", "ELSE-Block"),
    ("FOR", True, False, "FOR Block", "FOR-Block"),
    ("EACH", True, False, "EACH Block", "EACH-Block"),
    ("WHILE", True, False, "WHILE Block", "WHILE-Block"),
    ("REPEAT", True, False, "REPEAT Block", "REPEAT-Block"),
]

# Trigger blocks: (ID, center ingredient, count, English name, German name)
TRIGGERS = [
    ("trigger_block", "gold_ingot", 8, "Trigger Block", "Auslöserblock"),
    ("timer_block", "clock", 8, "Timer Block", "Zeitgeberblock"),
    ("debug_trigger_block", "redstone_block", 1, "Debug Trigger Block", "Debug-Auslöserblock"),
]

# The floor of the c0de dimension; their block states and block models are written by hand
FLOOR_BLOCKS = ["white_c0de_bedrock", "light_green_c0de_bedrock", "black_c0de_bedrock", "cursor_c0de_bedrock"]

# (item ID, English name, German name)
ITEMS = [
    ("type_base", "Type Base", "Typbasis"),
    ("structural_base", "Structural Base", "Strukturbasis"),
    ("data_item", "Data Item", "Datenkarte"),
    ("variable_item", "Variable", "Variable"),
    ("constant_item", "Constant", "Konstante"),
    ("debug_tool", "Debugging Tool", "Debugging-Werkzeug"),
    ("marker", "Text Marker", "Textmarker"),
    ("hammer", "Hammer", "Hammer"),
    ("c0de_disk", "c0de Disk", "c0de-Diskette"),
]

# Type items: (type, ingredient(s) of the type block)
TYPE_ITEMS = [
    ("BOOLEAN", "redstone_torch"),
    ("INTEGER", "sand"),
    ("FLOAT", "red_sand"),
    ("CHAR", "oak_planks"),
    ("STRING", "string"),
    ("ARRAY", "barrel"),
    ("HASH", ["chest", "trapped_chest"]),
    ("STRUCT", "scaffolding"),
    ("TIME", "clock"),
    ("ERROR", "campfire"),
]


def type_item(type_name: str) -> str:
    return "type_" + type_name.lower() + "_item"


def block_id(name: str) -> str:
    return name.lower() + "_block"


def c0de(name: str) -> str:
    """The ID of a c0de block (by CodeBlockType name) or item (by item ID)."""
    return "c0de:" + (block_id(name) if name.isupper() else name)


def mc(name: str) -> str:
    return "minecraft:" + name


# Recipes (docs/concept/18-block-reference.md). Ingredients are IDs or lists of alternative IDs.
def around(center: str, result: str, count: int = 8) -> dict:
    """Eight NOOP blocks around an item."""
    return shaped(["AAA", "ABA", "AAA"], {"A": c0de("NOOP"), "B": center}, result, count)


def shaped(pattern: list[str], key: dict, result: str, count: int = 1) -> dict:
    return {
        "type": "minecraft:crafting_shaped",
        "category": "redstone",
        "pattern": pattern,
        "key": {k: ingredient(v) for k, v in key.items()},
        "result": {"id": result, "count": count},
    }


def shapeless(ingredients: list, result: str, count: int = 1) -> dict:
    return {
        "type": "minecraft:crafting_shapeless",
        "category": "redstone",
        "ingredients": [ingredient(i) for i in ingredients],
        "result": {"id": result, "count": count},
    }


def ingredient(value) -> dict | list:
    if isinstance(value, list):
        return [{"item": v} for v in value]
    return {"item": value}


def recipes() -> dict[str, dict]:
    noop = c0de("NOOP")
    type_base = c0de("type_base")
    structural = c0de("structural_base")
    r = {
        "noop_block": shaped(
            ["ABA", "B B", "ABA"], {"A": mc("iron_ingot"), "B": mc("stick")}, noop, 64),
        "comment_block": around(mc("glass"), c0de("COMMENT")),
        "label_block": shapeless([noop, mc("paper")], c0de("LABEL")),
        "program_block": shapeless([noop, mc("book")], c0de("PROGRAM")),
        "include_block": shapeless([noop, mc("map")], c0de("INCLUDE")),
        "exit_block": around(mc("iron_door"), c0de("EXIT")),
        "begin_block": around(mc("yellow_wool"), c0de("BEGIN")),
        "begin_block_from_end_block": shapeless([c0de("END")], c0de("BEGIN")),
        "end_block": shapeless([c0de("BEGIN")], c0de("END")),
        "break_block": shapeless([noop, mc("stick")], c0de("BREAK")),
        "type_base": around(mc("red_carpet"), type_base),
        "type_boolean_block": shapeless([type_base, mc("redstone_torch")], c0de("TYPE_BOOLEAN")),
        "type_integer_block": shapeless([type_base, mc("sand")], c0de("TYPE_INTEGER")),
        "type_integer_block_from_type_float_block": shapeless(
            [c0de("TYPE_FLOAT"), mc("yellow_dye")], c0de("TYPE_INTEGER")),
        "type_float_block": shapeless([type_base, mc("red_sand")], c0de("TYPE_FLOAT")),
        "type_float_block_from_type_integer_block": shapeless(
            [c0de("TYPE_INTEGER"), mc("orange_dye")], c0de("TYPE_FLOAT")),
        "type_char_block": shapeless([type_base, mc("oak_planks")], c0de("TYPE_CHAR")),
        "type_string_block": shapeless([type_base, mc("string")], c0de("TYPE_STRING")),
        "type_array_block": shapeless([type_base, mc("barrel")], c0de("TYPE_ARRAY")),
        "type_hash_block": shapeless([type_base, [mc("chest"), mc("trapped_chest")]], c0de("TYPE_HASH")),
        "type_struct_block": shapeless([type_base, mc("scaffolding")], c0de("TYPE_STRUCT")),
        "type_time_block": shapeless([type_base, mc("clock")], c0de("TYPE_TIME")),
        "type_error_block": shapeless([type_base, mc("campfire")], c0de("TYPE_ERROR")),
        "variable_block": shapeless([type_base, mc("bone_block")], c0de("VARIABLE")),
        "variable_block_from_constant_block": shapeless([c0de("CONSTANT")], c0de("VARIABLE")),
        "constant_block": shapeless([type_base, mc("bone"), mc("bone"), mc("bone")], c0de("CONSTANT")),
        "constant_block_from_variable_block": shapeless([c0de("VARIABLE")], c0de("CONSTANT")),
        "literal_block": around(mc("white_wool"), c0de("LITERAL")),
        "tag_block": around(mc("brown_wool"), c0de("TAG")),
        "operator_block": around(mc("red_wool"), c0de("OPERATOR")),
        "assign_block": around(mc("cyan_wool"), c0de("ASSIGN")),
        "function_block": shapeless([noop, mc("stone")], c0de("FUNCTION")),
        "parameter_block": around(mc("hopper"), c0de("PARAMETER")),
        "parameter_block_from_result_block": shapeless([c0de("RESULT")], c0de("PARAMETER")),
        "result_block": shapeless([c0de("PARAMETER")], c0de("RESULT")),
        "return_block": around(mc("spruce_door"), c0de("RETURN")),
        "call_block": shapeless([noop, mc("smooth_stone")], c0de("CALL")),
        "structural_base": around(mc("green_wool"), structural),
    }
    for trigger, center, count, _, _ in TRIGGERS:
        r[trigger] = shaped(
            ["ABA", "BCB", "ABA"], {"A": mc("copper_ingot"), "B": mc("glass"), "C": mc(center)}, c0de(trigger), count)
    r["debug_tool"] = shaped(
        ["  A", " B ", "C  "], {"A": mc("leather"), "B": mc("stick"), "C": mc("red_wool")}, c0de("debug_tool"))
    r["marker"] = shapeless([mc("feather"), mc("yellow_dye")], c0de("marker"))
    r["hammer"] = shaped(
        ["AAA", " A ", " B "], {"A": mc("iron_ingot"), "B": mc("red_wool")}, c0de("hammer"))
    r["c0de_disk"] = shapeless([c0de("data_item"), mc("iron_ingot"), mc("black_dye")], c0de("c0de_disk"))
    r["data_item"] = shapeless([mc("paper"), mc("white_dye"), mc("yellow_dye")], c0de("data_item"), 4)
    # Uninitialized items have no type: result targets for results without a parameter
    r["variable_item_uninitialized"] = shapeless([c0de("data_item"), mc("light_blue_dye")], c0de("variable_item"))
    r["constant_item_uninitialized"] = shapeless([c0de("data_item"), mc("pink_dye")], c0de("constant_item"))
    for type_name, ingredient_ in TYPE_ITEMS:
        source = [mc(i) for i in ingredient_] if isinstance(ingredient_, list) else mc(ingredient_)
        r[type_item(type_name)] = shapeless([c0de("data_item"), mc("red_dye"), source], c0de(type_item(type_name)))
        for kind, dye in (("variable_item", "light_blue_dye"), ("constant_item", "pink_dye")):
            recipe = shapeless([c0de("data_item"), c0de(type_item(type_name)), mc(dye)], c0de(kind), 2)
            recipe["result"]["components"] = {"minecraft:custom_data": {"type": type_name}}
            r[kind + "_" + type_name.lower()] = recipe
    for block, seed, partner in [
        ("IF", "wheat_seeds", "ELSE"),
        ("ELSE", "wheat", "IF"),
        ("FOR", "dirt", "EACH"),
        ("EACH", "cobblestone", "FOR"),
        ("WHILE", "diorite", "REPEAT"),
        ("REPEAT", "granite", "WHILE"),
    ]:
        r[block_id(block)] = shapeless([structural, mc(seed)], c0de(block))
        r[block_id(block) + "_from_" + block_id(partner)] = shapeless([c0de(partner)], c0de(block))
    return r


# Translations of the user interface: (key, English, German)
MESSAGES = [
    ("itemGroup.c0de", "c0de", "c0de"),
    ("block.c0de.white_c0de_bedrock", "White c0de Bedrock", "Weißes c0de-Grundgestein"),
    ("block.c0de.light_green_c0de_bedrock", "Light Green c0de Bedrock", "Hellgrünes c0de-Grundgestein"),
    ("block.c0de.black_c0de_bedrock", "Black c0de Bedrock", "Schwarzes c0de-Grundgestein"),
    ("block.c0de.cursor_c0de_bedrock", "Cursor c0de Bedrock", "Cursor-c0de-Grundgestein"),
    ("block.c0de.pid_block", "PID Block", "PID-Block"),
    ("entity.c0de.cursor", "Cursor", "Cursor"),
    ("entity.c0de.mark", "Mark", "Markierung"),
    ("message.c0de.debug", "Debug cursor: %s", "Debug-Cursor: %s"),
    ("message.c0de.debug_started", "Debug process started, PID %s", "Debug-Prozess gestartet, PID %s"),
    ("message.c0de.not_started", "Not started: %s", "Nicht gestartet: %s"),
    ("message.c0de.no_marks", "Mark the lines to move or copy with the text marker first",
     "Markiere zuerst die Zeilen zum Verschieben oder Kopieren mit dem Textmarker"),
    ("message.c0de.target_marked", "The target line is marked", "Die Zielzeile ist markiert"),
    ("message.c0de.moved", "%s lines moved in front of line %s", "%s Zeilen vor Zeile %s verschoben"),
    ("message.c0de.copied", "%s lines copied in front of line %s", "%s Zeilen vor Zeile %s kopiert"),
    ("message.c0de.missing_blocks", "Missing blocks: %s", "Fehlende Blöcke: %s"),
    ("message.c0de.deleted", "Lines %s to %s deleted", "Zeilen %s bis %s gelöscht"),
    ("message.c0de.protected", "The program %s is running (PID %s), its sheet can't be changed",
     "Das Programm %s läuft (PID %s), sein Blatt kann nicht geändert werden"),
    ("message.c0de.exported", "Program %s saved to the disk", "Programm %s auf der Diskette gespeichert"),
    ("message.c0de.imported", "Program %s loaded from the disk", "Programm %s von der Diskette geladen"),
    ("dialog.c0de.disk.import", "Load a program", "Programm laden"),
    ("dialog.c0de.disk.overwrite", "Save the program", "Programm speichern"),
    ("dialog.c0de.disk.exists", "The file of %s exists. Overwrite it?", "Die Datei von %s existiert. Überschreiben?"),
    ("dialog.c0de.disk.confirm", "Overwrite", "Überschreiben"),
    ("dialog.c0de.disk.empty", "There are no saved programs", "Es gibt keine gespeicherten Programme"),
    ("dialog.c0de.trigger.input", "Input", "Eingabe"),
    ("dialog.c0de.trigger.output", "Output", "Ausgabe"),
    ("dialog.c0de.trigger.program", "Program", "Programm"),
    ("dialog.c0de.trigger.function", "Function", "Funktion"),
    ("dialog.c0de.trigger.interval", "Interval in ticks", "Intervall in Ticks"),
    ("dialog.c0de.trigger.camouflage", "Camouflage", "Tarnung"),
    ("dialog.c0de.trigger.pid", "Running, PID %s", "Läuft, PID %s"),
    ("dialog.c0de.trigger.exit_code", "Exit code %s", "Exit-Code %s"),
    ("dialog.c0de.trigger.error_at", "in %s, line %s", "in %s, Zeile %s"),
    ("dialog.c0de.trigger.owner", "Owner: %s", "Besitzer: %s"),
    ("item.c0de.wrench", "Wrench", "Schraubenschlüssel"),
    ("message.c0de.no_dimension", "The c0de dimension doesn't exist in this world.",
     "Die c0de-Dimension existiert in dieser Welt nicht."),
    ("message.c0de.not_saved", "Not saved: %s", "Nicht gespeichert: %s"),
    ("item.c0de.variable_item.named", "Variable %s", "Variable %s"),
    ("item.c0de.constant_item.named", "Constant %s", "Konstante %s"),
    ("item.c0de.variable_item.uninitialized", "Uninitialized Variable", "Nicht initialisierte Variable"),
    ("item.c0de.constant_item.uninitialized", "Uninitialized Constant", "Nicht initialisierte Konstante"),
    ("dialog.c0de.item.clear", "Clear all fields", "Alle Felder leeren"),
    ("item.c0de.value_item.type", "Type: %s", "Typ: %s"),
    ("item.c0de.value_item.value", "Value: %s", "Wert: %s"),
    ("dialog.c0de.item.name", "Name", "Name"),
    ("dialog.c0de.item.type", "Type, e.g. ARRAY INTEGER or STRUCT point", "Typ, z. B. ARRAY INTEGER oder STRUCT point"),
    ("dialog.c0de.item.value", "Value (JSON for ARRAY, HASH, STRUCT, ERROR)", "Wert (JSON für ARRAY, HASH, STRUCT, ERROR)"),
    ("message.c0de.not_placed", "Can't be placed here: %s", "Kann hier nicht platziert werden: %s"),
    ("dialog.c0de.ok", "Save", "Speichern"),
    ("dialog.c0de.read_only", " (read only)", " (schreibgeschützt)"),
    ("dialog.c0de.label", "Label", "Label"),
    ("dialog.c0de.label.new_name", "Name", "Name"),
    ("dialog.c0de.label.library", "Library", "Bibliothek"),
    ("dialog.c0de.label.function", "Function", "Funktion"),
    ("dialog.c0de.label.call_parameter", "Parameter", "Parameter"),
    ("dialog.c0de.label.call_result", "Result", "Ergebnis"),
    ("dialog.c0de.label.struct_type", "Struct type", "Struct-Typ"),
    ("dialog.c0de.label.reference", "Variable or constant", "Variable oder Konstante"),
    ("dialog.c0de.label.target", "Variable", "Variable"),
    ("dialog.c0de.label.index", "Index, e.g. [i][2], [\"key\"] or .element",
     "Index, z. B. [i][2], [\"key\"] oder .element"),
    ("dialog.c0de.literal", "Literal", "Literal"),
    ("dialog.c0de.literal.type", "Type", "Typ"),
    ("dialog.c0de.literal.value", "Value", "Wert"),
    ("dialog.c0de.operator", "Operator", "Operator"),
    ("dialog.c0de.comment", "Comment", "Kommentar"),
    ("dialog.c0de.comment.placeholder", "Explain your code", "Erkläre deinen Code"),
    ("dialog.c0de.tag", "Tag", "Tag"),
    ("dialog.c0de.tag.language", "Language", "Sprache"),
    ("dialog.c0de.tag.path", "Path, e.g. server.port", "Pfad, z. B. server.port"),
    ("dialog.c0de.tag.key", "Key, or - to skip the element", "Schlüssel, oder - zum Überspringen des Elements"),
]

# Names of the operators (enum Operator without NEGATE): (constant, English, German)
OPERATORS = [
    ("BITWISE_NOT", "Bitwise NOT", "Bitweises NICHT"),
    ("NOT", "Logical NOT", "Logisches NICHT"),
    ("POWER", "Power", "Potenz"),
    ("MULTIPLY", "Multiplication", "Multiplikation"),
    ("DIVIDE", "Division", "Division"),
    ("MODULUS", "Modulus", "Modulo"),
    ("ADD", "Addition", "Addition"),
    ("SUBTRACT", "Subtraction or negation", "Subtraktion oder Negation"),
    ("SHIFT_LEFT", "Shift left", "Linksverschiebung"),
    ("SHIFT_RIGHT", "Shift right", "Rechtsverschiebung"),
    ("CONCAT", "Concatenation", "Verkettung"),
    ("EQUAL", "Equal", "Gleich"),
    ("NOT_EQUAL", "Not equal", "Ungleich"),
    ("LESS", "Less than", "Kleiner als"),
    ("GREATER", "Greater than", "Größer als"),
    ("LESS_OR_EQUAL", "Less than or equal", "Kleiner oder gleich"),
    ("GREATER_OR_EQUAL", "Greater than or equal", "Größer oder gleich"),
    ("BITWISE_AND", "Bitwise AND", "Bitweises UND"),
    ("BITWISE_XOR", "Bitwise exclusive OR", "Bitweises exklusives ODER"),
    ("BITWISE_OR", "Bitwise OR", "Bitweises ODER"),
    ("AND", "Logical AND", "Logisches UND"),
    ("XOR", "Logical exclusive OR", "Logisches exklusives ODER"),
    ("OR", "Logical OR", "Logisches ODER"),
    ("OPEN", "Opening parenthesis", "Öffnende Klammer"),
    ("CLOSE", "Closing parenthesis", "Schließende Klammer"),
    ("END_OF_EXPRESSION", "End of the expression", "Ende des Ausdrucks"),
]


def write(path: Path, content: dict) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(content, indent=2, ensure_ascii=False) + "\n", encoding="utf-8", newline="\n")


# Blocks with a texture per side. The texture of the north side is mirrored compared with the south side, so
# the triangle of ASSIGN points to positive x on all sides; the sides facing along x show a dot (east, the arrow
# points at the viewer) and a cross (west, the arrow points away).
SIDES = {
    "ASSIGN": {"north": "left", "south": "right", "up": "right", "down": "right", "east": "dot", "west": "cross"},
}


def block_model(bid: str, suffix: str, sides: dict | None) -> dict:
    texture = "c0de:block/" + bid + suffix
    if sides is None:
        return {"parent": "minecraft:block/cube_all", "textures": {"all": texture}}
    textures = {side: texture + "_" + name for side, name in sides.items()}
    textures["particle"] = textures["north"]
    return {"parent": "minecraft:block/cube", "textures": textures}


def block_files(name: str, validated: bool, transparent: bool) -> None:
    bid = block_id(name)
    if validated:
        variants = {}
        for valid in ("true", "false"):
            suffix = "_valid" if valid == "true" else "_invalid"
            variants["valid=" + valid] = {"model": "c0de:block/" + bid + suffix}
            write(ASSETS / "models" / "block" / (bid + suffix + ".json"), block_model(bid, suffix, SIDES.get(name)))
        write(ASSETS / "blockstates" / (bid + ".json"), {"variants": variants})
        item_parent = "c0de:block/" + bid + "_valid"
    else:
        model = {"parent": "minecraft:block/cube_all", "textures": {"all": "c0de:block/" + bid}}
        if transparent:
            # NeoForge reads the render type from the model; Fabric sets it in the client entry point
            model["render_type"] = "minecraft:cutout"
        write(ASSETS / "models" / "block" / (bid + ".json"), model)
        write(ASSETS / "blockstates" / (bid + ".json"), {"variants": {"": {"model": "c0de:block/" + bid}}})
        item_parent = "c0de:block/" + bid
    write(ASSETS / "models" / "item" / (bid + ".json"), {"parent": item_parent})
    write(DATA / "loot_table" / "blocks" / (bid + ".json"), {
        "type": "minecraft:block",
        "pools": [{
            "rolls": 1,
            "entries": [{"type": "minecraft:item", "name": "c0de:" + bid}],
            "conditions": [{"condition": "minecraft:survives_explosion"}],
        }],
    })


def main() -> None:
    for name, validated, transparent, _, _ in BLOCKS:
        block_files(name, validated, transparent)
    for trigger, _, _, _, _ in TRIGGERS:
        write(ASSETS / "models" / "block" / (trigger + ".json"), {
            "parent": "minecraft:block/cube_all",
            "textures": {"all": "c0de:block/" + trigger},
            "render_type": "minecraft:cutout",
        })
        write(ASSETS / "blockstates" / (trigger + ".json"), {"variants": {"": {"model": "c0de:block/" + trigger}}})
        write(ASSETS / "models" / "item" / (trigger + ".json"), {"parent": "c0de:block/" + trigger})
        write(DATA / "loot_table" / "blocks" / (trigger + ".json"), {
            "type": "minecraft:block",
            "pools": [{
                "rolls": 1,
                "entries": [{"type": "minecraft:item", "name": "c0de:" + trigger}],
                "conditions": [{"condition": "minecraft:survives_explosion"}],
            }],
        })
    write(RESOURCES / "data" / "minecraft" / "tags" / "block" / "mineable" / "pickaxe.json",
          {"values": ["c0de:" + trigger for trigger, _, _, _, _ in TRIGGERS]})
    write(ASSETS / "models" / "block" / "pid_block.json",
          {"parent": "minecraft:block/cube_all", "textures": {"all": "c0de:block/pid_block"}})
    write(ASSETS / "blockstates" / "pid_block.json", {"variants": {"": {"model": "c0de:block/pid_block"}}})

    # Items of the floor blocks, so that creative players can pick them to repair the floor
    for floor in FLOOR_BLOCKS:
        write(ASSETS / "models" / "item" / (floor + ".json"), {"parent": "c0de:block/" + floor})
    for item in [i for i, _, _ in ITEMS] + [type_item(t) for t, _ in TYPE_ITEMS]:
        write(ASSETS / "models" / "item" / (item + ".json"),
              {"parent": "minecraft:item/generated", "textures": {"layer0": "c0de:item/" + item}})
    for name, recipe in recipes().items():
        write(DATA / "recipe" / (name + ".json"), recipe)

    for language, column in (("en_us", 1), ("de_de", 2)):
        entries = {key: texts[column - 1] for key, *texts in MESSAGES}
        for name, _, _, english, german in BLOCKS:
            entries["block.c0de." + block_id(name)] = english if column == 1 else german
        for item, english, german in ITEMS:
            entries["item.c0de." + item] = english if column == 1 else german
        for trigger, _, _, english, german in TRIGGERS:
            entries["block.c0de." + trigger] = english if column == 1 else german
        for type_name, _ in TYPE_ITEMS:
            entries["item.c0de." + type_item(type_name)] = (
                type_name + (" Type Item" if column == 1 else "-Typkarte"))
        for operator, english, german in OPERATORS:
            entries["dialog.c0de.operator." + operator.lower()] = english if column == 1 else german
        write(ASSETS / "lang" / (language + ".json"), entries)


if __name__ == "__main__":
    main()
