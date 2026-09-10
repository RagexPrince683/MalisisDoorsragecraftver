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

package net.malisis.doors.door.movement;

import net.malisis.doors.internal.block.BoundingBoxType;
import net.malisis.doors.internal.renderer.RenderParameters;
import net.malisis.doors.internal.renderer.animation.Animation;
import net.malisis.doors.internal.renderer.animation.transformation.Rotation;
import net.malisis.doors.internal.renderer.animation.transformation.Transformation;
import net.malisis.doors.internal.renderer.model.MalisisModel;
import net.malisis.doors.door.DoorState;
import net.malisis.doors.door.block.Door;
import net.malisis.doors.door.tileentity.DoorTileEntity;
import net.malisis.doors.door.tileentity.FenceGateTileEntity;
import net.malisis.doors.internal.renderer.element.Shape;
import net.minecraft.util.AxisAlignedBB;

/**
 * @author Ordinastie
 *
 */
public class FenceGateMovement implements IOptimizedDoorMovement
{

	@Override
	public AxisAlignedBB getBoundingBox(DoorTileEntity tileEntity, boolean topBlock, BoundingBoxType type)
	{
		//never called
		return null;
	}

	public Transformation getTransformation(DoorTileEntity tileEntity, boolean left)
	{
		boolean reversedOpen = ((tileEntity.getBlockMetadata() >> 1) & 1) == 1;
		int direction = tileEntity.getDirection();

		float hinge = -0.5F + 0.125F / 2;
		float angle = 90;
		if (direction == Door.DIR_NORTH || direction == Door.DIR_SOUTH)
			angle = -angle;
		if (!reversedOpen)
			angle = -angle;
		if (left)
		{
			angle = -angle;
			hinge = -hinge;
		}

		Rotation rotation = new Rotation(angle).aroundAxis(0, 1, 0).offset(hinge, 0, 0);
		rotation.reversed(tileEntity.getState() == DoorState.CLOSING || tileEntity.getState() == DoorState.CLOSED);
		rotation.forTicks(tileEntity.getDescriptor().getOpeningTime());

		return rotation;
	}

	@Override
	public void applyPose(DoorTileEntity tileEntity, MalisisModel model, float progress)
	{
		float poseProgress = tileEntity.getState() == DoorState.CLOSING || tileEntity.getState() == DoorState.CLOSED ? 1 - progress : progress;
		boolean reversedOpen = ((tileEntity.getBlockMetadata() >> 1) & 1) == 1;
		float rightAngle = tileEntity.getDirection() == Door.DIR_NORTH || tileEntity.getDirection() == Door.DIR_SOUTH ? -90 : 90;
		if (!reversedOpen)
			rightAngle = -rightAngle;
		rightAngle *= poseProgress;

		if (tileEntity instanceof FenceGateTileEntity)
		{
			FenceGateTileEntity gate = (FenceGateTileEntity) tileEntity;
			int pairSide = gate.getRenderPairSide();
			if (pairSide != 0)
			{
				float angle = pairSide < 0 ? -rightAngle : rightAngle;
				double radians = Math.toRadians(angle);
				double sine = Math.sin(radians);
				double cosine = Math.cos(radians);
				double hinge = pairSide < 0 ? 0.9375D : 0.0625D;
				for (Shape shape : model)
					shape.rotateVerticesAroundY(sine, cosine, hinge, 0.5D);
				return;
			}
		}

		double radians = Math.toRadians(rightAngle);
		double sine = Math.sin(radians);
		double cosine = Math.cos(radians);
		model.getShape("right").rotateVerticesAroundY(sine, cosine, 0.0625D, 0.5D);
		model.getShape("left").rotateVerticesAroundY(-sine, cosine, 0.9375D, 0.5D);
	}

	@Override
	public Animation[] getAnimations(DoorTileEntity tileEntity, MalisisModel model, RenderParameters rp)
	{
		DoorTileEntity doubleDoor = tileEntity.getDoubleDoor();
		if (doubleDoor != null)
		{
			boolean left = true;
			if (tileEntity.getDirection() == Door.DIR_NORTH || tileEntity.getDirection() == Door.DIR_SOUTH)
				left = tileEntity.zCoord < doubleDoor.zCoord;
			else
				left = tileEntity.xCoord > doubleDoor.xCoord;
			return new Animation[] { new Animation(model, getTransformation(tileEntity, left)) };
		}

		return new Animation[] { new Animation(model.getShape("left"), getTransformation(tileEntity, true)),
				new Animation(model.getShape("right"), getTransformation(tileEntity, false)) };
	}

	@Override
	public boolean isSpecial()
	{
		return true;
	}
}
