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

package net.malisis.doors.entity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import net.malisis.doors.block.GarageDoor;
import net.malisis.doors.door.DoorState;
import net.malisis.doors.door.tileentity.DoorTileEntity;
import net.malisis.doors.internal.util.TileEntityUtils;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

/**
 * Garage door segment. Structure caches contain coordinates rather than tile
 * entity references so that an unloaded or replaced segment cannot be retained.
 */
public class GarageDoorTileEntity extends DoorTileEntity
{
	public static final int maxOpenTime = 10;

	private ChunkCoordinates controllerPosition;
	private List<ChunkCoordinates> segmentPositions;
	private int columnHeight;
	private int cachedDirection = -1;
	private AxisAlignedBB renderBounds;

	public boolean isTopDoor()
	{
		ChunkCoordinates controller = getControllerPosition();
		return controller != null && controller.posX == xCoord && controller.posY == yCoord && controller.posZ == zCoord;
	}

	@Override
	public int getOpeningTime()
	{
		return getColumnHeight() * maxOpenTime;
	}

	public GarageDoorTileEntity getTopDoor()
	{
		ChunkCoordinates controller = getControllerPosition();
		return controller != null ? getGarageDoor(controller.posX, controller.posY, controller.posZ) : null;
	}

	public List<ChunkCoordinates> getSegmentPositions()
	{
		ensureStructureCache();
		if (controllerPosition == null)
			return Collections.emptyList();
		if (controllerPosition.posY == yCoord)
			return segmentPositions;

		GarageDoorTileEntity controller = getGarageDoor(controllerPosition.posX, controllerPosition.posY, controllerPosition.posZ);
		return controller != null ? controller.getSegmentPositions() : Collections.<ChunkCoordinates>emptyList();
	}

	public int getColumnHeight()
	{
		ensureStructureCache();
		return columnHeight;
	}

	public ChunkCoordinates getControllerPosition()
	{
		ensureStructureCache();
		return controllerPosition;
	}

	/** Compatibility helper for callers that need currently loaded segments. */
	public Set<GarageDoorTileEntity> getDoors()
	{
		Set<GarageDoorTileEntity> doors = new LinkedHashSet<>();
		for (ChunkCoordinates position : getSegmentPositions())
		{
			GarageDoorTileEntity door = getGarageDoor(position.posX, position.posY, position.posZ);
			if (door != null)
				doors.add(door);
		}
		return doors;
	}

	public void addChildDoors(Set<GarageDoorTileEntity> childDoors)
	{
		childDoors.addAll(getDoors());
	}

	public GarageDoorTileEntity getGarageDoor(ForgeDirection direction)
	{
		return getGarageDoor(xCoord + direction.offsetX, yCoord + direction.offsetY, zCoord + direction.offsetZ);
	}

	private GarageDoorTileEntity getGarageDoor(int x, int y, int z)
	{
		if (worldObj == null || !worldObj.blockExists(x, y, z))
			return null;

		GarageDoorTileEntity door = TileEntityUtils.getTileEntity(GarageDoorTileEntity.class, worldObj, x, y, z);
		if (door == null || door.getDirection() != getDirection())
			return null;
		return door;
	}

	private void ensureStructureCache()
	{
		if (worldObj == null || isInvalid())
		{
			clearStructureCache();
			return;
		}

		int direction = getDirection();
		if (segmentPositions != null && cachedDirection == direction)
			return;

		rebuildStructureCache(direction);
	}

	private void rebuildStructureCache(int direction)
	{
		clearStructureCache();
		if (!worldObj.blockExists(xCoord, yCoord, zCoord))
			return;

		int top = yCoord;
		for (int scanY = yCoord + 1; scanY < worldObj.getActualHeight(); scanY++)
		{
			if (getGarageDoor(xCoord, scanY, zCoord) == null)
				break;
			top = scanY;
		}

		List<ChunkCoordinates> positions = new ArrayList<>();
		for (int scanY = top; scanY >= 0; scanY--)
		{
			if (getGarageDoor(xCoord, scanY, zCoord) == null)
				break;
			positions.add(new ChunkCoordinates(xCoord, scanY, zCoord));
		}

		if (positions.isEmpty())
			return;

		controllerPosition = positions.get(0);
		segmentPositions = top == yCoord ? Collections.unmodifiableList(positions) : Collections.<ChunkCoordinates>emptyList();
		columnHeight = positions.size();
		cachedDirection = direction;
	}

	public void invalidateStructureCache()
	{
		clearStructureCache();
	}

	private void clearStructureCache()
	{
		controllerPosition = null;
		segmentPositions = null;
		columnHeight = 0;
		cachedDirection = -1;
		renderBounds = null;
	}

	public static void invalidateColumn(World world, int x, int y, int z)
	{
		if (world == null || !world.blockExists(x, y, z))
			return;

		for (Object entry : world.loadedTileEntityList)
		{
			if (!(entry instanceof GarageDoorTileEntity))
				continue;

			GarageDoorTileEntity door = (GarageDoorTileEntity) entry;
			if (!door.isInvalid() && door.xCoord == x && door.zCoord == z)
				door.invalidateStructureCache();
		}
	}

	@Override
	public void validate()
	{
		super.validate();
		clearStructureCache();
		invalidateColumn(worldObj, xCoord, yCoord, zCoord);
	}

	@Override
	public void invalidate()
	{
		clearStructureCache();
		super.invalidate();
		invalidateColumn(worldObj, xCoord, yCoord, zCoord);
	}

	@Override
	public void onChunkUnload()
	{
		clearStructureCache();
		invalidateColumn(worldObj, xCoord, yCoord, zCoord);
		super.onChunkUnload();
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt)
	{
		clearStructureCache();
		super.readFromNBT(nbt);
		clearStructureCache();
	}

	@Override
	public void setPowered(boolean powered)
	{
		if (isMoving() || isOpened() == powered)
			return;
		if ((state == DoorState.OPENING && powered) || (state == DoorState.CLOSING && !powered))
			return;

		DoorState newState = powered ? DoorState.OPENING : DoorState.CLOSING;
		for (GarageDoorTileEntity door : getDoors())
			door.setDoorState(newState);

		GarageDoorTileEntity door = getGarageDoor(GarageDoor.isEastOrWest(blockMetadata) ? ForgeDirection.NORTH : ForgeDirection.EAST);
		if (door != null)
			door.setPowered(powered);
		door = getGarageDoor(GarageDoor.isEastOrWest(blockMetadata) ? ForgeDirection.SOUTH : ForgeDirection.WEST);
		if (door != null)
			door.setPowered(powered);
	}

	@Override
	public void playSound()
	{}

	@Override
	public void updateEntity()
	{
		if (state != DoorState.CLOSED && state != DoorState.OPENED && timer.elapsedTick() > getOpeningTime())
			setDoorState(state == DoorState.CLOSING ? DoorState.CLOSED : DoorState.OPENED);
	}

	@Override
	public AxisAlignedBB getRenderBoundingBox()
	{
		ensureStructureCache();
		if (renderBounds == null)
		{
			int height = Math.max(1, columnHeight);
			int top = controllerPosition != null ? controllerPosition.posY : yCoord;
			int bottom = top - height + 1;
			// The panel can occupy its vertical column and the full horizontal
			// radius swept while each segment rotates and stacks overhead.
			renderBounds = AxisAlignedBB.getBoundingBox(xCoord - height, bottom, zCoord - height, xCoord + height + 1, top + 2,
					zCoord + height + 1);
		}
		return renderBounds;
	}
}
