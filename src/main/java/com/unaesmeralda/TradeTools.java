package com.unaesmeralda;

import java.lang.reflect.Field;
import java.util.List;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.npc.villager.AbstractVillager;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;

/** Lógica del lado del servidor: precios, usos ilimitados y cambio de trueques. */
public final class TradeTools {
    private static final Logger LOGGER = LogUtils.getLogger();

    /**
     * El precio final de la primera casilla de pago de una oferta es
     * clamp(base + demanda + descuentoEspecial, 1, tamañoMaxDePila).
     * Con un descuento muy negativo queda siempre en 1 (sea esmeralda u otro ítem).
     * Vanilla reinicia este descuento al cerrar el comercio; por eso se reaplica.
     */
    private static final int PRICE_DROP = -10_000;

    /** Usos máximos de cada oferta: tan alto que nunca se bloquea. */
    private static final int UNLIMITED_USES = 1_000_000;

    private static Field maxUsesField;
    private static boolean maxUsesFailed;
    private static Field offersField;
    private static boolean offersFieldSearched;

    private TradeTools() {}

    /** Deja todas las ofertas del aldeano a precio mínimo y sin límite de usos. */
    public static void apply(Villager villager) {
        MerchantOffers offers = villager.getOffers();
        for (MerchantOffer offer : offers) {
            if (offer.getSpecialPriceDiff() != PRICE_DROP) {
                offer.setSpecialPriceDiff(PRICE_DROP);
            }
            unlockUses(offer);
        }
    }

    /** Sube el máximo de usos de la oferta (el campo es final, se cambia por reflexión). */
    private static void unlockUses(MerchantOffer offer) {
        if (maxUsesFailed || offer.getMaxUses() >= UNLIMITED_USES) {
            return;
        }
        try {
            if (maxUsesField == null) {
                Field field = MerchantOffer.class.getDeclaredField("maxUses");
                field.setAccessible(true);
                maxUsesField = field;
            }
            maxUsesField.setInt(offer, UNLIMITED_USES);
        } catch (ReflectiveOperationException | RuntimeException e) {
            maxUsesFailed = true;
            LOGGER.error("[unaesmeralda] No se pudo quitar el limite de usos de los trueques", e);
        }
    }

    /**
     * Cambia los trueques del aldeano con el que el jugador está comerciando:
     * regenera las ofertas de cada nivel que ya tiene y reabre la ventana.
     */
    public static void reroll(ServerPlayer player) {
        if (!(player.level() instanceof ServerLevel level)) {
            return;
        }
        List<Villager> candidates = level.getEntitiesOfClass(
                Villager.class,
                player.getBoundingBox().inflate(10.0),
                villager -> villager.getTradingPlayer() == player);
        if (candidates.isEmpty()) {
            return;
        }

        Villager villager = candidates.get(0);
        if (!regenerateOffers(villager)) {
            return;
        }
        apply(villager);

        // Cierra y vuelve a abrir la ventana para que el cliente reciba las ofertas nuevas.
        player.closeContainer();
        villager.mobInteract(player, InteractionHand.MAIN_HAND);
    }

    /**
     * Vacía las ofertas y las vuelve a generar nivel por nivel (1..nivel actual),
     * usando el propio generador de vanilla. Devuelve false si no se pudo.
     */
    private static boolean regenerateOffers(Villager villager) {
        Field field = findOffersField();
        if (field == null) {
            return false;
        }

        MerchantOffers backup = villager.getOffers();
        if (backup.isEmpty()) {
            return false;
        }

        var original = villager.getVillagerData();
        int maxLevel = original.level();
        MerchantOffers fresh = new MerchantOffers();
        boolean ok = false;
        try {
            for (int lvl = 1; lvl <= maxLevel; lvl++) {
                field.set(villager, null);
                villager.setVillagerData(original.withLevel(lvl));
                fresh.addAll(villager.getOffers());
            }
            ok = !fresh.isEmpty();
        } catch (ReflectiveOperationException | RuntimeException e) {
            LOGGER.error("[unaesmeralda] Error al cambiar los trueques del aldeano", e);
        } finally {
            villager.setVillagerData(original);
            try {
                field.set(villager, ok ? fresh : backup);
            } catch (ReflectiveOperationException | RuntimeException e) {
                LOGGER.error("[unaesmeralda] No se pudieron restaurar los trueques", e);
            }
        }
        return ok;
    }

    /** Busca por tipo (no por nombre) el campo que guarda las ofertas del aldeano. */
    private static Field findOffersField() {
        if (!offersFieldSearched) {
            offersFieldSearched = true;
            for (Field field : AbstractVillager.class.getDeclaredFields()) {
                if (field.getType() == MerchantOffers.class) {
                    try {
                        field.setAccessible(true);
                        offersField = field;
                    } catch (RuntimeException e) {
                        LOGGER.error("[unaesmeralda] No se pudo acceder al campo de ofertas", e);
                    }
                    break;
                }
            }
            if (offersField == null) {
                LOGGER.error("[unaesmeralda] No se encontro el campo de ofertas; el boton de cambiar trueques no funcionara");
            }
        }
        return offersField;
    }
}
