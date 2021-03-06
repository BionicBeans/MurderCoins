package murder.murdercoin.common.machines.press;

import java.util.Random;

import murder.murdercoin.common.MurderCoins;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Icon;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import universalelectricity.prefab.block.BlockAdvanced;
import universalelectricity.prefab.tile.TileEntityAdvanced;

public class BlockCoinPress extends BlockAdvanced
{
	private Icon cPTop;
	private Icon cPSide;
	private Icon cPFront;
	private Icon cPPow;

	public BlockCoinPress(int id)
	{
		super(id, Material.iron);
		this.setCreativeTab(MurderCoins.murderTab);
		this.setStepSound(soundMetalFootstep);
	}

	public void registerIcons(IconRegister par1IconRegister)
	{
		this.cPTop = par1IconRegister.registerIcon("MurderCoins:coinPress-Top");
		this.cPSide = par1IconRegister.registerIcon("MurderCoins:coinPress-Side");
		this.cPFront = par1IconRegister.registerIcon("MurderCoins:coinPress-Front");
		this.cPPow = par1IconRegister.registerIcon("MurderCoins:coinPress-Pow");

	}

	@Override
	public Icon getBlockTextureFromSideAndMetadata(int side, int metadata)
	{
		if (side == 0 || side == 1) // bottom and top
		{
			return this.cPTop;
		}
		if (side == metadata + 2) // front
		{
			return this.cPFront;
		}
		if (side == ForgeDirection.getOrientation(metadata + 2).getOpposite().ordinal()) // back
		{
			return this.cPPow;
		}
		else
		// sides
		{
			return this.cPSide;

		}
	}

	/**
	 * Called when the block is right clicked by the player
	 */
	@Override
	public boolean onMachineActivated(World par1World, int x, int y, int z, EntityPlayer par5EntityPlayer, int side, float hitX, float hitY, float hitZ)
	{
		int metadata = par1World.getBlockMetadata(x, y, z);
		if (!par1World.isRemote)
		{
			par5EntityPlayer.openGui(MurderCoins.instance, 0, par1World, x, y, z);
			return true;
		}
		return true;
	}

	/**
	 * Called when the block is placed in the world.
	 */
	/**
	 * Called when the block is placed in the world.
	 */
	@Override
	public void onBlockPlacedBy(World par1World, int x, int y, int z, EntityLiving par5EntityLiving, ItemStack itemStack)
	{
		int angle = MathHelper.floor_double((par5EntityLiving.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3;
		switch (angle)
		{
			case 0:
				par1World.setBlock(x, y, z, this.blockID, 1, 0);
				break;
			case 1:
				par1World.setBlock(x, y, z, this.blockID, 2, 0);
				break;
			case 2:
				par1World.setBlock(x, y, z, this.blockID, 0, 0);
				break;
			case 3:
				par1World.setBlock(x, y, z, this.blockID, 3, 0);
				break;
		}

		((TileEntityAdvanced) par1World.getBlockTileEntity(x, y, z)).initiate();
		par1World.notifyBlocksOfNeighborChange(x, y, z, this.blockID);
	}

	@Override
	public void breakBlock(World world, int x, int y, int z, int i, int j)
	{
		dropItems(world, x, y, z);
		super.breakBlock(world, x, y, z, i, j);
	}

	private void dropItems(World world, int x, int y, int z)
	{
		Random rand = new Random();

		TileEntity tile_entity = world.getBlockTileEntity(x, y, z);

		if (!(tile_entity instanceof IInventory))
		{
			return;
		}

		IInventory inventory = (IInventory) tile_entity;

		for (int i = 0; i < inventory.getSizeInventory(); i++)
		{
			ItemStack item = inventory.getStackInSlot(i);

			if (item != null && item.stackSize > 0)
			{
				float rx = rand.nextFloat() * 0.6F + 0.1F;
				float ry = rand.nextFloat() * 0.6F + 0.1F;
				float rz = rand.nextFloat() * 0.6F + 0.1F;

				EntityItem entity_item = new EntityItem(world, x + rx, y + ry, z + rz, new ItemStack(item.itemID, item.stackSize, item.getItemDamage()));

				if (item.hasTagCompound())
				{
					entity_item.getEntityItem().setTagCompound((NBTTagCompound) item.getTagCompound().copy());
				}

				float factor = 0.5F;

				entity_item.motionX = rand.nextGaussian() * factor;
				entity_item.motionY = rand.nextGaussian() * factor + 0.2F;
				entity_item.motionZ = rand.nextGaussian() * factor;
				world.spawnEntityInWorld(entity_item);
				item.stackSize = 0;
			}
		}
	}

	@Override
	public boolean onUseWrench(World par1World, int x, int y, int z, EntityPlayer par5EntityPlayer, int side, float hitX, float hitY, float hitZ)
	{
		int metadata = par1World.getBlockMetadata(x, y, z);

		int change = 0;

		// Re-orient the block
		switch (metadata)
		{
			case 0:
				change = 3;
				break;
			case 3:
				change = 1;
				break;
			case 1:
				change = 2;
				break;
			case 2:
				change = 0;
				break;
		}

		par1World.setBlock(x, y, z, this.blockID, change, 0);
		par1World.markBlockForRenderUpdate(x, y, z);

		((TileEntityAdvanced) par1World.getBlockTileEntity(x, y, z)).initiate();

		return true;
	}

	@Override
	public TileEntity createNewTileEntity(World var1)
	{
		return new TileEntityCoinPress();
	}
}
