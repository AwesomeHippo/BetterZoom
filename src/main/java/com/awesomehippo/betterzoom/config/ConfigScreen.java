package com.awesomehippo.betterzoom.config;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public class ConfigScreen extends Screen {

    private final Screen parent;
    private ZoomSlider zoomSlider;
    private SensitivitySlider sensitivitySlider;
    private Checkbox holdToZoomCheckbox;
    private Checkbox smoothTransitionCheckbox;

    private static final float MIN_ZOOM_INCREMENT = 0.1f;
    private static final float MAX_ZOOM_INCREMENT = 10.0f;
    private static final float MIN_SENSITIVITY = 0.01f;
    private static final float MAX_SENSITIVITY = 1.0f;

    public ConfigScreen(Screen parent) {
        super(Component.translatable("betterzoom.config.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int centerX = width / 2;
        int topY = height / 4;

        // zoom increment slider
        zoomSlider = new ZoomSlider(centerX - 100, topY, 200, 20, MIN_ZOOM_INCREMENT, MAX_ZOOM_INCREMENT, Config.ZOOM_STEP);
        zoomSlider.setTooltip(Tooltip.create(Component.translatable("betterzoom.config.zoom.tooltip")));
        addRenderableWidget(zoomSlider);

        // sensitivity slider
        sensitivitySlider = new SensitivitySlider(centerX - 100, topY + 30, 200, 20, MIN_SENSITIVITY, MAX_SENSITIVITY, Config.ZOOM_SENSITIVITY_MULTIPLIER);
        sensitivitySlider.setTooltip(Tooltip.create(Component.translatable("betterzoom.config.sensitivity.tooltip")));
        addRenderableWidget(sensitivitySlider);

        // hold for zooming checkbox
        holdToZoomCheckbox = new Checkbox(centerX - 100, topY + 60, 95, 20,
                Component.translatable("betterzoom.config.hold.label"),
                Config.HOLD_TO_ZOOM);
        holdToZoomCheckbox.setTooltip(Tooltip.create(Component.translatable("betterzoom.config.hold.tooltip")));
        addRenderableWidget(holdToZoomCheckbox);

        // smooth transition checkbox
        smoothTransitionCheckbox = new Checkbox(centerX + 5, topY + 60, 95, 20,
                Component.translatable("betterzoom.config.smooth.label"),
                Config.ENABLE_SMOOTH_TRANSITION);
        smoothTransitionCheckbox.setTooltip(Tooltip.create(Component.translatable("betterzoom.config.smooth.tooltip")));
        addRenderableWidget(smoothTransitionCheckbox);

        //cancel
        addRenderableWidget(Button.builder(Component.translatable("gui.cancel"), button -> {
                    minecraft.setScreen(parent);
                }).bounds(centerX - 100, topY + 90, 95, 20)
                .tooltip(Tooltip.create(Component.translatable("betterzoom.config.cancel.tooltip")))
                .build());

        //save
        addRenderableWidget(Button.builder(Component.translatable("gui.done"), button -> {
                    Config.ZOOM_STEP = zoomSlider.getActualValue();
                    Config.ZOOM_SENSITIVITY_MULTIPLIER = sensitivitySlider.getActualValue();
                    Config.HOLD_TO_ZOOM = holdToZoomCheckbox.selected();
                    Config.ENABLE_SMOOTH_TRANSITION = smoothTransitionCheckbox.selected();
                    Config.save();
                    minecraft.setScreen(parent);
                }).bounds(centerX + 5, topY + 90, 95, 20)
                .tooltip(Tooltip.create(Component.translatable("betterzoom.config.save.tooltip")))
                .build());

    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        renderBackground(guiGraphics);
        guiGraphics.drawCenteredString(font, title, width / 2, height / 4 - 40, 0xFFFFFF);
        super.render(guiGraphics, mouseX, mouseY, partialTicks);
    }

    @Override
    public void onClose() {
        Minecraft.getInstance().setScreen(parent);
    }

    public static class ZoomSlider extends AbstractSliderButton {
        protected final float min;
        protected final float max;

        public ZoomSlider(int x, int y, int width, int height, float min, float max, float currentValue) {
            super(x, y, width, height, Component.empty(), (currentValue - min) / (max - min));
            this.min = min;
            this.max = max;
            updateMessage();
        }

        @Override
        protected void updateMessage() {
            setMessage(Component.translatable("betterzoom.config.zoom.label", String.format("%.2f", getActualValue())));
        }

        @Override
        protected void applyValue() {
            updateMessage();
        }

        public float getActualValue() {
            return min + (max - min) * (float) value;
        }

        /*public void setActualValue(float val) {
            this.value = (val - min) / (max - min);
            updateMessage();
        }*/
    }

    public static class SensitivitySlider extends ZoomSlider {
        public SensitivitySlider(int x, int y, int width, int height, float min, float max, float currentValue) {
            super(x, y, width, height, min, max, currentValue);
        }

        @Override
        protected void updateMessage() {
            setMessage(Component.translatable("betterzoom.config.sensitivity.label", String.format("%.2f", getActualValue())));
        }
    }
}
