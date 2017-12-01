package com.ferreusveritas.dynamictrees.blocks;

import java.util.List;
import java.util.Random;

import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.trees.DynamicTree;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockDynamicSapling extends Block {
	
	public DynamicTree tree;
	
	public BlockDynamicSapling(String name) {
		super(Material.PLANTS);
		setDefaultState(this.blockState.getBaseState());
		setSoundType(SoundType.PLANT);
		setTickRandomly(true);
		setUnlocalizedName(name);
		setRegistryName(name);
	}

	///////////////////////////////////////////
	// INTERACTION
	///////////////////////////////////////////

	@Override
	public void updateTick(World world, BlockPos pos, IBlockState state, Random rand) {
		generateTree(world, pos, state, rand);
	}

	public static boolean canSaplingStay(IBlockAccess world, DynamicTree tree, BlockPos pos) {
		//Ensure there are no adjacent branches or other saplings
		for(EnumFacing dir: EnumFacing.HORIZONTALS) {
			IBlockState blockState = world.getBlockState(pos.offset(dir));
			Block block = blockState.getBlock();
			if(TreeHelper.isBranch(block) || block instanceof BlockDynamicSapling) {
				return false;
			}
		}

		//Air above and acceptable soil below
		return world.isAirBlock(pos.up()) && tree.isAcceptableSoil(world, pos.down(), world.getBlockState(pos.down()));
	}

	public boolean canBlockStay(IBlockAccess world, BlockPos pos, IBlockState state) {
		return canSaplingStay(world, getTree(state), pos);
	}

	public void generateTree(World world, BlockPos pos, IBlockState state, Random rand) {
		DynamicTree tree = getTree(state);
		if(canBlockStay(world, pos, state)) {
			//Ensure planting conditions are right
			if(world.isAirBlock(pos.up()) && tree.isAcceptableSoil(world, pos.down(), world.getBlockState(pos.down()))) {
				world.setBlockState(pos, tree.getDynamicBranch().getDefaultState());//set to a single branch with 1 radius
				world.setBlockState(pos.up(), tree.getDynamicLeavesState());
				world.setBlockState(pos.down(), tree.getRootyDirtBlock().getDefaultState());//Set to fully fertilized rooty dirt
			}
		} else {
			dropBlock(world, tree, state, pos);
		}
	}

	///////////////////////////////////////////
	// TREE INFORMATION
	///////////////////////////////////////////

	public DynamicTree getTree(IBlockState state) {
		return this.tree;
	}

	public BlockDynamicSapling setTree(IBlockState state, DynamicTree tree) {
		this.tree = tree;
		return this;
	}

	///////////////////////////////////////////
	// DROPS
	///////////////////////////////////////////

	@Override
	public void neighborChanged(IBlockState state, World world, BlockPos pos, Block blockIn, BlockPos fromPos) {
		if (!this.canBlockStay(world, pos, state)) {
			dropBlock(world, getTree(state), state, pos);
		}
	}
	
	private void dropBlock(World world, DynamicTree tree, IBlockState state, BlockPos pos) {
		world.setBlockToAir(pos);
		dropBlockAsItem(world, pos, state, 0);
	}

	@Override
	public List<ItemStack> getDrops(IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
		List<ItemStack> dropped = super.getDrops(world, pos, state, fortune);
		dropped.add(getTree(state).getSeedStack());
		return dropped;
	}

	@Override
	public Item getItemDropped(IBlockState state, Random rand, int fortune) {
		return null;
	}

	@Override
	public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player) {
		return getTree(state).getSeedStack();
	}
	
	///////////////////////////////////////////
	// PHYSICAL BOUNDS
	///////////////////////////////////////////

	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
		return new AxisAlignedBB(0.25f, 0.0f, 0.25f, 0.75f, 0.75f, 0.75f);
	}

	///////////////////////////////////////////
	// RENDERING
	///////////////////////////////////////////

	@Override
	public boolean isFullCube(IBlockState state) {
		return false;
	}
	
	@Override
	public boolean isOpaqueCube(IBlockState state) {
		return false;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public BlockRenderLayer getBlockLayer() {
		return BlockRenderLayer.CUTOUT_MIPPED;
	}

}