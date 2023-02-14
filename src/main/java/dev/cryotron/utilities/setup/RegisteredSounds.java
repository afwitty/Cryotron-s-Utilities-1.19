package dev.cryotron.utilities.setup;

import dev.cryotron.utilities.CTUtilities;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class RegisteredSounds {
	public static final DeferredRegister<SoundEvent> SFX = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, CTUtilities.ID);
	 
	public static final RegistryObject<SoundEvent> PLAYER_DEATH = registerSound("death", "player.death");
	 
	private static RegistryObject<SoundEvent> registerSound(String registryName, String soundPath) {
		return SFX.register(registryName, () -> createSoundEvent(soundPath));
	}
	
	// Using AOA3 Sound methods for sound effects.
	private static SoundEvent createSoundEvent(String soundPath) {
		return new SoundEvent(new ResourceLocation("cryoutilities", soundPath));
	}
}