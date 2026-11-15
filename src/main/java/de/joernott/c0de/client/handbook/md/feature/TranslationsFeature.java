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
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;

import java.util.Optional;

public class TranslationsFeature implements MarkdownFeature {

    @Override
    public String name() {
        return "translations";
    }

    @Override
    public boolean supportsCompiler(MarkdownCompiler<?> compiler) {
        return true;
    }

    @Override
    public void registerTokens(TokenRegistrar registrar) {
        registrar.registerToken((nibbler, tokens) -> {
            if (!nibbler.tryConsume("%{")) return false;

            var content = nibbler.consumeEscapedString('}', false);
            if (content == null || !nibbler.tryConsume('%')) return false;

            tokens.add(new TranslationToken(content));
            return true;
        },  '%');
    }

    @Override
    public void registerNodes(NodeRegistrar registrar) {
        registrar.registerNode((parser, translation, tokens) -> {
            var result = Parser.Node.empty();
            Component.translatable(translation.key).visit((style, content) -> {
                result.addChild(new Parser.FormattingNode(style::applyTo).addChild(new Parser.TextNode(content)));
                return Optional.empty();
            }, Style.EMPTY);

            return result;
        }, (token, tokenListNibbler) -> token instanceof TranslationToken translation ? translation : null);
    }

    private static final class TranslationToken extends Lexer.Token {

        public final String key;

        public TranslationToken(String key) {
            super("%{" + key + "}%");
            this.key = key;
        }
    }
}
