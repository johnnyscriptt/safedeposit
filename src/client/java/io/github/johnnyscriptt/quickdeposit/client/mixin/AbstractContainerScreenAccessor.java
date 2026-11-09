package io.github.johnnyscriptt.quickdeposit.client.mixin;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** Reads the container panel's position and the slot under the mouse. */
@Mixin(AbstractContainerScreen.class)
public interface AbstractContainerScreenAccessor {
    @Accessor("leftPos")
    int quickdeposit$leftPos();

    @Accessor("topPos")
    int quickdeposit$topPos();

    @Accessor("hoveredSlot")
    Slot quickdeposit$hoveredSlot();
}
