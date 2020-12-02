package com.ferreusveritas.dynamictrees.systems.dropcreators;

import java.util.List;
import java.util.Random;

import com.ferreusveritas.dynamictrees.ModConfigs;
import com.ferreusveritas.dynamictrees.ModConstants;
import com.ferreusveritas.dynamictrees.api.treedata.IDropCreator;
import com.ferreusveritas.dynamictrees.event.SeedVoluntaryDropEvent;
import com.ferreusveritas.dynamictrees.trees.Species;

import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;

public class DropCreatorSeed implements IDropCreator {
	
	protected final float rarity;
	
	public DropCreatorSeed() {
		this(1.0f);
	}
	
	public DropCreatorSeed(float rarity) {
		this.rarity = rarity;
	}
	
	//Provided for customization via override
	protected float getHarvestRarity() {
		return rarity;
	}
	
	//Provided for customization via override
	protected float getVoluntaryRarity() {
		return rarity;
	}
	
	//Provided for customization via override
	protected float getLeavesRarity() {
		return rarity;
	}
	
	@Override
	public ResourceLocation getName() {
		return new ResourceLocation(ModConstants.MODID, "seed");
	}
	
	@Override
	public List<ItemStack> getHarvestDrop(World world, Species species, BlockPos leafPos, Random random, List<ItemStack> dropList, int soilLife, int fortune) {
		float rarity = getHarvestRarity();
		rarity *= (fortune + 1) / 8f;
		rarity *= Math.min(species.seasonalSeedDropFactor(world, leafPos) + 0.15f, 1.0);
		
		if(rarity > random.nextFloat()) {//1 in 64 chance to drop a seed on destruction..	
			dropList.add(species.getSeedStack(1));
		}
		return dropList;
	}
	
	@Override
	public List<ItemStack> getVoluntaryDrop(World world, Species species, BlockPos rootPos, Random random, List<ItemStack> dropList, int soilLife) {
		if(getVoluntaryRarity() * ModConfigs.seedDropRate * species.seasonalSeedDropFactor(world, rootPos) > random.nextFloat()) {
			dropList.add(species.getSeedStack(1));
			SeedVoluntaryDropEvent seedDropEvent = new SeedVoluntaryDropEvent(world, rootPos, species, dropList);
			MinecraftForge.EVENT_BUS.post(seedDropEvent);
			if(seedDropEvent.isCanceled()) {
				dropList.clear();
			}
		}
		return dropList;
	}
	
	@Override
	public List<ItemStack> getLeavesDrop(IBlockAccess access, Species species, BlockPos breakPos, Random random, List<ItemStack> dropList, int fortune) {
		int chance = 20; //See BlockLeaves#getSaplingDropChance(state);
		//Hokey fortune stuff here to match Vanilla logic.
		if (fortune > 0) {
			chance -= 2 << fortune;
			if (chance < 10) { 
				chance = 10;
			}
		}
		
		float seasonFactor = 1.0f;
		
		if(access instanceof World) {
			World world = (World) access;
			if(!world.isRemote) {
				seasonFactor = species.seasonalSeedDropFactor(world, breakPos);
			}
		}
		
		if(random.nextInt((int) (chance / getLeavesRarity())) == 0) {
			if(seasonFactor > random.nextFloat()) {
				dropList.add(species.getSeedStack(1));
			}
		}
		
		return dropList;
	}
	
	@Override
	public List<ItemStack> getLogsDrop(World world, Species species, BlockPos breakPos, Random random, List<ItemStack> dropList, float volume) {
		return dropList;
	}
	
}
