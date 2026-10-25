"""Generates the data and asset JSON files of the language blocks for both loaders.

The list of blocks matches the enum CodeBlockType in the mod. The script writes block states, block and item
models, loot tables, recipes and the language files en_us and de_de. Generated files are committed; the CI
runs the script and fails if they differ from the committed files.

Usage: python scripts/generate_content.py
"""

import json
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
RESOURCES = ROOT / "src" / "main" / "resources"
ASSETS = RESOURCES / "assets" / "c0de"
DATA = RESOURCES / "data" / "c0de"

# The block models are in their own module next to this script
sys.path.insert(0, str(Path(__file__).resolve().parent))
import block_models  # noqa: E402
import generate_book  # noqa: E402

# The recipes in the format of Minecraft 1.21.1 and, converted from them, of 1.21.2 and later (src/main/versioned)
RECIPES = generate_book.RECIPES
MODERN_RECIPES = ROOT / "src" / "main" / "versioned" / "recipes" / "1.21.2" / "data" / "c0de" / "recipe"
# The models of the language blocks; since 1.21.11 they name the model loader of the extruded shapes on NeoForge
BLOCK_MODELS = ROOT / "src" / "main" / "versioned" / "block-models"

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
    ("literal_item", "Literal", "Literal"),
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
    r["literal_item"] = shapeless([c0de("data_item"), mc("light_gray_dye")], c0de("literal_item"), 2)
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
    ("message.c0de.aborted", "Process %2$s of %1$s aborted", "Prozess %2$s von %1$s abgebrochen"),
    ("message.c0de.no_process", "No process with PID %s is running", "Es läuft kein Prozess mit der PID %s"),
    ("message.c0de.not_owner", "Only the owner of its trigger block or an operator can abort process %s",
     "Nur der Besitzer seines Auslöserblocks oder ein Operator kann Prozess %s abbrechen"),
    ("message.c0de.cursor_line", "Line %s: ", "Zeile %s: "),
    ("message.c0de.inserted", "Empty line inserted in front of line %s", "Leere Zeile vor Zeile %s eingefügt"),
    ("message.c0de.no_last_mark", "Mark or unmark a line on this sheet with a left click first",
     "Markiere zuerst eine Zeile auf diesem Blatt mit einem Linksklick oder entferne ihre Markierung"),
    ("message.c0de.sheet_given", "Sheet %s now belongs to %s", "Blatt %s gehört jetzt %s"),
    ("dialog.c0de.sheet_owner", "Give sheet %s to", "Blatt %s weitergeben an"),
    ("dialog.c0de.sheet_owner.current", "Owner: %s", "Besitzer: %s"),
    ("sheet.c0de.info.sheet", "Sheet %s", "Blatt %s"),
    ("sheet.c0de.info.owner", "Owner: %s", "Besitzer: %s"),
    ("sheet.c0de.info.nobody", "no owner", "kein Besitzer"),
    ("message.c0de.sheet_owner", "This sheet belongs to %s; only they and administrators can change it",
     "Dieses Blatt gehört %s; nur diese Person und Administratoren können es ändern"),
    ("message.c0de.protected", "The program %s is running (PID %s), its sheet can't be changed",
     "Das Programm %s läuft (PID %s), sein Blatt kann nicht geändert werden"),
    ("message.c0de.exported", "Program %s saved to the disk", "Programm %s auf der Diskette gespeichert"),
    ("message.c0de.imported", "Program %s loaded from the disk", "Programm %s von der Diskette geladen"),
    ("block.c0de.display_block", "Display Block", "Anzeigeblock"),
    ("block.c0de.io_block", "I/O Block", "E/A-Block"),
    ("dialog.c0de.io.unbound", "Not bound", "Nicht gebunden"),
    ("block.c0de.disk_drive", "Disk Drive", "Diskettenlaufwerk"),
    ("dialog.c0de.disk_drive.save", "Save", "Speichern"),
    ("dialog.c0de.disk_drive.load", "Load", "Laden"),
    ("dialog.c0de.disk_drive.saved", "Saved as %s", "Gespeichert als %s"),
    ("dialog.c0de.disk_drive.no_files", "The folder c0de has no programs", "Der Ordner c0de enthält keine Programme"),
    ("dialog.c0de.disk_drive.choose", "Choose a program", "Wähle ein Programm"),
    ("dialog.c0de.disk_drive.loading", "Loading %s onto the disk", "Lade %s auf die Diskette"),
    ("dialog.c0de.disk_drive.no_disk", "Put a c0de disk into the slot", "Lege eine c0de-Diskette in den Schacht"),
    ("item.c0de.c0de_disk.empty", "Empty", "Leer"),
    ("item.c0de.c0de_disk.program", "Program: %s", "Programm: %s"),
    ("message.c0de.disk_loaded", "The program is on the disk", "Das Programm ist auf der Diskette"),
    ("block.c0de.laptop", "Laptop", "Laptop"),
    ("item.c0de.tablet", "Tablet", "Tablet"),
    ("dialog.c0de.terminal", "Terminal", "Terminal"),
    ("dialog.c0de.terminal.keys", "Typed keys go to INKEY", "Getippte Tasten gehen an INKEY"),
    ("dialog.c0de.terminal.input", "INPUT %s:", "INPUT %s:"),
    ("item.c0de.literal_item.valued", "Literal %s", "Literal %s"),
    ("dialog.c0de.trigger.input", "Input", "Eingabe"),
    ("dialog.c0de.trigger.output", "Output", "Ausgabe"),
    ("dialog.c0de.trigger.program", "Program", "Programm"),
    ("dialog.c0de.trigger.function", "Function", "Funktion"),
    ("dialog.c0de.trigger.interval", "Interval in ticks", "Intervall in Ticks"),
    ("dialog.c0de.trigger.run", "Run", "Starten durch"),
    ("dialog.c0de.trigger.run.owner", "Only me", "Nur mich"),
    ("dialog.c0de.trigger.run.players", "Players", "Spieler"),
    ("dialog.c0de.trigger.run.mobs", "Mobs", "Mobs"),
    ("dialog.c0de.trigger.run.everyone", "Everyone", "Alle"),
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


def block_files(name: str, validated: bool, transparent: bool) -> None:
    bid = block_id(name)
    model_ref = "c0de:block/" + bid
    if name == "OPERATOR":
        variants = {}
        for symbol, model in block_models.operator_models().items():
            write(ASSETS / "models" / "block" / (bid + "_" + symbol + ".json"), model)
            variants["operator=" + symbol] = {"model": model_ref + "_" + symbol}
        write(ASSETS / "blockstates" / (bid + ".json"), {"variants": variants})
        # The item shows the question mark of a placed OPERATOR without symbol
        item_parent = model_ref + "_none"
    else:
        model = block_models.block_model(name, bid, transparent)
        write(BLOCK_MODELS / "1.21.1" / "assets" / "c0de" / "models" / "block" / (bid + ".json"), model)
        write(BLOCK_MODELS / "1.21.11" / "assets" / "c0de" / "models" / "block" / (bid + ".json"),
              {"loader": "c0de:extruded", "c0de:block": bid, **model,
               "textures": {k: block_atlas_name(v) for k, v in model.get("textures", {}).items()}})
        # The state of the line doesn't change the look of a block any more; the line numbers show it
        keys = ("valid=true", "valid=false") if validated else ("",)
        write(ASSETS / "blockstates" / (bid + ".json"), {"variants": {key: {"model": model_ref} for key in keys}})
        item_parent = model_ref
    # The symbols on the top are readable from the south; the inventory shows blocks from the north, so the items
    # are turned by 180 degrees
    # The steep slashes of COMMENT lean the wrong way from the usual diagonal; at 165 degrees they read as // (11.11.10)
    gui_yaw = 165 if name == "COMMENT" else 45
    write(ASSETS / "models" / "item" / (bid + ".json"), {"parent": item_parent, "display": {
        "gui": {"rotation": [30, gui_yaw, 0], "translation": [0, 0, 0], "scale": [0.625, 0.625, 0.625]}}})
    write(DATA / "loot_table" / "blocks" / (bid + ".json"), {
        "type": "minecraft:block",
        "pools": [{
            "rolls": 1,
            "entries": [{"type": "minecraft:item", "name": "c0de:" + bid}],
            "conditions": [{"condition": "minecraft:survives_explosion"}],
        }],
    })


# The devices of input and output (docs/concept/19-input-output.md)
DEVICES = ["display_block", "io_block", "laptop", "disk_drive"]

# The rotations of a model whose front faces north, for the six directions of a device
FACINGS = {"north": {}, "south": {"y": 180}, "west": {"y": 270}, "east": {"y": 90}, "up": {"x": 270}, "down": {"x": 90}}


def devices() -> None:
    """The display block: a dark front and iron sides, or only the front if it is camouflaged."""
    iron = "minecraft:block/iron_block"
    front = "c0de:block/display_front"
    write(ASSETS / "models" / "block" / "display_block.json", {
        "parent": "minecraft:block/cube",
        "textures": {"north": front, "south": iron, "east": iron, "west": iron, "up": iron, "down": iron,
                     "particle": iron},
    })
    # A camouflaged display: the renderer draws the camouflage block, the front lies just in front of it
    write(ASSETS / "models" / "block" / "display_block_front.json", {
        "parent": "minecraft:block/block",
        "textures": {"front": front, "particle": front},
        "elements": [{"from": [0, 0, -0.02], "to": [16, 16, 0], "faces": {"north": {"texture": "#front"}}}],
    })
    variants = {}
    for facing, rotation in FACINGS.items():
        for camouflaged in ("false", "true"):
            model = "c0de:block/display_block" + ("_front" if camouflaged == "true" else "")
            variants["camouflaged=" + camouflaged + ",facing=" + facing] = dict({"model": model}, **rotation)
    write(ASSETS / "blockstates" / "display_block.json", {"variants": variants})
    write(ASSETS / "models" / "item" / "display_block.json", {"parent": "c0de:block/display_block"})
    write(DATA / "loot_table" / "blocks" / "display_block.json", {
        "type": "minecraft:block",
        "pools": [{"rolls": 1, "entries": [{"type": "minecraft:item", "name": "c0de:display_block"}],
                   "conditions": [{"condition": "minecraft:survives_explosion"}]}],
    })
    write(RECIPES / "display_block.json", shaped(
        ["NNN", "RGN", "NNN"],
        {"N": mc("iron_nugget"), "R": mc("redstone"), "G": mc("glowstone_dust")},
        c0de("display_block"), 8))
    # The I/O block: the model of the hopper with blue textures
    write(ASSETS / "models" / "block" / "io_block.json", {
        "parent": "minecraft:block/hopper",
        "textures": {"particle": "c0de:block/io_block_outside", "top": "c0de:block/io_block_top",
                     "side": "c0de:block/io_block_outside", "inside": "c0de:block/io_block_inside"},
    })
    write(ASSETS / "blockstates" / "io_block.json", {"variants": {"": {"model": "c0de:block/io_block"}}})
    write(ASSETS / "models" / "item" / "io_block.json", {"parent": "c0de:block/io_block"})
    write(DATA / "loot_table" / "blocks" / "io_block.json", {
        "type": "minecraft:block",
        "pools": [{"rolls": 1, "entries": [{"type": "minecraft:item", "name": "c0de:io_block"}],
                   "conditions": [{"condition": "minecraft:survives_explosion"}]}],
    })
    write(RECIPES / "io_block.json", shapeless([c0de("data_item"), mc("hopper")], c0de("io_block")))
    # The laptop, its screen facing north: the keyboard in front, the lid at the back
    body = "c0de:block/laptop_body"
    write(ASSETS / "models" / "block" / "laptop.json", {
        "parent": "minecraft:block/block",
        "textures": {"body": body, "keyboard": "c0de:block/laptop_keyboard", "screen": "c0de:block/display_front",
                     "particle": body},
        "elements": [
            {"from": [1, 0, 1], "to": [15, 1, 13], "faces": {
                "up": {"uv": [1, 1, 15, 13], "texture": "#keyboard"},
                "down": {"uv": [1, 3, 15, 15], "texture": "#body", "cullface": "down"},
                **{side: {"uv": [1, 15, 15, 16], "texture": "#body"} for side in ("north", "south")},
                **{side: {"uv": [1, 15, 13, 16], "texture": "#body"} for side in ("east", "west")}}},
            {"from": [1, 1, 13], "to": [15, 11, 14], "faces": {
                "north": {"uv": [1, 5, 15, 15], "texture": "#body"},
                "south": {"uv": [1, 5, 15, 15], "texture": "#body"},
                "up": {"uv": [1, 13, 15, 14], "texture": "#body"},
                "east": {"uv": [2, 5, 3, 15], "texture": "#body"},
                "west": {"uv": [13, 5, 14, 15], "texture": "#body"}}},
            {"from": [2, 2, 12.95], "to": [14, 10, 13], "faces": {
                "north": {"uv": [2, 6, 14, 14], "texture": "#screen"}}},
        ],
    })
    write(ASSETS / "blockstates" / "laptop.json", {"variants": {
        "facing=" + facing: dict({"model": "c0de:block/laptop"}, **({"y": y} if y else {}))
        for facing, y in (("north", 0), ("east", 90), ("south", 180), ("west", 270))}})
    write(ASSETS / "models" / "item" / "laptop.json", {"parent": "c0de:block/laptop"})
    write(DATA / "loot_table" / "blocks" / "laptop.json", {
        "type": "minecraft:block",
        "pools": [{"rolls": 1, "entries": [{"type": "minecraft:item", "name": "c0de:laptop"}],
                   "conditions": [{"condition": "minecraft:survives_explosion"}]}],
    })
    write(RECIPES / "laptop.json",
          shapeless([c0de("display_block"), c0de("io_block")], c0de("laptop")))
    # A laptop folds into a tablet and back
    write(RECIPES / "laptop_from_tablet.json", shapeless([c0de("tablet")], c0de("laptop")))
    write(RECIPES / "tablet.json", shapeless([c0de("laptop")], c0de("tablet")))
    write(ASSETS / "models" / "item" / "tablet.json",
          {"parent": "minecraft:item/generated", "textures": {"layer0": "c0de:item/tablet"}})
    # The disk drive: a 3.5" floppy drive in a beige case, its front facing north
    write(ASSETS / "models" / "block" / "disk_drive.json", {
        "parent": "minecraft:block/orientable",
        "textures": {"front": "c0de:block/disk_drive_front", "side": "c0de:block/disk_drive_side",
                     "top": "c0de:block/disk_drive_side", "particle": "c0de:block/disk_drive_side"},
    })
    write(ASSETS / "blockstates" / "disk_drive.json", {"variants": {
        "facing=" + facing: dict({"model": "c0de:block/disk_drive"}, **({"y": y} if y else {}))
        for facing, y in (("north", 0), ("east", 90), ("south", 180), ("west", 270))}})
    write(ASSETS / "models" / "item" / "disk_drive.json", {"parent": "c0de:block/disk_drive"})
    write(DATA / "loot_table" / "blocks" / "disk_drive.json", {
        "type": "minecraft:block",
        "pools": [{"rolls": 1, "entries": [{"type": "minecraft:item", "name": "c0de:disk_drive"}],
                   "conditions": [{"condition": "minecraft:survives_explosion"}]}],
    })
    write(RECIPES / "disk_drive.json", shaped(
        ["III", "SRS", "III"],
        {"I": mc("iron_ingot"), "S": mc("smooth_stone_slab"), "R": mc("redstone")},
        c0de("disk_drive"), 1))
    write(ASSETS / "font" / "monocraft.json", {
        "providers": [{"type": "ttf", "file": "c0de:monocraft.ttf", "shift": [0, 0], "size": 9, "oversample": 4.0}],
    })


# Since 1.21.11, items have an atlas of their own, which wins for textures that are in both atlases, and block models
# may only use the blocks atlas. The models of some language blocks show items of Minecraft, so their textures are
# added to the blocks atlas under names of the mod.
def block_atlas_name(texture: str) -> str:
    """The name in the blocks atlas of a texture of Minecraft (minecraft:item/book -> c0de:block/item/book)."""
    return "c0de:block/" + texture.split(":", 1)[1] if texture.startswith("minecraft:item/") else texture


def item_textures_in_block_atlas() -> None:
    textures = set()
    for model in (BLOCK_MODELS / "1.21.1").rglob("*.json"):
        textures |= {t for t in json.loads(model.read_text(encoding="utf-8")).get("textures", {}).values()
                     if t.startswith("minecraft:item/")}
    write(ROOT / "src" / "main" / "versioned" / "atlases" / "1.21.11" / "assets" / "minecraft" / "atlases" / "blocks.json",
          {"sources": [{"type": "minecraft:single", "resource": t, "sprite": block_atlas_name(t)}
                       for t in sorted(textures)]})


def modern_ingredient(value):
    """An ingredient in the format of 1.21.2 and later: the ID of an item, #tag, or a list of IDs."""
    if isinstance(value, list):
        return [modern_ingredient(v) for v in value]
    if "item" in value:
        return value["item"]
    return "#" + value["tag"]


def modern_recipes() -> None:
    """Converts all recipes (also the hand-written ones) to the format of Minecraft 1.21.2 and later."""
    if MODERN_RECIPES.exists():
        for old in MODERN_RECIPES.glob("*.json"):
            old.unlink()
    for file in sorted(RECIPES.glob("*.json")):
        recipe = json.loads(file.read_text(encoding="utf-8"))
        if "key" in recipe:
            recipe["key"] = {k: modern_ingredient(v) for k, v in recipe["key"].items()}
        if "ingredients" in recipe:
            recipe["ingredients"] = [modern_ingredient(v) for v in recipe["ingredients"]]
        write(MODERN_RECIPES / file.name, recipe)


def item_definitions() -> None:
    """The client item definitions of Minecraft 1.21.4 and later (assets/c0de/items), one for every item model;
    the older versions ignore them."""
    for model in sorted((ASSETS / "models" / "item").glob("*.json")):
        definition = {"type": "minecraft:model", "model": "c0de:item/" + model.stem}
        if model.stem == "marker":
            # The yellow of the text marker (C0deClient.MARKER_COLOR in the older versions)
            definition["tints"] = [{"type": "minecraft:constant", "value": 0xFFD83A}]
        write(ASSETS / "items" / model.name, {"model": definition})


def main() -> None:
    generate_book.main()
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
    item_textures_in_block_atlas()
    devices()
    write(RESOURCES / "data" / "minecraft" / "tags" / "block" / "mineable" / "pickaxe.json",
          {"values": ["c0de:" + trigger for trigger, _, _, _, _ in TRIGGERS] + ["c0de:" + d for d in DEVICES]})
    write(ASSETS / "models" / "block" / "pid_block.json",
          {"parent": "minecraft:block/cube_all", "textures": {"all": "c0de:block/pid_block"}})
    write(ASSETS / "blockstates" / "pid_block.json", {"variants": {"": {"model": "c0de:block/pid_block"}}})

    # Items of the floor blocks, so that creative players can pick them to repair the floor
    for floor in FLOOR_BLOCKS:
        write(ASSETS / "models" / "item" / (floor + ".json"), {"parent": "c0de:block/" + floor})
    for item in [i for i, _, _ in ITEMS] + [type_item(t) for t, _ in TYPE_ITEMS]:
        # The text marker is the feather of Minecraft, colored yellow by the item color of the mod
        texture = "minecraft:item/feather" if item == "marker" else "c0de:item/" + item
        write(ASSETS / "models" / "item" / (item + ".json"),
              {"parent": "minecraft:item/generated", "textures": {"layer0": texture}})
    item_definitions()
    for name, recipe in recipes().items():
        write(RECIPES / (name + ".json"), recipe)
    modern_recipes()

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
