package io.github.johnnyscriptt.quickdeposit.server;

import io.github.johnnyscriptt.quickdeposit.core.SlotView;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/** Adapts a live Minecraft {@link Slot} to the pure {@link SlotView} the core uses. */
final class SlotViewAdapter implements SlotView {

    private final Slot slot;

    SlotViewAdapter(Slot slot) {
        this.slot = slot;
    }

    @Override
    public boolean isEmpty() {
        return slot.getItem().isEmpty();
    }

    @Override
    public Object key() {
        ItemStack stack = slot.getItem();
        return stack.isEmpty() ? null : new StackKey(stack);
    }

    @Override
    public int count() {
        return slot.getItem().getCount();
    }

    @Override
    public int maxCount() {
        ItemStack stack = slot.getItem();
        // Empty slots' max is never read by the core; 64 is a harmless default.
        return stack.isEmpty() ? 64 : slot.getMaxStackSize(stack);
    }

    @Override
    public boolean accepts(Object key) {
        return slot.mayPlace(((StackKey) key).prototype());
    }

    @Override
    public void set(Object key, int count) {
        if (key == null || count <= 0) {
            slot.set(ItemStack.EMPTY);
            return;
        }
        slot.set(((StackKey) key).newStack(count));
    }
}
