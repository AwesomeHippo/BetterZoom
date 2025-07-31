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
    private static float smoothFOV = mc.options.fov().get().floatValue(); // starting at the player's default
    private static float normalSensitivity = mc.options.sensitivity().get().floatValue();
    private static float targetSensitivity = normalSensitivity;
    private static float currentSensitivity = targetSensitivity;

    @SubscribeEvent
    public static void onKeyPress(InputEvent.Key event) {
        int key = event.getKey();
        int action = event.getAction();

        if (key == Keybinds.ZOOM_KEY.getKey().getValue()) {
            if (Config.HOLD_TO_ZOOM) {
                if (action == GLFW.GLFW_PRESS) {
                    isZooming = true;
                } else if (action == GLFW.GLFW_RELEASE) {
                    resetZoom();
                }
            } else {
                if (action == GLFW.GLFW_PRESS && !keyPressed) {
                    isZooming = !isZooming;
                    keyPressed = true;
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
        float baseFOV = mc.options.fov().get().floatValue();
        float targetFOV = isZooming ? zoomFOV : baseFOV;

        float optSensitivity = mc.options.sensitivity().get().floatValue();
        // float checking if sensitivity changed in option
        if (Math.abs(optSensitivity - currentSensitivity) > 1e-4f) {
            if (isZooming) {
                normalSensitivity = optSensitivity / Config.ZOOM_SENSITIVITY_MULTIPLIER;
            } else {
                normalSensitivity = optSensitivity;
            }
        }

        if (isZooming && Config.HOLD_TO_ZOOM && !Keybinds.ZOOM_KEY.isDown()) {
            resetZoom();
        }
        targetSensitivity = isZooming ? normalSensitivity * Config.ZOOM_SENSITIVITY_MULTIPLIER : normalSensitivity;

        if (Config.ENABLE_SMOOTH_TRANSITION) {
            // smoother transition when zooming if smooth transition enabled
            float easingFactor = 0.15f; // should be enough
            float eased = easingFactor * easingFactor * (3.0f - 2.0f * easingFactor); // (ease-out)
            smoothFOV += (targetFOV - smoothFOV) * eased;
            currentSensitivity += (targetSensitivity - currentSensitivity) * easingFactor;
        } else {
            smoothFOV = targetFOV;
            currentSensitivity = targetSensitivity;
        }

        mc.options.sensitivity().set((double) currentSensitivity);
        event.setFOV(smoothFOV);
    }

    // mouse wheel zooming!
    @SubscribeEvent
    public static void onMouseScroll(InputEvent.MouseScrollingEvent event) {
        if (isZooming) {
            float normalFOV = mc.options.fov().get().floatValue();
            zoomFOV -= (float) (event.getScrollDelta() * Config.ZOOM_STEP);
            zoomFOV = Math.max(1.0f, Math.min(normalFOV, zoomFOV)); // prevent strange dezooming
            event.setCanceled(true);
        }
    }

    private static void resetZoom() {
        if (isZooming) {
            isZooming = false;
        }
    }

    @SubscribeEvent
    public static void onLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        resetZoom();
    }
}