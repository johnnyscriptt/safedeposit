package io.github.johnnyscriptt.safedeposit.gametest;

import io.github.johnnyscriptt.safedeposit.client.config.ConfigIo;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.fabricmc.fabric.api.client.gametest.v1.screenshot.TestScreenshotOptions;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.client.gui.screens.inventory.ShulkerBoxScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;

/**
 * Drives the mod through real input: Alt-click two hotbar slots to lock them, press the
 * Safe Deposit button, then check on the server what moved. Also checks a shulker box is
 * never pushed into a shulker box, and that a second Alt-click unlocks.
 */
public final class DepositGameTest implements FabricClientGameTest {

    @Override
    public void runTest(ClientGameTestContext context) {
        context.getInput().resizeWindow(960, 540);
        context.runOnClient(client -> {
            ConfigIo.get().lockedMask = 0L;
            ConfigIo.save();
        });
        try (TestSingleplayerContext world = context.worldBuilder().create()) {
            world.getConnection().waitForChunksRender();
            BlockPos chest = world.getServer().computeOnServer(server -> {
                ServerPlayer player = server.getPlayerList().getPlayers().getFirst();
                ServerLevel level = player.level();
                BlockPos origin = player.blockPosition();
                level.setBlockAndUpdate(origin.offset(0, 0, 2), Blocks.CHEST.defaultBlockState());
                level.setBlockAndUpdate(origin.offset(2, 0, 0), Blocks.SHULKER_BOX.defaultBlockState());
                Inventory inv = player.getInventory();
                inv.clearContent();
                inv.setItem(0, new ItemStack(Items.DIAMOND_SWORD));
                inv.setItem(1, new ItemStack(Items.COBBLESTONE, 64));
                inv.setItem(5, new ItemStack(Items.BREAD, 20));
                inv.setItem(9, new ItemStack(Items.DIRT, 32));
                inv.setItem(20, new ItemStack(Items.SHULKER_BOX));
                return origin.offset(0, 0, 2);
            });

            TestActions.openAt(context, world, chest, ContainerScreen.class);
            TestActions.altClickInventorySlot(context, 0);
            TestActions.altClickInventorySlot(context, 5);
            TestActions.check(context.computeOnClient(c -> ConfigIo.get().locked().isLocked(0)
                    && ConfigIo.get().locked().isLocked(5)
                    && ConfigIo.get().locked().count() == 2), "Alt-click should lock slots 0 and 5");
            context.takeScreenshot(TestScreenshotOptions.of("deposit-01-locked").disableCounterPrefix());

            TestActions.clickDepositButton(context);
            context.waitTicks(10);
            context.takeScreenshot(TestScreenshotOptions.of("deposit-02-after").disableCounterPrefix());

            String afterChest = world.getServer().computeOnServer(server -> {
                Inventory inv = server.getPlayerList().getPlayers().getFirst().getInventory();
                Container box = (Container) server.overworld().getBlockEntity(chest);
                StringBuilder problems = new StringBuilder();
                expect(problems, inv.getItem(0).is(Items.DIAMOND_SWORD), "locked sword left the inventory");
                expect(problems, inv.getItem(5).is(Items.BREAD) && inv.getItem(5).getCount() == 20, "locked bread moved");
                expect(problems, inv.getItem(1).isEmpty(), "cobblestone stayed");
                expect(problems, inv.getItem(9).isEmpty(), "dirt stayed");
                expect(problems, inv.getItem(20).isEmpty(), "shulker box stayed (a chest accepts it)");
                expect(problems, count(box, Items.COBBLESTONE) == 64, "chest cobblestone " + count(box, Items.COBBLESTONE));
                expect(problems, count(box, Items.DIRT) == 32, "chest dirt " + count(box, Items.DIRT));
                expect(problems, count(box, Items.SHULKER_BOX) == 1, "chest shulker box missing");
                return problems.toString();
            });
            TestActions.check(afterChest.isEmpty(), "chest deposit: " + afterChest);
            context.setScreen(() -> null);

            // A shulker box refuses shulker boxes: that one must stay behind.
            BlockPos shulker = chest.offset(2, 0, -2);
            world.getServer().runOnServer(server -> {
                Inventory inv = server.getPlayerList().getPlayers().getFirst().getInventory();
                inv.setItem(20, new ItemStack(Items.SHULKER_BOX));
                inv.setItem(21, new ItemStack(Items.OAK_LOG, 16));
            });
            TestActions.openAt(context, world, shulker, ShulkerBoxScreen.class);
            TestActions.clickDepositButton(context);
            context.waitTicks(10);
            String afterShulker = world.getServer().computeOnServer(server -> {
                Inventory inv = server.getPlayerList().getPlayers().getFirst().getInventory();
                Container box = (Container) server.overworld().getBlockEntity(shulker);
                StringBuilder problems = new StringBuilder();
                expect(problems, inv.getItem(20).is(Items.SHULKER_BOX), "shulker box went into a shulker box");
                expect(problems, inv.getItem(21).isEmpty(), "oak logs stayed");
                expect(problems, count(box, Items.OAK_LOG) == 16, "shulker oak logs " + count(box, Items.OAK_LOG));
                expect(problems, inv.getItem(0).is(Items.DIAMOND_SWORD), "locked sword left (shulker)");
                return problems.toString();
            });
            TestActions.check(afterShulker.isEmpty(), "shulker deposit: " + afterShulker);

            TestActions.altClickInventorySlot(context, 5);
            TestActions.check(context.computeOnClient(c -> !ConfigIo.get().locked().isLocked(5)
                    && ConfigIo.get().locked().isLocked(0)), "a second Alt-click should unlock slot 5");
            context.setScreen(() -> null);
        } finally {
            context.runOnClient(client -> {
                ConfigIo.get().lockedMask = 0L;
                ConfigIo.save();
            });
        }
    }

    private static int count(Container box, net.minecraft.world.item.Item item) {
        int n = 0;
        for (int i = 0; i < box.getContainerSize(); i++) {
            if (box.getItem(i).is(item)) {
                n += box.getItem(i).getCount();
            }
        }
        return n;
    }

    private static void expect(StringBuilder problems, boolean ok, String message) {
        if (!ok) {
            problems.append(message).append("; ");
        }
    }
}
