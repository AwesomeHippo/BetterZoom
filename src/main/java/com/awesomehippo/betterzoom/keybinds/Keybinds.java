package com.awesomehippo.betterzoom.keybinds;

import com.awesomehippo.betterzoom.BetterZoom;
import net.minecraft.client.KeyMapping;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.bus.api.SubscribeEvent;

import org.lwjgl.glfw.GLFW;

@EventBusSubscriber(modid = BetterZoom.MOD_ID, value = Dist.CLIENT)
public class Keybinds {
    public static final String CATEGORY = "key.categories.betterzoom";
    // keybind to zoom
    public static final KeyMapping ZOOM_KEY = new KeyMapping(
            "key.betterzoom.zoom",
            GLFW.GLFW_KEY_C,
            CATEGORY
    );
    // keybind to open config ingame
    public static final KeyMapping CONFIG_KEY = new KeyMapping(
            "key.betterzoom.config",
            GLFW.GLFW_KEY_V,
            CATEGORY
    );
    // keybinds to zoom/dezeoom
    public static final KeyMapping ZOOM_IN_KEY = new KeyMapping(
            "key.betterzoom.zoom_in",
            GLFW.GLFW_KEY_KP_ADD,
            CATEGORY
    );
    public static final KeyMapping ZOOM_OUT_KEY = new KeyMapping(
            "key.betterzoom.zoom_out",
            GLFW.GLFW_KEY_KP_SUBTRACT,
            CATEGORY
    );

    @SubscribeEvent
    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(ZOOM_KEY);
        event.register(CONFIG_KEY);
        event.register(ZOOM_IN_KEY);
        event.register(ZOOM_OUT_KEY);
    }
}