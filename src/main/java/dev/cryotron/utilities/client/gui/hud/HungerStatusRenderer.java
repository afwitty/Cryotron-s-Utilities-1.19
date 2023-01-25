package dev.cryotron.utilities.client.gui.hud;

import dev.cryotron.utilities.CTUtilities;
import dev.cryotron.utilities.common.CTUtilitiesConfig;
import dev.cryotron.utilities.util.aoa.NumberUtil;
import dev.cryotron.utilities.util.aoa.RenderUtil;
import dev.cryotron.utilities.util.aoa.ColourUtil;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffects;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.gui.ForgeIngameGui;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.EventPriority;

@OnlyIn(Dist.CLIENT)
public class HungerStatusRenderer {
	private static final ResourceLocation HUNGER_BAR = CTUtilities.id("textures/gui/overlay/misc/hunger_bar.png");
	private static final ResourceLocation SandE_BAR = CTUtilities.id("textures/gui/overlay/misc/saturation_and_exhaustion.png");
	private static final ResourceLocation SATURATION_ICON = CTUtilities.id("textures/gui/overlay/misc/saturationicons.png");
	private static float deltaArmor = 0;
	private static int lastArmorTime = 0;
	private static float lastTickArmor = 0;
	
	public static void init() {
		MinecraftForge.EVENT_BUS.addListener(EventPriority.LOWEST, false, RenderGameOverlayEvent.PreLayer.class, HungerStatusRenderer::onHungerRender);		
	}
	
	private static void onHungerRender( RenderGameOverlayEvent.PreLayer ev ) {
		
		Minecraft mc = Minecraft.getInstance();
		
		LocalPlayer player = mc.player;
		PoseStack matrix = ev.getPoseStack();
		
		float currentHunger = player.getFoodData().getFoodLevel();
		float saturationLevel  = player.getFoodData().getSaturationLevel();
		float exhaustionLevel = player.getFoodData().getExhaustionLevel();
		boolean hunger = mc.player.hasEffect(MobEffects.HUNGER);
		boolean saturation = mc.player.hasEffect(MobEffects.SATURATION);
		float maxHealth = 20;	
	
		if (ev.isCanceled() || ev.getOverlay() != ForgeIngameGui.FOOD_LEVEL_ELEMENT) {
			return;
		}
		
		HungerRenderType renderType = CTUtilitiesConfig.CLIENT.hungerRenderType.get();
		
		if (renderType == HungerRenderType.ICONS) {
			return;
		}		

		if (!(mc.gui instanceof ForgeIngameGui gui) || mc.options.hideGui || !gui.shouldDrawSurvivalElements())
			return;

		int left = (mc.getWindow().getGuiScaledWidth() / 2) + 10;
		int top = mc.getWindow().getGuiScaledHeight() - gui.right_height; // 25;
		gui.right_height += 11;
		
		gui.setupOverlayRenderState(true, false);
		ev.setCanceled(true);

		mc.getProfiler().push("hunger");
		RenderSystem.enableBlend();
		
		if (renderType == HungerRenderType.NUMERIC) {
			renderNumeric(matrix, mc, gui, left, top, currentHunger, maxHealth, saturationLevel, hunger, saturation);
		}
		else {
			renderBar(matrix, mc, gui, left, top, currentHunger, maxHealth, saturationLevel, exhaustionLevel, hunger, saturation);

			if (renderType ==  HungerRenderType.BAR_NUMERIC)
				renderNumeric(matrix, mc, gui, left, top, currentHunger, maxHealth, saturationLevel, hunger, saturation);
		}
		
		RenderSystem.disableBlend();
		mc.getProfiler().pop();
		MinecraftForge.EVENT_BUS.post(new RenderGameOverlayEvent.Post(matrix, ev, RenderGameOverlayEvent.ElementType.LAYER));
		
	}
	
	private static void renderBar(PoseStack matrix, Minecraft mc, ForgeIngameGui gui, int left, int top, float currentHunger, float maxHealth, float saturationLevel, float exhaustionLevel, boolean hunger, boolean saturation) {
		int uvY = 0;
		if (hunger) {
			uvY = 36;
		}
		if (saturation) {
			uvY = 48;
		}

		if (!NumberUtil.roundToNthDecimalPlace(lastTickArmor, 1).equals(NumberUtil.roundToNthDecimalPlace(currentHunger, 1))) {
			if (lastTickArmor < currentHunger) {
				if (lastArmorTime == 0) {
					lastTickArmor = currentHunger;
					lastArmorTime = mc.player.tickCount;

					return;
				}

				if (deltaArmor < 0)
					deltaArmor = 0;

				deltaArmor += currentHunger - lastTickArmor;
			}
			else {
				if (deltaArmor > 0)
					deltaArmor = 0;

				deltaArmor -= lastTickArmor - currentHunger;
			}

			lastArmorTime = mc.player.tickCount + 12;
		}

		if (deltaArmor != 0 && lastArmorTime + 20 < mc.player.tickCount)
			deltaArmor = 0;

		lastTickArmor = currentHunger;
		matrix.pushPose();
		matrix.translate(left, top - 1.9, 0);
		RenderUtil.prepRenderTexture(HUNGER_BAR);

		//float maxWidth = 81 * maxHealth;
		float healthWidth = 81 * (currentHunger / maxHealth); 
		float saturationWidth = 81 * (saturationLevel / 20);
		float exhaustionWidth = 81 * (exhaustionLevel/ 4);

		if (currentHunger < maxHealth)
			RenderUtil.renderCustomSizedTexture(matrix, 0, 0, 0, 12, 81, 12, 81, 60);

		if (!mc.player.isAlive()) {
			matrix.popPose();

			return;
		}

		RenderUtil.renderCustomSizedTexture(matrix, 0, 0, 0, uvY, healthWidth, 12, 81, 60);

		RenderUtil.drawColouredBox(matrix, 0, 0, 0, 81, 11, 0x44000000);
		
		RenderUtil.prepRenderTexture(SandE_BAR);		
		
		RenderUtil.renderCustomSizedTexture(matrix, 0, 0, 0, 0, saturationWidth, 2, 81, 12);
		
		if (exhaustionLevel <= 4.0f) {
			RenderUtil.renderCustomSizedTexture(matrix, 0, 8, 0, 8, exhaustionWidth, 4, 81, 12);
		}
		
		// TODO: Implement Food Restoring Hunger.
		
		matrix.popPose();
	}

	private static void renderNumeric(PoseStack matrix, Minecraft mc, ForgeIngameGui gui, int left, int top, float currentHunger, float maxHealth, float saturationLevel, boolean hunger, boolean saturation) {
		int healthColour;

		healthColour = ColourUtil.RGB(252, 252, 252);
		if (hunger) {
			healthColour = ColourUtil.RGB(117, 113, 0);
		}
		if (saturation) {
			healthColour = ColourUtil.RGB(255, 204, 0);
		}

		matrix.pushPose();

		if (CTUtilitiesConfig.CLIENT.hungerRenderType.get() == HungerRenderType.NUMERIC) {
			
			if (saturationLevel > 0) 
				left -=15;

			matrix.translate(left + 15, top + 0.9, 0);
			matrix.scale(0.9f, 0.9f, 1);

			if (currentHunger > 0) {
				renderIcon(matrix, mc, currentHunger, maxHealth, handleHungerState(mc.player, gui, (int)Math.ceil(currentHunger)), saturationLevel, hunger);

				RenderUtil.drawCenteredScaledString(matrix, mc.font, NumberUtil.roundToNthDecimalPlace(currentHunger, 1) + "/" + NumberUtil.roundToNthDecimalPlace(maxHealth, 1), 34, 0, 1, healthColour, RenderUtil.StringRenderType.OUTLINED);

				if (saturationLevel > 0) {
					RenderUtil.drawCenteredScaledString(matrix, mc.font, "+", 67, 0, 1, ColourUtil.RGB(255, 204, 0), RenderUtil.StringRenderType.OUTLINED);
					RenderUtil.drawCenteredScaledString(matrix, mc.font, NumberUtil.roundToNthDecimalPlace(saturationLevel, 0), 83, 0, 1, ColourUtil.RGB(255, 204, 0), RenderUtil.StringRenderType.OUTLINED);
				}
				
			}
			else {
				RenderUtil.drawCenteredScaledString(matrix, mc.font,  "Starving!", (CTUtilitiesConfig.CLIENT.hungerRenderType.get() == HungerRenderType.BAR_NUMERIC ? 28.5f : 24), 0, 1, ColourUtil.RGB(255, 0, 0), RenderUtil.StringRenderType.OUTLINED);
			}
		}
		else {
			gui.left_height += 2;

			if (saturationLevel > 0) 
				left -=8;
			
			matrix.translate(left + 17, top + 1.2, 0);
			matrix.scale(0.8f, 0.8f, 1);

			if (currentHunger > 0) {
				renderIcon(matrix, mc, currentHunger, maxHealth, handleHungerState(mc.player, gui, (int)Math.ceil(currentHunger)), saturationLevel, hunger);

				RenderUtil.drawCenteredScaledString(matrix, mc.font, NumberUtil.roundToNthDecimalPlace(currentHunger, 1) + "/" + NumberUtil.roundToNthDecimalPlace(maxHealth, 1), 34, 0, 1, healthColour, RenderUtil.StringRenderType.OUTLINED);

				if (saturationLevel > 0) {
					RenderUtil.drawCenteredScaledString(matrix, mc.font, "+", 62, 0, 1, ColourUtil.RGB(255, 204, 0), RenderUtil.StringRenderType.OUTLINED);
					RenderUtil.drawCenteredScaledString(matrix, mc.font, NumberUtil.roundToNthDecimalPlace(saturationLevel, 0), 70, 0, 1, ColourUtil.RGB(255, 204, 0), RenderUtil.StringRenderType.OUTLINED);
				}
			}
			else {
				RenderUtil.drawCenteredScaledString(matrix, mc.font, "Starving!", (CTUtilitiesConfig.CLIENT.hungerRenderType.get() == HungerRenderType.BAR_NUMERIC ? 28.5f : 24), 0, 1, ColourUtil.RGB(255, 0, 0), RenderUtil.StringRenderType.OUTLINED);
			}
		}

		matrix.popPose();
	}
	
	private static void renderIcon(PoseStack matrix, Minecraft mc, float currentHunger, float maxHealth, boolean flashing, float currSaturation, boolean hunger) {
		int uvX = hunger ? 72 : 36;
		int uvY = 27;
		int y = -1;

		RenderSystem.setShaderTexture(0, SATURATION_ICON);
		if (currSaturation == 0) {
			RenderUtil.renderCustomSizedTexture(matrix, 0, -1, 0, 0, 9, 9, 45, 9);	
		}
		if ( currSaturation < 20 && currSaturation != 0) {
			RenderUtil.renderCustomSizedTexture(matrix, 0, -1, 9, 0, 9, 9, 45, 9);			
		}
		if ( currSaturation >= 20 ) {
			RenderUtil.renderCustomSizedTexture(matrix, 0, -1, 18, 0, 9, 9, 45, 9);			
		}
		if (hunger) {		
			RenderSystem.setShaderTexture(0, GuiComponent.GUI_ICONS_LOCATION);
			RenderUtil.renderCustomSizedTexture(matrix, 0, -1, 115, 27, 9, 9, 256, 256);	
			//RenderSystem.setShaderTexture(0, SATURATION_ICON);
		}
		RenderSystem.setShaderTexture(0, GuiComponent.GUI_ICONS_LOCATION);
		RenderUtil.renderCustomSizedTexture(matrix, 0, y, uvX + (currentHunger >= maxHealth ? 16 : 25), uvY, 9, 9, 256, 256);

	}

	// Possibly deprecated. -CT
	private static boolean handleHungerState(LocalPlayer player, ForgeIngameGui gui, float currentHunger) {
		boolean shouldFlash = gui.healthBlinkTime > (long)gui.tickCount && (gui.healthBlinkTime - (long)gui.tickCount) / 3L % 2L == 1L;

		if (currentHunger < gui.lastHealth && player.invulnerableTime > 0) {
			gui.lastHealthTime = Util.getMillis();
			gui.healthBlinkTime = gui.tickCount + 20;
		}
		else if (currentHunger > gui.lastHealth && player.invulnerableTime > 0) {
			gui.lastHealthTime = Util.getMillis();
			gui.healthBlinkTime = gui.tickCount + 10;
		}

		if (Util.getMillis() - gui.lastHealthTime > 1000L) {
			gui.lastHealth = (int)currentHunger;
			gui.displayHealth = (int)currentHunger;
			gui.lastHealthTime = Util.getMillis();
		}

		gui.lastHealth = (int)currentHunger;

		return shouldFlash;
	}

	public enum HungerRenderType {
		ICONS,
		BAR,
		NUMERIC,
		BAR_NUMERIC
	}
	
}
