package de.joernott.c0de.core.library.standard;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.joernott.c0de.core.error.C0deException;
import de.joernott.c0de.core.error.ErrorCode;
import de.joernott.c0de.core.library.Library;
import de.joernott.c0de.core.value.BooleanValue;
import de.joernott.c0de.core.value.StringValue;
import java.util.List;
import org.junit.jupiter.api.Test;

/** REGEX_MATCH and REGEX_REPLACE of c0de/strings (task 16.14). */
class RegexTest {
    private static final Library STRINGS = new StringsLibrary();

    private static boolean match(String text, String pattern) {
        return ((BooleanValue) FakeCall.of(STRINGS, "REGEX_MATCH")
                        .with("text", new StringValue(text))
                        .with("pattern", new StringValue(pattern))
                        .result())
                .value();
    }

    private static FakeCall replaceCall(String text, String pattern, String replacement, String flags) {
        FakeCall call = FakeCall.of(STRINGS, "REGEX_REPLACE")
                .with("text", new StringValue(text))
                .with("pattern", new StringValue(pattern))
                .with("replacement", new StringValue(replacement));
        return flags == null ? call : call.with("flags", new StringValue(flags));
    }

    private static String replace(String text, String pattern, String replacement, String flags) {
        return ((StringValue) replaceCall(text, pattern, replacement, flags).result()).value();
    }

    private static C0deException error(Runnable operation) {
        return assertThrows(C0deException.class, operation::run);
    }

    @Test
    void matchesAPartOfTheText() {
        assertTrue(match("Order 4711 shipped", "\\d+"));
        assertEquals(false, match("Order 4711 shipped", "^\\d+$"));
        assertTrue(match("2026-11-15", "^[0-9]{4}-[0-9]{2}-[0-9]{2}$"));
        assertTrue(match("HELLO", "(?i)hello"));
        // PCRE features of Java: lookahead, named groups and back references, possessive quantifiers
        assertTrue(match("abcabc", "(?<word>abc)\\k<word>"));
        assertTrue(match("password1", "^(?=.*\\d)[a-z0-9]+$"));
        assertEquals(false, match("aaa", "a++a"));
        assertTrue(match("Grüße", "^\\p{L}+$"));
    }

    @Test
    void invalidPatternsAreInvalidArguments() {
        C0deException e = error(() -> match("text", "(unclosed"));
        assertEquals(ErrorCode.INVALID_ARGUMENT, e.code());
        assertTrue(e.getMessage().contains("position"), e.getMessage());
    }

    @Test
    void replacesTheFirstMatchLikeSed() {
        assertEquals("b-a-a", replace("a-a-a", "a", "b", null));
        assertEquals("b-b-b", replace("a-a-a", "a", "b", "g"));
        assertEquals("a-b-a", replace("a-a-a", "a", "b", "2"));
        assertEquals("a-b-b", replace("a-a-a", "a", "b", "2g"));
        assertEquals("a-b-b", replace("a-a-a", "a", "b", "g2"));
        assertEquals("a-a-a", replace("a-a-a", "a", "b", "4"));
        assertEquals("x-x", replace("A-a", "a", "x", "gi"));
        assertEquals("#1\n#2", replace("1\n2", "^", "#", "gm"));
    }

    @Test
    void replacementsUseGroupsAndTheWholeMatch() {
        assertEquals("15.11.2026", replace("2026-11-15", "([0-9]+)-([0-9]+)-([0-9]+)", "\\3.\\2.\\1", null));
        assertEquals("[cat] [dog]", replace("cat dog", "\\w+", "[&]", "g"));
        assertEquals("a&b \\", replace("a+b", "\\+", "\\&", null) + " " + replace("x", "x", "\\\\", null));
        assertEquals(
                "line\nnext\ttab", replace("line next tab", " ", "\\n", null).replace(" ", "\t"));
        // A group that didn't take part inserts nothing
        assertEquals("<>b", replace("b", "(a)?", "<\\1>", null));
        C0deException e = error(() -> replace("abc", "(a)", "\\2", null));
        assertEquals(ErrorCode.INVALID_ARGUMENT, e.code());
    }

    @Test
    void emptyMatchesMoveOn() {
        assertEquals("-a-b-", replace("ab", "", "-", "g"));
        assertEquals("ab", replace("ab", "x*", "", "g"));
    }

    @Test
    void theFlagPPrintsAChangedResult() {
        FakeCall changed = replaceCall("cat", "c", "b", "p");
        assertEquals(new StringValue("bat"), changed.result());
        assertEquals(List.of("bat"), changed.printed);
        FakeCall unchanged = replaceCall("cat", "x", "b", "p");
        unchanged.result();
        assertEquals(List.of(), unchanged.printed);
    }

    @Test
    void invalidFlags() {
        for (String flags : new String[] {"x", "0", "1g2", "99999999999999999999"}) {
            C0deException e = error(() -> replace("a", "a", "b", flags));
            assertEquals(ErrorCode.INVALID_ARGUMENT, e.code(), flags);
        }
    }

    @Test
    void catastrophicBacktrackingIsStopped() {
        String text = "a".repeat(40) + "b";
        C0deException e = error(() -> match(text, "^(.*a){20}$"));
        assertEquals(ErrorCode.INVALID_ARGUMENT, e.code());
        assertTrue(e.getMessage().contains("too long"), e.getMessage());
        C0deException replace = error(() -> replace(text, "^(.*a){20}$", "", "g"));
        assertEquals(ErrorCode.INVALID_ARGUMENT, replace.code());
    }

    @Test
    void resultsAreLimited() {
        String text = "a".repeat(1000);
        C0deException e = error(() -> replace(text, "a", "x".repeat(2000), "g"));
        assertEquals(ErrorCode.STRING_TOO_LONG, e.code());
    }
}
