package io.github.johnnyscriptt.quickdeposit.core;

/**
 * A player inventory slot paired with its inventory index (0 to 8 hotbar, 9 to 35 main),
 * which is what a lock refers to. Menu slot numbers differ per container, so locks never
 * use them.
 */
public record InventorySlot(int index, SlotView view) {
}
