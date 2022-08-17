package dev.cryotron.utilities.setup;

import dev.cryotron.utilities.client.gui.hud.ArmorStatusRenderer;
import dev.cryotron.utilities.client.gui.hud.BreathStatusRenderer;
import dev.cryotron.utilities.client.gui.hud.HealthStatusRenderer;
import dev.cryotron.utilities.client.gui.hud.HungerStatusRenderer;
import dev.cryotron.utilities.client.gui.hud.MntHealthStatusRenderer;
import dev.cryotron.utilities.networking.SyncHandler;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
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

