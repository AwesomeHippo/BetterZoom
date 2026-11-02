package com.awesomehippo.betterzoom;

import com.awesomehippo.betterzoom.config.Config;
import com.awesomehippo.betterzoom.config.ConfigScreen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.ConfigGuiHandler;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.loading.FMLEnvironment;

@Mod(BetterZoom.MOD_ID)
public class BetterZoom {
    public static final String MOD_ID = "betterzoom";

    public BetterZoom() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, Config.SPEC);

        if (FMLEnvironment.dist == Dist.CLIENT) { // clientSideOnly=true is already enough?
            ModLoadingContext.get().registerExtensionPoint(ConfigGuiHandler.ConfigGuiFactory.class,
                    () -> new ConfigGuiHandler.ConfigGuiFactory((mc, screen) -> new ConfigScreen(screen)));
        }// else {} we could log that it only works on the client
    }
}
