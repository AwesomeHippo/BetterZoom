package com.awesomehippo.betterzoom.config;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Config {
    private static final Logger LOGGER = LogManager.getLogger(Config.class);

    // default parameters
    public static float ZOOM_STEP = 2.0f; // default incremement
    public static float ZOOM_SENSITIVITY_MULTIPLIER = 0.3f; // default sensitivity
    public static boolean HOLD_TO_ZOOM = true;
    public static boolean ENABLE_SMOOTH_TRANSITION = true;

    private static final Gson GSON = new Gson();
    private static final Path CONFIG_PATH = Path.of("config/betterzoom.json");

    public static void load() {
        if (Files.exists(CONFIG_PATH)) {
            try {
                String json = Files.readString(CONFIG_PATH);
                JsonObject obj = GSON.fromJson(json, JsonObject.class);

                // not too much keys so it'll be fine to hardcode this
                ZOOM_STEP = obj.has("zoomStep") ? obj.get("zoomStep").getAsFloat() : ZOOM_STEP;
                ZOOM_SENSITIVITY_MULTIPLIER = obj.has("zoomSensitivity") ? obj.get("zoomSensitivity").getAsFloat() : ZOOM_SENSITIVITY_MULTIPLIER;
                HOLD_TO_ZOOM = obj.has("holdToZoom") ? obj.get("holdToZoom").getAsBoolean() : HOLD_TO_ZOOM;
                ENABLE_SMOOTH_TRANSITION = obj.has("smoothTransition") ? obj.get("smoothTransition").getAsBoolean() : ENABLE_SMOOTH_TRANSITION;
            } catch (Exception e) {
                LOGGER.error("Failed to load config file: {}", CONFIG_PATH, e);
            }
        }
    }

    public static void save() {
        try {
            JsonObject obj = new JsonObject();
            obj.addProperty("zoomStep", ZOOM_STEP);
            obj.addProperty("zoomSensitivity", ZOOM_SENSITIVITY_MULTIPLIER);
            obj.addProperty("holdToZoom", HOLD_TO_ZOOM);
            obj.addProperty("smoothTransition", ENABLE_SMOOTH_TRANSITION);

            Files.writeString(CONFIG_PATH, GSON.toJson(obj), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (Exception e) {
            LOGGER.error("Failed to save config file: {}", CONFIG_PATH, e);
        }
    }
}
