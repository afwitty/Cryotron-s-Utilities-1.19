package dev.cryotron.utilities.setup;

import dev.cryotron.utilities.CTUtilities;
import dev.cryotron.utilities.client.particles.BarStates;
import dev.cryotron.utilities.client.particles.DamageDoneParticleRenderer;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.client.event.RenderLevelLastEvent;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = CTUtilities.ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientEvents {
	
	public static void init() {
	    MinecraftForge.EVENT_BUS.addListener(ClientEvents::playerTick);
	    MinecraftForge.EVENT_BUS.addListener(ClientEvents::entityRender);	
	    MinecraftForge.EVENT_BUS.addListener(ClientEvents::renderParticles);
	}
	
	private static Minecraft minecraft = Minecraft.getInstance();
	
    // Supplemental Methods - Will move them later.
    
    private static void playerTick(PlayerTickEvent event) {
        if (!event.player.level.isClientSide) {
          return;
        }
        BarStates.tick();
      }
    
    private static void entityRender(RenderLivingEvent.Post<? extends LivingEntity, ? extends EntityModel<?>> event) {

    		DamageDoneParticleRenderer.prepareRenderInWorld(event.getEntity());
    	  }

    
    @SuppressWarnings({ "removal", "deprecation" })
	private static void renderParticles(RenderLevelLastEvent event) {  	
        Camera camera = minecraft.gameRenderer.getMainCamera();
        DamageDoneParticleRenderer.renderParticles(event.getPoseStack(), camera);
        DamageDoneParticleRenderer.renderInWorld(event.getPartialTick(), event.getPoseStack(), camera);
      }
}
