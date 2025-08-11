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

    public ConfigScreen(Screen parent) {
        super(Component.translatable("betterzoom.config.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int centerX = width / 2;
        int contentWidth = Math.min(215, width - 40);
        int leftX = centerX - contentWidth / 2;
        int topY = height / 4;
        int controlHeight = 20;
        int spacing = 26;
        int y = topY;

        // zoom increment slider
        zoomSlider = new ZoomSlider(leftX, y, contentWidth, controlHeight, Config.MIN_ZOOM_INCREMENT, Config.MAX_ZOOM_INCREMENT, Config.ZOOM_STEP.get().floatValue());
        zoomSlider.setTooltip(Tooltip.create(Component.translatable("betterzoom.config.zoomstep.tooltip")));
        addRenderableWidget(zoomSlider);
        y += spacing;

        // sensitivity slider
        sensitivitySlider = new SensitivitySlider(leftX, y, contentWidth, controlHeight, Config.MIN_SENSITIVITY, Config.MAX_SENSITIVITY, Config.ZOOM_SENSITIVITY_MULTIPLIER.get().floatValue());
        sensitivitySlider.setTooltip(Tooltip.create(Component.translatable("betterzoom.config.sensitivity.tooltip")));
        addRenderableWidget(sensitivitySlider);
        y += spacing;

        // hold for zooming checkbox
        holdToZoomCheckbox = new Checkbox(leftX, y, contentWidth / 2 - 6, controlHeight,
                Component.translatable("betterzoom.config.hold.label"),
                Config.HOLD_TO_ZOOM.get());
        holdToZoomCheckbox.setTooltip(Tooltip.create(Component.translatable("betterzoom.config.hold.tooltip")));
        addRenderableWidget(holdToZoomCheckbox);

        // smooth transition checkbox
        smoothTransitionCheckbox = new Checkbox(leftX + contentWidth / 2 + 6, y, contentWidth / 2 - 6, controlHeight,
                Component.translatable("betterzoom.config.smooth.label"),
                Config.ENABLE_SMOOTH_TRANSITION.get());
        smoothTransitionCheckbox.setTooltip(Tooltip.create(Component.translatable("betterzoom.config.smooth.tooltip")));
        addRenderableWidget(smoothTransitionCheckbox);
        y += spacing;

        // zoom mode (cycle button)
        CycleButton<Config.ZoomMode> zoomModeCycle = CycleButton.builder(Config.ZoomMode::getDisplayName)
                .withValues(Config.ZoomMode.values())
                .withInitialValue(Config.ZOOM_MODE.get())
                .withTooltip(value -> Tooltip.create(Component.translatable("betterzoom.config.zoommode.tooltip")))
                .create(leftX, y, contentWidth, controlHeight, Component.translatable("betterzoom.config.zoommode.label"));
        addRenderableWidget(zoomModeCycle);
        y += spacing;

        // allow hotbar scroll checkbox
        Checkbox allowHotbarScrollCheckbox = new Checkbox(leftX, y, contentWidth, controlHeight,
                Component.translatable("betterzoom.config.hotbar_scroll.label"),
                Config.ALLOW_HOTBAR_SCROLL_WHILE_ZOOMING.get());
        allowHotbarScrollCheckbox.setTooltip(Tooltip.create(Component.translatable("betterzoom.config.hotbar_scroll.tooltip")));
        addRenderableWidget(allowHotbarScrollCheckbox);
        y += spacing + 6;

        // done (save)
        addRenderableWidget(Button.builder(Component.translatable("gui.done"), button -> {
                    Config.ZOOM_STEP.set((double) Math.round(zoomSlider.getActualValue() * 100) / 100);
                    Config.ZOOM_SENSITIVITY_MULTIPLIER.set((double) Math.round(sensitivitySlider.getActualValue() * 100) / 100);
                    Config.HOLD_TO_ZOOM.set(holdToZoomCheckbox.selected());
                    Config.ENABLE_SMOOTH_TRANSITION.set(smoothTransitionCheckbox.selected());
                    Config.ZOOM_MODE.set(zoomModeCycle.getValue());
                    Config.ALLOW_HOTBAR_SCROLL_WHILE_ZOOMING.set(allowHotbarScrollCheckbox.selected());
                    minecraft.setScreen(parent);
                }).bounds(leftX, y, contentWidth, controlHeight)
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
            setMessage(Component.translatable("betterzoom.config.zoomstep.label", String.format("%.2f", getActualValue())));
        }

        @Override
        protected void applyValue() {
            updateMessage();
        }

        public float getActualValue() {
            return min + (max - min) * (float) value;
        }
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
