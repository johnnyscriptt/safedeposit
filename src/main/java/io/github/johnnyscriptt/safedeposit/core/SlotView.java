package io.github.johnnyscriptt.safedeposit.core;

/**
 * A mutable view over a single inventory slot, abstracted away from Minecraft's
 * {@code ItemStack} so the deposit logic can be unit-tested without launching the game.
 *
 * <p>{@link #key()} is the stacking identity: two slots hold combinable items when both
 * keys are non-null and {@code equals}. A {@code null} key means the slot is empty.
 */
public interface SlotView {

    /** True when this slot holds nothing that can be moved. */
    boolean isEmpty();

    /** Stacking identity (item + relevant data components), or {@code null} when empty. */
    Object key();

    /** How many items are currently in this slot. */
    int count();

    /** Maximum stack size for the item keyed here (per item, e.g. 64, 16 or 1). */
    int maxCount();

    /** Whether this slot may hold the item keyed by {@code key} (shulker boxes refuse shulker boxes). */
    boolean accepts(Object key);

    /** Overwrite the slot. A {@code null} key (or {@code count <= 0}) empties it. */
    void set(Object key, int count);
}
