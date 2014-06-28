package com.kingrunes.somnia;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.opengl.GL11;

import scala.NotImplementedError;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.config.Configuration;

import com.kingrunes.somnia.common.CommonProxy;
import com.kingrunes.somnia.common.PacketHandler;
import com.kingrunes.somnia.server.ServerTickHandler;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.FMLEventChannel;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@Mod(modid = Somnia.MOD_ID, name = Somnia.NAME, version = Somnia.VERSION)
public class Somnia
{
	public static final String MOD_ID = "Somnia";
	public static final String NAME = "Somnia";
	public static final String VERSION = "1.1.1";
	
	public Configuration config;
	public List<ServerTickHandler> tickHandlers;
	
	public boolean serverTicking = false;
	
	@Instance
	public static Somnia instance;
	
	@SidedProxy(serverSide="com.kingrunes.somnia.common.CommonProxy", clientSide="com.kingrunes.somnia.client.ClientProxy")
	public static CommonProxy proxy;
	
	public static FMLEventChannel channel;
	
	public static long clientAutoWakeTime = -1;
	
	public Somnia()
	{
		this.tickHandlers = new ArrayList<ServerTickHandler>();
	}
	
	@EventHandler
    public void preInit(FMLPreInitializationEvent event)
	{
        proxy.configure(event.getSuggestedConfigurationFile());
    }
	
	@EventHandler
	public void init(FMLInitializationEvent event) 
	{
		channel = NetworkRegistry.INSTANCE.newEventDrivenChannel(MOD_ID);
		channel.register(new PacketHandler());
		
		proxy.register();
	}
	
	public void tick()
	{
		synchronized (Somnia.instance.tickHandlers)
		{
			for (ServerTickHandler serverTickHandler : Somnia.instance.tickHandlers)
				serverTickHandler.tickStart();
		}
	}
	
	public int countMultipliedTickHandlers()
	{
		int i = 0;
		for (ServerTickHandler serverTickHandler : tickHandlers)
			if (serverTickHandler.mbCheck)
				i++;
		return i;
	}
	
	/*
	 * Returns 1 if sleep is not allowed at the given worldServer's world time, 0 if everyone is sleeping, -1 otherwise.
	 */
	public int allPlayersSleeping(WorldServer worldServer)
	{
		boolean allSleeping = allPlayersSleeping(worldServer.playerEntities);
		
		if (!proxy.validSleepPeriod.isTimeWithin(worldServer.getWorldTime() % 24000))
			return 1;
		
		return allSleeping ? 0 : -1;
	}
	
	private boolean allPlayersSleeping(List<?> playerEntities)
	{
		if (playerEntities.isEmpty())
			return false;
		
		for (Object obj : playerEntities)
		{
			if (!((EntityPlayer)obj).isPlayerSleeping())
				return false;
		}
		
		return true;
	}

	public static String timeStringForWorldTime(long time)
	{
		time += 6000; // Tick -> Time offset
		
		time = time % 24000;
		int hours = (int) Math.floor(time / (double)1000);
		int minutes = (int) ((time % 1000) / 1000.0d * 60);
		
		String lsHours = String.valueOf(hours);
		String lsMinutes = String.valueOf(minutes);
		
		if (lsHours.length() == 1)
			lsHours = "0"+lsHours;
		if (lsMinutes.length() == 1)
			lsMinutes = "0"+lsMinutes;
		
		return lsHours + ":" + lsMinutes;
	}

	public static boolean doesPlayHaveAnyArmor(EntityPlayer e)
	{
		ItemStack[] armor = e.inventory.armorInventory;
		for (int a=0; a<armor.length; a++)
		{
			if (armor[a] != null)
				return true;
		}
		return false;
	}

	public static long calculateWakeTime(long totalWorldTime, int i)
	{
		long l;
		long timeInDay = totalWorldTime % 24000l;
		l = totalWorldTime - timeInDay + i;
		if (timeInDay > i)
			l += 24000l;
		return l;
	}

	/*
	 * These methods are referenced by ASM generated bytecode
	 * 
	*/

	@SideOnly(Side.CLIENT)
	public static void renderWorld(float par1, long par2)
	{
		if (Minecraft.getMinecraft().thePlayer.isPlayerSleeping() && proxy.disableRendering)
		{
			GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
			return;
		}
			
		Minecraft.getMinecraft().entityRenderer.renderWorld(par1, par2);
	}
	
	public static boolean doMobSpawning(WorldServer par1WorldServer)
	{
		if (!proxy.disableCreatureSpawning || !par1WorldServer.getGameRules().getGameRuleBooleanValue("doMobSpawning"))
			return false;
		
		for (ServerTickHandler serverTickHandler : instance.tickHandlers)
		{
			if (serverTickHandler.worldServer == par1WorldServer)
				return !serverTickHandler.mbCheck;
		}
		
		throw new NotImplementedError("tickHandlers doesn't contain match for given world server");
	}
	
	/*		
	public static void moodSoundAndLightCheck(int par1, int par2, Chunk par3Chunk)
	{
		if (proxy.disableMoodSoundAndLightCheck)
		{
			for (ServerTickHandler serverTickHandler : instance.tickHandlers)
			{
				if (serverTickHandler.worldServer == par3Chunk.worldObj)
					if (serverTickHandler.mbCheck)
						return;
			}
		}
		
		par3Chunk.worldObj.moodSoundAndLightCheck(par1, par2, par3Chunk);
	}
	*/
}