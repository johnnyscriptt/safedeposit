package io.github.johnnyscriptt.safedeposit.server;

import io.github.johnnyscriptt.safedeposit.core.DepositRules;
import io.github.johnnyscriptt.safedeposit.core.InventorySlot;
import io.github.johnnyscriptt.safedeposit.core.LockedSlots;
import io.github.johnnyscriptt.safedeposit.core.SafeDeposit;
import io.github.johnnyscriptt.safedeposit.core.SlotView;
import io.github.johnnyscriptt.safedeposit.net.SafeDepositPayload;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.ShulkerBoxMenu;
import net.minecraft.world.inventory.Slot;

/**
 * Server-authoritative deposit: checks the player's open container, splits its slots into
 * container and player inventory, runs {@link SafeDeposit} and syncs the result. Runs on the
 * server thread, so the plain {@link HashMap} needs no locking.
 */
public final class DepositHandler {

    private static final Map<UUID, Long> LAST_REQUEST_TICK = new HashMap<>();

    private DepositHandler() {
    }

    public static void handle(ServerPlayer player, SafeDepositPayload payload) {
        MinecraftServer server = player.level().getServer();
        long nowTick = server == null ? 0L : server.getTickCount();
        long lastTick = LAST_REQUEST_TICK.getOrDefault(player.getUUID(), DepositRules.NEVER_REQUESTED);
        if (!DepositRules.cooldownOk(lastTick, nowTick)) {
            return;
        }
        LAST_REQUEST_TICK.put(player.getUUID(), nowTick);

        AbstractContainerMenu menu = player.containerMenu;
        // ChestMenu covers chests, double chests, barrels and ender chests.
        if (!(menu instanceof ChestMenu || menu instanceof ShulkerBoxMenu) || !menu.stillValid(player)) {
            return;
        }
        // Never deposit while something is on the cursor; it would be easy to lose track of.
        if (!menu.getCarried().isEmpty()) {
            return;
        }

        Inventory playerInv = player.getInventory();
        List<SlotView> container = new ArrayList<>();
        List<InventorySlot> inventory = new ArrayList<>();
        for (Slot slot : menu.slots) {
            if (slot.container == playerInv) {
                int index = slot.getContainerSlot();
                if (index >= 0 && index < LockedSlots.SLOT_COUNT && slot.mayPickup(player)) {
                    inventory.add(new InventorySlot(index, new SlotViewAdapter(slot)));
                }
            } else {
                container.add(new SlotViewAdapter(slot));
            }
        }

        SafeDeposit.run(container, inventory, LockedSlots.fromLong(payload.lockedMask()));
        menu.broadcastChanges();
    }

    /** Drops a departing player's rate-limit entry so the map doesn't grow forever. */
    public static void forget(ServerPlayer player) {
        LAST_REQUEST_TICK.remove(player.getUUID());
    }
}
