package io.github.johnnyscriptt.quickdeposit.core;

import java.util.List;

/**
 * "Quick Deposit": moves everything from the player's unlocked inventory slots into the
 * chest. Each stack first tops up matching, non-full chest stacks, then starts new stacks in
 * empty chest slots that accept it. Whatever doesn't fit stays where it was.
 */
public final class QuickDeposit {

    private QuickDeposit() {
    }

    /**
     * Deposits into the chest. Both sides are mutated in place.
     *
     * @return the number of items moved
     */
    public static int run(List<SlotView> chest, List<InventorySlot> player, LockedSlots locks) {
        int moved = 0;
        for (InventorySlot slot : player) {
            SlotView p = slot.view();
            if (p.isEmpty() || locks.isLocked(slot.index())) {
                continue;
            }
            moved += topUp(chest, p);
            moved += fillEmpty(chest, p);
        }
        return moved;
    }

    /** Pass A: top up existing, matching, non-full stacks. */
    private static int topUp(List<SlotView> chest, SlotView p) {
        int moved = 0;
        for (SlotView c : chest) {
            if (p.isEmpty()) {
                break;
            }
            if (c.isEmpty() || !c.key().equals(p.key())) {
                continue;
            }
            int n = Math.min(c.maxCount() - c.count(), p.count());
            if (n <= 0) {
                continue;
            }
            c.set(c.key(), c.count() + n);
            p.set(p.key(), p.count() - n);
            moved += n;
        }
        return moved;
    }

    /** Pass B: overflow into empty slots that accept the item, starting new stacks. */
    private static int fillEmpty(List<SlotView> chest, SlotView p) {
        int moved = 0;
        for (SlotView c : chest) {
            if (p.isEmpty()) {
                break;
            }
            if (!c.isEmpty() || !c.accepts(p.key())) {
                continue;
            }
            int n = Math.min(p.maxCount(), p.count());
            c.set(p.key(), n);
            p.set(p.key(), p.count() - n);
            moved += n;
        }
        return moved;
    }
}
