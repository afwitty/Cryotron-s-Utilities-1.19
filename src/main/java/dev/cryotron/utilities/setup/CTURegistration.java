package dev.cryotron.utilities.setup;

import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

public class CTURegistration {
    public static void init() {
    	final IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
    	RegisteredSounds.SFX.register(bus);
    }
}