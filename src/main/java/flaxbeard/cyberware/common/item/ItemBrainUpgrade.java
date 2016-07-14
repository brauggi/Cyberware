package flaxbeard.cyberware.common.item;

import java.util.List;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.EnderTeleportEvent;
import net.minecraftforge.event.entity.living.LivingExperienceDropEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.BreakSpeed;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import flaxbeard.cyberware.api.CyberwareAPI;
import flaxbeard.cyberware.common.CyberwareContent;

public class ItemBrainUpgrade extends ItemCyberware
{

	public ItemBrainUpgrade(String name, EnumSlot slot, String[] subnames)
	{
		super(name, slot, subnames);
		MinecraftForge.EVENT_BUS.register(this);
		FMLCommonHandler.instance().bus().register(this);

	}

	
	@Override
	public boolean isIncompatible(ItemStack stack, ItemStack other)
	{
		return other.getItem() == this && stack.getItemDamage() == 0 && other.getItemDamage() == 2;
	}
	
	@SubscribeEvent
	public void handleTeleJam(EnderTeleportEvent event)
	{
		EntityLivingBase te = event.getEntityLiving();
		
		if (CyberwareAPI.isCyberwareInstalled(te, new ItemStack(this, 1, 1)))
		{
			event.setCanceled(true);
			return;
		}
		if (te != null)
		{
			float range = 25F;
			List<EntityLivingBase> test = te.worldObj.getEntitiesWithinAABB(EntityLivingBase.class, new AxisAlignedBB(te.posX - range, te.posY - range, te.posZ - range, te.posX + te.width + range, te.posY + te.height + range, te.posZ + te.width + range));
			for (EntityLivingBase e : test)
			{
				if (te.getDistanceToEntity(e) <= range)
				{
					if (CyberwareAPI.isCyberwareInstalled(e, new ItemStack(this, 1, 1)))
					{
						event.setCanceled(true);
						return;
					}
				}
			}
		}
		
	}

	@SubscribeEvent
	public void handleClone(PlayerEvent.Clone event)
	{
		if (event.isWasDeath())
		{
			EntityPlayer p = event.getOriginal();
			
			if (CyberwareAPI.isCyberwareInstalled(p, new ItemStack(this, 1, 0)) && !p.worldObj.getGameRules().getBoolean("keepInventory"))
			{
				/*float range = 5F;
				List<EntityXPOrb> orbs = p.worldObj.getEntitiesWithinAABB(EntityXPOrb.class, new AxisAlignedBB(p.posX - range, p.posY - range, p.posZ - range, p.posX + p.width + range, p.posY + p.height + range, p.posZ + p.width + range));
				for (EntityXPOrb orb : orbs)
				{
					orb.setDead();
				}*/

				if (!p.worldObj.isRemote)
				{
					ItemStack stack = new ItemStack(CyberwareContent.expCapsule);
					NBTTagCompound c = new NBTTagCompound();
					c.setInteger("xp", p.experienceTotal);
					stack.setTagCompound(c);
					EntityItem item = new EntityItem(p.worldObj, p.posX, p.posY, p.posZ, stack);
					p.worldObj.spawnEntityInWorld(item);
				}
			}
			else if (CyberwareAPI.isCyberwareInstalled(p, new ItemStack(this, 1, 2)) && !p.worldObj.getGameRules().getBoolean("keepInventory"))
			{
				event.getEntityPlayer().addExperience((int) (Math.min(100, p.experienceLevel * 7) * .9F));
			}
		}
	}
	
	@SubscribeEvent
	public void handleMining(BreakSpeed event)
	{
		EntityPlayer p = event.getEntityPlayer();
		
		if (CyberwareAPI.isCyberwareInstalled(p, new ItemStack(this, 1, 3)) && !p.isSneaking())
		{
			IBlockState state = event.getState();
			ItemStack tool = p.getHeldItem(EnumHand.MAIN_HAND);
			
			if (isToolEffective(tool, state)) return;
			
			for (int i = 0; i < 10; i++)
			{
				if (i != p.inventory.currentItem)
				{
					ItemStack potentialTool = p.inventory.mainInventory[i];
					if (isToolEffective(potentialTool, state))
					{
						p.inventory.currentItem = i;
						return;
					}
				}
			}
		}
	}
	
	public boolean isToolEffective(ItemStack tool, IBlockState state)
	{
		if (tool != null)
		{
			for (String toolType : tool.getItem().getToolClasses(tool))
			{
				if (state.getBlock().isToolEffective(toolType, state))
				{
					return true;
				}
			}
		}
		return false;
	}
	
	
	@SubscribeEvent
	public void handleXPDrop(LivingExperienceDropEvent event)
	{
		EntityLivingBase e = event.getEntityLiving();
		if (CyberwareAPI.isCyberwareInstalled(e, new ItemStack(this, 1, 0)) || CyberwareAPI.isCyberwareInstalled(e, new ItemStack(this, 1, 2)))
		{
			event.setCanceled(true);
		}
	}
}