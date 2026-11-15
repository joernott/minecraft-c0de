/*
 * Taken from lavender-md (https://github.com/wisp-forest/lavender-md, branch 26.1) for the handbooks of c0de
 * (docs/concept/21-handbooks.md). Changed for c0de: without the hover event, whose classes differ between the Minecraft versions.
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
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;

import java.util.Arrays;

public class KeybindFeature implements MarkdownFeature {

    @Override
    public String name() {
        return "keybindings";
    }

    @Override
    public boolean supportsCompiler(MarkdownCompiler<?> compiler) {
        return true;
    }

    @Override
    public void registerTokens(TokenRegistrar registrar) {
        registrar.registerToken((nibbler, tokens) -> {
            if (!nibbler.tryConsume("<keybind;")) return false;

            var keybindKey = nibbler.consumeUntil('>');
            if (keybindKey == null) return false;

            var binding = Arrays.stream(Minecraft.getInstance().options.keyMappings).filter($ -> $.getName().equals(keybindKey)).findAny();
            if (binding.isEmpty()) return false;

            tokens.add(new KeybindToken(keybindKey, binding.get()));
            return true;
        }, '<');
    }

    @Override
    public void registerNodes(NodeRegistrar registrar) {
        registrar.registerNode(
            (parser, keybindToken, tokens) -> new KeybindNode(keybindToken.binding),
            (token, tokens) -> token instanceof KeybindToken keybind ? keybind : null
        );
    }

    private static class KeybindToken extends Lexer.Token {

        public final KeyMapping binding;

        public KeybindToken(String content, KeyMapping binding) {
            super(content);
            this.binding = binding;
        }
    }

    private static class KeybindNode extends Parser.Node {

        private final KeyMapping binding;

        public KeybindNode(KeyMapping binding) {
            this.binding = binding;
        }

        @Override
        public void visitStart(MarkdownCompiler<?> compiler) {
            compiler.visitStyle(style -> style.withColor(ChatFormatting.GOLD));
            compiler.visitText(this.binding.getTranslatedKeyMessage().getString());
        }

        @Override
        protected void visitEnd(MarkdownCompiler<?> compiler) {
            compiler.visitStyleEnd();
        }
    }
}
