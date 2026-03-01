package com.domovishche.interfacetweaker;

import com.domovishche.interfacetweaker.config.ModConfig;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

@Mod(modid = com.domovishche.interfacetweaker.interfacetweaker.MODID, name = com.domovishche.interfacetweaker.interfacetweaker.NAME, version = com.domovishche.interfacetweaker.interfacetweaker.VERSION)
public class interfacetweaker {

    public static final String MODID   = "interfacetweaker";
    public static final String NAME    = "Small Item Tweaker";
    public static final String VERSION = "0.0.0.0.2-beta";

    @Mod.Instance
    public static com.domovishche.interfacetweaker.interfacetweaker INSTANCE;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        // Initialises config — creates/reads <gamedir>/config/mymod.cfg
        ModConfig.init(event.getSuggestedConfigurationFile());
    }
}
