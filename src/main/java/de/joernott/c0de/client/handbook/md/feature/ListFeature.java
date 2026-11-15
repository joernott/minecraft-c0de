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
package de.joernott.c0de.client.handbook.md.feature;

import de.joernott.c0de.client.handbook.md.Lexer;
import de.joernott.c0de.client.handbook.md.MarkdownFeature;
import de.joernott.c0de.client.handbook.md.Parser;
import de.joernott.c0de.client.handbook.md.compiler.MarkdownCompiler;
import de.joernott.c0de.client.handbook.md.util.StringNibbler;

import java.util.OptionalInt;

public class ListFeature implements MarkdownFeature {
    @Override
    public String name() {
        return "lists";
    }

    @Override
    public boolean supportsCompiler(MarkdownCompiler<?> compiler) {
        return true;
    }

    @Override
    public void registerTokens(TokenRegistrar registrar) {
        // unordered
        registrar.registerToken((nibbler, tokens) -> {
            int whitespace = whitespaceSinceLineBreak(nibbler);
            if (whitespace < 0) return false;

            nibbler.skip();
            if (!nibbler.tryConsume(' ')) return false;

            tokens.add(new ListToken(whitespace, OptionalInt.empty()));
            return true;
        }, '-');

        // ordered
        registrar.registerToken((nibbler, tokens) -> {
            int whitespace = whitespaceSinceLineBreak(nibbler);
            if (whitespace < 0) return false;

            var ordinal = nibbler.consumeUntilEndOr(c -> c < '0' || c > '9');
            if (!ordinal.matches("[0-9]+") || !nibbler.tryConsume(". ")) {
                return false;
            }

            tokens.add(new ListToken(whitespace, OptionalInt.of(Integer.parseInt(ordinal))));
            return true;
        }, '0', '1', '2', '3', '4', '5', '6', '7', '8', '9');
    }

    @Override
    public void registerNodes(NodeRegistrar registrar) {
        registrar.registerNode(
                (parser, current, tokens) -> new ListNode(current.ordinal).addChild(parser.parseUntil(tokens, $ -> $.isBoundary() && !($ instanceof ListToken list && list.depth > current.depth), $ -> false)),
                (token, tokens) -> token instanceof ListToken list ? list : null
        );
    }

    private static int whitespaceSinceLineBreak(StringNibbler nibbler) {
        int offset = 1;
        int whitespace = 0;

        while (nibbler.cursor() - offset >= 0) {
            //noinspection DataFlowIssue
            char current = nibbler.peekOffset(-offset);
            if (current == '\n') return whitespace;

            if (Character.isWhitespace(current)) {
                whitespace++;
            } else {
                return -1;
            }

            offset++;
        }

        return whitespace;
    }

    // --- token ---

    private static final class ListToken extends Lexer.Token {

        public final int depth;
        public final OptionalInt ordinal;

        public ListToken(int depth, OptionalInt ordinal) {
            super("- ");
            this.depth = depth;
            this.ordinal = ordinal;
        }

        @Override
        public boolean isBoundary() {
            return true;
        }
    }

    // --- node ---

    private static class ListNode extends Parser.Node {

        private final OptionalInt ordinal;

        public ListNode(OptionalInt ordinal) {
            this.ordinal = ordinal;
        }

        @Override
        protected void visitStart(MarkdownCompiler<?> compiler) {
            compiler.visitListItem(this.ordinal);
        }

        @Override
        protected void visitEnd(MarkdownCompiler<?> compiler) {
            compiler.visitListItemEnd();
        }
    }
}
