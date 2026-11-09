package io.github.johnnyscriptt.quickdeposit.client.ui;

import io.github.johnnyscriptt.quickdeposit.QuickDepositMod;
import io.github.johnnyscriptt.quickdeposit.client.QuickDepositClient;
import io.github.johnnyscriptt.quickdeposit.client.config.ConfigIo;
import io.github.johnnyscriptt.quickdeposit.client.mixin.AbstractContainerScreenAccessor;
import io.github.johnnyscriptt.quickdeposit.net.QuickDepositPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.components.SpriteIconButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.client.gui.screens.inventory.ShulkerBoxScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

/**
 * The Quick Deposit button, to the left of chest, barrel, ender chest and shulker box screens.
 * Shares the left column with Stardew Chest Buttons (sits under its two buttons when present).
 * Greyed out with an explanation when the server doesn't have the mod.
 */
public final class DepositButton {

    private static final int SIZE = 20;
    private static final int GAP = 4;
    private static final int ICON = 16;
    /** Stardew Chest Buttons stacks two 20 px buttons with 4 px gaps in the same spot. */
    private static final int CHEST_BUTTONS_OFFSET = 2 * (SIZE + GAP);

    private static final Identifier SPRITE = Identifier.fromNamespaceAndPath(QuickDepositMod.MOD_ID, "deposit");

    private DepositButton() {
    }

    public static void register() {
        ScreenEvents.AFTER_INIT.register((client, screen, width, height) -> {
            if (screen instanceof ContainerScreen || screen instanceof ShulkerBoxScreen) {
                Screens.getWidgets(screen).add(build((AbstractContainerScreenAccessor) screen));
            }
        });
    }

    private static SpriteIconButton build(AbstractContainerScreenAccessor panel) {
        boolean serverHasMod = ClientPlayNetworking.canSend(QuickDepositPayload.TYPE);
        Component label = Component.translatable("quickdeposit.button");
        SpriteIconButton button = SpriteIconButton.builder(label,
                        b -> ClientPlayNetworking.send(new QuickDepositPayload(ConfigIo.get().lockedMask)), true)
                .size(SIZE, SIZE)
                .sprite(SPRITE, ICON, ICON)
                .build();
        button.setTooltip(Tooltip.create(serverHasMod
                ? label.copy().append("\n").append(Component.translatable("quickdeposit.button.hint",
                        QuickDepositClient.LOCK_KEY.getTranslatedKeyMessage()))
                : Component.translatable("quickdeposit.button.needs_server")));
        button.active = serverHasMod;
        int y = panel.quickdeposit$topPos();
        if (FabricLoader.getInstance().isModLoaded("chestbuttons")) {
            y += CHEST_BUTTONS_OFFSET;
        }
        button.setX(panel.quickdeposit$leftPos() - SIZE - GAP);
        button.setY(y);
        return button;
    }
}
