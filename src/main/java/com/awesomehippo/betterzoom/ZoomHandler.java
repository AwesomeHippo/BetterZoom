package com.awesomehippo.betterzoom;

import com.awesomehippo.betterzoom.config.Config;
import com.awesomehippo.betterzoom.config.ConfigScreen;
import com.awesomehippo.betterzoom.keybinds.Keybinds;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;
import net.neoforged.neoforge.event.GameShuttingDownEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber(modid = BetterZoom.MOD_ID, value = Dist.CLIENT)
public class ZoomHandler {
    private static final Minecraft mc = Minecraft.getInstance();
    private static boolean isZooming = false;
    private static float zoomFOV = 30.0f; // default zoom fov
    private static float smoothedZoomFactor = 1f; // smooth zoom level (1 is no zoom by default)
    private static float normalSensitivity;
    private static float currentSensitivity;
    private static boolean normalBobView;
    private static boolean initialized = false;
    private static boolean wasZoomKeyDown = false;
    private static boolean isUnzoomTransition = false;
    private static boolean prevIsZooming = false;

    // check ifs using any item (which sould cover vanilla spyglass and modded items)
    private static boolean isUsingItem() {
        return mc.player != null && mc.player.isUsingItem();
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {

        if (!initialized && mc.options != null) {
            normalSensitivity = mc.options.sensitivity().get().floatValue();
            currentSensitivity = normalSensitivity;
            normalBobView = mc.options.bobView().get();
            initialized = true;
        }

        if (mc.screen == null && Keybinds.CONFIG_KEY.consumeClick()) {
            mc.setScreen(new ConfigScreen(null));
        }

        boolean zoomKeyDown = Keybinds.ZOOM_KEY.isDown();
        if (Config.HOLD_TO_ZOOM.get()) {
            isZooming = zoomKeyDown;
        }

        if (!Config.HOLD_TO_ZOOM.get()) {
            if (zoomKeyDown && !wasZoomKeyDown) {
                isZooming = !isZooming;
            }
        }
        wasZoomKeyDown = zoomKeyDown;

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

        // unzoom start...
        if (prevIsZooming && !isZooming && Config.SMOOTH_ZOOM.get()) {
            isUnzoomTransition = true;
        }
        prevIsZooming = isZooming;
    }

    @SubscribeEvent
    public static void onFOVChange(ViewportEvent.ComputeFov event) {
        if (isUsingItem() && !isZooming) {
            return; // skip to make it work with spyglass and other modded items like that
        }

        float baseFov = (float) event.getFOV();
        float playerFov = mc.options.fov().get().floatValue();
        float targetZoomFactor = isZooming ? Math.clamp(zoomFOV / playerFov, 0.01f, 1f) : 1f;

        // smooth the zoom or not
        if (Config.SMOOTH_ZOOM.get()) {
            float easing = Config.SMOOTH_EASING_FACTOR.get().floatValue();
            float easedAmount = easing * easing * (3f - 2f * easing); // (ease-out)
            smoothedZoomFactor += (targetZoomFactor - smoothedZoomFactor) * easedAmount;
        } else {
            smoothedZoomFactor = targetZoomFactor;
        }

        // apply zoom to the fov
        float finalFov = baseFov * smoothedZoomFactor;
        event.setFOV(finalFov);

        if (Config.DISABLE_BOBBING_WHILE_ZOOMING.get()) {
            if (isZooming && mc.options.bobView().get()) {
                mc.options.bobView().set(false);
            } else if (!isZooming && !mc.options.bobView().get() && normalBobView) {
                mc.options.bobView().set(true);
            }
        } else {
            if (isZooming && !mc.options.bobView().get()) {
                mc.options.bobView().set(true);
            }
        }

        // set target mouse sensitivity for zoom
        float targetSensitivity= normalSensitivity;
        if (isZooming) {
            targetSensitivity *= Config.AUTO_ADJUST_SENSITIVITY.get() ? smoothedZoomFactor : Config.ZOOM_SENSITIVITY_MULTIPLIER.get().floatValue();
        }

        if (Config.SMOOTH_ZOOM.get()) {
            // not really necessary except if it's zooming while moving mouse a lot
            float easing = Config.SMOOTH_EASING_FACTOR.get().floatValue();
            float easedAmount = easing * easing * (3f - 2f * easing);
            currentSensitivity += (targetSensitivity - currentSensitivity) * easedAmount;
        } else {
            currentSensitivity = targetSensitivity;
        }

        if (Math.abs(currentSensitivity - mc.options.sensitivity().get().floatValue()) > 1e-4f) {
            mc.options.sensitivity().set((double) currentSensitivity);
        }

        // checking if unzoom transition is complete
        if (!isZooming && isUnzoomTransition && Math.abs(smoothedZoomFactor - 1f) < 1e-3f && Math.abs(currentSensitivity - normalSensitivity) < 1e-4f) {
            isUnzoomTransition = false;
        }
    }

    // mouse wheel zooming!
    @SubscribeEvent
    public static void onMouseScroll(InputEvent.MouseScrollingEvent event) {
        if (isZooming && (Config.ZOOM_MODE.get() == Config.ZoomMode.WHEEL || Config.ZOOM_MODE.get() == Config.ZoomMode.BOTH)) {
            float normalFOV = mc.options.fov().get().floatValue();
            zoomFOV -= (float) (event.getScrollDeltaY() * Config.ZOOM_STEP.get());
            zoomFOV = Math.max(1.0f, Math.min(normalFOV, zoomFOV)); // prevent strange dezooming

            // cancel event in order to prevent the hotbar slot from changing when wheel zooming
            event.setCanceled(true);
        }
    }

    // restore correctly the changed stuff
    @SubscribeEvent
    public static void onGameShuttingDown(GameShuttingDownEvent event) {
        mc.options.sensitivity().set((double) normalSensitivity);
        mc.options.bobView().set(normalBobView);
        mc.options.save();
    }
    @SubscribeEvent
    public static void onLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        mc.options.sensitivity().set((double) normalSensitivity);
        mc.options.bobView().set(normalBobView);
        mc.options.save();
        isZooming = false;
        isUnzoomTransition = false;
    }
}