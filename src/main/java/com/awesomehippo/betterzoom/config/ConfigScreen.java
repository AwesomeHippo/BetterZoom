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
    private Checkbox cinematicCameraCheckbox;
    private CycleButton<Config.ZoomMode> zoomModeCycle;

    public ConfigScreen(Screen parent) {
        super(Component.translatable("betterzoom.config.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int centerX = width / 2;
        int contentWidth = Math.min(250, width - 40);
        int leftX = centerX - contentWidth / 2;
        int topY = (height / 5) - 10;
        int controlHeight = 20;
        int spacing = 26;
        int gap = 6;
        int y = topY;

        int leftWidth = (contentWidth - gap) * 2 / 3;

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

        autoAdjustSensitivityCheckbox = Checkbox.builder(Component.translatable("betterzoom.config.autosensitivity.label"), this.font)
                .pos(leftX + leftWidth + gap, y)
                .selected(Config.AUTO_ADJUST_SENSITIVITY.get())
                .onValueChange((checkbox, selected) -> {
                    sensitivitySlider.active = !selected;
                })
                .build();
        autoAdjustSensitivityCheckbox.setTooltip(Tooltip.create(Component.translatable("betterzoom.config.autosensitivity.tooltip")));
        sensitivitySlider.active = !Config.AUTO_ADJUST_SENSITIVITY.get();
        addRenderableWidget(autoAdjustSensitivityCheckbox);
        y += spacing;

        // row 3: easing factor & smooth transition checkbox
        easingFactorSlider = new EasingFactorSlider(leftX, y, leftWidth, controlHeight, Config.MIN_SMOOTH_EASE, Config.MAX_SMOOTH_EASE,
                Config.SMOOTH_EASING_FACTOR.get().doubleValue());
        easingFactorSlider.setTooltip(Tooltip.create(Component.translatable("betterzoom.config.smooth_easing.tooltip")));
        addRenderableWidget(easingFactorSlider);

        smoothZoomCheckbox = Checkbox.builder(Component.translatable("betterzoom.config.smooth.label"), this.font)
                .pos(leftX + leftWidth + gap, y)
                .selected(Config.SMOOTH_ZOOM.get())
                .onValueChange((checkbox, selected) -> {
                    easingFactorSlider.active = selected;
                })
                .build();
        smoothZoomCheckbox.setTooltip(Tooltip.create(Component.translatable("betterzoom.config.smooth.tooltip")));
        easingFactorSlider.active = Config.SMOOTH_ZOOM.get();
        addRenderableWidget(smoothZoomCheckbox);
        y += spacing;

        // row 4: hold to zoom & zoom mode checkbox
        holdToZoomCheckbox = Checkbox.builder(Component.translatable("betterzoom.config.hold.label"), this.font)
                .pos(leftX + leftWidth + gap, y)
                .selected(Config.HOLD_TO_ZOOM.get())
                .build();
        holdToZoomCheckbox.setTooltip(Tooltip.create(Component.translatable("betterzoom.config.hold.tooltip")));
        addRenderableWidget(holdToZoomCheckbox);

        zoomModeCycle = CycleButton.builder(Config.ZoomMode::getDisplayName)
                .withValues(Config.ZoomMode.values())
                .withInitialValue(Config.ZOOM_MODE.get())
                .withTooltip(value -> Tooltip.create(Component.translatable("betterzoom.config.zoommode.tooltip")))
                .create(leftX, y, leftWidth, controlHeight, Component.translatable("betterzoom.config.zoommode.label"));
        addRenderableWidget(zoomModeCycle);
        y += spacing;

        // row 5: disable bobbing while zooming checkbox
        disableBobbingCheckbox = Checkbox.builder(Component.translatable("betterzoom.config.disablebobbing.label"), this.font)
                .pos(leftX, y)
                .selected(Config.DISABLE_BOBBING_WHILE_ZOOMING.get())
                .build();
        disableBobbingCheckbox.setTooltip(Tooltip.create(Component.translatable("betterzoom.config.disablebobbing.tooltip")));
        addRenderableWidget(disableBobbingCheckbox);
        y += spacing;

        // row 6: cinematic camera while zooming
        cinematicCameraCheckbox = Checkbox.builder(Component.translatable("betterzoom.config.cinematic.label"), this.font)
                .pos(leftX, y)
                .selected(Config.CINEMATIC_CAMERA.get())
                .build();
        cinematicCameraCheckbox.setTooltip(Tooltip.create(Component.translatable("betterzoom.config.cinematic.tooltip")));
        addRenderableWidget(cinematicCameraCheckbox);
        y += spacing + 12;

        // done (save)
        addRenderableWidget(Button.builder(Component.translatable("gui.done"), button -> onClose())
                .bounds(leftX, y, contentWidth, controlHeight)
                .tooltip(Tooltip.create(Component.translatable("betterzoom.config.save.tooltip")))
                .build());
    }

    private void saveSettings() {
        Config.ZOOM_STEP.set(Math.round(zoomIncrementSlider.getActualValue() * 100) / 100.0);
        Config.ZOOM_SENSITIVITY_MULTIPLIER.set(Math.round(sensitivitySlider.getActualValue() * 100) / 100.0);
        Config.AUTO_ADJUST_SENSITIVITY.set(autoAdjustSensitivityCheckbox.selected());
        Config.HOLD_TO_ZOOM.set(holdToZoomCheckbox.selected());
        Config.SMOOTH_ZOOM.set(smoothZoomCheckbox.selected());
        Config.ZOOM_MODE.set(zoomModeCycle.getValue());
        Config.SMOOTH_EASING_FACTOR.set(Math.round(easingFactorSlider.getActualValue() * 100) / 100.0);
        Config.DISABLE_BOBBING_WHILE_ZOOMING.set(disableBobbingCheckbox.selected());
        Config.CINEMATIC_CAMERA.set(cinematicCameraCheckbox.selected());
        Config.SPEC.save();
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTicks);
        super.render(guiGraphics, mouseX, mouseY, partialTicks);
        // drawing text after or it will blurred
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, Math.max(8, (this.height / 5) - 38), 0xFFFFFF);
    }

    @Override
    public void onClose() {
        saveSettings();
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
