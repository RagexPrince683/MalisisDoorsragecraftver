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

package net.malisis.doors.trapdoor.renderer;

import java.util.ArrayList;

import net.malisis.doors.internal.renderer.RenderParameters;
import net.malisis.doors.internal.renderer.RenderType;
import net.malisis.doors.internal.renderer.animation.Animation;
import net.malisis.doors.door.movement.IOptimizedDoorMovement;
import net.malisis.doors.internal.renderer.element.Face;
import net.malisis.doors.internal.renderer.element.Shape;
import net.malisis.doors.internal.renderer.element.shape.Cube;
import net.malisis.doors.internal.renderer.model.MalisisModel;
import net.malisis.doors.MalisisDoors;
import net.malisis.doors.door.block.Door;
import net.malisis.doors.door.renderer.DoorRenderer;
import net.malisis.doors.trapdoor.block.TrapDoor;
import net.malisis.doors.trapdoor.movement.SlidingTrapDoorMovement;
import net.malisis.doors.trapdoor.movement.TrapDoorMovement;
import net.minecraft.client.renderer.DestroyBlockProgress;
import net.minecraftforge.common.util.ForgeDirection;

/**
 * @author Ordinastie
 *
 */
public class TrapDoorRenderer extends DoorRenderer
{
	RenderParameters rpTop;
	MalisisModel trapDoorModel;
	MalisisModel slidingTrapDoorModel;
	private final StationaryAoEntry[] stationaryAoCache = new StationaryAoEntry[32];
	private final ArrayList<int[][][]> movingAoStorage = new ArrayList<int[][][]>();
	private int activeAoStorageDepth;

	private static class StationaryAoEntry
	{
		private final long geometrySignature;
		private final int[][][] sampleOffsets;

		private StationaryAoEntry(long geometrySignature, int[][][] sampleOffsets)
		{
			this.geometrySignature = geometrySignature;
			this.sampleOffsets = sampleOffsets;
		}
	}

	@Override
	protected void initialize()
	{
		for (int i = 0; i < stationaryAoCache.length; i++)
			stationaryAoCache[i] = null;
		movingAoStorage.clear();
		activeAoStorageDepth = 0;

		Shape s = new Cube();
		s.setSize(1, Door.DOOR_WIDTH, 1);
		s.interpolateUV();

		trapDoorModel = new MalisisModel();
		trapDoorModel.addShape("shape", s);
		trapDoorModel.storeState();

		s.getFace(Face.nameFromDirection(ForgeDirection.UP)).getParameters().calculateAOColor.set(true);

		s = new Cube();
		s.setSize(1, Door.DOOR_WIDTH / 2, 1);
		s.interpolateUV();

		slidingTrapDoorModel = new MalisisModel();
		slidingTrapDoorModel.addShape("shape", s);
		slidingTrapDoorModel.storeState();

		initParams();
	}

	@Override
	public void render()
	{
		if (renderType == RenderType.ISBRH_WORLD)
			return;

		if (renderType == RenderType.ISBRH_INVENTORY)
		{
			model = block == MalisisDoors.Blocks.slidingTrapDoor ? slidingTrapDoorModel : trapDoorModel;
			model.resetState();
			model.translate(0, 0.5F, 0);
			model.render(this, rp);
			return;
		}

		super.render();
	}

	@Override
	protected void setup()
	{
		model = block == MalisisDoors.Blocks.slidingTrapDoor ? slidingTrapDoorModel : trapDoorModel;
		model.resetState();

		float angle = 0;
		if (direction == TrapDoor.DIR_NORTH)
			angle = 180;
		else if (direction == TrapDoor.DIR_EAST)
			angle = 90;
		else if (direction == TrapDoor.DIR_WEST)
			angle = 270;
		model.rotate(angle, 0, 1, 0, 0, 0, 0);

		if (topBlock)
			model.translate(0, 1 - Door.DOOR_WIDTH, 0);

		rp.brightness.set(block.getMixedBrightnessForBlock(world, x, y, z));
	}

	@Override
	protected void renderTileEntity()
	{
		ar.setStartTime(tileEntity.getTimer().getStart());

		setup();

		if (tileEntity.getMovement() instanceof IOptimizedDoorMovement)
		{
			IOptimizedDoorMovement movement = (IOptimizedDoorMovement) tileEntity.getMovement();
			movement.applyPose(tileEntity, model, getMovementProgress());
		}
		else if (tileEntity.getMovement() != null)
		{
			Animation[] anims = tileEntity.getMovement().getAnimations(tileEntity, model, rp);
			ar.animate(anims);
		}

		Shape s = model.getShape("shape");
		Face f = s.getFace(Face.nameFromDirection(ForgeDirection.UP));
		s.applyMatrix();

		int[][][] sampleOffsets = getStationarySampleOffsets(f);
		boolean usesMovingStorage = sampleOffsets == null;
		if (usesMovingStorage)
			sampleOffsets = acquireMovingSampleOffsets(f.getVertexes().length);

		int[][][] previousSampleOffsets = f.getParameters().aoMatrix.getValue();
		try
		{
			if (usesMovingStorage)
				f.calculateAoMatrix(ForgeDirection.UP, sampleOffsets);

			f.getParameters().aoMatrix.set(sampleOffsets);
			model.render(this, rp);
		}
		finally
		{
			f.getParameters().aoMatrix.set(previousSampleOffsets);
			if (usesMovingStorage)
				activeAoStorageDepth--;
		}
	}

	private int[][][] getStationarySampleOffsets(Face face)
	{
		if (tileEntity.isMoving())
			return null;

		int movementIndex;
		if (tileEntity.getMovement() != null && tileEntity.getMovement().getClass() == TrapDoorMovement.class)
			movementIndex = 0;
		else if (tileEntity.getMovement() != null && tileEntity.getMovement().getClass() == SlidingTrapDoorMovement.class)
			movementIndex = 1;
		else
			return null;

		int directionIndex = direction;
		if (directionIndex < 0 || directionIndex > 3)
			return null;

		int cacheIndex = movementIndex * 16 + directionIndex * 4 + (topBlock ? 2 : 0) + (opened ? 1 : 0);
		long geometrySignature = calculateGeometrySignature(face);
		StationaryAoEntry entry = stationaryAoCache[cacheIndex];
		if (entry == null || entry.geometrySignature != geometrySignature)
		{
			int[][][] sampleOffsets = new int[face.getVertexes().length][3][3];
			face.calculateAoMatrix(ForgeDirection.UP, sampleOffsets);
			entry = new StationaryAoEntry(geometrySignature, sampleOffsets);
			stationaryAoCache[cacheIndex] = entry;
		}

		return entry.sampleOffsets;
	}

	private int[][][] acquireMovingSampleOffsets(int vertexCount)
	{
		if (activeAoStorageDepth == movingAoStorage.size())
			movingAoStorage.add(new int[vertexCount][3][3]);

		int[][][] sampleOffsets = movingAoStorage.get(activeAoStorageDepth++);
		if (sampleOffsets.length != vertexCount)
		{
			sampleOffsets = new int[vertexCount][3][3];
			movingAoStorage.set(activeAoStorageDepth - 1, sampleOffsets);
		}
		return sampleOffsets;
	}

	private long calculateGeometrySignature(Face face)
	{
		long signature = 1125899906842597L;
		for (net.malisis.doors.internal.renderer.element.Vertex vertex : face.getVertexes())
		{
			signature = 31 * signature + Double.doubleToLongBits(vertex.getX());
			signature = 31 * signature + Double.doubleToLongBits(vertex.getY());
			signature = 31 * signature + Double.doubleToLongBits(vertex.getZ());
		}
		return signature;
	}

	@Override
	protected boolean isCurrentBlockDestroyProgress(DestroyBlockProgress dbp)
	{
		return dbp.getPartialBlockX() == x && dbp.getPartialBlockY() == y && dbp.getPartialBlockZ() == z;
	}

	@Override
	public boolean shouldRender3DInInventory(int modelId)
	{
		return true;
	}
}
