package com.vicious.ebmvccompat;

import com.google.common.collect.Maps;
import com.phylogeny.extrabitmanipulation.armor.LayerChiseledArmor;
import com.phylogeny.extrabitmanipulation.client.ClientHelper;
import com.phylogeny.extrabitmanipulation.init.RenderLayersExtraBitManipulation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLModDisabledEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

@Mod(modid = EBMVCCompat.MODID, name = EBMVCCompat.NAME, version = EBMVCCompat.VERSION, acceptableRemoteVersions = "*",dependencies = "required-after:extrabitmanipulation")
public class EBMVCCompat
{
    public static final String MODID = "ebmvccompat";
    public static final String NAME = "EBM ViveCraft Compat";
    public static final String VERSION = "1.0.0";
    public static Logger logger;
    @EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        logger = event.getModLog();
    }
    @EventHandler
    public void postInit(FMLPostInitializationEvent event)
    {
        try {
            if (event.getSide() == Side.CLIENT) clientPostInit(event);
        } catch(Exception e){
            if(e.getCause() != null) {
                System.err.println(e.getCause());
                e.getCause().printStackTrace();
            }
            else{
                e.printStackTrace();
            }
        }
    }

    /**
     * Puts the ViveCraft renderPlayerVR player into
     */
    @SubscribeEvent
    public static void onPlayerRender(RenderLivingEvent.Post<?> ev) throws ClassNotFoundException {
        Class<?> renderer = Class.forName("net.minecraft.client.renderer.entity.RenderPlayerVR");
        if(renderer.isInstance(ev.getRenderer())){
            if(ev.getEntity().getUniqueID() == Minecraft.getMinecraft().player.getUniqueID()) {
                ev.getRenderer().addLayer(new LayerChiseledArmor(ev.getRenderer()));
                MinecraftForge.EVENT_BUS.unregister(EBMVCCompat.class);
            }
        }
    }

    /**
     * Proxies are stupid. This works completely fine.
     * @param event
     */
    @SideOnly(Side.CLIENT)
    public void clientPostInit(FMLPostInitializationEvent event) throws Exception{
        try {
            Class.forName("net.minecraft.client.renderer.entity.RenderPlayerVR");
        } catch(ClassNotFoundException ex){
            logger.warn("EBM ViveCraft Compat is intended to be run on a vivecraft game instance. Safely disabling VR compatibility.");
            return;
        }
        MinecraftForge.EVENT_BUS.register(EBMVCCompat.class);
        //Reflectively inject the Chiseled armor layer
        Class<?> t = RenderManager.class;
        //These fields are patched in by JRBudda
        Field skinMapVR = t.getDeclaredField("skinMapVR");
        skinMapVR.setAccessible(true);
        Map<?, ?> map = (Map<?, ?>) skinMapVR.get(ClientHelper.getRenderManager());
        map.forEach((k,v)->{
            RenderLivingBase<AbstractClientPlayer> render = (RenderLivingBase<AbstractClientPlayer>) v;
            render.addLayer(new LayerChiseledArmor(render));
        });
    }
}
