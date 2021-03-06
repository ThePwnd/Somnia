package com.kingrunes.somnia.common.util;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.common.IExtendedEntityProperties;

public class SomniaEntityPlayerProperties implements IExtendedEntityProperties
{
	public static final String PROP_NAME = "somnia_player_props";
	
	private double fatigue;
	public int fatigueUpdateCounter = -1, lastSideEffectStage = -1;
	
	public static SomniaEntityPlayerProperties register(EntityPlayer player)
	{
		SomniaEntityPlayerProperties props = new SomniaEntityPlayerProperties();
		player.registerExtendedProperties(PROP_NAME, props);
		
		return props;
	}
	
	public static final SomniaEntityPlayerProperties get(EntityPlayer player)
	{
		return (SomniaEntityPlayerProperties) player.getExtendedProperties(PROP_NAME);
	}
	
	@Override
	public void saveNBTData(NBTTagCompound compound)
	{
		NBTTagCompound props = new NBTTagCompound();
		
		props.setDouble("fatigue", fatigue);
		props.setInteger("lastSideEffectStage", lastSideEffectStage);
		
		compound.setTag(PROP_NAME, props);
	}

	@Override
	public void loadNBTData(NBTTagCompound compound)
	{
		NBTTagCompound props = (NBTTagCompound) compound.getTag(PROP_NAME);
		if (props != null)
		{
			this.fatigue = props.getDouble("fatigue");
			this.lastSideEffectStage = props.getInteger("lastSideEffectStage");
		}
	}

	@Override
	public void init(Entity entity, World world)
	{}

	public double getFatigue()
	{
		return this.fatigue;
	}

	public void setFatigue(double fatigue)
	{
		this.fatigue = fatigue;
	}
}
