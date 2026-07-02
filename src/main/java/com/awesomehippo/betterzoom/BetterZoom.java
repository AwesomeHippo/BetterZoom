package com.awesomehippo.betterzoom;

import com.awesomehippo.betterzoom.config.Config;
import com.awesomehippo.betterzoom.config.ConfigScreen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

@Mod(value = BetterZoom.MOD_ID, dist = Dist.CLIENT)
public class BetterZoom {
    public static final String MOD_ID = "betterzoom";

    public BetterZoom() {
        ModLoadingContext.get().getActiveContainer().registerConfig(ModConfig.Type.CLIENT, Config.SPEC);

        ModLoadingContext.get().registerExtensionPoint(IConfigScreenFactory.class,
                () -> (container, parent) -> new ConfigScreen(parent));
    }
}
