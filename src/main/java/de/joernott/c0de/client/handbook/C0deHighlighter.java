package de.joernott.c0de.client.handbook;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;

/**
 * Colours the lines of c0de programs in the handbooks like the website c0de.at does (its file
 * site/.vitepress/c0de-highlighting.ts): the keywords in the colours of their blocks in the game, in the variants
 * for light paper. The first rule that matches at a position wins.
 */
public final class C0deHighlighter {
    /** The colour of text no rule matches. */
    private static final int FOREGROUND = 0x1D232A;

    private record Rule(Pattern pattern, Style style) {}

    private static final List<Rule> RULES = List.of(
            rule("//.*$", 0x1E7B1E, false, true),
            rule("\\bCOMMENT\\(\"(?:[^\"\\\\]|\\\\.)*\"\\)", 0x1E7B1E, false, true),
            rule("\"(?:[^\"\\\\]|\\\\.)*\"", 0x5C6670, false, false),
            rule("'(?:[^'\\\\]|\\\\.)'", 0x5C6670, false, false),
            rule("(?<![\\w/])\\d+(?:\\.\\d+)?(?![\\w])", 0x5C6670, false, false),
            rule(words("TRUE FALSE"), 0x5C6670, false, false),
            rule(words("NOOP"), 0x6B7178, false, false),
            rule(words("PROGRAM FUNCTION CALL"), 0x4D4D4D, true, false),
            rule(words("INCLUDE EXIT"), 0x111111, true, false),
            rule(words("BEGIN END BREAK"), 0x8A5A00, true, false),
            rule(words("IF ELSE FOR EACH WHILE REPEAT"), 0x1E7B1E, true, false),
            rule(words("PARAMETER RESULT RETURN"), 0x8F1F8F, true, false),
            rule(words("ASSIGN"), 0x00737A, true, false),
            rule(words("VARIABLE CONSTANT"), 0x1D4FBF, true, false),
            rule(words("TAG"), 0x6B3F16, true, false),
            rule(words("BOOLEAN INTEGER FLOAT CHAR STRING ARRAY HASH STRUCT TIME ERROR"), 0xC62828, false, false),
            rule(words("NOT AND OR XOR") + "|[;()+\\-*/\\\\^=≠<>≤≥⊕⊻&|¬←→]", 0x8B0000, false, false),
            rule("[A-Za-z][\\w/]*(?:\\[[^\\]]*\\]|\\.[A-Za-z]\\w*)*", 0x5C6670, false, false));

    private C0deHighlighter() {}

    private static String words(String list) {
        return "\\b(?:" + String.join("|", list.split(" ")) + ")\\b";
    }

    private static Rule rule(String regex, int color, boolean bold, boolean italic) {
        return new Rule(
                Pattern.compile(regex),
                Style.EMPTY.withColor(TextColor.fromRgb(color)).withBold(bold).withItalic(italic));
    }

    /** A line of a program with the colours of its keywords, names, values and comments. */
    public static MutableComponent line(String line) {
        MutableComponent result = Component.empty();
        Style plain = Style.EMPTY.withColor(TextColor.fromRgb(FOREGROUND));
        StringBuilder text = new StringBuilder();
        int position = 0;
        while (position < line.length()) {
            Rule found = null;
            int end = position;
            for (Rule rule : RULES) {
                Matcher matcher = rule.pattern().matcher(line);
                // Transparent bounds: \b and look-behinds see the characters before the position
                matcher.region(position, line.length()).useTransparentBounds(true).useAnchoringBounds(false);
                if (matcher.lookingAt() && matcher.end() > position) {
                    found = rule;
                    end = matcher.end();
                    break;
                }
            }
            if (found == null) {
                text.append(line.charAt(position));
                position++;
                continue;
            }
            if (!text.isEmpty()) {
                result.append(Component.literal(text.toString()).setStyle(plain));
                text.setLength(0);
            }
            result.append(Component.literal(line.substring(position, end)).setStyle(found.style()));
            position = end;
        }
        if (!text.isEmpty()) {
            result.append(Component.literal(text.toString()).setStyle(plain));
        }
        return result;
    }
}
