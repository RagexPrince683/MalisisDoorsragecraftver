/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 Ordinastie
 */
package net.malisis.doors.door.movement;

import net.malisis.doors.internal.renderer.model.MalisisModel;
import net.malisis.doors.door.tileentity.DoorTileEntity;

/** CPU-side pose path used by renderers that can avoid allocating animation objects. */
public interface IOptimizedDoorMovement extends IDoorMovement
{
	/**
	 * Applies the movement pose to vertices from the model's reset state.
	 *
	 * @param progress linear movement progress in the range 0 through 1
	 */
	public void applyPose(DoorTileEntity tileEntity, MalisisModel model, float progress);
}
