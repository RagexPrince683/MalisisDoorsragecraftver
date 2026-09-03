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

package net.malisis.doors.internal.renderer.icon;

import java.awt.image.BufferedImage;
import java.util.HashSet;
import java.util.Set;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.resources.data.AnimationMetadataSection;
import net.minecraft.util.IIcon;

/**
 * Extension of {@link TextureAtlasSprite} to allow common operations like clipping and offset.<br>
 * Allows to have an IIcon that is not registered, but depending on a registered one.
 *
 * @author Ordinastie
 *
 */
public class MalisisIcon extends TextureAtlasSprite
{
	/**
	 * Width of the global texture sheet.
	 */
	protected int sheetWidth;
	/**
	 * Height of the global texture sheet.
	 */
	protected int sheetHeight;
	/**
	 * Raw texture coordinates. TextureAtlasSprite keeps its corresponding fields private,
	 * so operations performed by this class must maintain their own writable state.
	 */
	protected float rawMinU;
	protected float rawMaxU;
	protected float rawMinV;
	protected float rawMaxV;
	/**
	 * Whether the sprite dimensions include Minecraft's anisotropic filtering border.
	 */
	protected boolean anisotropicFiltering;

	/**
	 * Is the icon flipped on the horizontal axis.
	 */
	protected boolean flippedU = false;
	/**
	 * Is the icon flipped on the vertical axis.
	 */
	protected boolean flippedV = false;
	/**
	 * Rotation value (clockwise)
	 */
	protected int rotation = 0;
	/**
	 * Lists of MalisisIcon depending on this one.
	 */
	protected Set<MalisisIcon> dependants = new HashSet<>();

	public MalisisIcon()
	{
		super("");
		rawMaxU = 1;
		rawMaxV = 1;
	}

	public MalisisIcon(String name)
	{
		super(name);
	}

	public MalisisIcon(MalisisIcon baseIcon)
	{
		super(baseIcon.getIconName());
		baseIcon.addDependant(this);
	}

	public MalisisIcon(String name, float u, float v, float U, float V)
	{
		this(name);
		setUVs(u, v, U, V);
	}

	public MalisisIcon(IIcon icon)
	{
		this(icon.getIconName(), icon.getMinU(), icon.getMinV(), icon.getMaxU(), icon.getMaxV());
	}

	/**
	 * Adds a {@link MalisisIcon} to be dependant on this one. Will call {@link #initIcon(MalisisIcon, int, int, int, int, boolean)} when
	 * stiched to the sheet.
	 *
	 * @param icon the icon
	 */
	public void addDependant(MalisisIcon icon)
	{
		dependants.add(icon);
	}

	//#region getters/setters
	/**
	 * Sets the size in pixel of this {@link MalisisIcon}.
	 *
	 * @param width the width
	 * @param height the height
	 */
	public void setSize(int width, int height)
	{
		this.width = width;
		this.height = height;
	}

	public void setUVs(float u, float v, float U, float V)
	{
		rawMinU = u;
		rawMinV = v;
		rawMaxU = U;
		rawMaxV = V;
	}

	@Override
	public float getMinU()
	{
		return this.flippedU ? rawMaxU : rawMinU;
	}

	@Override
	public float getMaxU()
	{
		return this.flippedU ? rawMinU : rawMaxU;
	}

	@Override
	public float getMinV()
	{
		return this.flippedV ? rawMaxV : rawMinV;
	}

	@Override
	public float getMaxV()
	{
		return this.flippedV ? rawMinV : rawMaxV;
	}

	@Override
	public float getInterpolatedU(double value)
	{
		return rawMinU + (rawMaxU - rawMinU) * (float) value / 16.0F;
	}

	@Override
	public float getInterpolatedV(double value)
	{
		return rawMinV + (rawMaxV - rawMinV) * (float) value / 16.0F;
	}

	/**
	 * Sets this {@link MalisisIcon} to be flipped.
	 *
	 * @param horizontal whether to flip horizontally
	 * @param vertical whether to flip vertically
	 * @return this {@link MalisisIcon}
	 */
	public MalisisIcon flip(boolean horizontal, boolean vertical)
	{
		flippedU = horizontal;
		flippedV = vertical;
		return this;
	}

	/**
	 * @return true if this {@link MalisisIcon} is flipped horizontally.
	 */
	public boolean isFlippedU()
	{
		return flippedU;
	}

	/**
	 * @return true if this {@link MalisisIcon} is flipped vertically.
	 */
	public boolean isFlippedV()
	{
		return flippedV;
	}

	/**
	 * @return true fi this {@link MalisisIcon} is rotated.
	 */
	public boolean isRotated()
	{
		return rotation != 0;
	}

	/**
	 * Sets the rotation for this {@link MalisisIcon}. The icon will be rotated <b>rotation</b> x 90 degrees clockwise.
	 *
	 * @param rotation the rotation
	 */
	public void setRotation(int rotation)
	{
		this.rotation = rotation;
	}

	/**
	 * @return the rotation for this {@link MalisisIcon}.
	 */
	public int getRotation()
	{
		return rotation;
	}

	//#end getters/setters

	/**
	 * Initializes this {@link MalisisIcon}. Called from the icon this one depends on, copying the <b>baseIcon</b> values.
	 *
	 * @param baseIcon the base icon
	 * @param width the width
	 * @param height the height
	 * @param x the x
	 * @param y the y
	 * @param rotated the rotated
	 */
	protected void initIcon(MalisisIcon baseIcon, int width, int height, int x, int y, boolean rotated)
	{
		copyFrom(baseIcon);
	}

	/**
	 * Offsets this {@link MalisisIcon} by a specified amount. <b>offsetX</b> and <b>offsetY</b> are specified in pixels.
	 *
	 * @param offsetX the x offset
	 * @param offsetY the y offset
	 * @return this {@link MalisisIcon}
	 */
	public MalisisIcon offset(int offsetX, int offsetY)
	{
		initSprite(sheetWidth, sheetHeight, getOriginX() + offsetX, getOriginY() + offsetY, isRotated());
		return this;
	}

	/**
	 * Clips this {@link MalisisIcon}. <b>offsetX</b>, <b>offsetY</b>, <b>width</b> and <b>height</b> are specified in pixels.
	 *
	 * @param offsetX the x offset
	 * @param offsetY the y offset
	 * @param width the width
	 * @param height the height
	 * @return this {@link MalisisIcon}
	 */
	public MalisisIcon clip(int offsetX, int offsetY, int width, int height)
	{
		this.width = width + (anisotropicFiltering ? 16 : 0);
		this.height = height + (anisotropicFiltering ? 16 : 0);
		offset(offsetX, offsetY);

		return this;
	}

	/**
	 * Clips this {@link MalisisIcon}. <b>offsetXFactor</b>, <b>offsetYFactor</b>, <b>widthFactor</b> and <b>heightFactor</b> are values
	 * from zero to one.
	 *
	 * @param offsetXFactor the x factor for offset
	 * @param offsetYFactor the y factor for offset
	 * @param widthFactor the width factor
	 * @param heightFactor the height factor
	 * @return this {@link MalisisIcon}
	 */
	public MalisisIcon clip(float offsetXFactor, float offsetYFactor, float widthFactor, float heightFactor)
	{
		if (anisotropicFiltering)
		{
			width -= 16;
			height -= 16;
		}

		int offsetX = Math.round(width * offsetXFactor);
		int offsetY = Math.round(height * offsetYFactor);

		width = Math.round(width * widthFactor);
		height = Math.round(height * heightFactor);

		if (anisotropicFiltering)
		{
			width += 16;
			height += 16;
		}

		offset(offsetX, offsetY);

		return this;
	}

	/**
	 * Called when the part represented by this {@link MalisisIcon} is stiched to the texture. Sets most of the icon fields.
	 *
	 * @param width the width
	 * @param height the height
	 * @param x the x
	 * @param y the y
	 * @param rotated the rotated
	 */
	@Override
	public void initSprite(int width, int height, int x, int y, boolean rotated)
	{
		this.sheetWidth = width;
		this.sheetHeight = height;
		super.initSprite(width, height, x, y, rotated);

		float insetU = (float) (0.01D / (double) width);
		float insetV = (float) (0.01D / (double) height);
		int anisotropicOffset = anisotropicFiltering ? 8 : 0;
		rawMinU = (float) (x + anisotropicOffset) / (float) width + insetU;
		rawMaxU = (float) (x + this.width - anisotropicOffset) / (float) width - insetU;
		rawMinV = (float) (y + anisotropicOffset) / (float) height + insetV;
		rawMaxV = (float) (y + this.height - anisotropicOffset) / (float) height - insetV;

		for (MalisisIcon dep : dependants)
			dep.initIcon(this, width, height, x, y, rotated);
	}

	@Override
	public void loadSprite(BufferedImage[] images, AnimationMetadataSection animationMetadata, boolean anisotropicFiltering)
	{
		this.anisotropicFiltering = anisotropicFiltering;
		super.loadSprite(images, animationMetadata, anisotropicFiltering);
	}

	/**
	 * Copies the values from {@link MalisisIcon base} to this {@link MalisisIcon}.
	 *
	 * @param base the icon to copy from
	 */
	public void copyFrom(MalisisIcon base)
	{
		super.copyFrom(base);
		this.rawMinU = base.rawMinU;
		this.rawMaxU = base.rawMaxU;
		this.rawMinV = base.rawMinV;
		this.rawMaxV = base.rawMaxV;
		this.anisotropicFiltering = base.anisotropicFiltering;
		this.sheetWidth = base.sheetWidth;
		this.sheetHeight = base.sheetHeight;
		this.flippedU = base.flippedU;
		this.flippedV = base.flippedV;
		this.rotation = base.rotation;
	}

	/**
	 * Creates a new {@link MalisisIcon} from this <code>MalisisIcon</code>.
	 *
	 * @return the new {@link MalisisIcon}
	 */
	public MalisisIcon copy()
	{
		MalisisIcon icon = new MalisisIcon();
		icon.copyFrom(this);
		return icon;
	}

	/**
	 * Attempts to register this {@link MalisisIcon} to the {@link TextureMap}. If an {@link IIcon} is already registered with this name,
	 * that registered icon will be returned instead.
	 *
	 * @param register the TextureMap
	 * @return this {@link MalisisIcon} if not already registered, otherwise, the MalisisIcon already inside the registry.
	 */
	public MalisisIcon register(TextureMap register)
	{
		TextureAtlasSprite icon = register.getTextureExtry(getIconName());
		if (icon != null)
			return (MalisisIcon) icon;

		register.setTextureEntry(getIconName(), this);
		return this;
	}
}
