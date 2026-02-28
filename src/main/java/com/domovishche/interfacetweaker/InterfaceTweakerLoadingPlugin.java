package com.domovishche.interfacetweaker;

import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import zone.rong.mixinbooter.IEarlyMixinLoader;

import java.util.Collections;
import java.util.List;

@IFMLLoadingPlugin.MCVersion("1.12.2")
public class InterfaceTweakerLoadingPlugin implements IFMLLoadingPlugin, IEarlyMixinLoader {

    @Override
    public List<String> getMixinConfigs() {
        return Collections.singletonList("interfacetweaker.default.mixin.json");
    }

    @Override public String[] getASMTransformerClass() { return new String[0]; }
    @Override public String getModContainerClass() { return null; }
    @Override public String getSetupClass() { return null; }
    @Override public void injectData(java.util.Map<String, Object> data) {}
    @Override public String getAccessTransformerClass() { return null; }
}
