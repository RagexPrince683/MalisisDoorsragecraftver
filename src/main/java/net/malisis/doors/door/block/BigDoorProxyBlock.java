package net.malisis.doors.door.block;

import java.util.ArrayList;
import java.util.List;

import net.malisis.doors.door.tileentity.BigDoorProxyTileEntity;
import net.malisis.doors.internal.block.BoundingBoxType;
import net.malisis.doors.internal.block.MalisisBlock;
import net.malisis.doors.internal.util.RaytraceBlock;
import net.malisis.doors.internal.util.TileEntityUtils;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BigDoorProxyBlock extends MalisisBlock implements ITileEntityProvider
{
	public BigDoorProxyBlock()
	{
		super(Material.wood);
		setBlockName("big_door_proxy");
		setHardness(5.0F);
		setResistance(10.0F);
	}

	public static void setOrigin(World world, int x, int y, int z, int originX, int originY, int originZ)
	{
		BigDoorProxyTileEntity proxy = TileEntityUtils.getTileEntity(BigDoorProxyTileEntity.class, world, x, y, z);
		if (proxy != null)
			proxy.setOrigin(originX, originY, originZ);
	}

	private BigDoorProxyTileEntity getProxy(IBlockAccess world, int x, int y, int z)
	{
		return TileEntityUtils.getTileEntity(BigDoorProxyTileEntity.class, world, x, y, z);
	}

	@Override
	public AxisAlignedBB[] getBoundingBox(IBlockAccess world, int x, int y, int z, BoundingBoxType type)
	{
		BigDoorProxyTileEntity proxy = getProxy(world, x, y, z);
		BigDoor origin = proxy != null ? proxy.getOriginBlock() : null;
		if (origin == null)
			return new AxisAlignedBB[0];

		AxisAlignedBB cell = AxisAlignedBB.getBoundingBox(x, y, z, x + 1, y + 1, z + 1);
		AxisAlignedBB[] originBoxes = origin.getBoundingBox(world, proxy.getOriginX(), proxy.getOriginY(), proxy.getOriginZ(), type);
		List<AxisAlignedBB> clipped = new ArrayList<AxisAlignedBB>();
		for (AxisAlignedBB box : originBoxes)
		{
			AxisAlignedBB worldBox = box.copy().offset(proxy.getOriginX(), proxy.getOriginY(), proxy.getOriginZ());
			double minX = Math.max(worldBox.minX, cell.minX);
			double minY = Math.max(worldBox.minY, cell.minY);
			double minZ = Math.max(worldBox.minZ, cell.minZ);
			double maxX = Math.min(worldBox.maxX, cell.maxX);
			double maxY = Math.min(worldBox.maxY, cell.maxY);
			double maxZ = Math.min(worldBox.maxZ, cell.maxZ);
			if (minX < maxX && minY < maxY && minZ < maxZ)
				clipped.add(AxisAlignedBB.getBoundingBox(minX - x, minY - y, minZ - z, maxX - x, maxY - y, maxZ - z));
		}
		return clipped.toArray(new AxisAlignedBB[clipped.size()]);
	}

	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ)
	{
		BigDoorProxyTileEntity proxy = getProxy(world, x, y, z);
		BigDoor origin = proxy != null ? proxy.getOriginBlock() : null;
		return origin != null && origin.onBlockActivated(world, proxy.getOriginX(), proxy.getOriginY(), proxy.getOriginZ(), player, side,
				hitX, hitY, hitZ);
	}

	@Override
	public void onBlockClicked(World world, int x, int y, int z, EntityPlayer player)
	{
		BigDoorProxyTileEntity proxy = getProxy(world, x, y, z);
		BigDoor origin = proxy != null ? proxy.getOriginBlock() : null;
		if (origin != null)
			origin.onBlockClicked(world, proxy.getOriginX(), proxy.getOriginY(), proxy.getOriginZ(), player);
	}

	@SuppressWarnings("deprecation")
	@Override
	public boolean removedByPlayer(World world, EntityPlayer player, int x, int y, int z)
	{
		BigDoorProxyTileEntity proxy = getProxy(world, x, y, z);
		BigDoor origin = proxy != null ? proxy.getOriginBlock() : null;
		if (origin == null)
			return world.setBlockToAir(x, y, z);
		return origin.removedByPlayer(world, player, proxy.getOriginX(), proxy.getOriginY(), proxy.getOriginZ());
	}

	@Override
	public MovingObjectPosition collisionRayTrace(World world, int x, int y, int z, Vec3 source, Vec3 destination)
	{
		return new RaytraceBlock(world, source, destination, x, y, z).trace();
	}

	@Override
	public TileEntity createNewTileEntity(World world, int metadata)
	{
		return new BigDoorProxyTileEntity();
	}

	@Override
	public Item getItemDropped(int metadata, java.util.Random random, int fortune)
	{
		return null;
	}

	@Override
	public int quantityDropped(java.util.Random random)
	{
		return 0;
	}

	@Override
	public boolean isOpaqueCube()
	{
		return false;
	}

	@Override
	public boolean renderAsNormalBlock()
	{
		return false;
	}

	@Override
	public int getRenderType()
	{
		return -1;
	}
}
