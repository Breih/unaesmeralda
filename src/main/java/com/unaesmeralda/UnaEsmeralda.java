package com.unaesmeralda;

import java.util.List;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.npc.villager.Villager;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

/**
 * Aldeanos a 1 esmeralda: precios mínimos, intercambios infinitos y botón para
 * cambiar los trueques sin romper la mesa de trabajo.
 *
 * Esta clase solo aplica periódicamente los ajustes a los aldeanos que tienen un
 * jugador cerca. La lógica está en {@link TradeTools}.
 */
@Mod(UnaEsmeralda.MODID)
@EventBusSubscriber(modid = UnaEsmeralda.MODID)
public class UnaEsmeralda {
    public static final String MODID = "unaesmeralda";

    /** Cada cuántos ticks se reaplican los ajustes (4 ticks = 0,2 s). */
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
                TradeTools.apply(villager);
            }
        }
    }
}
