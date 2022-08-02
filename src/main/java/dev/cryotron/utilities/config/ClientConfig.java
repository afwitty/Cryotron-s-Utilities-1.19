package dev.cryotron.utilities.config;

import dev.cryotron.utilities.client.gui.hud.HealthStatusRenderer;
import dev.cryotron.utilities.client.gui.hud.ArmorStatusRenderer;
import dev.cryotron.utilities.client.gui.hud.BreathStatusRenderer;
import dev.cryotron.utilities.client.gui.hud.HungerStatusRenderer;
import dev.cryotron.utilities.client.gui.hud.MntHealthStatusRenderer;
import net.minecraftforge.common.ForgeConfigSpec;

public final class ClientConfig {

	public final ForgeConfigSpec.BooleanValue disableHudPotionOffset;
	public final ForgeConfigSpec.EnumValue<HealthStatusRenderer.HealthRenderType> healthRenderType;
	public final ForgeConfigSpec.EnumValue<ArmorStatusRenderer.ArmorRenderType> armorRenderType;
	public final ForgeConfigSpec.EnumValue<HungerStatusRenderer.HungerRenderType> hungerRenderType;
	public final ForgeConfigSpec.EnumValue<BreathStatusRenderer.BreathRenderType> breathRenderType;
	public final ForgeConfigSpec.EnumValue<MntHealthStatusRenderer.MntHealthRenderType> mntHealthRenderType;
	
	public ClientConfig(ForgeConfigSpec.Builder specBuilder) {
		specBuilder.comment("CTUtilities client-side configuration options").push("General Settings");
		
		disableHudPotionOffset = specBuilder
				.comment("Set this to true to stop the skills and resources HUD elements shifting down when players have potion effects.")
				.translation("config.ctutilities.client.disableHudPotionOffset")
				.define("disableHudPotionOffset", false);
		
		healthRenderType = specBuilder
				.comment("Select what type of rendering CTUtilities replaces the vanila health bar with")
				.translation("config.ctutilities.client.healthRenderType")
				.defineEnum("healthRenderType", HealthStatusRenderer.HealthRenderType.BAR_NUMERIC);
	
		armorRenderType = specBuilder
				.comment("Select what type of rendering CTUtilities adds the armor bar with")
				.translation("config.ctutilities.client.armorRenderType")
				.defineEnum("armorRenderType", ArmorStatusRenderer.ArmorRenderType.BAR_NUMERIC);	
		
		hungerRenderType = specBuilder
				.comment("Select what type of rendering CTUtilities replaces the hunger bar with")
				.translation("config.ctutilities.client.hungerRenderType")
				.defineEnum("hungerRenderType", HungerStatusRenderer.HungerRenderType.BAR_NUMERIC);
		
		breathRenderType = specBuilder
				.comment("Select what type of rendering CTUtilities replaces the breath bar with")
				.translation("config.ctutilities.client.breathRenderType")
				.defineEnum("breathRenderType", BreathStatusRenderer.BreathRenderType.BAR_NUMERIC);
		
		mntHealthRenderType = specBuilder
				.comment("Select what type of rendering CTUtilities replaces the mount health bar with")
				.translation("config.ctutilities.client.breathRenderType")
				.defineEnum("mntHealthRenderType", MntHealthStatusRenderer.MntHealthRenderType.BAR_NUMERIC);
	}
	
}
