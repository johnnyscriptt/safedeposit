package io.github.johnnyscriptt.safedeposit;

import io.github.johnnyscriptt.safedeposit.net.SafeDepositPayload;
import io.github.johnnyscriptt.safedeposit.server.DepositHandler;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Common entrypoint: registers the deposit payload (on both physical sides) and the server
 * receiver, so the mod works on dedicated servers and, through the integrated server, in
 * singleplayer.
 */
public final class SafeDepositMod implements ModInitializer {
    public static final String MOD_ID = "safedeposit";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        PayloadTypeRegistry.serverboundPlay().register(SafeDepositPayload.TYPE, SafeDepositPayload.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(SafeDepositPayload.TYPE,
                (payload, context) -> DepositHandler.handle(context.player(), payload));
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> DepositHandler.forget(handler.getPlayer()));
    }
}
