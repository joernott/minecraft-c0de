/*
 * Taken from lavender-md (https://github.com/wisp-forest/lavender-md, branch 26.1) for the handbooks of c0de
 * (docs/concept/21-handbooks.md). Changed for c0de: the link is the insertion of the text instead of a click and a hover event, whose classes differ between the Minecraft versions.
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
import net.minecraft.ChatFormatting;
import org.jetbrains.annotations.NotNull;

public class LinkFeature implements MarkdownFeature {

    /** The prefix of the insertion of a link's text; the book screen opens the link on a click. */
    public static final String LINK = "c0de-link:";

    @Override
    public String name() {
        return "links";
    }

    @Override
    public boolean supportsCompiler(MarkdownCompiler<?> compiler) {
        return true;
    }

    @Override
    public void registerTokens(TokenRegistrar registrar) {
        registrar.registerToken(Lexer.Token.lexFromChar(OpenLinkToken::new), '[');
        registrar.registerToken((nibbler, tokens) -> {
            nibbler.skip();
            if (!nibbler.tryConsume('(')) return false;

            var link = nibbler.consumeUntil(')');
            if (link == null) return false;

            tokens.add(new CloseLinkToken(link));
            return true;
        }, ']');
    }

    @Override
    public void registerNodes(NodeRegistrar registrar) {
        registrar.registerNode((parser, left, tokens) -> {
            int pointer = tokens.pointer();
            var content = parser.parseUntil(tokens, CloseLinkToken.class);

            if (tokens.peek() instanceof CloseLinkToken right) {
                tokens.nibble();
                return new Parser.FormattingNode(style -> style.withInsertion(LINK + right.link)
                        .withColor(ChatFormatting.BLUE)).addChild(content);
            } else {
                tokens.setPointer(pointer);
                return new Parser.TextNode(left.content());
            }
        }, (token, tokens) -> token instanceof OpenLinkToken link ? link : null);
    }

    // --- tokens ---

    private static final class OpenLinkToken extends Lexer.Token {
        public OpenLinkToken() {
            super("[");
        }
    }

    private static final class CloseLinkToken extends Lexer.Token {

        public final @NotNull String link;

        public CloseLinkToken(@NotNull String link) {
            super("](" + link + ")");
            this.link = link;
        }
    }
}
