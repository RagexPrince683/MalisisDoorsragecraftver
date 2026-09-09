package net.malisis.doors.door.item;

import java.util.ArrayList;
import java.util.List;

import net.malisis.doors.MalisisDoors;
import net.malisis.doors.door.block.BigDoor;
import net.malisis.doors.door.block.Door;
import net.malisis.doors.internal.util.BlockPos;
import net.malisis.doors.internal.util.EntityUtils;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

public class BigDoorItemBlock extends ItemBlock
{
	public BigDoorItemBlock(Block block)
	{
		super(block);
	}

	@Override
	public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY,
			float hitZ)
	{
		if (side != 1)
			return false;

		y++;
		BigDoor door = (BigDoor) field_150939_a;
		ForgeDirection direction = EntityUtils.getEntityFacing(player);
		BlockPos[] positions = door.getStructurePositions(x, y, z, direction);
		if (!canPlace(world, player, stack, positions, x, y, z))
			return false;

		List<BlockPos> placed = new ArrayList<BlockPos>();
		if (!world.setBlock(x, y, z, door, Door.dirToInt(direction), 3))
			return false;
		placed.add(new BlockPos(x, y, z));

		for (BlockPos pos : positions)
		{
			if (pos.getX() == x && pos.getY() == y && pos.getZ() == z)
				continue;
			if (!world.setBlock(pos.getX(), pos.getY(), pos.getZ(), MalisisDoors.Blocks.bigDoorProxy, 0, 3))
			{
				rollback(world, placed);
				return false;
			}
			net.malisis.doors.door.block.BigDoorProxyBlock.setOrigin(world, pos.getX(), pos.getY(), pos.getZ(), x, y, z);
			placed.add(pos);
		}

		door.onBlockPlacedBy(world, x, y, z, player, stack);
		stack.stackSize--;
		return true;
	}

	private boolean canPlace(World world, EntityPlayer player, ItemStack stack, BlockPos[] positions, int originX, int originY, int originZ)
	{
		for (BlockPos pos : positions)
		{
			if (pos.getY() < 0 || pos.getY() >= world.getHeight())
				return false;
			if (!player.canPlayerEdit(pos.getX(), pos.getY(), pos.getZ(), 1, stack))
				return false;
			Block existing = world.getBlock(pos.getX(), pos.getY(), pos.getZ());
			if (!existing.isReplaceable(world, pos.getX(), pos.getY(), pos.getZ()))
				return false;
			if (pos.getY() == originY && !world.getBlock(pos.getX(), pos.getY() - 1, pos.getZ()).isSideSolid(world, pos.getX(),
					pos.getY() - 1, pos.getZ(), ForgeDirection.UP))
				return false;
		}

		return true;
	}

	private void rollback(World world, List<BlockPos> placed)
	{
		for (BlockPos pos : placed)
			world.setBlockToAir(pos.getX(), pos.getY(), pos.getZ());
	}
}
