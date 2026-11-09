package io.github.johnnyscriptt.quickdeposit.server;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;

/**
 * Stacking identity for an {@link ItemStack}: two keys are equal when the stacks have the
 * same item and data components (they could combine into one stack). Used as the opaque
 * {@code key} the core logic compares.
 */
final class StackKey {

    private final ItemStack prototype; // count-1 copy carrying item + components

    StackKey(ItemStack stack) {
        this.prototype = stack.copyWithCount(1);
    }

    /** A fresh stack of this item with the given count. */
    ItemStack newStack(int count) {
        return prototype.copyWithCount(count);
    }

    /** A count-1 stack for checks like {@code Slot.mayPlace}; callers must not mutate it. */
    ItemStack prototype() {
        return prototype;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof StackKey other
                && ItemStack.isSameItemSameComponents(prototype, other.prototype);
    }

    @Override
    public int hashCode() {
        // Item-only hash is correct (equals disambiguates component differences within a bucket).
        return BuiltInRegistries.ITEM.getId(prototype.getItem());
    }
}
