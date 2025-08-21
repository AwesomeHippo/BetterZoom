package com.awesomehippo.betterzoom.config;

import com.awesomehippo.betterzoom.keybinds.Keybinds;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public class ConfigScreen extends Screen {
    private final Screen parent;
    private ZoomSlider zoomIncrementSlider;
    private SensitivitySlider sensitivitySlider;
    private EasingFactorSlider easingFactorSlider;
    private Checkbox holdToZoomCheckbox;
    private Checkbox smoothZoomCheckbox;
    private Checkbox autoAdjustSensitivityCheckbox;
    private Checkbox disableBobbingCheckbox;

    public ConfigScreen(Screen parent) {
        super(Component.translatable("betterzoom.config.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int centerX = width / 2;
        int contentWidth = Math.min(250, width - 40);
        int leftX = centerX - contentWidth / 2;
        int topY = (height / 4) - 6;
        int controlHeight = 20;
        int spacing = 26;
        int gap = 6;
        int y = topY;

        int leftWidth = (contentWidth - gap) * 2 / 3;
        int rightWidth = contentWidth - gap - leftWidth;

        // row 1: zoom increment step slider
        zoomIncrementSlider = new ZoomSlider(leftX, y, contentWidth, controlHeight,
                Config.MIN_ZOOM_INCREMENT, Config.MAX_ZOOM_INCREMENT, Config.ZOOM_STEP.get().doubleValue());
        zoomIncrementSlider.setTooltip(Tooltip.create(Component.translatable("betterzoom.config.zoomstep.tooltip")));
        addRenderableWidget(zoomIncrementSlider);
        y += spacing;

        // row 2: sensitivity & auto adjust checkbox
        sensitivitySlider = new SensitivitySlider(leftX, y, leftWidth, controlHeight,
                Config.MIN_SENSITIVITY, Config.MAX_SENSITIVITY, Config.ZOOM_SENSITIVITY_MULTIPLIER.get().doubleValue());
        sensitivitySlider.setTooltip(Tooltip.create(Component.translatable("betterzoom.config.sensitivity.tooltip")));
        addRenderableWidget(sensitivitySlider);

        autoAdjustSensitivityCheckbox = new Checkbox(leftX + leftWidth + gap, y, rightWidth, controlHeight,
                Component.translatable("betterzoom.config.autosensitivity.label"),
                Config.AUTO_ADJUST_SENSITIVITY.get()) {
            @Override
            public void onPress() {
                super.onPress();
                sensitivitySlider.active = !this.selected();
            }
        };
        autoAdjustSensitivityCheckbox.setTooltip(Tooltip.create(Component.translatable("betterzoom.config.autosensitivity.tooltip")));
        sensitivitySlider.active = !Config.AUTO_ADJUST_SENSITIVITY.get();
        addRenderableWidget(autoAdjustSensitivityCheckbox);
        y += spacing;

        // row 3: easing factor & smooth transition checkbox
        easingFactorSlider = new EasingFactorSlider(leftX, y, leftWidth, controlHeight, Config.MIN_SMOOTH_EASE, Config.MAX_SMOOTH_EASE,
                Config.SMOOTH_EASING_FACTOR.get().doubleValue());
        easingFactorSlider.setTooltip(Tooltip.create(Component.translatable("betterzoom.config.smooth_easing.tooltip")));
        addRenderableWidget(easingFactorSlider);

        smoothZoomCheckbox = new Checkbox(leftX + leftWidth + gap, y, rightWidth, controlHeight,
                Component.translatable("betterzoom.config.smooth.label"),
                Config.SMOOTH_ZOOM.get()) {
            @Override
            public void onPress() {
                super.onPress();
                easingFactorSlider.active = this.selected();
            }
        };
        smoothZoomCheckbox.setTooltip(Tooltip.create(Component.translatable("betterzoom.config.smooth.tooltip")));
        easingFactorSlider.active = Config.SMOOTH_ZOOM.get();
        addRenderableWidget(smoothZoomCheckbox);
        y += spacing;

        // row 4: hold to zoom & zoom mode checkbox
        holdToZoomCheckbox = new Checkbox(leftX + leftWidth + gap, y, rightWidth, controlHeight,
                Component.translatable("betterzoom.config.hold.label"),
                Config.HOLD_TO_ZOOM.get());
        holdToZoomCheckbox.setTooltip(Tooltip.create(Component.translatable("betterzoom.config.hold.tooltip")));
        addRenderableWidget(holdToZoomCheckbox);

        CycleButton<Config.ZoomMode> zoomModeCycle = CycleButton.builder(Config.ZoomMode::getDisplayName)
                .withValues(Config.ZoomMode.values())
                .withInitialValue(Config.ZOOM_MODE.get())
                .withTooltip(value -> Tooltip.create(Component.translatable("betterzoom.config.zoommode.tooltip")))
                .create(leftX, y, leftWidth, controlHeight, Component.translatable("betterzoom.config.zoommode.label"));
        addRenderableWidget(zoomModeCycle);
        y += spacing;

        // row 5: disable bobbing while zooming checkbox
        disableBobbingCheckbox = new Checkbox(leftX, y, contentWidth, controlHeight,
                Component.translatable("betterzoom.config.disablebobbing.label"),
                Config.DISABLE_BOBBING_WHILE_ZOOMING.get());
        disableBobbingCheckbox.setTooltip(Tooltip.create(Component.translatable("betterzoom.config.disablebobbing.tooltip")));
        addRenderableWidget(disableBobbingCheckbox);
        y += spacing + 12;

        // done (save)
        addRenderableWidget(Button.builder(Component.translatable("gui.done"), button -> {
                    Config.ZOOM_STEP.set(Math.round(zoomIncrementSlider.getActualValue() * 100) / 100.0);
                    Config.ZOOM_SENSITIVITY_MULTIPLIER.set(Math.round(sensitivitySlider.getActualValue() * 100) / 100.0);
                    Config.AUTO_ADJUST_SENSITIVITY.set(autoAdjustSensitivityCheckbox.selected());
                    Config.HOLD_TO_ZOOM.set(holdToZoomCheckbox.selected());
                    Config.SMOOTH_ZOOM.set(smoothZoomCheckbox.selected());
                    Config.ZOOM_MODE.set(zoomModeCycle.getValue());
                    Config.SMOOTH_EASING_FACTOR.set(Math.round(easingFactorSlider.getActualValue() * 100) / 100.0);
                    Config.DISABLE_BOBBING_WHILE_ZOOMING.set(disableBobbingCheckbox.selected());
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

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (Keybinds.CONFIG_KEY.isActiveAndMatches(InputConstants.getKey(keyCode, scanCode))) {
            this.onClose();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (Keybinds.CONFIG_KEY.isActiveAndMatches(InputConstants.Type.MOUSE.getOrCreate(button))) {
            this.onClose();
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    public static class ZoomSlider extends AbstractSliderButton {
        protected final double min;
        protected final double max;

        public ZoomSlider(int x, int y, int width, int height, double min, double max, double currentValue) {
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

        public double getActualValue() {
            return min + (max - min) * value;
        }
    }

    public static class SensitivitySlider extends ZoomSlider {
        public SensitivitySlider(int x, int y, int width, int height, double min, double max, double currentValue) {
            super(x, y, width, height, min, max, currentValue);
        }

        @Override
        protected void updateMessage() {
            setMessage(Component.translatable("betterzoom.config.sensitivity.label", String.format("%.2f", getActualValue())));
        }
    }

    public static class EasingFactorSlider extends ZoomSlider {
        public EasingFactorSlider(int x, int y, int width, int height, double min, double max, double currentValue) {
            super(x, y, width, height, min, max, currentValue);
        }

        @Override
        protected void updateMessage() {
            setMessage(Component.translatable("betterzoom.config.smooth_easing.label", String.format("%.2f", getActualValue())));
        }
    }
}