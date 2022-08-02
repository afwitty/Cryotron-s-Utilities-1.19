package dev.cryotron.utilities;

import java.util.Locale;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import dev.cryotron.utilities.common.CTUtilitiesConfig;
import dev.cryotron.utilities.setup.CTUSetup;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.common.Mod;

@Mod(CTUtilities.ID)
public class CTUtilities {

	public static final String ID = "cryoutilities";
	
	public static final Logger LOGGER = LogManager.getLogger(ID);
	
	public CTUtilities() {
    	LOGGER.info("Cryotron's Utilities Online!");
    	
    	// Config
		CTUtilitiesConfig.init();	 	
    	
    	// PreInit
    	CTUSetup.preInit();	    	
	}
	
	public static ResourceLocation id(String name) {
		return new ResourceLocation(ID, name.toLowerCase(Locale.ROOT));
	}
}



