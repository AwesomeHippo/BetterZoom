package com.awesomehippo.betterzoom;

import com.awesomehippo.betterzoom.config.Config;
import com.awesomehippo.betterzoom.config.ConfigScreen;
import com.awesomehippo.betterzoom.keybinds.Keybinds;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.event.GameShuttingDownEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import static net.minecraft.util.Mth.clamp;

@Mod.EventBusSubscriber(modid = BetterZoom.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ZoomHandler {
    private static final Minecraft mc = Minecraft.getInstance();
    private static boolean isZooming = false;
    private static float zoomFOV = 30.0f; // default zoom fov
    private static float smoothedZoomFactor = 1f; // smooth zoom level (1 is no zoom by default)
    private static float normalSensitivity;
    private static boolean normalBobView;
    private static boolean initialized = false;
    private static boolean wasZoomKeyDown = false;
    private static boolean isUnzoomTransition = false;
    private static boolean prevIsZooming = false;
    private static boolean sensitivityModified = false; // track if we modified the original sensitivity

    // check ifs using any item (which sould cover vanilla spyglass and modded items)
    private static boolean isUsingItem() {
        return mc.player != null && mc.player.isUsingItem();
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        if (!initialized && mc.options != null) {
            normalSensitivity = mc.options.sensitivity().get().floatValue();
            normalBobView = mc.options.bobView().get();
            initialized = true;
        }

        // refresh the options correctly
        if (!isZooming && !isUnzoomTransition && mc.options != null) {
            float actual = mc.options.sensitivity().get().floatValue();
            if (Math.abs(actual - normalSensitivity) > 1e-6f) {
                normalSensitivity = actual;
                sensitivityModified = false;
            }
            boolean actualBob = mc.options.bobView().get();
            if (actualBob != normalBobView) {
                normalBobView = actualBob;
            }
        }

        if (mc.screen != null) return;

        if (Keybinds.CONFIG_KEY.consumeClick()) {
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
        float targetZoomFactor = isZooming ? clamp(zoomFOV / playerFov, 0.01f, 1f) : 1f;

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
        if (isZooming) {
            float targetSensitivity = normalSensitivity * (Config.AUTO_ADJUST_SENSITIVITY.get() ? smoothedZoomFactor : Config.ZOOM_SENSITIVITY_MULTIPLIER.get().floatValue());
            // check if it's different
            if (Math.abs(mc.options.sensitivity().get().floatValue() - targetSensitivity) > 1e-6f) {
                mc.options.sensitivity().set((double) targetSensitivity);
                sensitivityModified = true;
            } else {
                // already good
                sensitivityModified = sensitivityModified;
            }
        } else if (sensitivityModified) {
            if (Math.abs(mc.options.sensitivity().get().floatValue() - normalSensitivity) > 1e-6f) {
                mc.options.sensitivity().set((double) normalSensitivity);
            }
            sensitivityModified = false;
        }

        // checking if unzoom transition is complete
        if (!isZooming && isUnzoomTransition && Math.abs(smoothedZoomFactor - 1f) < 1e-3f) {
            isUnzoomTransition = false;
        }
    }

    // mouse wheel zooming!
    @SubscribeEvent
    public static void onMouseScroll(InputEvent.MouseScrollingEvent event) {
        if (isZooming && (Config.ZOOM_MODE.get() == Config.ZoomMode.WHEEL || Config.ZOOM_MODE.get() == Config.ZoomMode.BOTH)) {
            float normalFOV = mc.options.fov().get().floatValue();
            zoomFOV -= (float) (event.getScrollDelta() * Config.ZOOM_STEP.get());
            zoomFOV = Math.max(1.0f, Math.min(normalFOV, zoomFOV)); // prevent strange dezooming

            // cancel event in order to prevent the hotbar slot from changing when wheel zooming
            event.setCanceled(true);
        }
    }

    // restore correctly the changed stuff
    @SubscribeEvent
    public static void onGameShuttingDown(GameShuttingDownEvent event) {
        restoreModifiedOptions();
        isZooming = false;
        isUnzoomTransition = false;
    }
    @SubscribeEvent
    public static void onLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        restoreModifiedOptions();
        isZooming = false;
        isUnzoomTransition = false;
    }

    // restore the actually modified options by us
    private static void restoreModifiedOptions() {
        if (sensitivityModified || Math.abs(mc.options.sensitivity().get() - normalSensitivity) > 1e-6f) {
            mc.options.sensitivity().set((double) normalSensitivity);
        }
        if (mc.options.bobView().get() != normalBobView) {
            mc.options.bobView().set(normalBobView);
        }
        mc.options.save();
        sensitivityModified = false;
    }

}
