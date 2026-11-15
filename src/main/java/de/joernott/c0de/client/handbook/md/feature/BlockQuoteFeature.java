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

public class BlockQuoteFeature implements MarkdownFeature {
    @Override
    public String name() {
        return "block_quotes";
    }

    @Override
    public boolean supportsCompiler(MarkdownCompiler<?> compiler) {
        return true;
    }

    @Override
    public void registerTokens(TokenRegistrar registrar) {
        registrar.registerToken((nibbler, tokens) -> {
            var brackets = nibbler.consumeUntilEndOr(c -> c != '>');
            if (!nibbler.tryConsume(' ')) return false;

            tokens.add(new QuotationToken(brackets.length()));
            return true;
        }, '>');
    }

    @Override
    public void registerNodes(NodeRegistrar registrar) {
        registrar.registerNode(
                (parser, current, tokens) -> new QuotationNode().addChild(parser.parseUntil(tokens, $ -> $.isBoundary() && (!($ instanceof QuotationToken) || ((QuotationToken) $).depth < current.depth), $ -> $ instanceof QuotationToken quote && quote.depth == current.depth)),
                (token, tokens) -> token instanceof QuotationToken current && (tokens.peek(-2) == null || tokens.peek(-2) instanceof Lexer.NewlineToken) ? current : null
        );
    }

    // --- token ---

    private static final class QuotationToken extends Lexer.Token {

        public final int depth;

        public QuotationToken(int depth) {
            super(">".repeat(depth) + " ");
            this.depth = depth;
        }

        @Override
        public boolean isBoundary() {
            return true;
        }
    }

    // --- node ---

    private static class QuotationNode extends Parser.Node {
        @Override
        protected void visitStart(MarkdownCompiler<?> compiler) {
            compiler.visitBlockQuote();
        }

        @Override
        protected void visitEnd(MarkdownCompiler<?> compiler) {
            compiler.visitBlockQuoteEnd();
        }
    }
}
