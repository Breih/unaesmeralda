package com.unaesmeralda;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

/** Registro del paquete de red del botón. Es opcional: quien no tenga el mod puede conectarse igual. */
@EventBusSubscriber(modid = UnaEsmeralda.MODID)
public class Network {

    @SubscribeEvent
    public static void onRegisterPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1").optional();
        registrar.playToServer(RerollPayload.TYPE, RerollPayload.CODEC, Network::onReroll);
    }

    private static void onReroll(RerollPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                TradeTools.reroll(player);
            }
        });
    }
}
