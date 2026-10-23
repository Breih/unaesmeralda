package com.unaesmeralda;

import java.util.List;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.MerchantOffer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

/**
 * Todos los aldeanos cobran 1 esmeralda por intercambio, sin curar ni reputación.
 *
 * Funcionamiento: el precio final de una oferta es
 *   clamp(precioBase + demanda + descuentoEspecial, 1, tamañoMaxDePila)
 * Al fijar el descuentoEspecial en un valor muy negativo, el precio queda siempre
 * en el mínimo (1). Vanilla reinicia ese descuento al cerrar el comercio, así que
 * se reaplica periódicamente a los aldeanos que tienen un jugador cerca.
 */
@Mod(UnaEsmeralda.MODID)
@EventBusSubscriber(modid = UnaEsmeralda.MODID)
public class UnaEsmeralda {
    public static final String MODID = "unaesmeralda";

    /** Suficientemente negativo para que cualquier precio de esmeraldas (máx. 64) caiga a 1. */
    private static final int PRICE_DROP = -10_000;

    /** Cada cuántos ticks se reaplica el precio (4 ticks = 0,2 s). */
    private static final int INTERVAL_TICKS = 4;

    /** Radio (en bloques) alrededor de cada jugador donde se ajustan los aldeanos. */
    private static final double RADIUS = 12.0;

    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel level)) {
            return;
        }
        if (level.getGameTime() % INTERVAL_TICKS != 0) {
            return;
        }

        for (ServerPlayer player : level.players()) {
            List<Villager> villagers = level.getEntitiesOfClass(
                    Villager.class, player.getBoundingBox().inflate(RADIUS));
            for (Villager villager : villagers) {
                for (MerchantOffer offer : villager.getOffers()) {
                    // Solo se toca el pago en esmeraldas. Los pagos en otros ítems
                    // (p. ej. 20 de trigo -> 1 esmeralda) se dejan como en vanilla.
                    if (offer.getBaseCostA().is(Items.EMERALD)
                            && offer.getSpecialPriceDiff() != PRICE_DROP) {
                        offer.setSpecialPriceDiff(PRICE_DROP);
                    }
                }
            }
        }
    }
}
