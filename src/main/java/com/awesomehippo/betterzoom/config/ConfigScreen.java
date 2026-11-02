package com.awesomehippo.betterzoom.config;

import com.awesomehippo.betterzoom.ZoomHandler;
import com.awesomehippo.betterzoom.keybinds.Keybinds;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraftforge.common.ForgeConfigSpec;
import org.jetbrains.annotations.NotNull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConfigScreen extends Screen {
    private static final int LINE_COLOR = 0x505050;
    private static final float PREVIEW_ALPHA = 0.3f;
    private final Screen parent;
    private final Map<AbstractWidget, Component> tooltips = new HashMap<>();
    private final List<SettingsTab> tabs = new ArrayList<>();
    private final List<TabButton> tabButtons = new ArrayList<>();
    private SettingsTab currentTab;
    private Button resetButton;
    private Button doneButton;
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
        super(new TranslatableComponent("betterzoom.config.title"));
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
        tabs.add(generalTab);
        tabs.add(new VisualsTab());
        tabs.add(new ExtrasTab());
        for (SettingsTab tab : tabs) {
            tabButtons.add(addRenderableWidget(new TabButton(tab)));
        }

        // reset & done (save)
        resetButton = addRenderableWidget(new Button(0, 0, 150, 20, new TranslatableComponent("controls.reset"), button -> resetSettings()));
        tooltips.put(resetButton, new TranslatableComponent("betterzoom.config.reset.tooltip"));
        doneButton = addRenderableWidget(new Button(0, 0, 150, 20, new TranslatableComponent("gui.done"), button -> onClose()));
        tooltips.put(doneButton, new TranslatableComponent("betterzoom.config.save.tooltip"));

        for (SettingsTab tab : tabs) {
            for (List<AbstractWidget> row : tab.rows) {
                for (AbstractWidget widget : row) {
                    addRenderableWidget(widget);
                }
            }
        }
        setCurrentTab(generalTab);
        repositionElements();
    }

    @Override
    public void resize(@NotNull Minecraft minecraft, int width, int height) {
        this.width = width;
        this.height = height;
        repositionElements();
    }

    private void repositionElements() {
        for (int i = 0; i < tabButtons.size(); i++) {
            tabButtons.get(i).x = (width - 320) / 2 + i * 110;
            tabButtons.get(i).y = 26;
        }
        resetButton.x = (width - 310) / 2;
        resetButton.y = height - 28;
        doneButton.x = (width - 310) / 2 + 160;
        doneButton.y = height - 28;
        // same top for every tab
        for (SettingsTab tab : tabs) {
            for (int row = 0; row < tab.rows.size(); row++) {
                int x = (width - 310) / 2 + 130;
                for (AbstractWidget widget : tab.rows.get(row)) {
                    widget.x = x;
                    widget.y = 64 + row * 24;
                    x += widget.getWidth() + 4;
                }
            }
        }
    }

    private void setCurrentTab(SettingsTab tab) {
        currentTab = tab;
        for (SettingsTab other : tabs) {
            for (List<AbstractWidget> row : other.rows) {
                for (AbstractWidget widget : row) {
                    widget.visible = other == tab;
                }
            }
        }
    }

    abstract static class SettingsTab {
        private final Component title;
        private final List<Component> labels = new ArrayList<>();
        private final List<List<AbstractWidget>> rows = new ArrayList<>();

        SettingsTab(Component title) {
            this.title = title;
        }

        void addRow(String labelKey, AbstractWidget... widgets) {
            labels.add(new TranslatableComponent(labelKey));
            rows.add(List.of(widgets));
        }
    }

    class GeneralTab extends SettingsTab {
        GeneralTab() {
            super(new TranslatableComponent("betterzoom.config.tab.general"));

            incrementSlider = new IntSlider(0, 0, 180, 20, Config.MIN_ZOOM_INCREMENT, Config.MAX_ZOOM_INCREMENT, Config.ZOOM_STEP.get());
            tooltips.put(incrementSlider, new TranslatableComponent("betterzoom.config.zoomstep.tooltip"));
            addRow("betterzoom.config.zoomstep.label", incrementSlider);

            holdToZoomButton = new ToggleOptionButton(0, 0, 180, 20, Config.HOLD_TO_ZOOM.get());
            tooltips.put(holdToZoomButton, new TranslatableComponent("betterzoom.config.hold.tooltip"));
            addRow("betterzoom.config.hold.label", holdToZoomButton);

            zoomModeCycle = CycleButton.builder(Config.ZoomMode::getDisplayName)
                    .withValues(Config.ZoomMode.values())
                    .withInitialValue(Config.ZOOM_MODE.get())
                    .displayOnlyValue()
                    .create(0, 0, 180, 20, new TranslatableComponent("betterzoom.config.zoommode.label"));
            tooltips.put(zoomModeCycle, new TranslatableComponent("betterzoom.config.zoommode.tooltip"));
            addRow("betterzoom.config.zoommode.label", zoomModeCycle);

            easingFactorSlider = new DecimalSlider(0, 0, 88, 20, Config.MIN_SMOOTH_EASE, Config.MAX_SMOOTH_EASE, Config.SMOOTH_EASING_FACTOR.get());
            tooltips.put(easingFactorSlider, new TranslatableComponent("betterzoom.config.smooth_easing.tooltip"));
            easingFactorSlider.active = Config.SMOOTH_ZOOM.get();
            smoothZoomCheckbox = new Checkbox(0, 0, 88, 20, new TranslatableComponent("betterzoom.config.smooth.label"), Config.SMOOTH_ZOOM.get()) {
                @Override
                public void onPress() {
                    super.onPress();
                    easingFactorSlider.active = this.selected();
                }
            };
            tooltips.put(smoothZoomCheckbox, new TranslatableComponent("betterzoom.config.smooth.tooltip"));
            addRow("betterzoom.config.smooth_easing.label", easingFactorSlider, smoothZoomCheckbox);

            sensitivitySlider = new DecimalSlider(0, 0, 88, 20, Config.MIN_SENSITIVITY, Config.MAX_SENSITIVITY, Config.ZOOM_SENSITIVITY_MULTIPLIER.get());
            tooltips.put(sensitivitySlider, new TranslatableComponent("betterzoom.config.sensitivity.tooltip"));
            sensitivitySlider.active = !Config.AUTO_ADJUST_SENSITIVITY.get();
            autoAdjustSensitivityCheckbox = new Checkbox(0, 0, 88, 20, new TranslatableComponent("betterzoom.config.autosensitivity.label"), Config.AUTO_ADJUST_SENSITIVITY.get()) {
                @Override
                public void onPress() {
                    super.onPress();
                    sensitivitySlider.active = !this.selected();
                }
            };
            tooltips.put(autoAdjustSensitivityCheckbox, new TranslatableComponent("betterzoom.config.autosensitivity.tooltip"));
            addRow("betterzoom.config.sensitivity.label", sensitivitySlider, autoAdjustSensitivityCheckbox);
        }
    }

    class VisualsTab extends SettingsTab {
        VisualsTab() {
            super(new TranslatableComponent("betterzoom.config.tab.visuals"));

            hideGuiButton = new ToggleOptionButton(0, 0, 180, 20, Config.HIDE_GUI.get());
            tooltips.put(hideGuiButton, new TranslatableComponent("betterzoom.config.hidegui.tooltip"));
            addRow("betterzoom.config.hidegui.label", hideGuiButton);

            hideHandButton = new ToggleOptionButton(0, 0, 180, 20, Config.HIDE_HAND.get());
            tooltips.put(hideHandButton, new TranslatableComponent("betterzoom.config.hidehand.tooltip"));
            addRow("betterzoom.config.hidehand.label", hideHandButton);

            disableBobbingButton = new ToggleOptionButton(0, 0, 180, 20, Config.DISABLE_BOBBING_WHILE_ZOOMING.get());
            tooltips.put(disableBobbingButton, new TranslatableComponent("betterzoom.config.disablebobbing.tooltip"));
            addRow("betterzoom.config.disablebobbing.label", disableBobbingButton);

            cinematicCameraButton = new ToggleOptionButton(0, 0, 180, 20, Config.CINEMATIC_CAMERA.get());
            tooltips.put(cinematicCameraButton, new TranslatableComponent("betterzoom.config.cinematic.tooltip"));
            addRow("betterzoom.config.cinematic.label", cinematicCameraButton);

            cinematicBarsButton = new ToggleOptionButton(0, 0, 180, 20, Config.CINEMATIC_BARS.get());
            tooltips.put(cinematicBarsButton, new TranslatableComponent("betterzoom.config.cinematicbars.tooltip"));
            addRow("betterzoom.config.cinematicbars.label", cinematicBarsButton);
        }
    }

    class ExtrasTab extends SettingsTab {
        ExtrasTab() {
            super(new TranslatableComponent("betterzoom.config.tab.extras"));

            zoomSoundButton = new ToggleOptionButton(0, 0, 180, 20, Config.ZOOM_SOUND.get());
            tooltips.put(zoomSoundButton, new TranslatableComponent("betterzoom.config.zoomsound.tooltip"));
            addRow("betterzoom.config.zoomsound.label", zoomSoundButton);

            zoomLevelButton = new ToggleOptionButton(0, 0, 180, 20, Config.ZOOM_LEVEL.get());
            tooltips.put(zoomLevelButton, new TranslatableComponent("betterzoom.config.zoomlevel.tooltip"));
            addRow("betterzoom.config.zoomlevel.label", zoomLevelButton);

            settingsHintButton = new ToggleOptionButton(0, 0, 180, 20, Config.SETTINGS_HINT.get());
            tooltips.put(settingsHintButton, new TranslatableComponent("betterzoom.config.settingshint.tooltip"));
            addRow("betterzoom.config.settingshint.label", settingsHintButton);
        }
    }

    private void resetSettings() {
        incrementSlider.setActualValue(getDefault(Config.ZOOM_STEP));
        sensitivitySlider.setActualValue(getDefault(Config.ZOOM_SENSITIVITY_MULTIPLIER));
        easingFactorSlider.setActualValue(getDefault(Config.SMOOTH_EASING_FACTOR));
        if (smoothZoomCheckbox.selected() != getDefault(Config.SMOOTH_ZOOM)) {
            smoothZoomCheckbox.onPress();
        }
        if (autoAdjustSensitivityCheckbox.selected() != getDefault(Config.AUTO_ADJUST_SENSITIVITY)) {
            autoAdjustSensitivityCheckbox.onPress();
        }
        holdToZoomButton.setSelected(getDefault(Config.HOLD_TO_ZOOM));
        disableBobbingButton.setSelected(getDefault(Config.DISABLE_BOBBING_WHILE_ZOOMING));
        cinematicCameraButton.setSelected(getDefault(Config.CINEMATIC_CAMERA));
        zoomSoundButton.setSelected(getDefault(Config.ZOOM_SOUND));
        hideGuiButton.setSelected(getDefault(Config.HIDE_GUI));
        hideHandButton.setSelected(getDefault(Config.HIDE_HAND));
        cinematicBarsButton.setSelected(getDefault(Config.CINEMATIC_BARS));
        zoomLevelButton.setSelected(getDefault(Config.ZOOM_LEVEL));
        settingsHintButton.setSelected(getDefault(Config.SETTINGS_HINT));
        zoomModeCycle.setValue(getDefault(Config.ZOOM_MODE));
    }

    @SuppressWarnings("unchecked")
    private static <T> T getDefault(ForgeConfigSpec.ConfigValue<T> value) {
        return (T) ((ForgeConfigSpec.ValueSpec) Config.SPEC.getSpec().get(value.getPath())).getDefault();
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
    public void render(@NotNull PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
        float alpha = previewing ? PREVIEW_ALPHA : 1.0f;
        int alphaBits = (int) (alpha * 255) << 24;
        if (!previewing) {
            renderBackground(poseStack);
        }
        drawCenteredString(poseStack, font, title, width / 2, 8, 0xFFFFFF | alphaBits);
        fill(poseStack, 0, 22, width, 23, LINE_COLOR | alphaBits);
        fill(poseStack, 0, 48, width, 49, LINE_COLOR | alphaBits);
        fill(poseStack, 0, height - 37, width, height - 36, LINE_COLOR | alphaBits);
        if (minecraft.level != null) {
            String hintKey = holdToZoomButton.selected() ? "betterzoom.config.preview.hold" : "betterzoom.config.preview.press";
            drawCenteredString(poseStack, font, new TranslatableComponent(hintKey, Keybinds.ZOOM_KEY.getTranslatedKeyMessage()), width / 2, height - 49, 0xA0A0A0 | alphaBits);
        }
        for (int row = 0; row < currentTab.labels.size(); row++) {
            drawString(poseStack, font, currentTab.labels.get(row), (width - 310) / 2, 64 + row * 24 + 5, 0xFFFFFF | alphaBits);
        }
        for (Widget renderable : renderables) {
            if (renderable instanceof AbstractWidget widget) {
                widget.setAlpha(alpha);
            }
        }
        super.render(poseStack, previewing ? -1 : mouseX, previewing ? -1 : mouseY, partialTicks);
        for (Widget renderable : renderables) {
            if (renderable instanceof AbstractWidget widget && widget.visible && widget.isHoveredOrFocused() && tooltips.containsKey(widget)) {
                renderTooltip(poseStack, font.split(tooltips.get(widget), 170), mouseX, mouseY);
            }
        }
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
        private final SettingsTab tab;

        TabButton(SettingsTab tab) {
            super(0, 0, 100, 20, tab.title);
            this.tab = tab;
        }

        @Override
        public void onPress() {
            setCurrentTab(tab);
        }

        @Override
        public void renderButton(@NotNull PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
            boolean selected = currentTab == tab;
            int alphaBits = (int) (alpha * 255) << 24;
            int color = selected || isHoveredOrFocused() ? 0xFFFFFF : 0xA0A0A0;
            drawCenteredString(poseStack, font, getMessage(), x + width / 2, y + (height - 8) / 2, color | alphaBits);
            if (selected) {
                int textWidth = font.width(getMessage());
                fill(poseStack, x + (width - textWidth) / 2, y + height - 3, x + (width + textWidth) / 2, y + height - 2, 0xFFFFFF | alphaBits);
            }
        }

        @Override
        public void updateNarration(@NotNull NarrationElementOutput output) {
            defaultButtonNarrationText(output);
        }
    }

    public abstract static class BaseSlider extends AbstractSliderButton {
        protected final double min;
        protected final double max;

        public BaseSlider(int x, int y, int width, int height, double min, double max, double currentValue) {
            super(x, y, width, height, TextComponent.EMPTY, (currentValue - min) / (max - min));
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
            setMessage(new TextComponent(String.valueOf(getIntValue())));
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
            setMessage(new TextComponent(String.format("%.2f", getActualValue())));
        }
    }

    public static class ToggleOptionButton extends Button {
        private boolean selected;

        public ToggleOptionButton(int x, int y, int width, int height, boolean selected) {
            super(x, y, width, height, TextComponent.EMPTY, btn -> {});
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
            setMessage(new TranslatableComponent(this.selected ? "betterzoom.config.toggle.enabled" : "betterzoom.config.toggle.disabled").withStyle(this.selected ? ChatFormatting.GREEN : ChatFormatting.RED));
        }
    }
}
