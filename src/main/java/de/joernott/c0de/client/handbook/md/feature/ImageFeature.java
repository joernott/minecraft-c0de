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
import net.minecraft.resources.ResourceLocation;

public class ImageFeature implements MarkdownFeature {

    @Override
    public String name() {
        return "images";
    }

    @Override
    public boolean supportsCompiler(MarkdownCompiler<?> compiler) {
        return true;
    }

    @Override
    public void registerTokens(TokenRegistrar registrar) {
        registrar.registerToken((nibbler, tokens) -> {
            nibbler.skip();
            if (!nibbler.tryConsume('[')) return false;

            var description = nibbler.consumeUntil(']');
            if (description == null || !nibbler.tryConsume('(')) return false;

            var identifier = nibbler.consumeUntil(')');
            if (identifier == null) return false;

            boolean fit = identifier.endsWith(",fit");
            if (fit) identifier = identifier.substring(0, identifier.length() - 4);
            if (ResourceLocation.tryParse(identifier) == null) return false;

            tokens.add(new ImageToken(description, identifier, fit));
            return true;
        }, '!');

    }

    @Override
    public void registerNodes(NodeRegistrar registrar) {
        registrar.registerNode(
                (parser, image, tokens) -> new ImageNode(image.identifier, image.description, image.fit),
                (token, tokens) -> token instanceof ImageToken image ? image : null
        );
    }

    // --- token ---

    private static final class ImageToken extends Lexer.Token {

        public final String description, identifier;
        public final boolean fit;

        public ImageToken(String description, String identifier, boolean fit) {
            super("![" + description + "](" + identifier + ")");
            this.description = description;
            this.identifier = identifier;
            this.fit = fit;
        }
    }

    // --- node ---

    private static class ImageNode extends Parser.Node {

        private final String identifier, description;
        private final boolean fit;

        public ImageNode(String identifier, String description, boolean fit) {
            this.identifier = identifier;
            this.description = description;
            this.fit = fit;
        }

        @Override
        protected void visitStart(MarkdownCompiler<?> compiler) {
            compiler.visitImage(ResourceLocation.parse(this.identifier), this.description, this.fit);
        }

        @Override
        protected void visitEnd(MarkdownCompiler<?> compiler) {}
    }
}
