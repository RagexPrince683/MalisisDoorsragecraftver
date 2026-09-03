package net.malisis.doors.internal.asm;

import java.util.Map;

import cpw.mods.fml.relauncher.IFMLLoadingPlugin;

/**
 * Loads only the Minecraft hooks required by the standalone large-door support.
 */
@IFMLLoadingPlugin.TransformerExclusions("net.malisis.doors.internal.asm")
@IFMLLoadingPlugin.SortingIndex(1001)
public class MalisisDoorsCorePlugin implements IFMLLoadingPlugin
{
    @Override
    public String[] getASMTransformerClass()
    {
        return new String[] { MalisisDoorsTransformer.class.getName() };
    }

    @Override
    public String getModContainerClass()
    {
        return null;
    }

    @Override
    public String getSetupClass()
    {
        return null;
    }

    @Override
    public void injectData(Map<String, Object> data)
    {
    }

    @Override
    public String getAccessTransformerClass()
    {
        return null;
    }
}
