package com.awesomehippo.betterzoom;

import com.awesomehippo.betterzoom.config.Config;
import com.awesomehippo.betterzoom.config.ConfigScreen;
import com.awesomehippo.betterzoom.keybinds.Keybinds;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = BetterZoom.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ZoomHandler {
    private static final Minecraft mc = Minecraft.getInstance();
    private static boolean isZooming = false;
    private static float zoomFOV = 30.0f; // default zoom fov
    private static float smoothFOV; // starting at the player's default
    private static float normalSensitivity;
    private static float targetSensitivity;
    private static float currentSensitivity;
    private static float lastSetSensitivity;
    private static boolean initialized = false;
    private static boolean wasZoomKeyDown = false;

    // check ifs using any item (which sould cover vanilla spyglass and modded items)
    private static boolean isUsingItem() {
        return mc.player != null && mc.player.isUsingItem();
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        if (!initialized && mc.options != null) {
            smoothFOV = mc.options.fov().get().floatValue();
            normalSensitivity = mc.options.sensitivity().get().floatValue();
            targetSensitivity = normalSensitivity;
            currentSensitivity = targetSensitivity;
            lastSetSensitivity = currentSensitivity;
            initialized = true;
        }

        boolean zoomKeyDown = Keybinds.ZOOM_KEY.isDown();

        if (Config.HOLD_TO_ZOOM.get()) {
            isZooming = zoomKeyDown;
        }

        // allow the configscreen only, so we can test the zoom
        if (mc.screen != null && !(mc.screen instanceof ConfigScreen)) {
            return;
        }

        if (!Config.HOLD_TO_ZOOM.get()) {
            if (zoomKeyDown && !wasZoomKeyDown) {
                isZooming = !isZooming;
            }
        }
        wasZoomKeyDown = zoomKeyDown;

        // configuration GUI
        if (Keybinds.CONFIG_KEY.consumeClick()) {
            if (mc.screen instanceof ConfigScreen) {
                mc.setScreen(null);
            } else if (mc.screen == null) {
                mc.setScreen(new ConfigScreen(null));
            }
        }

        // zooming with hotkeys
        if (isZooming && (Config.ZOOM_MODE.get() == Config.ZoomMode.HOTKEYS || Config.ZOOM_MODE.get() == Config.ZoomMode.BOTH)) {
            float normalFOV = mc.options.fov().get().floatValue();
            float step = Config.ZOOM_STEP.get().floatValue();

            if (Keybinds.ZOOM_IN_KEY.isDown()) {
                zoomFOV = Math.max(1.0f, zoomFOV - step);
            }
            if (Keybinds.ZOOM_OUT_KEY.isDown()) {
                zoomFOV = Math.min(normalFOV, zoomFOV + step);
            }
        }
    }

    @SubscribeEvent
    public static void onFOVChange(ViewportEvent.ComputeFov event) {
        if (isUsingItem() && !isZooming) {
            return;
        }

        float baseFOV = mc.options.fov().get().floatValue();
        float targetFOV = isZooming ? zoomFOV : baseFOV;
        float multiplier = Config.ZOOM_SENSITIVITY_MULTIPLIER.get().floatValue();
        float ratio = Config.AUTO_ADJUST_SENSITIVITY.get() ? (targetFOV / baseFOV) : 1.0f;

        float optSensitivity = mc.options.sensitivity().get().floatValue();
        // float checking if sensitivity changed in option
        if (Math.abs(optSensitivity - lastSetSensitivity) > 1e-4f) {
            normalSensitivity = optSensitivity;
        }

        if (isZooming && Config.HOLD_TO_ZOOM.get() && !Keybinds.ZOOM_KEY.isDown()) {
            resetZoom(true);
        }

        if (isZooming) {
            targetSensitivity = normalSensitivity * (Config.AUTO_ADJUST_SENSITIVITY.get() ? ratio : multiplier);
        } else {
            targetSensitivity = normalSensitivity;
        }

        if (Config.ENABLE_SMOOTH_TRANSITION.get()) {
            // smoother transition when zooming if smooth transition enabled
            float easingFactor = 0.15f; // should be enough
            float eased = easingFactor * easingFactor * (3.0f - 2.0f * easingFactor); // (ease-out)
            smoothFOV += (targetFOV - smoothFOV) * eased;
            currentSensitivity += (targetSensitivity - currentSensitivity) * eased;
        } else {
            smoothFOV = targetFOV;
            currentSensitivity = targetSensitivity;
        }

        float currentOpt = mc.options.sensitivity().get().floatValue();
        if (Math.abs(currentSensitivity - currentOpt) > 1e-4f) {
            mc.options.sensitivity().set((double) currentSensitivity);
            lastSetSensitivity = currentSensitivity;
        }
        event.setFOV(smoothFOV);
    }

    // mouse wheel zooming!
    @SubscribeEvent
    public static void onMouseScroll(InputEvent.MouseScrollingEvent event) {
        if (isZooming && (Config.ZOOM_MODE.get() == Config.ZoomMode.WHEEL || Config.ZOOM_MODE.get() == Config.ZoomMode.BOTH)) {
            float normalFOV = mc.options.fov().get().floatValue();
            zoomFOV -= (float) (event.getScrollDelta() * Config.ZOOM_STEP.get());
            zoomFOV = Math.max(1.0f, Math.min(normalFOV, zoomFOV)); // prevent strange dezooming

            // cancel event in order to prevent the hotbar slot from changing when wheel zooming (if the config allows it)
            if (!Config.ALLOW_HOTBAR_SCROLL_WHILE_ZOOMING.get()) {
                event.setCanceled(true);
            }
        }
    }

    private static void resetZoom(boolean keepfadeout) {
        // fade in/out is handled in onFOVChange
        if (isZooming) {
            isZooming = false;
        }

        /*
        actually reset sensitivity and FOV values
        it kills the fade out though. TODO: add a smooth mode between none/fade in/fade out/both?
         */
        if (!keepfadeout) {
            float baseFOV = mc.options.fov().get().floatValue();
            smoothFOV = baseFOV;
            currentSensitivity = normalSensitivity;
            mc.options.sensitivity().set((double) normalSensitivity);
            lastSetSensitivity = normalSensitivity;
        }
    }

    @SubscribeEvent
    public static void onLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        // special case on logout: reset the zoom instantly
        resetZoom(false);
    }
}