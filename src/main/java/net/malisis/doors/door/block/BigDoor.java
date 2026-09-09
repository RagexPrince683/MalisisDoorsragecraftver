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

package net.malisis.doors.door.block;

import java.util.ArrayList;

import net.malisis.doors.internal.block.BoundingBoxType;
import net.malisis.doors.internal.InternalSupport;
import net.malisis.doors.internal.block.MalisisBlock;
import net.malisis.doors.internal.util.AABBUtils;
import net.malisis.doors.internal.util.BlockPos;
import net.malisis.doors.internal.util.BlockState;
import net.malisis.doors.door.tileentity.BigDoorProxyTileEntity;
import net.malisis.doors.internal.util.EntityUtils;
import net.malisis.doors.internal.util.TileEntityUtils;
import net.malisis.doors.MalisisDoors;
import net.malisis.doors.MalisisDoors.Items;
import net.malisis.doors.door.tileentity.BigDoorTileEntity;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

/**
 * @author Ordinastie
 *
 */
public class BigDoor extends MalisisBlock implements ITileEntityProvider
{
	public enum Type
	{
		CARRIAGE("carriage_door", net.minecraft.init.Items.wooden_door), MEDIEVAL("medieval_door", Items.doorSpruceItem);

		public String name;
		public Item door;

		private Type(String name, Item door)
		{
			this.name = name;
			this.door = door;
		}
	}

	public static int renderId;
	public static int renderPass = -1;
	private AxisAlignedBB defaultBoundingBox = AxisAlignedBB.getBoundingBox(0, 0, 1 - Door.DOOR_WIDTH, 4, 5, 1);
	private Type type;

	public BigDoor(Type type)
	{
		super(Material.wood);
		this.type = type;
		setHardness(5.0F);
		setResistance(10.0F);
		setStepSound(soundTypeStone);
		setBlockName(type.name);
		setCreativeTab(MalisisDoors.tab);
	}

	@Override
	public void registerBlockIcons(IIconRegister register)
	{
		blockIcon = register.registerIcon(MalisisDoors.modid + ":" + type.name);
	}

	@Override
	public String getItemIconName()
	{
		return MalisisDoors.modid + ":" + type.name + "_item";
	}

	@Override
	public boolean canPlaceBlockOnSide(World world, int x, int y, int z, int side)
	{
		if (side != 1)
			return false;

		ForgeDirection dir = ForgeDirection.getOrientation(side).getOpposite();
		return world.getBlock(x + dir.offsetX, y + dir.offsetY, z + dir.offsetZ).isSideSolid(world, x, y, z, dir);
	}

	@Override
	public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase player, ItemStack itemStack)
	{
		ForgeDirection dir = EntityUtils.getEntityFacing(player);
		int metadata = Door.dirToInt(dir);
		world.setBlockMetadataWithNotify(x, y, z, metadata, 2);

		BigDoorTileEntity te = TileEntityUtils.getTileEntity(BigDoorTileEntity.class, world, x, y, z);
		if (te != null)
			te.setFrameState(BlockState.fromNBT(itemStack.getTagCompound()));

		if (!world.isRemote)
			repairProxies(world, x, y, z);
	}

	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ)
	{
		if (world.isRemote)
			return true;

		BigDoorTileEntity te = TileEntityUtils.getTileEntity(BigDoorTileEntity.class, world, x, y, z);
		if (te == null)
			return true;

		te.openOrCloseDoor();
		return true;
	}

	@Override
	public AxisAlignedBB[] getPlacedBoundingBox(IBlockAccess world, int x, int y, int z, int side, EntityPlayer player, ItemStack itemStack)
	{
		ForgeDirection dir = EntityUtils.getEntityFacing(player);
		return AABBUtils.rotate(new AxisAlignedBB[] { defaultBoundingBox.copy() }, dir);
	}

	@Override
	public AxisAlignedBB[] getBoundingBox(IBlockAccess world, int x, int y, int z, BoundingBoxType type)
	{
		BigDoorTileEntity te = TileEntityUtils.getTileEntity(BigDoorTileEntity.class, world, x, y, z);
		if (te == null)
			return AABBUtils.identities();

		//InternalSupport.message(te.getDirection());

		AxisAlignedBB[] aabbs = new AxisAlignedBB[] { defaultBoundingBox.copy() };
		if (type == BoundingBoxType.RENDER)
		{
			aabbs[0].minZ = -.5F;
		}
		else if ((type == BoundingBoxType.COLLISION || type == BoundingBoxType.CHUNKCOLLISION || type == BoundingBoxType.RAYTRACE)
				&& (te.isOpened() || te.isMoving()))
		{
			aabbs = new AxisAlignedBB[] { AxisAlignedBB.getBoundingBox(0, 0, -0.5F, 0.5F, 4, 1),
					AxisAlignedBB.getBoundingBox(3.5F, 0, -0.5F, 4, 4, 1), AxisAlignedBB.getBoundingBox(0, 4, 1 - Door.DOOR_WIDTH, 4, 5, 1) };
		}

		return AABBUtils.rotate(aabbs, Door.intToDir(te.getDirection()));
	}

	@Override
	public TileEntity createNewTileEntity(World world, int metadata)
	{
		return new BigDoorTileEntity();
	}

	@SuppressWarnings("deprecation")
	@Override
	public boolean removedByPlayer(World world, EntityPlayer player, int x, int y, int z)
	{
		if (!player.capabilities.isCreativeMode)
		{
			BigDoorTileEntity te = TileEntityUtils.getTileEntity(BigDoorTileEntity.class, world, x, y, z);
			if (te != null)
				dropBlockAsItem(world, x, y, z, te.getDroppedItemStack());
		}
		boolean removed = super.removedByPlayer(world, player, x, y, z);
		if (removed && !world.isRemote)
			removeProxies(world, x, y, z);
		return removed;
	}

	@Override
	public ArrayList<ItemStack> getDrops(World world, int x, int y, int z, int metadata, int fortune)
	{
		return new ArrayList<ItemStack>();
	}

	@Override
	public void breakBlock(World world, int x, int y, int z, Block block, int metadata)
	{
		if (!world.isRemote)
			removeProxies(world, x, y, z);
		super.breakBlock(world, x, y, z, block, metadata);
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
		return renderId;
	}

	@Override
	public int getRenderBlockPass()
	{
		return 1;
	}

	@Override
	public boolean canRenderInPass(int pass)
	{
		renderPass = pass;
		return true;
	}

	public BlockPos[] getStructurePositions(int x, int y, int z, ForgeDirection direction)
	{
		AxisAlignedBB footprint = AABBUtils.rotate(defaultBoundingBox.copy(), direction);
		int minX = (int) Math.floor(footprint.minX + 0.0001D);
		int maxX = (int) Math.ceil(footprint.maxX - 0.0001D);
		int minZ = (int) Math.floor(footprint.minZ + 0.0001D);
		int maxZ = (int) Math.ceil(footprint.maxZ - 0.0001D);
		ArrayList<BlockPos> positions = new ArrayList<BlockPos>();
		for (int px = minX; px < maxX; px++)
		{
			for (int py = 0; py < 5; py++)
			{
				for (int pz = minZ; pz < maxZ; pz++)
					positions.add(new BlockPos(x + px, y + py, z + pz));
			}
		}
		return positions.toArray(new BlockPos[positions.size()]);
	}

	public void repairProxies(World world, int x, int y, int z)
	{
		ForgeDirection direction = Door.intToDir(world.getBlockMetadata(x, y, z));
		for (BlockPos pos : getStructurePositions(x, y, z, direction))
		{
			if (pos.getX() == x && pos.getY() == y && pos.getZ() == z)
				continue;

			if (world.getBlock(pos.getX(), pos.getY(), pos.getZ()) == MalisisDoors.Blocks.bigDoorProxy)
			{
				BigDoorProxyTileEntity proxy = TileEntityUtils.getTileEntity(BigDoorProxyTileEntity.class, world, pos.getX(), pos.getY(),
						pos.getZ());
				if (proxy == null || !proxy.hasOrigin(x, y, z))
					InternalSupport.log.warn("Could not repair large door proxy at {}, {}, {} because it belongs to another structure",
							pos.getX(), pos.getY(), pos.getZ());
				continue;
			}

			if (!world.getBlock(pos.getX(), pos.getY(), pos.getZ()).isReplaceable(world, pos.getX(), pos.getY(), pos.getZ()))
			{
				InternalSupport.log.warn("Could not repair large door proxy at {}, {}, {} because the cell is occupied", pos.getX(), pos.getY(),
						pos.getZ());
				continue;
			}

			world.setBlock(pos.getX(), pos.getY(), pos.getZ(), MalisisDoors.Blocks.bigDoorProxy, 0, 3);
			BigDoorProxyBlock.setOrigin(world, pos.getX(), pos.getY(), pos.getZ(), x, y, z);
		}
	}

	public void removeProxies(World world, int x, int y, int z)
	{
		for (int px = x - 4; px <= x + 4; px++)
		{
			for (int py = y; py < y + 5; py++)
			{
				for (int pz = z - 4; pz <= z + 4; pz++)
				{
					if (world.getBlock(px, py, pz) != MalisisDoors.Blocks.bigDoorProxy)
						continue;
					BigDoorProxyTileEntity proxy = TileEntityUtils.getTileEntity(BigDoorProxyTileEntity.class, world, px, py, pz);
					if (proxy != null && proxy.hasOrigin(x, y, z))
						world.setBlockToAir(px, py, pz);
				}
			}
		}
	}
}
