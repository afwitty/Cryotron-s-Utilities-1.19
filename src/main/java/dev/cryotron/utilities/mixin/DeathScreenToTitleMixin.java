package dev.cryotron.utilities.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import dev.cryotron.utilities.CTUtilities;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.DeathScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

@Mixin(DeathScreen.class)
public abstract class DeathScreenToTitleMixin {

	/*
	 * I guess I have to edit this method manually. -CT 
	 */
	@Inject(method = "init", at = @At("HEAD"), cancellable = true)
	private void skipTitleScreenPrompt( CallbackInfo ci ) 
	{
        DeathScreen ds = (DeathScreen) (Object) this;
        
        ds.delayTicker = 0;
        ds.exitButtons.clear();
        ds.exitButtons.add(ds.addRenderableWidget(new Button(ds.width / 2 - 100, ds.height / 4 + 72, 200, 20, ds.hardcore ? Component.translatable("deathScreen.spectate") : Component.translatable("deathScreen.respawn"), (p_95930_) -> {
           ds.minecraft.player.respawn();
           ds.minecraft.setScreen((Screen)null);
        })));
        ds.exitButtons.add(ds.addRenderableWidget(new Button(ds.width / 2 - 100, ds.height / 4 + 96, 200, 20, Component.translatable("deathScreen.titleScreen"), (p_95925_) -> {
           if (ds.hardcore) {
              ds.confirmResult(true);
              ds.exitToTitleScreen();
           } else {
              ConfirmScreen confirmscreen = new ConfirmScreen(ds::confirmResult, Component.translatable("deathScreen.quit.confirm"), CommonComponents.EMPTY, Component.translatable("deathScreen.titleScreen"), Component.translatable("deathScreen.respawn"));
              ds.minecraft.setScreen(confirmscreen);
              confirmscreen.setDelay(20);
           }
        })));

        for(Button button : ds.exitButtons) {
           button.active = false;
        }

        ds.deathScore = Component.translatable("deathScreen.score").append(": ").append(Component.literal(Integer.toString(ds.minecraft.player.getScore())).withStyle(ChatFormatting.YELLOW));

		ci.cancel();
	}
	
}
