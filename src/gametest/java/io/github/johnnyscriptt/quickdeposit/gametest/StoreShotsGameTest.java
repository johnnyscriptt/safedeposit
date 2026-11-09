package io.github.johnnyscriptt.quickdeposit.gametest;

import io.github.johnnyscriptt.quickdeposit.client.config.ConfigIo;
import io.github.johnnyscriptt.quickdeposit.core.LockedSlots;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.fabricmc.fabric.api.client.gametest.v1.screenshot.TestScreenshotOptions;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.gui.screens.inventory.ShulkerBoxScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.state.properties.ChestType;

/**
 * Real in-game captures for the store pages. Not a pass/fail test: it exists so every store
 * image is an honest screenshot of the mod. Shots are taken at 960x540 and enlarged 2x by
 * {@code caption_gallery.py}. Output: {@code build/run/clientGameTest/screenshots/store-*.png}.
 */
public final class StoreShotsGameTest implements FabricClientGameTest {

    /** Sword, pickaxe, torches and food: the slots a miner keeps. */
    private static final int[] KEPT = {0, 1, 2, 8};

    @Override
    public void runTest(ClientGameTestContext context) {
        context.getInput().resizeWindow(960, 540);
        context.runOnClient(client -> {
            LockedSlots locks = LockedSlots.NONE;
            for (int i : KEPT) {
                locks = locks.toggle(i);
            }
            ConfigIo.get().lockedMask = locks.toLong();
            ConfigIo.save();
        });
        try (TestSingleplayerContext world = context.worldBuilder().create()) {
            world.getConnection().waitForChunksRender();
            for (String command : new String[] {"time set noon", "weather clear",
                    "gamerule advance_time false", "difficulty peaceful"}) {
                world.getServer().runCommand(command);
            }
            BlockPos origin = world.getServer().computeOnServer(server -> {
                ServerPlayer player = server.getPlayerList().getPlayers().getFirst();
                BlockPos base = player.blockPosition();
                buildScene(player.level(), base);
                fillMiningHaul(player.getInventory());
                return base;
            });
            BlockPos chest = origin.offset(0, 0, 2);
            BlockPos shulker = origin.offset(-2, 0, 0);

            // 02 first (it is the "before"): locked slots tinted, button tooltip explaining the lock key.
            TestActions.openAt(context, world, chest, ContainerScreen.class);
            TestActions.hoverDepositButton(context);
            shot(context, "store-02-lock-what-you-keep");

            // 01: one click later, the haul is in the chest and the kept slots are not.
            TestActions.clickDepositButton(context);
            context.waitTicks(4);
            context.getInput().setCursorPos(940, 520);
            shot(context, "store-01-one-click");
            context.setScreen(() -> null);

            // 03: the same locks in the survival inventory.
            context.setScreen(() -> new InventoryScreen(context.computeOnClient(c -> c.player)));
            context.getInput().setCursorPos(940, 520);
            shot(context, "store-03-inventory");
            context.setScreen(() -> null);

            // 04: a shulker box takes a second load.
            world.getServer().runOnServer(server ->
                    fillSecondLoad(server.getPlayerList().getPlayers().getFirst().getInventory()));
            TestActions.openAt(context, world, shulker, ShulkerBoxScreen.class);
            TestActions.clickDepositButton(context);
            context.waitTicks(4);
            context.getInput().setCursorPos(940, 520);
            shot(context, "store-04-shulker");
            context.setScreen(() -> null);

            // 05: a nearly full chest. What doesn't fit stays in the inventory.
            BlockPos small = origin.offset(2, 0, 0);
            world.getServer().runOnServer(server -> {
                ServerPlayer player = server.getPlayerList().getPlayers().getFirst();
                player.level().setBlockAndUpdate(small, Blocks.CHEST.defaultBlockState());
                if (player.level().getBlockEntity(small) instanceof net.minecraft.world.Container box) {
                    for (int i = 0; i < box.getContainerSize() - 3; i++) {
                        box.setItem(i, new ItemStack(i % 2 == 0 ? Items.STONE : Items.DEEPSLATE, 64));
                    }
                }
                fillMiningHaul(player.getInventory());
            });
            TestActions.openAt(context, world, small, ContainerScreen.class);
            TestActions.clickDepositButton(context);
            context.waitTicks(4);
            context.getInput().setCursorPos(940, 520);
            shot(context, "store-05-full-chest");
            context.setScreen(() -> null);
        } finally {
            context.runOnClient(client -> {
                ConfigIo.get().lockedMask = 0L;
                ConfigIo.save();
            });
        }
    }

    /** A double chest in front of the player and a shulker box to the side. */
    private static void buildScene(ServerLevel level, BlockPos base) {
        BlockPos left = base.offset(0, 0, 2);
        level.setBlockAndUpdate(left, Blocks.CHEST.defaultBlockState()
                .setValue(ChestBlock.FACING, Direction.NORTH).setValue(ChestBlock.TYPE, ChestType.LEFT));
        level.setBlockAndUpdate(left.east(), Blocks.CHEST.defaultBlockState()
                .setValue(ChestBlock.FACING, Direction.NORTH).setValue(ChestBlock.TYPE, ChestType.RIGHT));
        level.setBlockAndUpdate(base.offset(-2, 0, 0), Blocks.SHULKER_BOX.defaultBlockState());
    }

    /** A full inventory after a trip down: tools and food in the locked slots, loot everywhere else. */
    private static void fillMiningHaul(Inventory inv) {
        inv.clearContent();
        inv.setItem(0, new ItemStack(Items.DIAMOND_SWORD));
        inv.setItem(1, new ItemStack(Items.DIAMOND_PICKAXE));
        inv.setItem(2, new ItemStack(Items.TORCH, 48));
        inv.setItem(8, new ItemStack(Items.COOKED_BEEF, 24));
        put(inv, 3, Items.COBBLESTONE, 64);
        put(inv, 4, Items.COBBLED_DEEPSLATE, 64);
        put(inv, 5, Items.RAW_IRON, 23);
        put(inv, 6, Items.COAL, 41);
        put(inv, 7, Items.GRAVEL, 17);
        Object[][] main = {
                {Items.COBBLESTONE, 64}, {Items.COBBLESTONE, 64}, {Items.COBBLESTONE, 64},
                {Items.COBBLED_DEEPSLATE, 64}, {Items.COBBLED_DEEPSLATE, 64}, {Items.ANDESITE, 51},
                {Items.DIORITE, 33}, {Items.GRANITE, 40}, {Items.TUFF, 64},
                {Items.RAW_IRON, 30}, {Items.RAW_GOLD, 11}, {Items.RAW_COPPER, 46},
                {Items.COAL, 64}, {Items.REDSTONE, 38}, {Items.LAPIS_LAZULI, 22},
                {Items.DIAMOND, 7}, {Items.EMERALD, 2}, {Items.FLINT, 9},
                {Items.AMETHYST_SHARD, 14}, {Items.POINTED_DRIPSTONE, 6}, {Items.CALCITE, 19},
                {Items.SMOOTH_BASALT, 8}, {Items.GLOW_LICHEN, 5}, {Items.STRING, 4},
                {Items.BONE, 6}, {Items.ROTTEN_FLESH, 11}, {Items.GUNPOWDER, 3}};
        for (int i = 0; i < main.length; i++) {
            put(inv, 9 + i, (Item) main[i][0], (Integer) main[i][1]);
        }
    }

    /** Fewer stacks, so the shulker box shot shows a tidy box and an emptied inventory. */
    private static void fillSecondLoad(Inventory inv) {
        Object[][] load = {
                {Items.OAK_LOG, 64}, {Items.OAK_LOG, 64}, {Items.BIRCH_LOG, 37}, {Items.OAK_SAPLING, 12},
                {Items.APPLE, 5}, {Items.STICK, 22}, {Items.WHEAT_SEEDS, 31}, {Items.SPRUCE_LOG, 48}};
        for (int i = 0; i < load.length; i++) {
            put(inv, 9 + i, (Item) load[i][0], (Integer) load[i][1]);
        }
    }

    private static void put(Inventory inv, int slot, Item item, int count) {
        inv.setItem(slot, new ItemStack(item, count));
    }

    /** Screens: let the GUI settle, clear toasts and chat, then capture. */
    private static void shot(ClientGameTestContext context, String name) {
        context.waitTicks(8);
        context.runOnClient(client -> {
            client.gui.toastManager().clear();
            client.gui.hud.getChat().clearMessages(false);
        });
        context.waitTick();
        context.takeScreenshot(TestScreenshotOptions.of(name).disableCounterPrefix());
    }
}
