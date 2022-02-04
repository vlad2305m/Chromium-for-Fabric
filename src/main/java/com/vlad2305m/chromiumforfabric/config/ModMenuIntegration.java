package com.vlad2305m.chromiumforfabric.config;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import com.vlad2305m.chromiumforfabric.ChromiumScreen;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;


@Environment(EnvType.CLIENT)
public class ModMenuIntegration implements ModMenuApi
{

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return screen -> new ChromiumScreen();
    }

}