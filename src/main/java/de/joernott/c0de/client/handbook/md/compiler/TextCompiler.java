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
package de.joernott.c0de.client.handbook.md.compiler;

import de.joernott.c0de.client.handbook.md.util.TextBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;

import java.util.OptionalInt;
import java.util.function.UnaryOperator;

/**
 * lavender-md's default compiler implementation which compiles to a
 * single Minecraft {@link Component} - depending on the input
 * AST it might contain multiple lines
 */
public class TextCompiler implements MarkdownCompiler<Component> {

    private final TextBuilder builder = new TextBuilder();
    private final int assumedOutputWidth;

    private int quoteDepth = 0;
    private int listDepth = 0;

    public TextCompiler() {
        this(50);
    }

    public TextCompiler(int assumedOutputWidth) {
        this.assumedOutputWidth = assumedOutputWidth;
    }

    @Override
    public void visitText(String text) {
        if (this.quoteDepth != 0 && text.contains("\n")) {
            if (text.equals("\n")) {
                this.builder.append(this.quoteMarker());
            } else {
                for (var line : text.split("\n")) {
                    this.builder.append(this.quoteMarker().append(Component.literal(line)));
                }
            }
        } else if (this.listDepth != 0 && text.contains("\n")) {
            if (text.equals("\n")) {
                this.builder.append(Component.literal("\n   " + "  ".repeat(this.listDepth - 1)));
            } else {
                var lines = text.split("\n");
                for (int i = 0; i < lines.length; i++) {
                    this.builder.append(Component.literal((i > 0 ? "\n   " : "   ") + "  ".repeat(this.listDepth - 1)).append(Component.literal(lines[i])));
                }
            }
        } else {
            this.builder.append(Component.literal(text));
        }
    }

    @Override
    public void visitStyle(UnaryOperator<Style> style) {
        this.builder.pushStyle(style);
    }

    @Override
    public void visitStyleEnd() {
        this.builder.popStyle();
    }

    @Override
    public void visitBlockQuote() {
        this.quoteDepth++;
        this.builder.append(this.quoteMarker());
        this.builder.pushStyle(style -> style.withColor(ChatFormatting.GRAY).withItalic(true));
    }

    @Override
    public void visitBlockQuoteEnd() {
        this.builder.popStyle();
        this.quoteDepth--;

        if (this.quoteDepth > 0) {
            this.builder.append(this.quoteMarker());
        } else {
            this.builder.append(Component.literal("\n"));
        }
    }

    private MutableComponent quoteMarker() {
        return Component.literal("\n >" + ">".repeat(this.quoteDepth) + " ").withStyle(ChatFormatting.DARK_GRAY);
    }

    @Override
    public void visitHorizontalRule() {
        this.builder.append(Component.literal("-".repeat(this.assumedOutputWidth)).withStyle(ChatFormatting.DARK_GRAY));
    }

    @Override
    public void visitImage(ResourceLocation image, String description, boolean fit) {
        this.builder.append(Component.literal("[" + description + "]").withStyle(ChatFormatting.YELLOW));
    }

    @Override
    public void visitListItem(OptionalInt ordinal) {
        var listPrefix = ordinal.isPresent() ? " " + ordinal.getAsInt() + ". " : " • ";

        if (this.listDepth > 0) {
            this.builder.append(Component.literal("\n" + "   ".repeat(this.listDepth) + listPrefix));
        } else {
            this.builder.append(Component.literal(listPrefix));
        }

        this.listDepth++;
    }

    @Override
    public void visitListItemEnd() {
        this.listDepth--;

        if (this.listDepth > 0) {
            this.builder.append(Component.literal("   ".repeat(this.listDepth)));
        } else {
            this.builder.append(Component.literal("\n"));
        }
    }

    @Override
    public Component compile() {
        return this.builder.build();
    }

    @Override
    public String name() {
        return "lavender_builtin_text";
    }
}
