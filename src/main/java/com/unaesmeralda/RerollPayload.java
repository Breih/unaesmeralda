package com.unaesmeralda;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/** Paquete cliente -> servidor: "cambia los trueques del aldeano con el que estoy comerciando". */
public record RerollPayload() implements CustomPacketPayload {
    public static final Type<RerollPayload> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(UnaEsmeralda.MODID, "reroll"));

    public static final StreamCodec<RegistryFriendlyByteBuf, RerollPayload> CODEC =
            StreamCodec.unit(new RerollPayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
