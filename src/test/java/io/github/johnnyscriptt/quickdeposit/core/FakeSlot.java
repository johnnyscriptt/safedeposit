package io.github.johnnyscriptt.quickdeposit.core;

import java.util.function.Predicate;

/** A plain in-memory {@link SlotView} for unit tests. */
final class FakeSlot implements SlotView {

    private Object key;
    private int count;
    private final int maxCount;
    private final Predicate<Object> accepts;

    private FakeSlot(Object key, int count, int maxCount, Predicate<Object> accepts) {
        this.key = key;
        this.count = count;
        this.maxCount = maxCount;
        this.accepts = accepts;
    }

    /** A slot holding {@code count} of {@code key}, stacking up to {@code max}. */
    static FakeSlot of(Object key, int count, int max) {
        return new FakeSlot(key, count, max, k -> true);
    }

    /** An empty slot able to hold stacks up to {@code max}. */
    static FakeSlot empty(int max) {
        return new FakeSlot(null, 0, max, k -> true);
    }

    /** An empty slot that refuses {@code refused} (like a shulker box slot refusing shulker boxes). */
    static FakeSlot emptyRefusing(Object refused) {
        return new FakeSlot(null, 0, 64, k -> !k.equals(refused));
    }

    @Override
    public boolean isEmpty() {
        return key == null || count <= 0;
    }

    @Override
    public Object key() {
        return key;
    }

    @Override
    public int count() {
        return count;
    }

    @Override
    public int maxCount() {
        return maxCount;
    }

    @Override
    public boolean accepts(Object key) {
        return accepts.test(key);
    }

    @Override
    public void set(Object key, int count) {
        if (key == null || count <= 0) {
            this.key = null;
            this.count = 0;
        } else {
            this.key = key;
            this.count = count;
        }
    }
}
