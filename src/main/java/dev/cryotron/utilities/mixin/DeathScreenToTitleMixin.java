package dev.cryotron.utilities.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import dev.cryotron.utilities.CTUtilities;
import net.minecraft.client.gui.screens.DeathScreen;

@Mixin(DeathScreen.class)
public abstract class DeathScreenToTitleMixin {

	@Inject(method = "handleExitToTitleScreen", at = @At("HEAD"), cancellable = true)
	private void skipTitleScreenPrompt( CallbackInfo ci ) 
	{
        DeathScreen deathScreen = (DeathScreen) (Object) this;
        
		deathScreen.exitToTitleScreen();
		ci.cancel();
	}
	
}
