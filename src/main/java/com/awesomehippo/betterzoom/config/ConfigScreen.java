package com.awesomehippo.betterzoom.config;

import com.awesomehippo.betterzoom.ZoomHandler;
import com.awesomehippo.betterzoom.keybinds.Keybinds;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.components.tabs.GridLayoutTab;
import net.minecraft.client.gui.components.tabs.Tab;
import net.minecraft.client.gui.components.tabs.TabManager;
import net.minecraft.client.gui.layouts.FrameLayout;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public class ConfigScreen extends Screen {
    private static final int LINE_COLOR = 0x505050;
    private static final float PREVIEW_ALPHA = 0.3f;
    private final Screen parent;
    private final TabManager tabManager = new TabManager(this::addRenderableWidget, this::removeWidget);
    private GridLayout tabButtons;
    private GridLayout bottomButtons;
    private IntSlider incrementSlider;
    private DecimalSlider sensitivitySlider;
    private DecimalSlider easingFactorSlider;
    private Checkbox smoothZoomCheckbox;
    private Checkbox autoAdjustSensitivityCheckbox;
    private ToggleOptionButton holdToZoomButton;
    private ToggleOptionButton disableBobbingButton;
    private ToggleOptionButton cinematicCameraButton;
    private ToggleOptionButton zoomSoundButton;
    private ToggleOptionButton hideGuiButton;
    private ToggleOptionButton hideHandButton;
    private ToggleOptionButton cinematicBarsButton;
    private ToggleOptionButton zoomLevelButton;
    private ToggleOptionButton settingsHintButton;
    private CycleButton<Config.ZoomMode> zoomModeCycle;
    private boolean previewing;
    private boolean zoomKeyDown;

    public ConfigScreen(Screen parent) {
        super(Component.translatable("betterzoom.config.title"));
        this.parent = parent;
    }

    public boolean isPreviewing() {
        return previewing;
    }

    private void zoomKeyPressed() {
        if (!zoomKeyDown && minecraft.level != null) {
            zoomKeyDown = true;
            applySettings();
            previewing = holdToZoomButton.selected() || !previewing;
        }
    }

    private void zoomKeyReleased() {
        zoomKeyDown = false;
        if (holdToZoomButton.selected()) {
            previewing = false;
        }
    }

    @Override
    protected void init() {
        GeneralTab generalTab = new GeneralTab();
        tabButtons = new GridLayout().columnSpacing(10);
        GridLayout.RowHelper tabs = tabButtons.createRowHelper(3);
        tabs.addChild(new TabButton(generalTab));
        tabs.addChild(new TabButton(new VisualsTab()));
        tabs.addChild(new TabButton(new ExtrasTab()));
        tabButtons.visitWidgets(this::addRenderableWidget);

        // reset & done (save)
        bottomButtons = new GridLayout().columnSpacing(10);
        GridLayout.RowHelper buttons = bottomButtons.createRowHelper(2);
        buttons.addChild(Button.builder(Component.translatable("controls.reset"), button -> resetSettings()).tooltip(Tooltip.create(Component.translatable("betterzoom.config.reset.tooltip"))).build());
        buttons.addChild(Button.builder(Component.translatable("gui.done"), button -> onClose()).tooltip(Tooltip.create(Component.translatable("betterzoom.config.save.tooltip"))).build());
        bottomButtons.visitWidgets(this::addRenderableWidget);

        tabManager.setCurrentTab(generalTab, false);
        repositionElements();
    }

    @Override
    protected void repositionElements() {
        tabButtons.arrangeElements();
        FrameLayout.centerInRectangle(tabButtons, 0, 24, width, 24);
        bottomButtons.arrangeElements();
        FrameLayout.centerInRectangle(bottomButtons, 0, height - 36, width, 36);
        tabManager.setTabArea(new ScreenRectangle(0, 64, width, height - 51 - 64));
    }

    private void addRow(GridLayout.RowHelper rows, String labelKey, LayoutElement element) {
        rows.addChild(new StringWidget(120, 20, Component.translatable(labelKey), font).alignLeft());
        rows.addChild(element);
    }

    private GridLayout pair(AbstractWidget slider, AbstractWidget checkbox) {
        GridLayout pair = new GridLayout().columnSpacing(4);
        pair.addChild(slider, 0, 0);
        pair.addChild(checkbox, 0, 1);
        return pair;
    }

    // same top for every tab
    abstract static class SettingsTab extends GridLayoutTab {
        SettingsTab(Component title) {
            super(title);
        }

        @Override
        public void doLayout(ScreenRectangle rectangle) {
            layout.arrangeElements();
            FrameLayout.alignInRectangle(layout, rectangle, 0.5F, 0.0F);
        }
    }

    class GeneralTab extends SettingsTab {
        GeneralTab() {
            super(Component.translatable("betterzoom.config.tab.general"));
            GridLayout.RowHelper rows = layout.rowSpacing(4).columnSpacing(10).createRowHelper(2);

            incrementSlider = new IntSlider(0, 0, 180, 20, Config.MIN_ZOOM_INCREMENT, Config.MAX_ZOOM_INCREMENT, Config.ZOOM_STEP.get());
            incrementSlider.setTooltip(Tooltip.create(Component.translatable("betterzoom.config.zoomstep.tooltip")));
            addRow(rows, "betterzoom.config.zoomstep.label", incrementSlider);

            holdToZoomButton = new ToggleOptionButton(0, 0, 180, 20, Config.HOLD_TO_ZOOM.get());
            holdToZoomButton.setTooltip(Tooltip.create(Component.translatable("betterzoom.config.hold.tooltip")));
            addRow(rows, "betterzoom.config.hold.label", holdToZoomButton);

            zoomModeCycle = CycleButton.builder(Config.ZoomMode::getDisplayName)
                    .withValues(Config.ZoomMode.values())
                    .withInitialValue(Config.ZOOM_MODE.get())
                    .withTooltip(value -> Tooltip.create(Component.translatable("betterzoom.config.zoommode.tooltip")))
                    .displayOnlyValue()
                    .create(0, 0, 180, 20, Component.translatable("betterzoom.config.zoommode.label"));
            addRow(rows, "betterzoom.config.zoommode.label", zoomModeCycle);

            easingFactorSlider = new DecimalSlider(0, 0, 88, 20, Config.MIN_SMOOTH_EASE, Config.MAX_SMOOTH_EASE, Config.SMOOTH_EASING_FACTOR.get());
            easingFactorSlider.setTooltip(Tooltip.create(Component.translatable("betterzoom.config.smooth_easing.tooltip")));
            easingFactorSlider.active = Config.SMOOTH_ZOOM.get();
            smoothZoomCheckbox = new Checkbox(0, 0, 88, 20, Component.translatable("betterzoom.config.smooth.label"), Config.SMOOTH_ZOOM.get()) {
                @Override
                public void onPress() {
                    super.onPress();
                    easingFactorSlider.active = this.selected();
                }
            };
            smoothZoomCheckbox.setTooltip(Tooltip.create(Component.translatable("betterzoom.config.smooth.tooltip")));
            addRow(rows, "betterzoom.config.smooth_easing.label", pair(easingFactorSlider, smoothZoomCheckbox));

            sensitivitySlider = new DecimalSlider(0, 0, 88, 20, Config.MIN_SENSITIVITY, Config.MAX_SENSITIVITY, Config.ZOOM_SENSITIVITY_MULTIPLIER.get());
            sensitivitySlider.setTooltip(Tooltip.create(Component.translatable("betterzoom.config.sensitivity.tooltip")));
            sensitivitySlider.active = !Config.AUTO_ADJUST_SENSITIVITY.get();
            autoAdjustSensitivityCheckbox = new Checkbox(0, 0, 88, 20, Component.translatable("betterzoom.config.autosensitivity.label"), Config.AUTO_ADJUST_SENSITIVITY.get()) {
                @Override
                public void onPress() {
                    super.onPress();
                    sensitivitySlider.active = !this.selected();
                }
            };
            autoAdjustSensitivityCheckbox.setTooltip(Tooltip.create(Component.translatable("betterzoom.config.autosensitivity.tooltip")));
            addRow(rows, "betterzoom.config.sensitivity.label", pair(sensitivitySlider, autoAdjustSensitivityCheckbox));
        }
    }

    class VisualsTab extends SettingsTab {
        VisualsTab() {
            super(Component.translatable("betterzoom.config.tab.visuals"));
            GridLayout.RowHelper rows = layout.rowSpacing(4).columnSpacing(10).createRowHelper(2);

            hideGuiButton = new ToggleOptionButton(0, 0, 180, 20, Config.HIDE_GUI.get());
            hideGuiButton.setTooltip(Tooltip.create(Component.translatable("betterzoom.config.hidegui.tooltip")));
            addRow(rows, "betterzoom.config.hidegui.label", hideGuiButton);

            hideHandButton = new ToggleOptionButton(0, 0, 180, 20, Config.HIDE_HAND.get());
            hideHandButton.setTooltip(Tooltip.create(Component.translatable("betterzoom.config.hidehand.tooltip")));
            addRow(rows, "betterzoom.config.hidehand.label", hideHandButton);

            disableBobbingButton = new ToggleOptionButton(0, 0, 180, 20, Config.DISABLE_BOBBING_WHILE_ZOOMING.get());
            disableBobbingButton.setTooltip(Tooltip.create(Component.translatable("betterzoom.config.disablebobbing.tooltip")));
            addRow(rows, "betterzoom.config.disablebobbing.label", disableBobbingButton);

            cinematicCameraButton = new ToggleOptionButton(0, 0, 180, 20, Config.CINEMATIC_CAMERA.get());
            cinematicCameraButton.setTooltip(Tooltip.create(Component.translatable("betterzoom.config.cinematic.tooltip")));
            addRow(rows, "betterzoom.config.cinematic.label", cinematicCameraButton);

            cinematicBarsButton = new ToggleOptionButton(0, 0, 180, 20, Config.CINEMATIC_BARS.get());
            cinematicBarsButton.setTooltip(Tooltip.create(Component.translatable("betterzoom.config.cinematicbars.tooltip")));
            addRow(rows, "betterzoom.config.cinematicbars.label", cinematicBarsButton);
        }
    }

    class ExtrasTab extends SettingsTab {
        ExtrasTab() {
            super(Component.translatable("betterzoom.config.tab.extras"));
            GridLayout.RowHelper rows = layout.rowSpacing(4).columnSpacing(10).createRowHelper(2);

            zoomSoundButton = new ToggleOptionButton(0, 0, 180, 20, Config.ZOOM_SOUND.get());
            zoomSoundButton.setTooltip(Tooltip.create(Component.translatable("betterzoom.config.zoomsound.tooltip")));
            addRow(rows, "betterzoom.config.zoomsound.label", zoomSoundButton);

            zoomLevelButton = new ToggleOptionButton(0, 0, 180, 20, Config.ZOOM_LEVEL.get());
            zoomLevelButton.setTooltip(Tooltip.create(Component.translatable("betterzoom.config.zoomlevel.tooltip")));
            addRow(rows, "betterzoom.config.zoomlevel.label", zoomLevelButton);

            settingsHintButton = new ToggleOptionButton(0, 0, 180, 20, Config.SETTINGS_HINT.get());
            settingsHintButton.setTooltip(Tooltip.create(Component.translatable("betterzoom.config.settingshint.tooltip")));
            addRow(rows, "betterzoom.config.settingshint.label", settingsHintButton);
        }
    }

    private void resetSettings() {
        incrementSlider.setActualValue(Config.ZOOM_STEP.getDefault());
        sensitivitySlider.setActualValue(Config.ZOOM_SENSITIVITY_MULTIPLIER.getDefault());
        easingFactorSlider.setActualValue(Config.SMOOTH_EASING_FACTOR.getDefault());
        if (smoothZoomCheckbox.selected() != Config.SMOOTH_ZOOM.getDefault()) {
            smoothZoomCheckbox.onPress();
        }
        if (autoAdjustSensitivityCheckbox.selected() != Config.AUTO_ADJUST_SENSITIVITY.getDefault()) {
            autoAdjustSensitivityCheckbox.onPress();
        }
        holdToZoomButton.setSelected(Config.HOLD_TO_ZOOM.getDefault());
        disableBobbingButton.setSelected(Config.DISABLE_BOBBING_WHILE_ZOOMING.getDefault());
        cinematicCameraButton.setSelected(Config.CINEMATIC_CAMERA.getDefault());
        zoomSoundButton.setSelected(Config.ZOOM_SOUND.getDefault());
        hideGuiButton.setSelected(Config.HIDE_GUI.getDefault());
        hideHandButton.setSelected(Config.HIDE_HAND.getDefault());
        cinematicBarsButton.setSelected(Config.CINEMATIC_BARS.getDefault());
        zoomLevelButton.setSelected(Config.ZOOM_LEVEL.getDefault());
        settingsHintButton.setSelected(Config.SETTINGS_HINT.getDefault());
        zoomModeCycle.setValue(Config.ZOOM_MODE.getDefault());
    }

    private void applySettings() {
        Config.ZOOM_STEP.set(incrementSlider.getIntValue());
        Config.ZOOM_SENSITIVITY_MULTIPLIER.set(Math.round(sensitivitySlider.getActualValue() * 100) / 100.0);
        Config.AUTO_ADJUST_SENSITIVITY.set(autoAdjustSensitivityCheckbox.selected());
        Config.HOLD_TO_ZOOM.set(holdToZoomButton.selected());
        Config.SMOOTH_ZOOM.set(smoothZoomCheckbox.selected());
        Config.ZOOM_MODE.set(zoomModeCycle.getValue());
        Config.SMOOTH_EASING_FACTOR.set(Math.round(easingFactorSlider.getActualValue() * 100) / 100.0);
        Config.DISABLE_BOBBING_WHILE_ZOOMING.set(disableBobbingButton.selected());
        Config.CINEMATIC_CAMERA.set(cinematicCameraButton.selected());
        Config.ZOOM_SOUND.set(zoomSoundButton.selected());
        Config.HIDE_GUI.set(hideGuiButton.selected());
        Config.HIDE_HAND.set(hideHandButton.selected());
        Config.CINEMATIC_BARS.set(cinematicBarsButton.selected());
        Config.ZOOM_LEVEL.set(zoomLevelButton.selected());
        Config.SETTINGS_HINT.set(settingsHintButton.selected());
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        float alpha = previewing ? PREVIEW_ALPHA : 1.0f;
        int alphaBits = (int) (alpha * 255) << 24;
        if (!previewing) {
            renderBackground(guiGraphics);
        }
        guiGraphics.drawCenteredString(font, title, width / 2, 8, 0xFFFFFF | alphaBits);
        guiGraphics.fill(0, 22, width, 23, LINE_COLOR | alphaBits);
        guiGraphics.fill(0, 48, width, 49, LINE_COLOR | alphaBits);
        guiGraphics.fill(0, height - 37, width, height - 36, LINE_COLOR | alphaBits);
        if (minecraft.level != null) {
            String hintKey = holdToZoomButton.selected() ? "betterzoom.config.preview.hold" : "betterzoom.config.preview.press";
            guiGraphics.drawCenteredString(font, Component.translatable(hintKey, Keybinds.ZOOM_KEY.getTranslatedKeyMessage()), width / 2, height - 49, 0xA0A0A0 | alphaBits);
        }
        for (Renderable renderable : renderables) {
            if (renderable instanceof StringWidget label) {
                label.setColor(0xFFFFFF | alphaBits);
            } else if (renderable instanceof AbstractWidget widget) {
                widget.setAlpha(alpha);
            }
        }
        super.render(guiGraphics, previewing ? -1 : mouseX, previewing ? -1 : mouseY, partialTicks);
    }

    @Override
    public void onClose() {
        applySettings();
        Config.SPEC.save();
        Minecraft.getInstance().setScreen(parent);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        InputConstants.Key key = InputConstants.getKey(keyCode, scanCode);
        if (Keybinds.CONFIG_KEY.isActiveAndMatches(key)) {
            this.onClose();
            // don't zoom after clsoing the config screen
            Keybinds.ZOOM_KEY.setDown(false);
            return true;
        }
        if (Keybinds.ZOOM_KEY.isActiveAndMatches(key)) {
            zoomKeyPressed();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        if (Keybinds.ZOOM_KEY.isActiveAndMatches(InputConstants.getKey(keyCode, scanCode))) {
            zoomKeyReleased();
            return true;
        }
        return super.keyReleased(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        InputConstants.Key key = InputConstants.Type.MOUSE.getOrCreate(button);
        if (Keybinds.CONFIG_KEY.isActiveAndMatches(key)) {
            this.onClose();
            return true;
        }
        if (Keybinds.ZOOM_KEY.isActiveAndMatches(key)) {
            zoomKeyPressed();
            return true;
        }
        if (previewing) {
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (previewing && ZoomHandler.scrollZoom(delta)) {
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (Keybinds.ZOOM_KEY.isActiveAndMatches(InputConstants.Type.MOUSE.getOrCreate(button))) {
            zoomKeyReleased();
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    class TabButton extends AbstractButton {
        private final Tab tab;

        TabButton(Tab tab) {
            super(0, 0, 100, 20, tab.getTabTitle());
            this.tab = tab;
        }

        @Override
        public void onPress() {
            tabManager.setCurrentTab(tab, true);
        }

        @Override
        protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
            boolean selected = tabManager.getCurrentTab() == tab;
            int alphaBits = (int) (alpha * 255) << 24;
            int color = selected || isHoveredOrFocused() ? 0xFFFFFF : 0xA0A0A0;
            guiGraphics.drawCenteredString(font, getMessage(), getX() + width / 2, getY() + (height - 8) / 2, color | alphaBits);
            if (selected) {
                int textWidth = font.width(getMessage());
                guiGraphics.fill(getX() + (width - textWidth) / 2, getY() + height - 3, getX() + (width + textWidth) / 2, getY() + height - 2, 0xFFFFFF | alphaBits);
            }
        }

        @Override
        protected void updateWidgetNarration(@NotNull NarrationElementOutput output) {
            defaultButtonNarrationText(output);
        }
    }

    public abstract static class BaseSlider extends AbstractSliderButton {
        protected final double min;
        protected final double max;

        public BaseSlider(int x, int y, int width, int height, double min, double max, double currentValue) {
            super(x, y, width, height, Component.empty(), (currentValue - min) / (max - min));
            this.min = min;
            this.max = max;
        }

        @Override
        protected void applyValue() {
            updateMessage();
        }

        public double getActualValue() {
            return min + (max - min) * value;
        }

        public void setActualValue(double actualValue) {
            this.value = (actualValue - min) / (max - min);
            updateMessage();
        }
    }

    public static class IntSlider extends BaseSlider {
        public IntSlider(int x, int y, int width, int height, double min, double max, double currentValue) {
            super(x, y, width, height, min, max, currentValue);
            updateMessage();
        }

        @Override
        protected void updateMessage() {
            setMessage(Component.literal(String.valueOf(getIntValue())));
        }

        @Override
        protected void applyValue() {
            this.value = (getIntValue() - min) / (max - min);
            updateMessage();
        }

        public int getIntValue() {
            return (int) Math.round(getActualValue());
        }
    }

    public static class DecimalSlider extends BaseSlider {
        public DecimalSlider(int x, int y, int width, int height, double min, double max, double currentValue) {
            super(x, y, width, height, min, max, currentValue);
            updateMessage();
        }

        @Override
        protected void updateMessage() {
            setMessage(Component.literal(String.format("%.2f", getActualValue())));
        }
    }

    public static class ToggleOptionButton extends Button {
        private boolean selected;

        public ToggleOptionButton(int x, int y, int width, int height, boolean selected) {
            super(x, y, width, height, Component.empty(), btn -> {}, DEFAULT_NARRATION);
            setSelected(selected);
        }

        @Override
        public void onPress() {
            setSelected(!this.selected);
        }

        public boolean selected() {
            return this.selected;
        }

        public void setSelected(boolean selected) {
            this.selected = selected;
            setMessage(Component.translatable(this.selected ? "betterzoom.config.toggle.enabled" : "betterzoom.config.toggle.disabled").withStyle(this.selected ? ChatFormatting.GREEN : ChatFormatting.RED));
        }
    }
}
