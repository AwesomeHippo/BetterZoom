package com.awesomehippo.betterzoom.config;

import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.common.ModConfigSpec;

public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final int MIN_ZOOM_INCREMENT = 1;
    public static final int MAX_ZOOM_INCREMENT = 20;
    public static final double MIN_SENSITIVITY = 0.01;
    public static final double MAX_SENSITIVITY = 1.0;
    public static final double MIN_SMOOTH_EASE = 0.05;
    public static final double MAX_SMOOTH_EASE = 0.25;

    public static final ModConfigSpec.IntValue ZOOM_STEP;
    public static final ModConfigSpec.DoubleValue ZOOM_SENSITIVITY_MULTIPLIER;
    public static final ModConfigSpec.BooleanValue HOLD_TO_ZOOM;
    public static final ModConfigSpec.DoubleValue SMOOTH_EASING_FACTOR;
    public static final ModConfigSpec.BooleanValue SMOOTH_ZOOM;
    public static final ModConfigSpec.EnumValue<ZoomMode> ZOOM_MODE;
    public static final ModConfigSpec.BooleanValue AUTO_ADJUST_SENSITIVITY;
    public static final ModConfigSpec.BooleanValue DISABLE_BOBBING_WHILE_ZOOMING;
    public static final ModConfigSpec.BooleanValue CINEMATIC_CAMERA;
    public static final ModConfigSpec.BooleanValue ZOOM_SOUND;
    public static final ModConfigSpec.BooleanValue HIDE_GUI;
    public static final ModConfigSpec.BooleanValue HIDE_HAND;
    public static final ModConfigSpec.BooleanValue CINEMATIC_BARS;
    public static final ModConfigSpec.BooleanValue ZOOM_LEVEL;
    public static final ModConfigSpec.BooleanValue SETTINGS_HINT;

    public enum ZoomMode {
        WHEEL(Component.translatable("betterzoom.config.zoommode.wheel")),
        HOTKEYS(Component.translatable("betterzoom.config.zoommode.hotkeys")),
        BOTH(Component.translatable("betterzoom.config.zoommode.both"));

        private final Component displayName;

        ZoomMode(Component displayName) {
            this.displayName = displayName;
        }

        public Component getDisplayName() {
            return displayName;
        }
    }

    static {
        BUILDER.comment("Better Zoom Configuration");

        ZOOM_STEP = BUILDER.translation("betterzoom.config.zoomstep.label").comment("The zoom increment step per zoom in/out (higher values zoom/dezoom faster)").defineInRange("zoomStep", 5, MIN_ZOOM_INCREMENT, MAX_ZOOM_INCREMENT);
        ZOOM_SENSITIVITY_MULTIPLIER = BUILDER.translation("betterzoom.config.sensitivity.label").comment("Mouse sensitivity multiplier while zooming (lower values make it less sensitive)").defineInRange("zoomSensitivity", 0.01, MIN_SENSITIVITY, MAX_SENSITIVITY);
        AUTO_ADJUST_SENSITIVITY = BUILDER.translation("betterzoom.config.autosensitivity.label").comment("Adjusts mouse sensitivity based on the zoom level").define("autoAdjustSensitivity", true);
        HOLD_TO_ZOOM = BUILDER.translation("betterzoom.config.hold.label").comment("Require holding the zoom key").define("holdToZoom", true);
        SMOOTH_EASING_FACTOR = BUILDER.translation("betterzoom.config.smooth_easing.label").comment("Speed of zoom transitions (higher = faster/less smooth, lower = slower/more smooth)").defineInRange("smoothEasingFactor", 0.2, MIN_SMOOTH_EASE, MAX_SMOOTH_EASE);
        SMOOTH_ZOOM = BUILDER.translation("betterzoom.config.smooth.label").comment("Enable smooth zooming transitions (ease in/out)").define("smoothZoom", true);
        ZOOM_MODE = BUILDER.translation("betterzoom.config.zoommode.label").comment("Zoom adjustment mode").defineEnum("zoomMode", ZoomMode.BOTH);
        DISABLE_BOBBING_WHILE_ZOOMING = BUILDER.translation("betterzoom.config.disablebobbing.label").comment("Disables bobbing/bob view while zooming to prevent visual wobble").define("disableBobbing", true);
        CINEMATIC_CAMERA = BUILDER.translation("betterzoom.config.cinematic.label").comment("Enable cinematic camera movement while zooming").define("cinematicCamera", false);
        ZOOM_SOUND = BUILDER.translation("betterzoom.config.zoomsound.label").comment("Play the spyglass sound when zooming/unzooming").define("zoomSound", true);
        HIDE_GUI = BUILDER.translation("betterzoom.config.hidegui.label").comment("Hide the GUI while zooming").define("hideGui", true);
        HIDE_HAND = BUILDER.translation("betterzoom.config.hidehand.label").comment("Hide your hand while zooming").define("hideHand", false);
        CINEMATIC_BARS = BUILDER.translation("betterzoom.config.cinematicbars.label").comment("Show black bars at the top and bottom while zooming").define("cinematicBars", false);
        ZOOM_LEVEL = BUILDER.translation("betterzoom.config.zoomlevel.label").comment("Show the zoom level (like x2.5) while zooming").define("zoomLevel", false);
        SETTINGS_HINT = BUILDER.translation("betterzoom.config.settingshint.label").comment("Show how to open these settings on the first zoom of each join").define("settingsHint", true);
    }

    public static final ModConfigSpec SPEC = BUILDER.build();
}
