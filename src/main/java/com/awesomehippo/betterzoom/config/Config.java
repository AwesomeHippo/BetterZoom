package com.awesomehippo.betterzoom.config;

import net.minecraft.network.chat.Component;
import net.minecraftforge.common.ForgeConfigSpec;

public class Config {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    public static final double MIN_ZOOM_INCREMENT = 0.1;
    public static final double MAX_ZOOM_INCREMENT = 20.0;
    public static final double MIN_SENSITIVITY = 0.01;
    public static final double MAX_SENSITIVITY = 1.0;
    public static final double MIN_SMOOTH_EASE = 0.05;
    public static final double MAX_SMOOTH_EASE = 0.2;

    public static final ForgeConfigSpec.DoubleValue ZOOM_STEP;
    public static final ForgeConfigSpec.DoubleValue ZOOM_SENSITIVITY_MULTIPLIER;
    public static final ForgeConfigSpec.BooleanValue HOLD_TO_ZOOM;
    public static final ForgeConfigSpec.DoubleValue SMOOTH_EASING_FACTOR;
    public static final ForgeConfigSpec.BooleanValue SMOOTH_ZOOM;
    public static final ForgeConfigSpec.EnumValue<ZoomMode> ZOOM_MODE;
    public static final ForgeConfigSpec.BooleanValue AUTO_ADJUST_SENSITIVITY;
    public static final ForgeConfigSpec.BooleanValue DISABLE_BOBBING_WHILE_ZOOMING;
    public static final ForgeConfigSpec.BooleanValue CINEMATIC_CAMERA;

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

        ZOOM_STEP = BUILDER.translation("betterzoom.config.zoomstep.label").comment("The zoom increment step per zoom in/out (higher values zoom/dezoom faster)").defineInRange("zoomStep",5.0, MIN_ZOOM_INCREMENT, MAX_ZOOM_INCREMENT);
        ZOOM_SENSITIVITY_MULTIPLIER = BUILDER.translation("betterzoom.config.sensitivity.label").comment("Mouse sensitivity multiplier while zooming (lower values make it less sensitive)").defineInRange("zoomSensitivity", 0.01, MIN_SENSITIVITY, MAX_SENSITIVITY);
        AUTO_ADJUST_SENSITIVITY = BUILDER.translation("betterzoom.config.autosensitivity.label").comment("Adjusts mouse sensitivity based on the zoom level").define("autoAdjustSensitivity", true);
        HOLD_TO_ZOOM = BUILDER.translation("betterzoom.config.hold.label").comment("Require holding the zoom key").define("holdToZoom", true);
        SMOOTH_EASING_FACTOR = BUILDER.translation("betterzoom.config.transition_speed.label").comment("Easing factor for zoom transitions (higher = faster/less smooth, lower = slower/more smooth)").defineInRange("smoothEasingFactor", 0.15, MIN_SMOOTH_EASE, MAX_SMOOTH_EASE);
        SMOOTH_ZOOM = BUILDER.translation("betterzoom.config.smooth.label").comment("Enable smooth zooming transitions (ease in/out)").define("smoothZoom", true);
        ZOOM_MODE = BUILDER.translation("betterzoom.config.zoommode.label").comment("Zoom adjustment mode").defineEnum("zoomMode", ZoomMode.BOTH);
        DISABLE_BOBBING_WHILE_ZOOMING = BUILDER.translation("betterzoom.config.disablebobbing.label").comment("Disables bobbing/bob view while zooming to prevent visual wobble").define("disableBobbing", true);
        CINEMATIC_CAMERA = BUILDER.translation("betterzoom.config.cinematic.label").comment("Enable cinematic camera movement while zooming").define("cinematicCamera", false);
    }

    public static final ForgeConfigSpec SPEC = BUILDER.build();
}