package io.github.johnnyscriptt.quickdeposit.net;

import io.github.johnnyscriptt.quickdeposit.QuickDepositMod;
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
public record QuickDepositPayload(long lockedMask) implements CustomPacketPayload {

    public static final Type<QuickDepositPayload> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(QuickDepositMod.MOD_ID, "deposit"));

    public static final StreamCodec<RegistryFriendlyByteBuf, QuickDepositPayload> CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.LONG, QuickDepositPayload::lockedMask,
                    QuickDepositPayload::new);

    @Override
    public Type<QuickDepositPayload> type() {
        return TYPE;
    }
}
