package io.github.johnnyscriptt.safedeposit.gametest;

import com.mojang.blaze3d.platform.InputConstants;
import io.github.johnnyscriptt.safedeposit.client.mixin.AbstractContainerScreenAccessor;
import java.util.List;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

/** Real-input helpers shared by the gametests: open a container, Alt-click a slot, use the button. */
final class TestActions {

    private TestActions() {
    }

    static void openAt(ClientGameTestContext context, TestSingleplayerContext world, BlockPos pos,
                       Class<? extends AbstractContainerScreen<?>> screen) {
        world.getServer().runOnServer(server -> {
            ServerPlayer player = server.getPlayerList().getPlayers().getFirst();
            ServerLevel level = player.level();
            player.openMenu(level.getBlockState(pos).getMenuProvider(level, pos));
        });
        context.waitForScreen(screen);
        context.waitTicks(5);
    }

    /** Hold Left Alt and left-click the menu slot backed by inventory index {@code index}. */
    static void altClickInventorySlot(ClientGameTestContext context, int index) {
        double[] pos = context.computeOnClient(client -> {
            AbstractContainerScreen<?> screen = (AbstractContainerScreen<?>) client.gui.screen();
            AbstractContainerScreenAccessor access = (AbstractContainerScreenAccessor) screen;
            for (Slot slot : screen.getMenu().slots) {
                if (slot.container instanceof Inventory && slot.getContainerSlot() == index) {
                    return toWindow(client, access.safedeposit$leftPos() + slot.x + 8,
                            access.safedeposit$topPos() + slot.y + 8);
                }
            }
            throw new AssertionError("no menu slot for inventory index " + index);
        });
        context.getInput().setCursorPos(pos[0], pos[1]);
        context.waitTick();
        context.getInput().holdKey(InputConstants.KEY_LALT);
        context.waitTick();
        context.getInput().pressMouse(InputConstants.MOUSE_BUTTON_LEFT);
        context.waitTick();
        context.getInput().releaseKey(InputConstants.KEY_LALT);
        context.waitTick();
    }

    /** Moves the cursor onto the Safe Deposit button (shows its tooltip). */
    static void hoverDepositButton(ClientGameTestContext context) {
        double[] pos = context.computeOnClient(client -> {
            AbstractWidget button = depositButton(client);
            return toWindow(client, button.getX() + button.getWidth() / 2.0, button.getY() + button.getHeight() / 2.0);
        });
        context.getInput().setCursorPos(pos[0], pos[1]);
        context.waitTick();
    }

    static void clickDepositButton(ClientGameTestContext context) {
        hoverDepositButton(context);
        context.getInput().pressMouse(InputConstants.MOUSE_BUTTON_LEFT);
    }

    /** The single, active Safe Deposit button on the open screen. */
    static AbstractWidget depositButton(Minecraft client) {
        List<AbstractWidget> widgets = Screens.getWidgets(client.gui.screen()).stream()
                .filter(w -> w instanceof AbstractWidget)
                .map(w -> (AbstractWidget) w)
                .filter(w -> w.getMessage().getString().equals("Safe Deposit"))
                .toList();
        check(widgets.size() == 1, "expected one Safe Deposit button, found " + widgets.size());
        check(widgets.getFirst().active, "Safe Deposit button should be active in singleplayer");
        return widgets.getFirst();
    }

    static double[] toWindow(Minecraft client, double guiX, double guiY) {
        double scale = client.getWindow().getGuiScale();
        return new double[] {guiX * scale, guiY * scale};
    }

    static void check(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
