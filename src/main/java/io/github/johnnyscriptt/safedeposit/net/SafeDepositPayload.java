package io.github.johnnyscriptt.safedeposit.net;

import io.github.johnnyscriptt.safedeposit.SafeDepositMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/**
 * Client to server: "deposit my inventory into the container I have open, except these
 * slots". {@code lockedMask} is a {@code LockedSlots} bit mask over inventory indices 0..35;
 * the server strips any other bits, so nothing here is trusted.
 */
public record SafeDepositPayload(long lockedMask) implements CustomPacketPayload {

    public static final Type<SafeDepositPayload> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(SafeDepositMod.MOD_ID, "deposit"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SafeDepositPayload> CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.LONG, SafeDepositPayload::lockedMask,
                    SafeDepositPayload::new);

    @Override
    public Type<SafeDepositPayload> type() {
        return TYPE;
    }
}
