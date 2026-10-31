package com.awesomehippo.betterzoom;

import com.awesomehippo.betterzoom.config.Config;
import com.awesomehippo.betterzoom.config.ConfigScreen;
import com.awesomehippo.betterzoom.keybinds.Keybinds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.client.event.RenderHandEvent;
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
    private static float smoothedZoomFactor = 1.0f; // smooth zoom level (1 is no zoom by default)
    private static float unzoomStartFactor = 1.0f;
    private static float normalSensitivity;
    private static boolean normalBobView;
    private static boolean normalSmoothCamera;
    private static boolean initialized = false;
    private static boolean wasZoomKeyDown = false;
    private static boolean isUnzoomTransition = false;
    private static boolean prevIsZooming = false;
    private static boolean sensitivityModified = false; // track if we modified the original sensitivity
    private static boolean bobbingModified = false;
    private static boolean cinematicModified = false;
    private static boolean needsSave = false;
    private static boolean zoomSoundActive = false;
    private static float barProgress = 0.0f;
    private static boolean hintShown = false;
    private static boolean hintVisible = false;

    // check ifs using any item (which sould cover vanilla spyglass and modded items)
    private static boolean isUsingItem() {
        return mc.player != null && mc.player.isUsingItem();
    }

    private static boolean isZoomActive() {
        return isZooming || isUnzoomTransition;
    }

    private static boolean hasOptionsModified() {
        return sensitivityModified || bobbingModified || cinematicModified;
    }

    private static boolean shouldModifyOptions() {
        return isZoomActive() && mc.screen == null;
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        if (!initialized && mc.options != null) {
            normalSensitivity = mc.options.sensitivity().get().floatValue();
            normalBobView = mc.options.bobView().get();
            normalSmoothCamera = mc.options.smoothCamera;
            initialized = true;
        }

        if (mc.screen != null) {
            if (mc.screen instanceof ConfigScreen configScreen) {
                isZooming = configScreen.isPreviewing();
            } else if (Config.HOLD_TO_ZOOM.get() && isZooming) {
                isZooming = false;
            }
            if (prevIsZooming && !isZooming && Config.SMOOTH_ZOOM.get()) {
                unzoomStartFactor = smoothedZoomFactor;
                isUnzoomTransition = true;
            }
            updateZoomSound();
            prevIsZooming = isZooming;
        } else {
            if (Keybinds.CONFIG_KEY.consumeClick()) {
                mc.setScreen(new ConfigScreen(null));
            }

            boolean zoomKeyDown = Keybinds.ZOOM_KEY.isDown();
            if (Config.HOLD_TO_ZOOM.get()) {
                isZooming = zoomKeyDown;
            } else if (zoomKeyDown && !wasZoomKeyDown) {
                isZooming = !isZooming;
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
                unzoomStartFactor = smoothedZoomFactor;
                isUnzoomTransition = true;
            }
            // on first zoom of each join, notice where is the config screen
            if (!prevIsZooming && isZooming && !hintShown && Config.SETTINGS_HINT.get() && !Keybinds.CONFIG_KEY.isUnbound()) {
                hintShown = true;
                hintVisible = true;
            }
            updateZoomSound();
            prevIsZooming = isZooming;
        }

        if (!isZooming) {
            hintVisible = false;
        }

        // checking if unzoom transition is complete
        if (!isZooming && isUnzoomTransition && Math.abs(smoothedZoomFactor - 1.0f) < 1e-3f) {
            smoothedZoomFactor = 1.0f;
            isUnzoomTransition = false;
        }

        if (isZoomActive() || hasOptionsModified()) {
            updateBobbing();
            updateCinematic();
            updateSensitivity();
        }

        // refresh the options correctly
        if (!hasOptionsModified() && mc.options != null) {
            float actual = mc.options.sensitivity().get().floatValue();
            if (Math.abs(actual - normalSensitivity) > 1e-6f) {
                normalSensitivity = actual;
            }
            boolean actualBob = mc.options.bobView().get();
            if (actualBob != normalBobView) {
                normalBobView = actualBob;
            }
            if (mc.options.smoothCamera != normalSmoothCamera) {
                normalSmoothCamera = mc.options.smoothCamera;
            }
        }
    }

    @SubscribeEvent
    public static void onFOVChange(ViewportEvent.ComputeFov event) {
        if (mc.options == null) return;

        if (isUsingItem() && !isZooming && !isUnzoomTransition) {
            return; // skip to make it work with spyglass and other modded items like that
        }

        if (!isZoomActive() && Math.abs(smoothedZoomFactor - 1.0f) < 1e-4f) {
            return;
        }

        float baseFov = (float) event.getFOV();
        float playerFov = Math.max(mc.options.fov().get().floatValue(), 1.0f);
        float targetZoomFactor = isZooming ? clamp(zoomFOV / playerFov, 0.01f, 1.0f) : 1.0f;

        // smooth the zoom or not
        if (Config.SMOOTH_ZOOM.get()) {
            float easing = Config.SMOOTH_EASING_FACTOR.get().floatValue();
            float dt = Math.max(mc.getDeltaFrameTime(), 0f);
            float alpha = 1.0f - (float) Math.pow(1.0f - easing, dt);
            alpha = clamp(alpha, 0f, 1.0f);
            smoothedZoomFactor += (targetZoomFactor - smoothedZoomFactor) * alpha;
        } else {
            smoothedZoomFactor = targetZoomFactor;
        }

        if (Math.abs(smoothedZoomFactor - targetZoomFactor) < 1e-4f) {
            smoothedZoomFactor = targetZoomFactor;
        }

        // apply zoom to the fov
        event.setFOV(baseFov * smoothedZoomFactor);

        // checking if unzoom transition is complete
        if (!isZooming && isUnzoomTransition && Math.abs(smoothedZoomFactor - 1.0f) < 1e-3f) {
            smoothedZoomFactor = 1.0f;
            isUnzoomTransition = false;
        }
    }

    private static void updateZoomSound() {
        if (mc.player == null || isZooming == prevIsZooming) return;
        if (isZooming) {
            if (!Config.ZOOM_SOUND.get()) return;
            mc.player.playSound(SoundEvents.SPYGLASS_USE, 1.0F, 1.0F);
            zoomSoundActive = true;
        } else if (zoomSoundActive) {
            zoomSoundActive = false;
            if (!Config.ZOOM_SOUND.get()) return;
            mc.player.playSound(SoundEvents.SPYGLASS_STOP_USING, 1.0F, 1.0F);
        }
    }

    private static void updateBobbing() {
        if (mc.options == null) return;

        if (!Config.DISABLE_BOBBING_WHILE_ZOOMING.get()) {
            if (bobbingModified) {
                if (mc.options.bobView().get() != normalBobView) {
                    mc.options.bobView().set(normalBobView);
                }
                bobbingModified = false;
            }
            return;
        }

        if (shouldModifyOptions()) {
            if (mc.options.bobView().get()) {
                normalBobView = true;
                mc.options.bobView().set(false);
                bobbingModified = true;
                needsSave = true;
            }
        } else if (bobbingModified) {
            if (mc.options.bobView().get() != normalBobView) {
                mc.options.bobView().set(normalBobView);
            }
            bobbingModified = false;
        }
    }

    private static void updateCinematic() {
        if (mc.options == null) return;

        if (!Config.CINEMATIC_CAMERA.get()) {
            if (cinematicModified) {
                mc.options.smoothCamera = normalSmoothCamera;
                cinematicModified = false;
            }
            return;
        }

        if (shouldModifyOptions()) {
            if (!mc.options.smoothCamera) {
                normalSmoothCamera = false;
                mc.options.smoothCamera = true;
                cinematicModified = true;
            }
        } else if (cinematicModified) {
            mc.options.smoothCamera = normalSmoothCamera;
            cinematicModified = false;
        }
    }

    private static void updateSensitivity() {
        if (mc.options == null) return;

        if (shouldModifyOptions()) {
            float mult;
            if (Config.AUTO_ADJUST_SENSITIVITY.get()) {
                mult = smoothedZoomFactor;
                // special auto-adjust sensitivity for cinematic mode to prevent extra slow mouse:p
                if (Config.CINEMATIC_CAMERA.get()) {
                    mult = mult + (1.0f - mult) * 0.45f;
                }
            } else {
                float fixed = Config.ZOOM_SENSITIVITY_MULTIPLIER.get().floatValue();
                if (isZooming) {
                    mult = fixed;
                } else {
                    float start = unzoomStartFactor;
                    float progress;
                    if (start >= 1.0f - 1e-6f) {
                        progress = 1.0f;
                    } else {
                        progress = clamp((smoothedZoomFactor - start) / (1.0f - start), 0f, 1.0f);
                    }
                    mult = fixed + (1.0f - fixed) * progress;
                }
            }
            float targetSensitivity = normalSensitivity * mult;
            // check if it's different
            if (Math.abs(mc.options.sensitivity().get().floatValue() - targetSensitivity) > 1e-6f) {
                mc.options.sensitivity().set((double) targetSensitivity);
                needsSave = true;
            }
            sensitivityModified = true;
        } else if (sensitivityModified) {
            if (Math.abs(mc.options.sensitivity().get().floatValue() - normalSensitivity) > 1e-6f) {
                mc.options.sensitivity().set((double) normalSensitivity);
            }
            sensitivityModified = false;
        }
    }

    // mouse wheel zooming!
    @SubscribeEvent
    public static void onMouseScroll(InputEvent.MouseScrollingEvent event) {
        if (scrollZoom(event.getScrollDelta())) {
            // cancel event in order to prevent the hotbar slot from changing when wheel zooming
            event.setCanceled(true);
        }
    }

    public static boolean scrollZoom(double delta) {
        if (isZooming && (Config.ZOOM_MODE.get() == Config.ZoomMode.WHEEL || Config.ZOOM_MODE.get() == Config.ZoomMode.BOTH)) {
            float normalFOV = mc.options.fov().get().floatValue();
            zoomFOV -= (float) (Math.signum(delta) * Config.ZOOM_STEP.get());
            zoomFOV = Math.max(1.0f, Math.min(normalFOV, zoomFOV)); // prevent strange dezooming
            return true;
        }
        return false;
    }

    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Pre event) {
        GuiGraphics guiGraphics = event.getGuiGraphics();
        int width = guiGraphics.guiWidth();
        int height = guiGraphics.guiHeight();

        float barTarget = isZooming && Config.CINEMATIC_BARS.get() ? 1.0f : 0.0f;
        if (barTarget > 0.0f || barProgress > 0.0f) {
            if (Config.SMOOTH_ZOOM.get()) {
                float easing = Config.SMOOTH_EASING_FACTOR.get().floatValue();
                barProgress += (barTarget - barProgress) * (1.0f - (float) Math.pow(1.0f - easing, Math.max(mc.getDeltaFrameTime(), 0f)));
            } else {
                barProgress = barTarget;
            }
            if (barProgress < 0.001f) {
                barProgress = 0.0f;
            }
            int barHeight = (int) (Math.max(0.0f, (height - width / 2.39f) / 2.0f) * barProgress);
            guiGraphics.fill(0, 0, width, barHeight, 0xFF000000);
            guiGraphics.fill(0, height - barHeight, width, height, 0xFF000000);
        }

        if (Config.ZOOM_LEVEL.get() && isZoomActive() && smoothedZoomFactor < 0.999f) {
            float level = 1.0f / smoothedZoomFactor;
            guiGraphics.drawCenteredString(mc.font, String.format(level < 10.0f ? "x%.1f" : "x%.0f", level), width / 2, 10, 0xFFFFFF);
        }

        boolean hideHud = isZooming && Config.HIDE_GUI.get() && (mc.screen == null || mc.screen instanceof ConfigScreen);

        if (hintVisible) {
            guiGraphics.drawCenteredString(mc.font, Component.translatable("betterzoom.hint.settings", Keybinds.CONFIG_KEY.getTranslatedKeyMessage()), width / 2, hideHud ? height - 20 : height - 72, 0xFFFFFF);
        }

        if (hideHud) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onRenderHand(RenderHandEvent event) {
        if (isZoomActive() && Config.HIDE_HAND.get()) {
            event.setCanceled(true);
        }
    }

    // restore correctly the changed stuff
    @SubscribeEvent
    public static void onGameShuttingDown(GameShuttingDownEvent event) {
        resetZoomState();
    }

    @SubscribeEvent
    public static void onLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        resetZoomState();
    }

    private static void resetZoomState() {
        isZooming = false;
        isUnzoomTransition = false;
        prevIsZooming = false;
        zoomSoundActive = false;
        smoothedZoomFactor = 1.0f;
        barProgress = 0.0f;
        hintShown = false;
        hintVisible = false;
        restoreModifiedOptions();
    }

    // restore the actually modified options by us
    private static void restoreModifiedOptions() {
        if (mc.options == null) return;

        if (sensitivityModified) {
            if (Math.abs(mc.options.sensitivity().get().floatValue() - normalSensitivity) > 1e-6f) {
                mc.options.sensitivity().set((double) normalSensitivity);
            }
            sensitivityModified = false;
        }
        if (bobbingModified) {
            if (mc.options.bobView().get() != normalBobView) {
                mc.options.bobView().set(normalBobView);
            }
            bobbingModified = false;
        }
        if (cinematicModified) {
            mc.options.smoothCamera = normalSmoothCamera;
            cinematicModified = false;
        }
        if (needsSave) {
            mc.options.save();
            needsSave = false;
        }
    }
}
