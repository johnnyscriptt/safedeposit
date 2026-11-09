package io.github.johnnyscriptt.quickdeposit.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class QuickDepositTest {

    private static List<SlotView> emptyChest(int size) {
        List<SlotView> chest = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            chest.add(FakeSlot.empty(64));
        }
        return chest;
    }

    private static List<InventorySlot> inventory(FakeSlot... slots) {
        List<InventorySlot> list = new ArrayList<>();
        for (int i = 0; i < slots.length; i++) {
            list.add(new InventorySlot(i, slots[i]));
        }
        return list;
    }

    @Test
    void depositsEveryUnlockedSlot() {
        List<SlotView> chest = emptyChest(27);
        FakeSlot stone = FakeSlot.of("stone", 64, 64);
        FakeSlot dirt = FakeSlot.of("dirt", 12, 64);

        QuickDeposit.run(chest, inventory(stone, dirt), LockedSlots.NONE);

        assertTrue(stone.isEmpty());
        assertTrue(dirt.isEmpty());
        assertEquals("stone", chest.get(0).key());
        assertEquals(64, chest.get(0).count());
        assertEquals("dirt", chest.get(1).key());
        assertEquals(12, chest.get(1).count());
    }

    @Test
    void leavesLockedSlotsAlone() {
        List<SlotView> chest = emptyChest(27);
        FakeSlot sword = FakeSlot.of("sword", 1, 1);
        FakeSlot stone = FakeSlot.of("stone", 64, 64);
        FakeSlot bread = FakeSlot.of("bread", 20, 64);

        QuickDeposit.run(chest, inventory(sword, stone, bread), LockedSlots.NONE.toggle(0).toggle(2));

        assertEquals("sword", sword.key());
        assertEquals(1, sword.count());
        assertEquals("bread", bread.key());
        assertEquals(20, bread.count());
        assertTrue(stone.isEmpty());
        assertEquals("stone", chest.get(0).key());
        assertTrue(chest.get(1).isEmpty());
    }

    @Test
    void lockFollowsInventoryIndexNotListPosition() {
        List<SlotView> chest = emptyChest(27);
        FakeSlot hotbar = FakeSlot.of("pickaxe", 1, 1);
        FakeSlot main = FakeSlot.of("cobble", 40, 64);
        List<InventorySlot> inv = List.of(new InventorySlot(9, main), new InventorySlot(0, hotbar));

        QuickDeposit.run(chest, inv, LockedSlots.NONE.toggle(0));

        assertEquals("pickaxe", hotbar.key());
        assertTrue(main.isEmpty());
    }

    @Test
    void topsUpExistingStacksBeforeStartingNewOnes() {
        List<SlotView> chest = new ArrayList<>();
        chest.add(FakeSlot.empty(64));
        chest.add(FakeSlot.of("stone", 60, 64));
        chest.add(FakeSlot.empty(64));
        FakeSlot stone = FakeSlot.of("stone", 10, 64);

        QuickDeposit.run(chest, inventory(stone), LockedSlots.NONE);

        assertEquals(64, chest.get(1).count());
        assertEquals("stone", chest.get(0).key());
        assertEquals(6, chest.get(0).count());
        assertTrue(chest.get(2).isEmpty());
        assertTrue(stone.isEmpty());
    }

    @Test
    void respectsSlotsThatRefuseAnItem() {
        List<SlotView> chest = new ArrayList<>();
        chest.add(FakeSlot.emptyRefusing("shulker_box"));
        FakeSlot shulker = FakeSlot.of("shulker_box", 1, 1);
        FakeSlot stone = FakeSlot.of("stone", 5, 64);

        QuickDeposit.run(chest, inventory(shulker, stone), LockedSlots.NONE);

        assertEquals("shulker_box", shulker.key());
        assertEquals("stone", chest.get(0).key());
        assertTrue(stone.isEmpty());
    }

    @Test
    void fullChestLeavesTheRemainderInTheInventory() {
        List<SlotView> chest = new ArrayList<>();
        chest.add(FakeSlot.of("stone", 50, 64));
        chest.add(FakeSlot.of("dirt", 64, 64));
        FakeSlot stone = FakeSlot.of("stone", 30, 64);
        FakeSlot gravel = FakeSlot.of("gravel", 8, 64);

        QuickDeposit.run(chest, inventory(stone, gravel), LockedSlots.NONE);

        assertEquals(64, chest.get(0).count());
        assertEquals(16, stone.count());
        assertEquals(8, gravel.count());
    }

    @Test
    void honoursPerItemStackLimits() {
        // Fake slots carry their own limit; real ones read it from the item (pearls stack to 16).
        List<SlotView> chest = new ArrayList<>(List.of(FakeSlot.empty(16), FakeSlot.empty(16), FakeSlot.empty(16)));
        FakeSlot pearls = FakeSlot.of("ender_pearl", 16, 16);
        FakeSlot morePearls = FakeSlot.of("ender_pearl", 10, 16);
        FakeSlot bucket = FakeSlot.of("water_bucket", 1, 1);

        QuickDeposit.run(chest, inventory(pearls, morePearls, bucket), LockedSlots.NONE);

        assertEquals(16, chest.get(0).count());
        assertEquals("ender_pearl", chest.get(1).key());
        assertEquals(10, chest.get(1).count());
        assertEquals("water_bucket", chest.get(2).key());
        assertTrue(pearls.isEmpty());
        assertTrue(morePearls.isEmpty());
        assertTrue(bucket.isEmpty());
    }

    @Test
    void reportsHowManyItemsMoved() {
        List<SlotView> chest = emptyChest(27);
        int moved = QuickDeposit.run(chest,
                inventory(FakeSlot.of("stone", 64, 64), FakeSlot.of("dirt", 3, 64)),
                LockedSlots.NONE.toggle(1));
        assertEquals(64, moved);
    }

    @Test
    void emptySlotsAreSkipped() {
        List<SlotView> chest = emptyChest(2);
        QuickDeposit.run(chest, inventory(FakeSlot.empty(64)), LockedSlots.NONE);
        assertNull(chest.get(0).key());
    }
}
