/*
 * Taken from lavender-md (https://github.com/wisp-forest/lavender-md, branch 26.1) for the handbooks of c0de
 * (docs/concept/21-handbooks.md).
 *
 * The MIT License (MIT)
 *
 * Copyright (c) 2023
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of
 * the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package de.joernott.c0de.client.handbook.md.util;

import net.minecraft.CharPredicate;
import org.jetbrains.annotations.Nullable;

import java.util.function.Predicate;

/**
 * String-analog of {@link ListNibbler} with many added functions
 * for testing for and consuming individual characters or substrings
 * <p>
 * Very useful for lexing strings, essentially a more advanced version of
 * Brigadier's {@link com.mojang.brigadier.StringReader}
 */
public class StringNibbler {

    public final String string;
    private int cursor;

    public StringNibbler(String string) {
        this.string = string;
    }

    public int cursor() {
        return this.cursor;
    }

    /**
     * @return The character at this nibbler's cursor
     * in the underlying string
     */
    public char peek() {
        return this.string.charAt(this.cursor);
    }

    /**
     * Shorthand of {@link #skip(int)} with {@link 1} for {@code count}
     */
    public void skip() {
        this.skip(1);
    }

    /**
     * Skip {@code count} characters forward in this nibbler's
     * underlying string
     */
    public void skip(int count) {
        this.cursor += count;
    }

    /**
     * Consume the character at this nibbler's cursor
     * in the underlying string (or {@code null} if there are
     * no more characters to consume
     */
    public @Nullable Character next() {
        return this.cursor < this.string.length() ? this.string.charAt(this.cursor++) : null;
    }

    /**
     * @return The substring of this nibbler's underlying string
     * which has already been consumed
     */
    public String consumed() {
        return this.string.substring(0, this.cursor);
    }

    /**
     * @return {@code true} if this nibbler has more characters to consume
     */
    public boolean hasNext() {
        return this.cursor < this.string.length();
    }

    /**
     * @return The character at this nibbler's cursor + {@code offset}
     * in the underlying string, or {@code null} if that index is out of range
     */
    public @Nullable Character peekOffset(int offset) {
        int charIndex = this.cursor + offset;
        return charIndex >= 0 && charIndex < this.string.length() ? this.string.charAt(charIndex) : null;
    }

    /**
     * Consume a substring until {@code terminator}, skipping terminators escaped
     * by a backslash ({@code \}). If no {@code terminator} is found, return {@code null} if
     * {@code allowUnterminated} is false, the consumed substring otherwise
     */
    public @Nullable String consumeEscapedString(char terminator, boolean allowUnterminated) {
        var read = new StringBuilder();
        while (this.hasNext()) {
            char next = this.next();
            if (next == '\\' && this.expect(0, terminator)) {
                this.skip();
                read.append(terminator);
            } else if (next == terminator) {
                return read.toString();
            } else {
                read.append(next);
            }
        }

        return allowUnterminated ? read.toString() : null;
    }

    /**
     * Shorthand of {@link #consumeUntil(char, boolean)} with {@code true} for {@code skipDelimiter}
     */
    public @Nullable String consumeUntil(char delimiter) {
        return this.consumeUntil(delimiter, true);
    }

    /**
     * Consume a substring until {@code delimiter}. If no {@code delimiter}
     * is encountered, return {@code null}. If {@code skipDelimiter} is true, place
     * the cursor on the character immediately following the delimiter
     */
    public @Nullable String consumeUntil(char delimiter, boolean skipDelimiter) {
        var read = new StringBuilder();
        if (this.tryMatch($ -> {
            while (this.hasNext()) {
                if (this.peek() == delimiter) {
                    if (skipDelimiter) this.skip();
                    return true;
                }

                read.append(this.next());
            }

            return false;
        })) {
            return read.toString();
        } else {
            return null;
        }
    }

    /**
     * Consume a substring up to but not including the first character
     * matched by {@code until}. If no such character is encountered, return
     * the entire rest of the underlying string
     */
    public String consumeUntilEndOr(CharPredicate until) {
        var read = new StringBuilder();
        while (this.hasNext() && !until.test(this.peek())) {
            read.append(this.next());
        }

        return read.toString();
    }

    /**
     * Return {@code true} if the character at this nibbler's
     * cursor + {@code offset} is equal to {@code expect}. Return {@code false}
     * if the character is not matched or that index is out of range
     */
    public boolean expect(int offset, char expect) {
        int charIndex = this.cursor + offset;
        return charIndex >= 0 && charIndex < this.string.length() && this.string.charAt(charIndex) == expect;
    }

    /**
     * Return {@code true} and advance the cursor if the character at
     * this nibbler's cursor if equal to {@code consume}
     */
    public boolean tryConsume(char consume) {
        return tryMatch($ -> {
            var next = next();
            return next != null && next == consume;
        });
    }

    /**
     * Return {@code true} and consume if the substring {@code consume}
     * is found to begin at this nibbler's cursor
     */
    public boolean tryConsume(String consume) {
        return this.tryMatch(stringNibbler -> {
            for (int i = 0; i < consume.length(); i++) {
                var next = this.next();
                if (next == null || next != consume.charAt(i)) return false;
            }

            return true;
        });
    }

    /**
     * Return {@code true} if {@code matcher} returns {@code true} when invoked
     * on this nibbler instance. Otherwise, return {@code false} and revert
     * the nibbler's cursor to its position before calling {@code matcher}
     */
    public boolean tryMatch(Predicate<StringNibbler> matcher) {
        int cursorPos = this.cursor;
        if (!matcher.test(this)) {
            this.cursor = cursorPos;
            return false;
        } else {
            return true;
        }
    }
}
