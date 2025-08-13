package com.awesomehippo.betterzoom.config;

import net.minecraft.network.chat.Component;
import net.minecraftforge.common.ForgeConfigSpec;

public class Config {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    public static final double MIN_ZOOM_INCREMENT = 0.1;
    public static final double MAX_ZOOM_INCREMENT = 20.0;
    public static final double MIN_SENSITIVITY = 0.01;
    public static final double MAX_SENSITIVITY = 1.0;

    public static final ForgeConfigSpec.DoubleValue ZOOM_STEP;
    public static final ForgeConfigSpec.DoubleValue ZOOM_SENSITIVITY_MULTIPLIER;
    public static final ForgeConfigSpec.BooleanValue HOLD_TO_ZOOM;
    public static final ForgeConfigSpec.BooleanValue ENABLE_SMOOTH_TRANSITION;
    public static final ForgeConfigSpec.BooleanValue ALLOW_HOTBAR_SCROLL_WHILE_ZOOMING;
    public static final ForgeConfigSpec.EnumValue<ZoomMode> ZOOM_MODE;
    public static final ForgeConfigSpec.BooleanValue AUTO_ADJUST_SENSITIVITY;

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

        ZOOM_STEP = BUILDER.translation("betterzoom.config.zoomstep.label").comment("The zoom increment step (higher values zoom faster when scrolling)").defineInRange("zoomStep",5.0, MIN_ZOOM_INCREMENT, MAX_ZOOM_INCREMENT);
        ZOOM_SENSITIVITY_MULTIPLIER = BUILDER.translation("betterzoom.config.sensitivity.label").comment("Mouse sensitivity multiplier while zooming (lower values make it less sensitive)").defineInRange("zoomSensitivity", 0.01, MIN_SENSITIVITY, MAX_SENSITIVITY);
        AUTO_ADJUST_SENSITIVITY = BUILDER.translation("betterzoom.config.autosensitivity.label").comment("Adjusts mouse sensitivity based on the zoom level").define("autoAdjustSensitivity", true);
        HOLD_TO_ZOOM = BUILDER.translation("betterzoom.config.hold.label").comment("Require holding the zoom key").define("holdToZoom", true);
        ENABLE_SMOOTH_TRANSITION = BUILDER.translation("betterzoom.config.smooth.label").comment("Enable smooth zooming transitions (fade in/out)").define("smoothTransition", true);
        ALLOW_HOTBAR_SCROLL_WHILE_ZOOMING = BUILDER.translation("betterzoom.config.hotbar_scroll.label").comment("Allow hotbar slots to switch with mouse wheel, while zooming").define("allowHotbarScrollWhileZooming", false);
        ZOOM_MODE = BUILDER.translation("betterzoom.config.zoommode.label").comment("Zoom adjustment mode").defineEnum("zoomMode", ZoomMode.BOTH);
    }

    public static final ForgeConfigSpec SPEC = BUILDER.build();
}