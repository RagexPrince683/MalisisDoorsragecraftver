package net.malisis.doors.door.tileentity;

import net.malisis.doors.door.block.BigDoor;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;

public class BigDoorProxyTileEntity extends TileEntity
{
	private int originX;
	private int originY;
	private int originZ;
	private boolean originSet;
	private int validationDelay = 20;

	public void setOrigin(int x, int y, int z)
	{
		originX = x;
		originY = y;
		originZ = z;
		originSet = true;
		markDirty();
		if (worldObj != null)
			worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
	}

	public boolean hasOrigin(int x, int y, int z)
	{
		return originSet && originX == x && originY == y && originZ == z;
	}

	public BigDoor getOriginBlock()
	{
		if (!originSet || worldObj == null)
			return null;
		if (!worldObj.blockExists(originX, originY, originZ))
			return null;

		if (worldObj.getBlock(originX, originY, originZ) instanceof BigDoor)
			return (BigDoor) worldObj.getBlock(originX, originY, originZ);
		return null;
	}

	public int getOriginX()
	{
		return originX;
	}

	public int getOriginY()
	{
		return originY;
	}

	public int getOriginZ()
	{
		return originZ;
	}

	@Override
	public void updateEntity()
	{
		if (worldObj == null || worldObj.isRemote || validationDelay-- > 0)
			return;
		validationDelay = 20;

		if (!originSet || worldObj.blockExists(originX, originY, originZ) && getOriginBlock() == null)
			worldObj.setBlockToAir(xCoord, yCoord, zCoord);
	}

	@Override
	public Packet getDescriptionPacket()
	{
		NBTTagCompound nbt = new NBTTagCompound();
		writeToNBT(nbt);
		return new S35PacketUpdateTileEntity(xCoord, yCoord, zCoord, 0, nbt);
	}

	@Override
	public void onDataPacket(NetworkManager network, S35PacketUpdateTileEntity packet)
	{
		readFromNBT(packet.func_148857_g());
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt)
	{
		super.readFromNBT(nbt);
		originSet = nbt.getBoolean("originSet");
		originX = nbt.getInteger("originX");
		originY = nbt.getInteger("originY");
		originZ = nbt.getInteger("originZ");
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt)
	{
		super.writeToNBT(nbt);
		nbt.setBoolean("originSet", originSet);
		nbt.setInteger("originX", originX);
		nbt.setInteger("originY", originY);
		nbt.setInteger("originZ", originZ);
	}
}
