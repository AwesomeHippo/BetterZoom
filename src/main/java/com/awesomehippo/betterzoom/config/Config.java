package com.awesomehippo.betterzoom.config;

import net.minecraft.network.chat.Component;
import net.minecraftforge.common.ForgeConfigSpec;

public class Config {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    public static final float MIN_ZOOM_INCREMENT = 0.1f;
    public static final float MAX_ZOOM_INCREMENT = 20.0f;
    public static final float MIN_SENSITIVITY = 0.01f;
    public static final float MAX_SENSITIVITY = 1.0f;

    public static final ForgeConfigSpec.DoubleValue ZOOM_STEP;
    public static final ForgeConfigSpec.DoubleValue ZOOM_SENSITIVITY_MULTIPLIER;
    public static final ForgeConfigSpec.BooleanValue HOLD_TO_ZOOM;
    public static final ForgeConfigSpec.BooleanValue ENABLE_SMOOTH_TRANSITION;
    public static final ForgeConfigSpec.BooleanValue ALLOW_HOTBAR_SCROLL_WHILE_ZOOMING;
    public static final ForgeConfigSpec.EnumValue<ZoomMode> ZOOM_MODE;

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
        BUILDER.comment(String.valueOf(Component.translatable("betterzoom.config.title")));

        ZOOM_STEP = BUILDER.translation("betterzoom.config.zoom_step").comment("The zoom increment step (higher values zoom faster when scrolling)").defineInRange("zoomStep",5.0, MIN_ZOOM_INCREMENT, MAX_ZOOM_INCREMENT);
        ZOOM_SENSITIVITY_MULTIPLIER = BUILDER.translation("betterzoom.config.zoom_sensitivity").comment("Mouse sensitivity multiplier while zooming (lower values make it less sensitive)").defineInRange("zoomSensitivity", 0.01, MIN_SENSITIVITY, MAX_SENSITIVITY);
        HOLD_TO_ZOOM = BUILDER.translation("betterzoom.config.hold_to_zoom").comment("Require holding the zoom key").define("holdToZoom", true);
        ENABLE_SMOOTH_TRANSITION = BUILDER.translation("betterzoom.config.smooth_transition").comment("Enable smooth zooming transitions (fade in/out)").define("smoothTransition", true);
        ALLOW_HOTBAR_SCROLL_WHILE_ZOOMING = BUILDER.translation("betterzoom.config.allow_hotbar_scroll").comment("Allow hotbar slots to switch with mouse wheel, while zooming").define("allowHotbarScrollWhileZooming", false);
        ZOOM_MODE = BUILDER.translation("betterzoom.config.zoom_mode").comment("Zoom adjustment mode").defineEnum("zoomMode", ZoomMode.BOTH);
    }

    public static final ForgeConfigSpec SPEC = BUILDER.build();
}