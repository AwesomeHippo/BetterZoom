package com.awesomehippo.betterzoom.config;

import net.minecraft.network.chat.Component;
import net.minecraftforge.common.ForgeConfigSpec;

public class Config {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    public static final ForgeConfigSpec.DoubleValue ZOOM_STEP;
    public static final ForgeConfigSpec.DoubleValue ZOOM_SENSITIVITY_MULTIPLIER;
    public static final ForgeConfigSpec.BooleanValue HOLD_TO_ZOOM;
    public static final ForgeConfigSpec.BooleanValue ENABLE_SMOOTH_TRANSITION;

    static {
        BUILDER.comment(String.valueOf(Component.translatable("betterzoom.config.title")));

        ZOOM_STEP = BUILDER.translation("betterzoom.config.zoom_step").comment("The zoom increment step (higher values zoom faster when scrolling)").defineInRange("zoomStep", 2.0, 0.1, 10.0);
        ZOOM_SENSITIVITY_MULTIPLIER = BUILDER.translation("betterzoom.config.zoom_sensitivity").comment("Mouse sensitivity multiplier while zooming (lower values make it less sensitive)").defineInRange("zoomSensitivity", 0.3, 0.01, 1.0);
        HOLD_TO_ZOOM = BUILDER.translation("betterzoom.config.hold_to_zoom").comment("Require holding the zoom key").define("holdToZoom", true);
        ENABLE_SMOOTH_TRANSITION = BUILDER.translation("betterzoom.config.smooth_transition").comment("Enable smooth zooming transitions (fade in/out)").define("smoothTransition", true);
    }

    public static final ForgeConfigSpec SPEC = BUILDER.build();
}
