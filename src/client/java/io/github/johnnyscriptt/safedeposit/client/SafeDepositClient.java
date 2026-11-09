package io.github.johnnyscriptt.safedeposit.client;

import com.mojang.blaze3d.platform.InputConstants;
import io.github.johnnyscriptt.safedeposit.client.config.ConfigIo;
import io.github.johnnyscriptt.safedeposit.client.ui.DepositButton;
import io.github.johnnyscriptt.safedeposit.client.ui.SlotLocking;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;

import static io.github.johnnyscriptt.safedeposit.SafeDepositMod.MOD_ID;

/** Client entrypoint: the lock key, the slot-lock click handler and overlay, and the button. */
public final class SafeDepositClient implements ClientModInitializer {
    private static final KeyMapping.Category CATEGORY =
            KeyMapping.Category.register(Identifier.fromNamespaceAndPath(MOD_ID, "main"));

    /** Hold this and left-click one of your inventory slots to lock or unlock it. */
    public static final KeyMapping LOCK_KEY =
            new KeyMapping("key." + MOD_ID + ".lock_slot", InputConstants.KEY_LALT, CATEGORY);

    @Override
    public void onInitializeClient() {
        KeyMappingHelper.registerKeyMapping(LOCK_KEY);
        ConfigIo.get();
        SlotLocking.register();
        DepositButton.register();
    }
}
