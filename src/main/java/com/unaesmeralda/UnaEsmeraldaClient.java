package com.unaesmeralda;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.MerchantScreen;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

/** Solo cliente: añade el botón "Cambiar trueques" a la ventana de comercio. */
@EventBusSubscriber(modid = UnaEsmeralda.MODID, value = Dist.CLIENT)
public class UnaEsmeraldaClient {
    private static final int BUTTON_WIDTH = 96;
    private static final int BUTTON_HEIGHT = 20;

    @SubscribeEvent
    public static void onScreenInit(ScreenEvent.Init.Post event) {
        if (!(event.getScreen() instanceof MerchantScreen screen)) {
            return;
        }

        // A la derecha de la ventana; si no cabe, se pega al borde de la pantalla.
        int x = Math.min(screen.getGuiLeft() + screen.getXSize() + 4, screen.width - BUTTON_WIDTH - 2);
        int y = screen.getGuiTop() + 10;

        Button button = Button.builder(
                        Component.literal("Cambiar trueques"),
                        pressed -> ClientPacketDistributor.sendToServer(new RerollPayload()))
                .bounds(x, y, BUTTON_WIDTH, BUTTON_HEIGHT)
                .build();
        event.addListener(button);
    }
}
