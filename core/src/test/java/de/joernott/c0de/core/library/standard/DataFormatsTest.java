package de.joernott.c0de.core.library.standard;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import de.joernott.c0de.core.error.C0deException;
import de.joernott.c0de.core.error.ErrorCode;
import de.joernott.c0de.core.eval.Variable;
import de.joernott.c0de.core.library.Library;
import de.joernott.c0de.core.value.ArrayValue;
import de.joernott.c0de.core.value.BooleanValue;
import de.joernott.c0de.core.value.CharValue;
import de.joernott.c0de.core.value.FloatValue;
import de.joernott.c0de.core.value.HashValue;
import de.joernott.c0de.core.value.IntegerValue;
import de.joernott.c0de.core.value.StringValue;
import de.joernott.c0de.core.value.StructDefinition;
import de.joernott.c0de.core.value.StructValue;
import de.joernott.c0de.core.value.TimeValue;
import de.joernott.c0de.core.value.Type;
import de.joernott.c0de.core.value.Value;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

/** TO_JSON, FROM_JSON (task 5.5), TO_YAML and FROM_YAML (task 5.6). */
class DataFormatsTest {
    private static final Library STRINGS = new StringsLibrary();

    /** A struct with tags: playerName is "name" in JSON and YAML, secret is skipped in JSON. */
    private static final StructDefinition PLAYER = new StructDefinition(
            "player",
            List.of(
                    new StructDefinition.Element(
                            "playerName", Type.Str.UNLIMITED, Map.of("json", "name", "yaml", "name")),
                    new StructDefinition.Element("secret", Type.Str.UNLIMITED, Map.of("json", "-")),
                    new StructDefinition.Element("level", Type.Simple.INTEGER, Map.of()),
                    new StructDefinition.Element("joined", Type.Simple.TIME, Map.of())));

    private static StructValue player() {
        StructValue player = new StructValue(PLAYER);
        player.set("playerName", new StringValue("Steve"));
        player.set("secret", new StringValue("x"));
        player.set("level", new IntegerValue(3));
        player.set("joined", new TimeValue(1792411509250L));
        return player;
    }

    private static String toJson(Value value, Map<String, String> tags, int indent) {
        return ((StringValue) FakeCall.of(STRINGS, "TO_JSON")
                        .with("value", value)
                        .with("indent", new IntegerValue(indent))
                        .tags("value", tags)
                        .result())
                .value();
    }

    private static String toYaml(Value value, Map<String, String> tags) {
        return ((StringValue) FakeCall.of(STRINGS, "TO_YAML")
                        .with("value", value)
                        .tags("value", tags)
                        .result())
                .value();
    }

    private static void fromJson(String json, Variable destination) {
        FakeCall.of(STRINGS, "FROM_JSON")
                .with("source", new StringValue(json))
                .reference("destination", destination)
                .call();
    }

    private static void fromYaml(String yaml, Variable destination) {
        FakeCall.of(STRINGS, "FROM_YAML")
                .with("source", new StringValue(yaml))
                .reference("destination", destination)
                .call();
    }

    private static void assertCode(ErrorCode code, Runnable operation) {
        C0deException e = assertThrows(C0deException.class, operation::run);
        assertEquals(code, e.code(), e.getMessage());
    }

    @Test
    void structsUseTheirTags() {
        assertEquals(
                "{\"name\":\"Steve\",\"level\":3,\"joined\":\"2026-10-19T12:05:09.250Z\"}",
                toJson(player(), Map.of(), 0));
        assertEquals("""
                {
                  "name": "Steve",
                  "level": 3,
                  "joined": "2026-10-19T12:05:09.250Z"
                }""", toJson(player(), Map.of(), 2));
        assertEquals("""
                name: Steve
                secret: x
                level: 3
                joined: 2026-10-19T12:05:09.250Z
                """, toYaml(player(), Map.of()));
    }

    @Test
    void variableTagsCreateAMatchingStructure() {
        IntegerValue port = new IntegerValue(25565);
        assertEquals("{\"server\":{\"port\":25565}}", toJson(port, Map.of("json", "server.port"), 0));
        assertEquals("{\"servers\":[null,{\"port\":25565}]}", toJson(port, Map.of("json", "servers.1.port"), 0));
        assertEquals("server:\n  port: 25565\n", toYaml(port, Map.of("yaml", "server.port")));
        assertEquals("25565", toJson(port, Map.of("yaml", "server.port"), 0), "only the tag of the language is used");
    }

    @Test
    void valuesOfAllTypes() {
        ArrayValue list = new ArrayValue(Type.Simple.FLOAT, List.of(new FloatValue(1.5), new FloatValue(2)));
        HashValue hash = new HashValue(Type.Simple.BOOLEAN, Map.of("b", BooleanValue.FALSE, "a", BooleanValue.TRUE));
        assertEquals("[1.5,2.0]", toJson(list, Map.of(), 0));
        assertEquals("{\"a\":true,\"b\":false}", toJson(hash, Map.of(), 0));
        assertEquals("\"x\"", toJson(new CharValue('x'), Map.of(), 0));
        assertEquals("\"a\\\"b\\n\"", toJson(new StringValue("a\"b\n"), Map.of(), 0));
        assertCode(ErrorCode.INVALID_ARGUMENT, () -> toJson(new FloatValue(Double.NaN), Map.of(), 0));
        assertCode(ErrorCode.INVALID_ARGUMENT, () -> toJson(list, Map.of(), -1));
    }

    @Test
    void readingKeepsMissingElementsAndIgnoresUnknownKeys() {
        Variable variable = new Variable("p", new Type.Struct(PLAYER), player(), false, Map.of());
        fromJson("{\"name\": \"Alex\", \"secret\": \"ignored\", \"level\": null, \"unknown\": [1, 2]}", variable);
        StructValue p = (StructValue) variable.value();
        assertEquals(new StringValue("Alex"), p.get("playerName"));
        assertEquals(new StringValue("x"), p.get("secret"), "skipped in JSON");
        assertEquals(new IntegerValue(3), p.get("level"), "null keeps the value");
        fromYaml("name: Bob\nsecret: y\njoined: 2026-10-19T14:05:09+02:00\n", variable);
        p = (StructValue) variable.value();
        assertEquals(new StringValue("Bob"), p.get("playerName"));
        assertEquals(new StringValue("y"), p.get("secret"), "read in YAML");
        assertEquals(new TimeValue(1792411509000L), p.get("joined"));
    }

    @Test
    void readingWithAPath() {
        Variable port =
                new Variable("port", Type.Simple.INTEGER, new IntegerValue(1), false, Map.of("json", "server.port"));
        fromJson("{\"server\": {\"port\": 25565, \"motd\": \"Hello\"}}", port);
        assertEquals(new IntegerValue(25565), port.value());
        Variable second =
                new Variable("second", Type.Simple.INTEGER, new IntegerValue(1), false, Map.of("yaml", "servers.1"));
        fromYaml("servers: [10, 20, 30]", second);
        assertEquals(new IntegerValue(20), second.value());
        assertCode(ErrorCode.UNKNOWN_KEY, () -> fromJson("{\"server\": {}}", port));
        assertEquals(new IntegerValue(25565), port.value(), "unchanged after an error");
    }

    @Test
    void arraysAndHashesAreReplaced() {
        Variable list = new Variable(
                "list",
                new Type.Array(Type.Simple.INTEGER),
                new ArrayValue(Type.Simple.INTEGER, List.of(new IntegerValue(9))),
                false,
                Map.of());
        fromJson("[1, 2]", list);
        assertEquals(
                new ArrayValue(Type.Simple.INTEGER, List.of(new IntegerValue(1), new IntegerValue(2))), list.value());
        Variable hash = Variable.of("hash", new Type.Hash(Type.Simple.FLOAT));
        fromYaml("a: 1\nb: 2.5\n", hash);
        assertEquals(new FloatValue(1.0), ((HashValue) hash.value()).elements().get("a"));
    }

    @Test
    void readingErrorsLeaveTheVariableUnchanged() {
        Variable variable = new Variable("p", new Type.Struct(PLAYER), player(), false, Map.of());
        assertCode(ErrorCode.TYPE_MISMATCH, () -> fromJson("{\"name\": \"Alex\", \"level\": 1.5}", variable));
        assertEquals(new StringValue("Steve"), ((StructValue) variable.value()).get("playerName"));
        assertCode(ErrorCode.TYPE_MISMATCH, () -> fromJson("[1]", variable));
        assertCode(ErrorCode.INVALID_ARGUMENT, () -> fromJson("{\"name\": ", variable));
        Variable limited = Variable.of("limited", new Type.Str(2L));
        assertCode(ErrorCode.STRING_TOO_LONG, () -> fromJson("\"abc\"", limited));
        Variable c = Variable.of("c", Type.Simple.CHAR);
        assertCode(ErrorCode.TYPE_MISMATCH, () -> fromJson("\"ab\"", c));
        fromJson("\"b\"", c);
        assertEquals(new CharValue('b'), c.value());
    }

    @Test
    void roundTrip() {
        Variable copy = Variable.of("copy", new Type.Struct(PLAYER));
        fromYaml(toYaml(player(), Map.of()), copy);
        assertEquals(player(), copy.value());
    }
}
