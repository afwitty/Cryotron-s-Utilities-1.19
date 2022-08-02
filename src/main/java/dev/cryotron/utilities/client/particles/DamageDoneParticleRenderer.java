package dev.cryotron.utilities.client.particles;

import com.google.gson.annotations.JsonAdapter;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;

import dev.cryotron.utilities.CTUtilities;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.opengl.GL11;

public class DamageDoneParticleRenderer {
	
	  private static final List<LivingEntity> renderedEntities = new ArrayList<>();
	  private static final float FULL_SIZE = 40;
	  
	  public static void prepareRenderInWorld(LivingEntity entity) {
		    if (!entity.getLevel().isClientSide)
		    	return;
		  
		    BarStates.getState(entity);
		    renderedEntities.add(entity);

		}
	
	  public static void renderParticles(PoseStack matrix, Camera camera) {
		    for (BarParticle p : BarStates.PARTICLES) {
		      renderParticle(matrix, p, camera);
		    }
		  }

		  private static void renderParticle(PoseStack matrix, BarParticle particle, Camera camera) {
		    double distanceSquared = camera.getPosition().distanceToSqr(particle.x, particle.y, particle.z);



		    Minecraft client = Minecraft.getInstance();
		    float tickDelta = client.getDeltaFrameTime();

		    double x = Mth.lerp((double) tickDelta, particle.xPrev, particle.x);
		    double y = Mth.lerp((double) tickDelta, particle.yPrev, particle.y);
		    double z = Mth.lerp((double) tickDelta, particle.zPrev, particle.z);

		    Vec3 camPos = camera.getPosition();
		    double camX = camPos.x;
		    double camY = camPos.y;
		    double camZ = camPos.z;

		    matrix.pushPose();
		    matrix.translate(x - camX, y - camY, z - camZ);
		    matrix.mulPose(Vector3f.YP.rotationDegrees(-camera.getYRot()));
		    matrix.mulPose(Vector3f.XP.rotationDegrees(camera.getXRot()));
		    
		    double deltaX = x - camX;
		    double deltaY = y - camY;
		    double deltaZ = z - camZ;
		    
		    float scaleToGui = 0.025f;
		    // PROTOTYPE: Number should display bigger when the damaged entity is at least 10 blocks away from any direction.
		    if ( (deltaX > 10 || deltaX < -10) || (deltaY > 10 || deltaY < -10) || (deltaZ > 10 || deltaZ < -10) ) {
		    	scaleToGui = 0.066f; // From 0.075f
		    }

		    
		    matrix.scale(-scaleToGui, -scaleToGui, scaleToGui);

		    RenderSystem.setShader(GameRenderer::getPositionColorShader);
		    RenderSystem.enableDepthTest();
		    RenderSystem.enableBlend();
		    RenderSystem.blendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);

		    boolean isCrit = particle.isCrit;
		    if (isCrit == true) {
		    	matrix.scale(1.5f, 1.5f, 1.5f);
		    	drawDamageNumber(matrix, particle.damage, 0, 0, 20, isCrit);    	
		    } else {
		    	matrix.scale(1.0f, 1.0f, 1.0f);
		    	drawDamageNumber(matrix, particle.damage, 0, 0, 10, isCrit);
		    }
		    
		    RenderSystem.disableBlend();

		    matrix.popPose();
		  }
		  
		  public static void drawDamageNumber(PoseStack matrix, int dmg, double x, double y, float width, boolean isCrit) {
			    int i = Math.abs(Math.round(dmg));
			    if (i == 0) {
			      return;
			    }
			    String s = Integer.toString(i);
			    Minecraft minecraft = Minecraft.getInstance();
			    
			    int damageColor = 0xffffff;
			    int healColor = 0x00ff00;
			    int critColor = 0xffff00;
			    
			    int sw = minecraft.font.width(s);
			    int color = dmg < 0 ? healColor : damageColor;
			    if (isCrit == true) {
			    	color = critColor;
				    minecraft.font.draw(matrix, s, (int) (x + (width / 2) - sw), (int) y + 5, color);			    	
			    } else {
				    minecraft.font.draw(matrix, s, (int) (x + (width / 2) - sw), (int) y + 5, color);			    	
			    }
			    

			  }

		public static void renderInWorld(float partialTick, PoseStack matrix, Camera camera) {
		    Minecraft client = Minecraft.getInstance();

		    if (camera == null) {
		      camera = client.getEntityRenderDispatcher().camera;
		    }

		    if (camera == null) {
		      renderedEntities.clear();
		      return;
		    }

		    if (renderedEntities.isEmpty()) {
		      return;
		    }

		    RenderSystem.setShader(GameRenderer::getPositionColorShader);
		    RenderSystem.enableDepthTest();
		    RenderSystem.enableBlend();
		    RenderSystem.blendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE,
		        GL11.GL_ZERO);

		    for (LivingEntity entity : renderedEntities) {
		      float scaleToGui = 0.025f;
		      boolean sneaking = entity.isCrouching();
		      float height = entity.getBbHeight() + 0.6F - (sneaking ? 0.25F : 0.0F);

		      double x = Mth.lerp((double) partialTick, entity.xo, entity.getX());
		      double y = Mth.lerp((double) partialTick, entity.yo, entity.getY());
		      double z = Mth.lerp((double) partialTick, entity.zo, entity.getZ());

		      Vec3 camPos = camera.getPosition();
		      double camX = camPos.x();
		      double camY = camPos.y();
		      double camZ = camPos.z();

		      matrix.pushPose();
		      matrix.translate(x - camX, (y + height) - camY, z - camZ);
		      matrix.mulPose(Vector3f.YP.rotationDegrees(-camera.getYRot()));
		      matrix.mulPose(Vector3f.XP.rotationDegrees(camera.getXRot()));
		      matrix.scale(-scaleToGui, -scaleToGui, scaleToGui);

		      //render(matrix, entity, 0, 0, FULL_SIZE, true);

		      matrix.popPose();
		    }

		    RenderSystem.disableBlend();

		    renderedEntities.clear();
			
		}


}
