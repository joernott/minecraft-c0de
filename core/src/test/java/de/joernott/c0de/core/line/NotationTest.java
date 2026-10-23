package de.joernott.c0de.core.line;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.joernott.c0de.core.line.Block.CommentBlock;
import de.joernott.c0de.core.line.Block.KeywordBlock;
import de.joernott.c0de.core.line.Block.LabelBlock;
import de.joernott.c0de.core.line.Block.LiteralBlock;
import de.joernott.c0de.core.line.Block.OperatorBlock;
import de.joernott.c0de.core.line.Block.TagBlock;
import de.joernott.c0de.core.value.BooleanValue;
import de.joernott.c0de.core.value.CharValue;
import de.joernott.c0de.core.value.FloatValue;
import de.joernott.c0de.core.value.IntegerValue;
import de.joernott.c0de.core.value.StringValue;
import de.joernott.c0de.core.value.TimeValue;
import java.time.ZoneOffset;
import java.util.List;
import org.junit.jupiter.api.Test;

class NotationTest {
    private static List<Block> blocks(String row) {
        return Notation.parseLine(0, row, ZoneOffset.UTC).cells().stream()
                .map(Line.Cell::block)
                .toList();
    }

    @Test
    void indentationAndGaps() {
        Line line = Notation.parseLine(3, "        ASSIGN a _ 1 ;", ZoneOffset.UTC);
        assertEquals(3, line.z());
        assertEquals(
                List.of(7, 8, 10, 11), line.cells().stream().map(Line.Cell::x).toList());
    }

    @Test
    void tokens() {
        List<Block> blocks = blocks(
                "VARIABLE x STRING 10 \"a \\\"b\\\"\" 'c' -1 2.5 TRUE @2026-10-19T12:05:09.250Z ≠ <> NOT ( ; TAG(json: \"a.b\")");
        assertEquals(new KeywordBlock(Keyword.VARIABLE), blocks.get(0));
        assertEquals(new LabelBlock("x"), blocks.get(1));
        assertEquals(new KeywordBlock(Keyword.STRING), blocks.get(2));
        assertEquals(new LiteralBlock(new IntegerValue(10)), blocks.get(3));
        assertEquals(new LiteralBlock(new StringValue("a \"b\"")), blocks.get(4));
        assertEquals(new LiteralBlock(new CharValue('c')), blocks.get(5));
        assertEquals(new LiteralBlock(new IntegerValue(-1)), blocks.get(6));
        assertEquals(new LiteralBlock(new FloatValue(2.5)), blocks.get(7));
        assertEquals(new LiteralBlock(BooleanValue.TRUE), blocks.get(8));
        assertEquals(new LiteralBlock(new TimeValue(1792411509250L)), blocks.get(9));
        assertEquals(new OperatorBlock(Operator.NOT_EQUAL), blocks.get(10));
        assertEquals(new OperatorBlock(Operator.NOT_EQUAL), blocks.get(11));
        assertEquals(new OperatorBlock(Operator.NOT), blocks.get(12));
        assertEquals(new OperatorBlock(Operator.OPEN), blocks.get(13));
        assertEquals(new OperatorBlock(Operator.END_OF_EXPRESSION), blocks.get(14));
        assertEquals(new TagBlock("json", "a.b"), blocks.get(15));
    }

    @Test
    void labelsWithIndex() {
        LabelBlock label =
                assertInstanceOf(LabelBlock.class, blocks("m[i][2][\"a b\"].x").get(0));
        assertEquals("m", label.name());
        assertEquals(
                List.of(
                        new IndexEntry.Variable("i"),
                        new IndexEntry.Literal(new IntegerValue(2)),
                        new IndexEntry.Literal(new StringValue("a b")),
                        new IndexEntry.Element("x")),
                label.index());
        assertEquals(
                "c0de/strings/MID", ((LabelBlock) blocks("c0de/strings/MID").get(0)).name());
    }

    @Test
    void uninitializedBlocksAndComments() {
        assertEquals(
                List.of(
                        new LabelBlock(null),
                        new LiteralBlock(null),
                        new OperatorBlock(null),
                        new CommentBlock("rest of line")),
                blocks("LABEL LITERAL OPERATOR // rest of line"));
        assertEquals(List.of(new CommentBlock("x"), new LiteralBlock(new IntegerValue(1))), blocks("COMMENT(\"x\") 1"));
        // Parentheses and ; are separate blocks even without spaces
        assertEquals(4, blocks("(a);").size());
    }

    @Test
    void sheetsEndWithTheFirstEmptyLineAndRoundTrip() {
        String text = """
                PROGRAM demo
                NOOP
                INCLUDE c0de/strings
                VARIABLE s STRING 3 "a\\"b" TAG(json: "x.y")

                VARIABLE c CHAR 'q' COMMENT("disabled") _ 1
                """;
        Sheet sheet = Notation.parse(text);
        assertEquals(6, sheet.size());
        assertTrue(sheet.line(4).isEmpty());
        assertTrue(sheet.line(99).isEmpty());
        assertEquals(sheet, Notation.parse(Notation.format(sheet)));
    }
}
