package io.github.johnnyscriptt.quickdeposit.client.ui;

import com.mojang.blaze3d.platform.InputConstants;
import io.github.johnnyscriptt.quickdeposit.client.QuickDepositClient;
import io.github.johnnyscriptt.quickdeposit.client.config.ConfigIo;
import io.github.johnnyscriptt.quickdeposit.client.mixin.AbstractContainerScreenAccessor;
import io.github.johnnyscriptt.quickdeposit.core.LockedSlots;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

/**
 * Lock-key + left click on one of your own inventory slots toggles its lock, on any container
 * screen (the survival inventory too). Locked slots get a blue tint, nothing else, so the item stays readable. The creative
 * inventory is left out because its slots are wrappers that don't map cleanly.
 */
public final class SlotLocking {

    /** Accent #4FC3F7 at the passive-overlay alpha (~0.22). */
    private static final int TINT = 0x384FC3F7;

    private SlotLocking() {
    }

    public static void register() {
        ScreenEvents.AFTER_INIT.register((client, screen, width, height) -> {
            if (screen instanceof AbstractContainerScreen<?> && !(screen instanceof CreativeModeInventoryScreen)) {
                ScreenMouseEvents.allowMouseClick(screen).register(SlotLocking::allowClick);
                ScreenEvents.afterExtract(screen).register(SlotLocking::drawLocks);
            }
        });
    }

    /** Returns false (swallowing the click) when it toggled a lock. */
    private static boolean allowClick(Screen screen, MouseButtonEvent click) {
        if (click.button() != InputConstants.MOUSE_BUTTON_LEFT || !isLockKeyDown()) {
            return true;
        }
        int index = inventoryIndex(((AbstractContainerScreenAccessor) screen).quickdeposit$hoveredSlot());
        if (index < 0) {
            return true;
        }
        ConfigIo.get().toggle(index);
        ConfigIo.save();
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.6F));
        return false;
    }

    private static void drawLocks(Screen screen, GuiGraphicsExtractor g, int mouseX, int mouseY, float delta) {
        LockedSlots locks = ConfigIo.get().locked();
        if (locks.count() == 0) {
            return;
        }
        AbstractContainerScreen<?> container = (AbstractContainerScreen<?>) screen;
        AbstractContainerScreenAccessor access = (AbstractContainerScreenAccessor) screen;
        for (Slot slot : container.getMenu().slots) {
            int index = inventoryIndex(slot);
            if (index >= 0 && locks.isLocked(index)) {
                int x = access.quickdeposit$leftPos() + slot.x;
                int y = access.quickdeposit$topPos() + slot.y;
                g.fill(x, y, x + 16, y + 16, TINT);
            }
        }
    }

    /** The player inventory index (0..35) behind a menu slot, or -1 for anything else. */
    static int inventoryIndex(Slot slot) {
        if (slot == null || !(slot.container instanceof Inventory)) {
            return -1;
        }
        int index = slot.getContainerSlot();
        return index >= 0 && index < LockedSlots.SLOT_COUNT ? index : -1;
    }

    /** Key mappings don't update while a screen is open, so read the physical key. */
    private static boolean isLockKeyDown() {
        InputConstants.Key key = KeyMappingHelper.getBoundKeyOf(QuickDepositClient.LOCK_KEY);
        return key.getType() == InputConstants.Type.KEYBOARD && InputConstants.isKeyDown(key.getValue());
    }
}
