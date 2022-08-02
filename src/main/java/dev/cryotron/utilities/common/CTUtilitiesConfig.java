package dev.cryotron.utilities.common;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.fml.loading.FileUtils;

import java.io.File;

import org.apache.commons.lang3.tuple.Pair;

import dev.cryotron.utilities.CTUtilities;
import dev.cryotron.utilities.config.ClientConfig;
import dev.cryotron.utilities.config.CommonConfig;
import dev.cryotron.utilities.config.IntegrationsConfig;
import dev.cryotron.utilities.config.ServerConfig;

public final class CTUtilitiesConfig {
	
	public static final ClientConfig CLIENT;
	public static final ForgeConfigSpec CLIENT_CONFIG_SPEC;

	public static final CommonConfig COMMON;
	public static final ForgeConfigSpec COMMON_CONFIG_SPEC;

	public static final ServerConfig SERVER;
	public static final ForgeConfigSpec SERVER_CONFIG_SPEC;

	public static final IntegrationsConfig INTEGRATIONS;
	public static final ForgeConfigSpec INTEGRATIONS_CONFIG_SPEC;
	
	public static void init() {
		FileUtils.getOrCreateDirectory(FMLPaths.CONFIGDIR.get().resolve(CTUtilities.ID), CTUtilities.ID);
		ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, CTUtilitiesConfig.SERVER_CONFIG_SPEC, CTUtilities.ID + "_server_config.toml");
		ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, CTUtilitiesConfig.COMMON_CONFIG_SPEC, CTUtilities.ID + File.separator + "common_config.toml");
		ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, CTUtilitiesConfig.CLIENT_CONFIG_SPEC, CTUtilities.ID + File.separator + "client_config.toml");
		ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, CTUtilitiesConfig.INTEGRATIONS_CONFIG_SPEC, CTUtilities.ID + File.separator + "integrations_config.toml");
	}
	
	static {

		final Pair<ClientConfig, 				ForgeConfigSpec> clientSpecPair 				= new ForgeConfigSpec.Builder().configure(ClientConfig::new);
		final Pair<CommonConfig, 		ForgeConfigSpec> commonSpecPair 		= new ForgeConfigSpec.Builder().configure(CommonConfig::new);
		final Pair<ServerConfig, 			ForgeConfigSpec> serverSpecPair 			= new ForgeConfigSpec.Builder().configure(ServerConfig::new);
		final Pair<IntegrationsConfig, 	ForgeConfigSpec> integrationSpecPair 	= new ForgeConfigSpec.Builder().configure(IntegrationsConfig::new);

		CLIENT_CONFIG_SPEC = clientSpecPair.getRight();
		CLIENT = clientSpecPair.getLeft();
		COMMON_CONFIG_SPEC = commonSpecPair.getRight();
		COMMON = commonSpecPair.getLeft();
		SERVER_CONFIG_SPEC = serverSpecPair.getRight();
		SERVER = serverSpecPair.getLeft();
		INTEGRATIONS_CONFIG_SPEC = integrationSpecPair.getRight();
		INTEGRATIONS = integrationSpecPair.getLeft();
	}
}