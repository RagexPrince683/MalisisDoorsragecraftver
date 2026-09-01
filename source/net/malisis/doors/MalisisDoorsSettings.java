/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 Ordinastie
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package net.malisis.doors;

import java.io.File;

import net.minecraftforge.common.config.Configuration;

public class MalisisDoorsSettings
{
	private static final String CATEGORY = Configuration.CATEGORY_GENERAL;
	private final Configuration configuration;

	public static boolean modifyVanillaDoors = true;
	public static boolean enableMixedBlocks = true;
	public static boolean enhancedMixedBlockPlacement = true;
	public static boolean simpleMixedBlockRendering = false;
	public static boolean enableVanishingBlocks = true;
	public static boolean enableVanishingGlitch = true;
	public static double vanishingGlitchChance = 0.0005D;
	public static boolean enableCamoFenceGate = true;

	public MalisisDoorsSettings(File file)
	{
		configuration = new Configuration(file);
		load();
	}

	public void load()
	{
		configuration.load();
		modifyVanillaDoors = configuration.getBoolean("modifyVanillaDoors", CATEGORY, true,
				"Replace vanilla doors with animated MalisisDoors equivalents.");
		enableMixedBlocks = configuration.getBoolean("enableMixedBlocks", CATEGORY, true, "Enable mixed blocks.");
		enhancedMixedBlockPlacement = configuration.getBoolean("enhancedMixedBlockPlacement", CATEGORY, true,
				"Use the enhanced mixed-block placement behavior.");
		simpleMixedBlockRendering = configuration.getBoolean("simpleMixedBlockRendering", CATEGORY, false,
				"Use the simpler mixed-block renderer.");
		enableVanishingBlocks = configuration.getBoolean("enableVanishingBlocks", CATEGORY, true, "Enable vanishing blocks.");
		enableVanishingGlitch = configuration.getBoolean("enableVanishingGlitch", CATEGORY, true,
				"Allow the occasional vanishing-block visual glitch.");
		vanishingGlitchChance = configuration.get(CATEGORY, "vanishingGlitchChance", 0.0005D,
				"Chance of a vanishing-block glitch per update.").getDouble(0.0005D);
		enableCamoFenceGate = configuration.getBoolean("enableCamoFenceGate", CATEGORY, true, "Enable camouflage fence gates.");
		save();
	}

	public void save()
	{
		configuration.get(CATEGORY, "simpleMixedBlockRendering", false).set(simpleMixedBlockRendering);
		if (configuration.hasChanged())
			configuration.save();
	}
}
