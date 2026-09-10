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

package net.malisis.doors.renderer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.malisis.doors.internal.renderer.MalisisRenderer;
import net.malisis.doors.internal.renderer.RenderParameters;
import net.malisis.doors.internal.renderer.RenderType;
import net.malisis.doors.internal.renderer.animation.AnimationRenderer;
import net.malisis.doors.internal.renderer.animation.transformation.ChainedTransformation;
import net.malisis.doors.internal.renderer.animation.transformation.ParallelTransformation;
import net.malisis.doors.internal.renderer.animation.transformation.Rotation;
import net.malisis.doors.internal.renderer.animation.transformation.Transformation;
import net.malisis.doors.internal.renderer.animation.transformation.Translation;
import net.malisis.doors.internal.renderer.element.shape.Cube;
import net.malisis.doors.internal.util.TileEntityUtils;
import net.malisis.doors.door.DoorState;
import net.malisis.doors.door.block.Door;
import net.malisis.doors.entity.GarageDoorTileEntity;
import net.minecraft.client.renderer.DestroyBlockProgress;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChunkCoordinates;

/**
 * @author Ordinastie
 *
 */
public class GarageDoorRenderer extends MalisisRenderer
{
	private GarageDoorTileEntity tileEntity;
	protected int direction;
	protected boolean opened;
	protected boolean reversed;
	protected boolean topBlock;
	protected List<ChunkCoordinates> segmentPositions = Collections.emptyList();
	protected List<Transformation> openingAnimations = Collections.emptyList();
	protected List<Transformation> closingAnimations = Collections.emptyList();
	protected int animationHeight = -1;

	protected AnimationRenderer ar = new AnimationRenderer();

	@Override
	public void renderTileEntityAt(TileEntity tileEntity, double x, double y, double z, float partialTick)
	{
		if (!(tileEntity instanceof GarageDoorTileEntity) || !((GarageDoorTileEntity) tileEntity).isTopDoor())
			return;

		super.renderTileEntityAt(tileEntity, x, y, z, partialTick);
	}

	@Override
	protected void initialize()
	{
		shape = new Cube().setSize(Door.DOOR_WIDTH, 1, 1);
		shape.storeState();

		rp = new RenderParameters();
		rp.renderAllFaces.set(true);
		rp.calculateAOColor.set(false);
		rp.useBlockBounds.set(false);
		rp.useEnvironmentBrightness.set(false);
		rp.calculateBrightness.set(false);
		rp.interpolateUV.set(false);
		rp.useWorldSensitiveIcon.set(false);
	}

	@Override
	public void render()
	{

		if (renderType == RenderType.ITEM_INVENTORY)
		{
			enableBlending();

			shape.resetState();
			shape.translate(0.5F - Door.DOOR_WIDTH / 2, 0, 0);
			rp.icon.set(null);
			blockMetadata = Door.FLAG_TOPBLOCK;
			drawShape(shape, rp);
			return;
		}

		tileEntity = TileEntityUtils.getTileEntity(GarageDoorTileEntity.class, world, x, y, z);
		if (tileEntity == null || !tileEntity.isTopDoor())
		{
			getBlockDamage = false;
			return;
		}

		getBlockDamage = true;

		direction = tileEntity.getDirection();
		opened = tileEntity.isOpened();
		reversed = tileEntity.isReversed();

		rp.icon.set(null);

		enableBlending();
		renderTileEntity();
	}

	protected void renderTileEntity()
	{
		//set the start timer
		ar.setStartTime(tileEntity.getTimer().getStart());

		segmentPositions = tileEntity.getSegmentPositions();
		ensureAnimations(segmentPositions.size());
		for (int index = 0; index < segmentPositions.size(); index++)
		{
			ChunkCoordinates position = segmentPositions.get(index);
			shape.resetState();
			shape.rotate(-90 * tileEntity.getDirection(), 0, 1, 0);
			shape.translate(0.5F - Door.DOOR_WIDTH / 2, 0, 0);

			y = position.posY;
			int delta = tileEntity.yCoord - position.posY;

			if (delta == 0)
				blockMetadata |= Door.FLAG_TOPBLOCK;
			else
				blockMetadata &= ~Door.FLAG_TOPBLOCK;

			rp.brightness.set(block.getMixedBrightnessForBlock(world, x, y, z));

			boolean closing = tileEntity.getState() == DoorState.CLOSING || tileEntity.getState() == DoorState.CLOSED;
			ar.animate(shape, closing ? closingAnimations.get(index) : openingAnimations.get(index));
			drawShape(shape, rp);
		}
		//restore correct y coord
		y = tileEntity.yCoord;
	}

	private void ensureAnimations(int height)
	{
		if (animationHeight == height)
			return;

		List<Transformation> opening = new ArrayList<>();
		List<Transformation> closing = new ArrayList<>();
		for (int delta = 0; delta < height; delta++)
		{
			opening.add(createSegmentAnimation(delta, height, false));
			closing.add(createSegmentAnimation(delta, height, true));
		}
		openingAnimations = Collections.unmodifiableList(opening);
		closingAnimations = Collections.unmodifiableList(closing);
		animationHeight = height;
	}

	private Transformation createSegmentAnimation(int delta, int height, boolean reversed)
	{
		int ticks = GarageDoorTileEntity.maxOpenTime;
		int horizontalDistance = height - delta - 1;
		Transformation vertical = new Translation(0, -delta, 0, 0, 0, 0).forTicks(ticks * delta, 0);
		Transformation rotate = new ParallelTransformation(new Translation(0, 1, 0).forTicks(ticks, 0),
				new Rotation(0, -90).aroundAxis(0, 0, 1).offset(-0.5F, -0.5F, 0).forTicks(ticks, 0));
		Transformation horizontal = new Translation(0, 0, 0, 0, horizontalDistance, 0).forTicks(ticks * horizontalDistance, 0);
		return new ChainedTransformation(vertical, rotate, horizontal).reversed(reversed);
	}

	@Override
	public void renderDestroyProgress()
	{
		rp.icon.set(damagedIcons[destroyBlockProgress.getPartialBlockDamage()]);
		int y = this.y - destroyBlockProgress.getPartialBlockY();
		shape.resetState();
		shape.rotate(-90 * tileEntity.getDirection(), 0, 1, 0);
		shape.translate(0.505F - Door.DOOR_WIDTH / 2, -y, 0);
		shape.scale(1.011F);
		drawShape(shape, rp);
	}

	@Override
	protected boolean isCurrentBlockDestroyProgress(DestroyBlockProgress dbp)
	{
		if (dbp.getPartialBlockX() == x && dbp.getPartialBlockY() == y && dbp.getPartialBlockZ() == z)
			return true;

		for (ChunkCoordinates position : segmentPositions)
		{
			if (dbp.getPartialBlockX() == position.posX && dbp.getPartialBlockY() == position.posY
					&& dbp.getPartialBlockZ() == position.posZ)
				return true;
		}
		return false;
	}

	@Override
	public boolean shouldRender3DInInventory(int modelId)
	{
		return true;
	}
}
