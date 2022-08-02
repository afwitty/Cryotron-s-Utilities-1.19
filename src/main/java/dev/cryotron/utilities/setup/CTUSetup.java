package dev.cryotron.utilities.setup;

import dev.cryotron.utilities.CTUtilities;
import dev.cryotron.utilities.client.gui.hud.ArmorStatusRenderer;
import dev.cryotron.utilities.client.gui.hud.BreathStatusRenderer;
import dev.cryotron.utilities.client.gui.hud.HealthStatusRenderer;
import dev.cryotron.utilities.client.gui.hud.HungerStatusRenderer;
import dev.cryotron.utilities.client.gui.hud.MntHealthStatusRenderer;
import dev.cryotron.utilities.client.particles.BarState;
import dev.cryotron.utilities.client.particles.BarStates;
import dev.cryotron.utilities.client.particles.DamageDoneParticleRenderer;
import dev.cryotron.utilities.common.CTUtilitiesConfig;
import dev.cryotron.utilities.networking.SyncHandler;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.client.event.RenderLevelLastEvent;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.event.entity.player.CriticalHitEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

public class CTUSetup {
	
	public static void preInit() {
		
		IEventBus modbus = FMLJavaModLoadingContext.get().getModEventBus();
		  	
		modbus.addListener(CTUSetup::commonSetup);
        modbus.addListener(EventPriority.NORMAL, false, FMLClientSetupEvent.class, CTUSetup::clientSetup);        
	}
	
    public static void commonSetup(final FMLCommonSetupEvent event) {
    	
        	// Appleskin
    		SyncHandler.init();				    		
	
    }
    
    public static void clientSetup(FMLClientSetupEvent event) {	
    	ClientEvents.init();
    	
    	HealthStatusRenderer.init();
    	ArmorStatusRenderer.init();
    	HungerStatusRenderer.init();
    	BreathStatusRenderer.init();
    	MntHealthStatusRenderer.init();

    	
    }
    


    
}

