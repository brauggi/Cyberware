package flaxbeard.cyberware.common.item;

import java.util.ArrayList;
import java.util.List;

import scala.actors.threadpool.Arrays;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.shader.ShaderGroup;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import flaxbeard.cyberware.api.CyberwareAPI;
import flaxbeard.cyberware.api.ICyberware;
import flaxbeard.cyberware.api.ICyberwareUserData;

public class VanillaWares
{
	public static class SpiderEyeWare implements ICyberware
	{
		public SpiderEyeWare()
		{
			FMLCommonHandler.instance().bus().register(this);
			MinecraftForge.EVENT_BUS.register(this);
		}

		@Override
		public EnumSlot getSlot(ItemStack stack)
		{
			return EnumSlot.EYES;
		}

		@Override
		public int installedStackSize(ItemStack stack)
		{
			return 1;
		}

		@Override
		public ItemStack[] required(ItemStack stack)
		{
			return new ItemStack[0];
		}

		@Override
		public boolean isIncompatible(ItemStack stack, ItemStack other)
		{
			return CyberwareAPI.getCyberware(other).isEssential(other);
		}

		@Override
		public boolean isEssential(ItemStack stack)
		{
			return true;
		}
		
		@SubscribeEvent
		public void handleSpiderNightVision(LivingUpdateEvent event)
		{
			EntityLivingBase e = event.getEntityLiving();
			
			if (CyberwareAPI.isCyberwareInstalled(e, new ItemStack(Items.SPIDER_EYE)))
			{
				e.addPotionEffect(new PotionEffect(MobEffects.NIGHT_VISION, Integer.MAX_VALUE, -53, true, false));
			}
			else
			{
				PotionEffect effect = e.getActivePotionEffect(MobEffects.NIGHT_VISION);
				if (effect != null && effect.getAmplifier() == -53)
				{
					e.removePotionEffect(MobEffects.NIGHT_VISION);
				}
			}
		}
		
		@SubscribeEvent
		@SideOnly(Side.CLIENT)
		public void handleSpiderVision(TickEvent.ClientTickEvent event)
		{
			if (event.phase == TickEvent.Phase.START)
			{
				EntityPlayer e = Minecraft.getMinecraft().thePlayer;
				
				if (CyberwareAPI.isCyberwareInstalled(e, new ItemStack(Items.SPIDER_EYE)))
				{
					if (Minecraft.getMinecraft().entityRenderer.getShaderGroup() == null)
					{
						Minecraft.getMinecraft().entityRenderer.loadShader(new ResourceLocation("shaders/post/spider.json"));
					}
				}
				else if (e != null && !e.isSpectator())
				{
					ShaderGroup sg = Minecraft.getMinecraft().entityRenderer.getShaderGroup();
					if (sg != null && sg.getShaderGroupName().equals("minecraft:shaders/post/spider.json"))
					{
						Minecraft.getMinecraft().entityRenderer.stopUseShader();
					}
				}
				
			}
		}

		@Override
		public List<String> getInfo(ItemStack stack)
		{
			List<String> ret = new ArrayList<String>();
			String[] desc = this.getDesciption(stack);
			if (desc != null && desc.length > 0)
			{
				String format = desc[0];
				if (format.length() > 0)
				{
					ret.addAll(Arrays.asList(desc));
				}
			}
			return ret;
		}

		private String[] getDesciption(ItemStack stack)
		{
			return I18n.format("cyberware.tooltip.spiderEye").split("\\\\n");
		}


		
	}
}