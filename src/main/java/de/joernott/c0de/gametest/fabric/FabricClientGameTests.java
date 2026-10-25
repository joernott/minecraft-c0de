package de.joernott.c0de.gametest.fabric;

//? if fabric && >=1.21.11 {
/*import de.joernott.c0de.client.TerminalScreen;
import de.joernott.c0de.client.TriggerScreen;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;
import javax.imageio.ImageIO;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;

/^*
 * Client GameTests (task 12.4, entry point {@code fabric-client-gametest}): a real client creates a world, renders
 * the c0de dimension and the blocks of the mod and opens its screens. They run with {@code runClientGameTest}
 * where Fabric API has client GameTests (1.21.4 and later); the screenshots are in
 * {@code build/clientGameTest/screenshots}.
 ^/
public class FabricClientGameTests implements FabricClientGameTest {
    /^* The color of missing textures (magenta of the black and magenta checkerboard). ^/
    private static final int MISSING_TEXTURE = 0xF800F8;

    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext world = context.worldBuilder().create()) {
            // No fall damage while the tests teleport the player around
            world.getServer().runCommand("gamemode creative @a");
            dimensionIsLit(context, world);
            blocksHaveTheirTextures(context, world);
            triggerDialogOpens(context, world);
            terminalOpens(context, world);
        }
    }

    /^* The floor of the c0de dimension is lit without sky light (26.1 made it black, task 12.2). ^/
    private static void dimensionIsLit(ClientGameTestContext context, TestSingleplayerContext world) {
        world.getServer().runCommand("execute in c0de:c0de run tp @a 8.5 -3 2.5 0 60");
        waitForChunks(context, world);
        BufferedImage image = screenshot(context, "c0de-dimension");
        double brightness = brightness(image);
        if (brightness < 40) {
            throw new AssertionError("The c0de dimension is too dark: average brightness " + brightness);
        }
    }

    /^*
     * Language blocks with shapes of their own and with item textures of Minecraft, and the devices, have their
     * textures: no pixel has the color of missing textures (task 12.1: item textures in block models).
     ^/
    private static void blocksHaveTheirTextures(ClientGameTestContext context, TestSingleplayerContext world) {
        String[] blocks = {
            "program_block", "begin_block", "if_block", "for_block", "literal_block", "assign_block",
            "type_string_block", "type_time_block", "type_error_block", "comment_block", "display_block", "laptop"
        };
        for (int i = 0; i < blocks.length; i++) {
            world.getServer().runCommand(
                    "execute in c0de:c0de run setblock " + (6 + i % 6) + " -3 " + (5 + i / 6) + " c0de:" + blocks[i]);
        }
        world.getServer().runCommand("execute in c0de:c0de run tp @a 8.5 -1 1.5 0 40");
        waitForChunks(context, world);
        BufferedImage image = screenshot(context, "c0de-blocks");
        int missing = countColor(image, MISSING_TEXTURE);
        if (missing > 20) {
            throw new AssertionError(missing + " pixels have the color of missing textures");
        }
    }

    /^* Using a trigger block with an empty hand opens its dialog. ^/
    private static void triggerDialogOpens(ClientGameTestContext context, TestSingleplayerContext world) {
        useBlockInFront(context, world, "trigger_block");
        context.waitForScreen(TriggerScreen.class);
        screenshot(context, "c0de-trigger-dialog");
        context.setScreen(() -> null);
    }

    /^* Using a laptop opens its terminal, which takes keys. ^/
    private static void terminalOpens(ClientGameTestContext context, TestSingleplayerContext world) {
        useBlockInFront(context, world, "laptop");
        context.waitForScreen(TerminalScreen.class);
        context.getInput().typeChars("hello");
        context.waitTicks(5);
        screenshot(context, "c0de-terminal");
        context.setScreen(() -> null);
    }

    /^* Places a block in the overworld at eye height in front of the player and uses it. ^/
    private static void useBlockInFront(ClientGameTestContext context, TestSingleplayerContext world, String block) {
        world.getServer().runCommand("execute in minecraft:overworld run tp @a 0.5 100 0.5 0 0");
        // Blocks can only be placed in loaded chunks
        waitForChunks(context, world);
        world.getServer().runCommand("execute in minecraft:overworld run fill -1 99 -1 1 99 3 minecraft:stone");
        world.getServer().runCommand("execute in minecraft:overworld run fill -1 100 1 1 102 3 minecraft:air");
        world.getServer().runCommand("execute in minecraft:overworld run setblock 0 101 2 c0de:" + block);
        // The player fell while the chunks were loaded; now the platform is there
        world.getServer().runCommand("execute in minecraft:overworld run tp @a 0.5 100 0.5 0 0");
        context.waitTicks(10);
        // The crosshair must point at the block
        net.minecraft.core.BlockPos target = new net.minecraft.core.BlockPos(0, 101, 2);
        for (int tick = 0; !pointsAt(context, target); tick++) {
            if (tick == 100) {
                screenshot(context, "c0de-no-target-" + block);
                String seen = context.computeOnClient(client -> client.hitResult instanceof
                        net.minecraft.world.phys.BlockHitResult hit
                        ? hit.getType() + " " + hit.getBlockPos() + " " + client.level.getBlockState(hit.getBlockPos())
                        : String.valueOf(client.hitResult));
                String player = context.computeOnClient(client ->
                        client.player.position() + " " + client.player.getYRot() + " " + client.player.getXRot());
                throw new AssertionError("The crosshair doesn't point at the " + block + " but at " + seen
                        + "; player " + player);
            }
            context.waitTick();
        }
        screenshot(context, "c0de-" + block);
        context.getInput().pressKey(options -> options.keyUse);
    }

    private static boolean pointsAt(ClientGameTestContext context, net.minecraft.core.BlockPos target) {
        return context.computeOnClient(client -> client.hitResult instanceof net.minecraft.world.phys.BlockHitResult hit
                && hit.getType() == net.minecraft.world.phys.HitResult.Type.BLOCK
                && hit.getBlockPos().equals(target));
    }

    private static void waitForChunks(ClientGameTestContext context, TestSingleplayerContext world) {
        context.waitTicks(20);
        //? if >=26.2 {
        /^world.getConnection().waitForChunksRender();
        ^///?} elif >=26.1 {
        /^world.getClientLevel().waitForChunksRender();
        ^///?} else {
        world.getClientWorld().waitForChunksRender();
        //?}
        context.waitTicks(10);
    }

    private static BufferedImage screenshot(ClientGameTestContext context, String name) {
        Path file = context.takeScreenshot(name);
        try {
            return ImageIO.read(file.toFile());
        } catch (IOException e) {
            throw new AssertionError("The screenshot " + file + " can't be read", e);
        }
    }

    /^* The average brightness (0 to 255) of the upper two thirds of the image, above the hotbar. ^/
    private static double brightness(BufferedImage image) {
        long sum = 0;
        int count = 0;
        for (int y = 0; y < image.getHeight() * 2 / 3; y += 4) {
            for (int x = 0; x < image.getWidth(); x += 4) {
                int rgb = image.getRGB(x, y);
                sum += ((rgb >> 16) & 0xFF) + ((rgb >> 8) & 0xFF) + (rgb & 0xFF);
                count += 3;
            }
        }
        return count == 0 ? 0 : (double) sum / count;
    }

    /^* The number of pixels of the upper two thirds of the image that have about the given color. ^/
    private static int countColor(BufferedImage image, int color) {
        int count = 0;
        for (int y = 0; y < image.getHeight() * 2 / 3; y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int rgb = image.getRGB(x, y);
                if (Math.abs(((rgb >> 16) & 0xFF) - ((color >> 16) & 0xFF)) < 16
                        && Math.abs(((rgb >> 8) & 0xFF) - ((color >> 8) & 0xFF)) < 16
                        && Math.abs((rgb & 0xFF) - (color & 0xFF)) < 16) {
                    count++;
                }
            }
        }
        return count;
    }
}
*///?}
