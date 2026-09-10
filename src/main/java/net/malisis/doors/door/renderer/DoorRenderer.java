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

package net.malisis.doors.door.renderer;

import net.malisis.doors.internal.renderer.MalisisRenderer;
import net.malisis.doors.internal.renderer.RenderParameters;
import net.malisis.doors.internal.renderer.RenderType;
import net.malisis.doors.internal.renderer.animation.Animation;
import net.malisis.doors.internal.renderer.animation.AnimationRenderer;
import net.malisis.doors.internal.renderer.element.Shape;
import net.malisis.doors.internal.renderer.element.shape.Cube;
import net.malisis.doors.internal.renderer.model.MalisisModel;
import net.malisis.doors.internal.util.Timer;
import net.malisis.doors.MalisisDoorsSettings;
import net.malisis.doors.door.DoorDescriptor;
import net.malisis.doors.door.block.Door;
import net.malisis.doors.door.descriptor.VanillaDoor;
import net.malisis.doors.door.descriptor.WoodDoor;
import net.malisis.doors.door.movement.RotatingDoorMovement;
import net.malisis.doors.door.tileentity.DoorTileEntity;
import net.minecraft.client.renderer.DestroyBlockProgress;

public class DoorRenderer extends MalisisRenderer
{
	protected DoorTileEntity tileEntity;
	protected MalisisModel model;
	protected int direction;
	protected boolean opened;
	protected boolean reversed;
	protected boolean topBlock;

	protected Shape shape;
	protected RenderParameters rp;
	protected AnimationRenderer ar = new AnimationRenderer();

	public DoorRenderer()
	{
		getBlockDamage = true;
	}

	@Override
	protected void initialize()
	{
		Shape bottom = new Cube();
		bottom.setSize(1, 1, Door.DOOR_WIDTH);
		bottom.scale(1, 1, 0.995F);
		Shape top = new Shape(bottom);
		top.translate(0, 1, 0);

		shape = bottom;
		model = new MalisisModel();
		model.addShape("bottom", bottom);
		model.addShape("top", top);

		model.storeState();

		initParams();
	}

	protected void initParams()
	{
		rp = new RenderParameters();
		configureParams(rp);
	}

	protected void configureParams(RenderParameters parameters)
	{
		parameters.renderAllFaces.set(true);
		parameters.calculateAOColor.set(false);
		parameters.useBlockBounds.set(false);
		parameters.useEnvironmentBrightness.set(false);
		parameters.calculateBrightness.set(false);
		parameters.interpolateUV.set(false);
	}

	@Override
	public void render()
	{
		if (renderType == RenderType.ISBRH_WORLD)
		{
			renderStationaryBlock();
			return;
		}

		setTileEntity();

		direction = tileEntity.getDirection();
		opened = tileEntity.isOpened();
		reversed = tileEntity.isReversed();
		topBlock = tileEntity.isTopBlock(x, y, z);

		rp.icon.set(null);

		renderTileEntity();
	}

	/**
	 * Chunk/TESR handoff needs notification after compiled geometry becomes visible. Forge 1.7.10 exposes no such notification, and the
	 * supported Angelica setup does not publish one. Keep this false until a renderer adapter can provide that contract; merely observing
	 * this ISBRH callback proves compilation started, not that its buffers were uploaded.
	 */
	private boolean hasSafeChunkRebuildCompletionHook()
	{
		return false;
	}

	private boolean isHybridEligible(DoorTileEntity door)
	{
		if (!MalisisDoorsSettings.hybridDoorRendering || !hasSafeChunkRebuildCompletionHook())
			return false;
		if (door == null || door.getClass() != DoorTileEntity.class || door.getBlockType() == null || door.getBlockType().getClass() != Door.class)
			return false;

		DoorDescriptor descriptor = door.getDescriptor();
		if (!(descriptor instanceof WoodDoor) && !(descriptor instanceof VanillaDoor))
			return false;
		if (descriptor.getMovement() == null || descriptor.getMovement().getClass() != RotatingDoorMovement.class)
			return false;

		return door.getBlockType().getRenderBlockPass() == 0;
	}

	private void renderStationaryBlock()
	{
		DoorTileEntity door = Door.getDoor(world, x, y, z);
		if (!isHybridEligible(door) || door.isMoving())
			return;

		// Each callback owns exactly its one-block-high half. All data is local because chunk compilation may be concurrent.
		Shape stationaryShape = new Cube();
		stationaryShape.setSize(1, 1, Door.DOOR_WIDTH);
		stationaryShape.scale(1, 1, 0.995F);
		applyStationaryPose(stationaryShape, door);

		RenderParameters stationaryParameters = new RenderParameters();
		configureParams(stationaryParameters);
		stationaryParameters.icon.set(null);
		stationaryParameters.brightness.set(block.getMixedBrightnessForBlock(world, x, y, z));
		drawShape(stationaryShape, stationaryParameters);
	}

	private void applyStationaryPose(Shape stationaryShape, DoorTileEntity door)
	{
		int doorDirection = door.getDirection();
		if (doorDirection == Door.DIR_SOUTH)
			stationaryShape.rotate(180, 0, 1, 0, 0, 0, 0);
		else if (doorDirection == Door.DIR_EAST)
			stationaryShape.rotate(-90, 0, 1, 0, 0, 0, 0);
		else if (doorDirection == Door.DIR_WEST)
			stationaryShape.rotate(90, 0, 1, 0, 0, 0, 0);

		if (door.isCentered())
			stationaryShape.translate(0, 0, 0.5F - Door.DOOR_WIDTH / 2);

		if (door.isOpened())
		{
			float angle = door.isReversed() ? -90 : 90;
			float hingeX = 0.5F - Door.DOOR_WIDTH / 2;
			float hingeZ = -0.5F + Door.DOOR_WIDTH / 2;
			if (door.isReversed())
				hingeX = -hingeX;
			stationaryShape.rotate(angle, 0, 1, 0, hingeX, 0, hingeZ);
		}
	}

	protected void setTileEntity()
	{
		this.tileEntity = (DoorTileEntity) super.tileEntity;
	}

	protected void setup()
	{
		model.resetState();

		if (direction == Door.DIR_SOUTH)
			model.rotate(180, 0, 1, 0, 0, 0, 0);
		if (direction == Door.DIR_EAST)
			model.rotate(-90, 0, 1, 0, 0, 0, 0);
		if (direction == Door.DIR_WEST)
			model.rotate(90, 0, 1, 0, 0, 0, 0);

		if (tileEntity.isCentered())
			model.translate(0, 0, 0.5F - Door.DOOR_WIDTH / 2);
	}

	/** Returns the original timer's linear completion once for the current render. */
	protected float getMovementProgress()
	{
		if (!tileEntity.isMoving())
			return 1;

		long duration = Timer.tickToTime(tileEntity.getOpeningTime());
		if (duration <= 0)
			return 0;

		float progress = (float) (System.currentTimeMillis() - tileEntity.getTimer().getStart()) / duration;
		return Math.max(0, Math.min(1, progress));
	}

	protected void renderTileEntity()
	{
		enableBlending();
		ar.setStartTime(tileEntity.getTimer().getStart());

		setup();

		if (tileEntity.getMovement() != null)
		{
			Animation[] anims = tileEntity.getMovement().getAnimations(tileEntity, model, rp);
			ar.animate(anims);
		}

		//model.render(this, rp);
		rp.brightness.set(block.getMixedBrightnessForBlock(world, x, y, z));
		drawShape(model.getShape("bottom"), rp);

		blockMetadata |= Door.FLAG_TOPBLOCK;
		y++;
		rp.brightness.set(block.getMixedBrightnessForBlock(world, x, y, z));
		drawShape(model.getShape("top"), rp);
		y--;
	}

	@Override
	protected boolean isCurrentBlockDestroyProgress(DestroyBlockProgress dbp)
	{
		return dbp.getPartialBlockX() == x && (dbp.getPartialBlockY() == y || dbp.getPartialBlockY() == y + 1)
				&& dbp.getPartialBlockZ() == z;
	}

	@Override
	public boolean shouldRender3DInInventory(int modelId)
	{
		return false;
	}

}
