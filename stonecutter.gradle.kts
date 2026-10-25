plugins {
    id("dev.kikugie.stonecutter")
}

stonecutter active "1.21.1-fabric"

// See https://stonecutter.kikugie.dev/wiki/config/params
stonecutter parameters {
    val (version, loader) = current.project.split('-', limit = 2)

    // Version- and loader-specific properties from `stonecutter.properties.toml`
    properties {
        tags(version, loader)
    }

    // Constants for Stonecutter comments, e.g. `//? if fabric {`
    constants {
        match(loader, "fabric", "neoforge")
    }

    swaps["mod_version"] = "\"${properties.get<String>("mod.version")}\";"
    swaps["minecraft"] = "\"${node.metadata.version}\";"

    replacements {
        // Replacements work in both directions (when the active node changes), so they must be unambiguous
        string(current.parsed >= "1.21.11") {
            replace("ResourceLocation", "Identifier")
            replace(".location()", ".identifier()")
        }
        // 26.1 renamed the GUI drawing methods; the overridden headers are Stonecutter conditions in the screens
        string(current.parsed >= "26.1") {
            replace("import net.minecraft.client.gui.GuiGraphics;", "import net.minecraft.client.gui.GuiGraphicsExtractor;")
            replace("GuiGraphics graphics", "GuiGraphicsExtractor graphics")
            replace("graphics.drawString(", "graphics.text(")
            replace("graphics.drawCenteredString(", "graphics.centeredText(")
            replace("graphics.drawWordWrap(", "graphics.textWithWordWrap(")
            replace(".render(graphics, ", ".extractRenderState(graphics, ")
            replace("super.renderBackground(graphics, ", "super.extractBackground(graphics, ")
        }
        // Classes and methods that moved or were renamed in 26.1
        string(current.parsed >= "26.1") {
            replace("net.minecraft.client.renderer.state.CameraRenderState", "net.minecraft.client.renderer.state.level.CameraRenderState")
            replace("net.minecraft.client.renderer.block.model.BakedQuad", "net.minecraft.client.resources.model.geometry.BakedQuad")
            replace("net.minecraft.client.renderer.block.model.ItemTransforms", "net.minecraft.client.resources.model.cuboid.ItemTransforms")
            replace("net.minecraft.client.renderer.block.model.TextureSlots", "net.minecraft.client.resources.model.sprite.TextureSlots")
            replace("net.minecraft.client.resources.model.UnbakedGeometry", "net.minecraft.client.resources.model.geometry.UnbakedGeometry")
            replace("net.minecraft.client.resources.model.QuadCollection", "net.minecraft.client.resources.model.geometry.QuadCollection")
            replace("import net.minecraft.client.renderer.LightTexture;", "import net.minecraft.util.LightCoordsUtil;")
            replace("LightTexture.FULL_BRIGHT", "LightCoordsUtil.FULL_BRIGHT")
            replace("LevelRenderer.getLightColor(", "LevelRenderer.getLightCoords(")
            replace("DimensionDataStorage", "SavedDataStorage")
            replace("chunk.x, chunk.z", "chunk.x(), chunk.z()")
            replace("chunk.z != 0", "chunk.z() != 0")
            replace("player.getTags()", "player.entityTags()")
        }
        string(current.parsed >= "26.1" && loader == "neoforge") {
            replace("net.neoforged.neoforge.event.level.BlockEvent.BreakEvent.class", "net.neoforged.neoforge.event.level.block.BreakBlockEvent.class")
        }
        // The GameTest framework was rewritten in 1.21.5; Fabric has an annotation of its own (FabricGameTests)
        string(current.parsed >= "1.21.5" && loader == "fabric") {
            replace("import net.minecraft.gametest.framework.GameTest;", "import net.fabricmc.fabric.api.gametest.v1.GameTest;")
            replace("@GameTest(template = ", "@GameTest(structure = ")
            replace(", timeoutTicks = ", ", maxTicks = ")
        }
        // NeoForge registers the tests of NeoForgeGameTests itself since 1.21.11 (GameTestRegistration)
        string(current.parsed >= "1.21.11" && loader == "neoforge") {
            replace("import net.minecraft.gametest.framework.GameTest;", "import de.joernott.c0de.gametest.neoforge.GameTestRegistration.GameTest;")
            replace("import net.neoforged.neoforge.gametest.GameTestHolder;", "import de.joernott.c0de.gametest.neoforge.GameTestRegistration.GameTestHolder;")
            replace("import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;", "import de.joernott.c0de.gametest.neoforge.GameTestRegistration.PrefixGameTestTemplate;")
        }
    }
}
