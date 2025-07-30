package com.awesomehippo.betterzoom;

import com.awesomehippo.betterzoom.config.Config;
import com.awesomehippo.betterzoom.config.ConfigScreen;
import com.awesomehippo.betterzoom.keybinds.Keybinds;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(modid = BetterZoom.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ZoomHandler {
    private static final Minecraft mc = Minecraft.getInstance();
    private static boolean isZooming = false;
    private static boolean keyPressed = false;
    private static float zoomFOV = 30.0f; // default zoom fov
    private static float smoothFOV = mc.options.fov().get();
    private static final float NORMAL_SENSITIVITY = mc.options.sensitivity().get().floatValue();

    @SubscribeEvent
    public static void onKeyPress(InputEvent.Key event) {
        int key = event.getKey();
        int action = event.getAction();

        if (key == Keybinds.ZOOM_KEY.getKey().getValue()) {
            if (Config.HOLD_TO_ZOOM) {
                if (action == GLFW.GLFW_PRESS) {
                    isZooming = true;
                    mc.options.sensitivity().set((double) (NORMAL_SENSITIVITY * Config.ZOOM_SENSITIVITY_MULTIPLIER));
                } else if (action == GLFW.GLFW_RELEASE) {
                    resetZoom();
                }
            } else {
                if (action == GLFW.GLFW_PRESS && !keyPressed) {
                    isZooming = !isZooming;
                    keyPressed = true;
                    mc.options.sensitivity().set((double) (isZooming ? NORMAL_SENSITIVITY * Config.ZOOM_SENSITIVITY_MULTIPLIER : NORMAL_SENSITIVITY));
                } else if (action == GLFW.GLFW_RELEASE) {
                    keyPressed = false;
                }
            }
        }

        if (action == GLFW.GLFW_PRESS && key == Keybinds.CONFIG_KEY.getKey().getValue()) {
            mc.setScreen(new ConfigScreen(mc.screen));
        }
    }

    @SubscribeEvent
    public static void onFOVChange(ViewportEvent.ComputeFov event) {
        float currentFOV = mc.options.fov().get();
        if (isZooming) {
            smoothFOV += (zoomFOV - smoothFOV) * 0.2f;
        } else {
            smoothFOV += (currentFOV - smoothFOV) * 0.2f;
        }
        event.setFOV(smoothFOV);
    }

    // mouse wheel zooming!
    @SubscribeEvent
    public static void onMouseScroll(InputEvent.MouseScrollingEvent event) {
        if (isZooming) {
            float normalFOV = mc.options.fov().get().floatValue(); // would use smoothFOV but it gets modified actually
            zoomFOV -= (float) (event.getScrollDelta() * Config.ZOOM_STEP);
            zoomFOV = Math.max(1.0f, Math.min(normalFOV, zoomFOV)); // in order to prevent some strange dezooming
            event.setCanceled(true);
        }
    }

    private static void resetZoom() {
        if (isZooming) {
            isZooming = false;
            mc.options.sensitivity().set((double) NORMAL_SENSITIVITY);
        }
    }

    @SubscribeEvent
    public static void onLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        resetZoom();
    }
}