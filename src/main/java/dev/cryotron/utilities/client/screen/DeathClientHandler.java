package dev.cryotron.utilities.client.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.DeathScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenOpenEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import dev.cryotron.utilities.CTUtilities;
import dev.cryotron.utilities.setup.RegisteredSounds;

@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = CTUtilities.ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class DeathClientHandler {
	
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onScreenOpen(ScreenOpenEvent event) {
        Screen screen = event.getScreen();
        if (screen instanceof DeathScreen deathScreen && !(screen instanceof DeathSplashScreen)) {
            LocalPlayer player = Minecraft.getInstance().player;
            if (player != null && !(Minecraft.getInstance().screen instanceof DeathScreen)) {
                event.setScreen(new DeathSplashScreen(new DeathScreenWrapper(deathScreen)));
                Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(RegisteredSounds.PLAYER_DEATH.get(), 1.0F, 1.0F));
            }
        }
    }
	
}
