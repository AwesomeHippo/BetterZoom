package com.awesomehippo.betterzoom.keybinds;

import com.awesomehippo.betterzoom.BetterZoom;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.ClientRegistry;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(modid = BetterZoom.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class Keybinds {
    public static final String CATEGORY = "key.categories.betterzoom";
    // keybind to zoom
    public static final KeyMapping ZOOM_KEY = new KeyMapping(
            "key.betterzoom.zoom",
            GLFW.GLFW_KEY_C,
            CATEGORY
    );
    // keybind to open config ingame
    public static final KeyMapping CONFIG_KEY = new KeyMapping("key.betterzoom.config", KeyConflictContext.UNIVERSAL, KeyModifier.ALT, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_C, CATEGORY);
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
    public static void registerKeyMappings(FMLClientSetupEvent event) {
        ClientRegistry.registerKeyBinding(ZOOM_KEY);
        ClientRegistry.registerKeyBinding(CONFIG_KEY);
        ClientRegistry.registerKeyBinding(ZOOM_IN_KEY);
        ClientRegistry.registerKeyBinding(ZOOM_OUT_KEY);
    }
}