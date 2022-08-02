package dev.cryotron.utilities.client.gui.hud;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import dev.cryotron.utilities.CTUtilities;
import dev.cryotron.utilities.common.CTUtilitiesConfig;
import dev.cryotron.utilities.util.aoa.NumberUtil;
import dev.cryotron.utilities.util.aoa.RenderUtil;
import dev.cryotron.utilities.util.aoa.ColourUtil;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.gui.ForgeIngameGui;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.EventPriority;

public class ArmorStatusRenderer {
	private static final ResourceLocation ARMOR_BAR = CTUtilities.id("textures/gui/overlay/misc/armor_bar.png");
	private static float deltaArmor = 0;
	private static int lastArmorTime = 0;
	private static float lastTickArmor = 0;
	
	public static void init() {
		MinecraftForge.EVENT_BUS.addListener(EventPriority.LOWEST, false, RenderGameOverlayEvent.PreLayer.class, ArmorStatusRenderer::onArmorRender);	
	}
	
	private static void onArmorRender( RenderGameOverlayEvent.PreLayer ev ) {
		
		Minecraft mc = Minecraft.getInstance();
		
		LocalPlayer player = mc.player;
		PoseStack matrix = ev.getPoseStack();
		
		float currentArmor = player.getArmorValue();
		float maxHealth = 20;
		float toughness = (float) player.getAttributeValue(Attributes.ARMOR_TOUGHNESS);
		

		if ( currentArmor == 0) return;
		
		if (ev.isCanceled() || ev.getOverlay() != ForgeIngameGui.ARMOR_LEVEL_ELEMENT) {
			return;
		}
		
		ArmorRenderType renderType = CTUtilitiesConfig.CLIENT.armorRenderType.get();
		
		if (renderType == ArmorRenderType.ICONS) {
			return;
		}		

		if (!(mc.gui instanceof ForgeIngameGui gui) || mc.options.hideGui || !gui.shouldDrawSurvivalElements())
			return;

		int left = (mc.getWindow().getGuiScaledWidth() / 2) - 91;
		int top = mc.getWindow().getGuiScaledHeight() - gui.left_height;
		gui.left_height += 10;
		
		gui.setupOverlayRenderState(true, false);
		ev.setCanceled(true);

		mc.getProfiler().push("armor");
		RenderSystem.enableBlend();
		
		if (renderType == ArmorRenderType.NUMERIC) {
			renderNumeric(matrix, mc, gui, left, top, currentArmor, maxHealth, toughness);
		}
		else {
			renderBar(matrix, mc, gui, left, top, currentArmor, maxHealth);

			if (renderType ==  ArmorRenderType.BAR_NUMERIC)
				renderNumeric(matrix, mc, gui, left, top, currentArmor, maxHealth, toughness);
		}
		
		RenderSystem.disableBlend();
		mc.getProfiler().pop();
		MinecraftForge.EVENT_BUS.post(new RenderGameOverlayEvent.Post(matrix, ev, RenderGameOverlayEvent.ElementType.LAYER));
		
	}
	
	private static void renderBar(PoseStack matrix, Minecraft mc, ForgeIngameGui gui, int left, int top, float currentArmor, float maxHealth) {
		int uvY = 0;

		if (!NumberUtil.roundToNthDecimalPlace(lastTickArmor, 1).equals(NumberUtil.roundToNthDecimalPlace(currentArmor, 1))) {
			if (lastTickArmor < currentArmor) {
				if (lastArmorTime == 0) {
					lastTickArmor = currentArmor;
					lastArmorTime = mc.player.tickCount;

					return;
				}

				if (deltaArmor < 0)
					deltaArmor = 0;

				deltaArmor += currentArmor - lastTickArmor;
			}
			else {
				if (deltaArmor > 0)
					deltaArmor = 0;

				deltaArmor -= lastTickArmor - currentArmor;
			}

			lastArmorTime = mc.player.tickCount + 12;
		}

		if (deltaArmor != 0 && lastArmorTime + 20 < mc.player.tickCount)
			deltaArmor = 0;

		lastTickArmor = currentArmor;
		matrix.pushPose();
		matrix.translate(left, top - 1.9, 0);
		RenderUtil.prepRenderTexture(ARMOR_BAR);

		float healthWidth = 81 * (currentArmor / maxHealth); 

		if (currentArmor < maxHealth)
			RenderUtil.renderCustomSizedTexture(matrix, 0, 0, 0, 12, 81, 12, 81, 24);

		if (!mc.player.isAlive()) {
			matrix.popPose();

			return;
		}

		RenderUtil.renderCustomSizedTexture(matrix, 0, 0, 0, uvY, healthWidth, 12, 81, 24);


		RenderUtil.drawColouredBox(matrix, 0, 0, 0, 81, 11, 0x44000000);
		matrix.popPose();
	}

	private static void renderNumeric(PoseStack matrix, Minecraft mc, ForgeIngameGui gui, int left, int top, float currentArmor, float maxHealth, float toughness) {
		int healthColour;

		if (true) {
			healthColour = ColourUtil.RGB(252, 252, 252);
		}

		matrix.pushPose();

		if (CTUtilitiesConfig.CLIENT.armorRenderType.get() == ArmorRenderType.NUMERIC) {
			if (toughness > 0)
				left -= 15;

			matrix.translate(left + 15, top + 0.9, 0);
			matrix.scale(0.9f, 0.9f, 1);

			if (currentArmor > 0) {
				renderIcon(matrix, mc, currentArmor, maxHealth, handleArmorState(mc.player, gui, (int)Math.ceil(currentArmor)));

				RenderUtil.drawCenteredScaledString(matrix, mc.font, NumberUtil.roundToNthDecimalPlace(currentArmor, 1) + "/" + NumberUtil.roundToNthDecimalPlace(maxHealth, 1), 34, 0, 1, healthColour, RenderUtil.StringRenderType.OUTLINED);
				if (toughness > 0) {
					RenderUtil.drawCenteredScaledString(matrix, mc.font, "+", 67, 0, 1, healthColour, RenderUtil.StringRenderType.OUTLINED);
					RenderUtil.drawCenteredScaledString(matrix, mc.font, NumberUtil.roundToNthDecimalPlace(toughness, 1), 83, 0, 1, healthColour, RenderUtil.StringRenderType.OUTLINED);
				}
			}
			else {
				RenderUtil.drawCenteredScaledString(matrix, mc.font,  " No Armor Equipped", (CTUtilitiesConfig.CLIENT.armorRenderType.get() == ArmorRenderType.BAR_NUMERIC ? 28.5f : 24), 0, 1, ColourUtil.RGB(255, 255, 255), RenderUtil.StringRenderType.OUTLINED);
			}
		}
		else {
			gui.left_height += 2;
			
			if (toughness > 0)
				left -= 8;

			matrix.translate(left + 17, top + 1.2, 0);
			matrix.scale(0.8f, 0.8f, 1);

			if (currentArmor > 0) {
				renderIcon(matrix, mc, currentArmor, maxHealth, handleArmorState(mc.player, gui, (int)Math.ceil(currentArmor)));

				RenderUtil.drawCenteredScaledString(matrix, mc.font, NumberUtil.roundToNthDecimalPlace(currentArmor, 1) + "/" + NumberUtil.roundToNthDecimalPlace(maxHealth, 1), 34, 0, 1, healthColour, RenderUtil.StringRenderType.OUTLINED);
				if (toughness > 0) {
					RenderUtil.drawCenteredScaledString(matrix, mc.font, "+", 62, 0, 1, healthColour, RenderUtil.StringRenderType.OUTLINED);
					RenderUtil.drawCenteredScaledString(matrix, mc.font, NumberUtil.roundToNthDecimalPlace(toughness, 1), 70, 0, 1, healthColour, RenderUtil.StringRenderType.OUTLINED);
				}
			}
			else {
				RenderUtil.drawCenteredScaledString(matrix, mc.font, " No Armor Equipped", (CTUtilitiesConfig.CLIENT.armorRenderType.get() == ArmorRenderType.BAR_NUMERIC ? 28.5f : 24), 0, 1, ColourUtil.RGB(255, 255, 255), RenderUtil.StringRenderType.OUTLINED);
			}
		}

		matrix.popPose();
	}
	
	private static void renderIcon(PoseStack matrix, Minecraft mc, float currentArmor, float maxHealth, boolean flashing) {
		int uvX = 0;
		int uvY = 9;
		int y = -1;


		RenderSystem.setShaderTexture(0, GuiComponent.GUI_ICONS_LOCATION);

		RenderUtil.renderCustomSizedTexture(matrix, 0, y, uvX + (currentArmor >= maxHealth ? 43 : 25), uvY, 9, 9, 256, 256);
	}
	
	// Possibly deprecated. -CT
	private static boolean handleArmorState(LocalPlayer player, ForgeIngameGui gui, float currentArmor) {
		boolean shouldFlash = gui.healthBlinkTime > (long)gui.tickCount && (gui.healthBlinkTime - (long)gui.tickCount) / 3L % 2L == 1L;

		if (currentArmor < gui.lastHealth && player.invulnerableTime > 0) {
			gui.lastHealthTime = Util.getMillis();
			gui.healthBlinkTime = gui.tickCount + 20;
		}
		else if (currentArmor > gui.lastHealth && player.invulnerableTime > 0) {
			gui.lastHealthTime = Util.getMillis();
			gui.healthBlinkTime = gui.tickCount + 10;
		}

		if (Util.getMillis() - gui.lastHealthTime > 1000L) {
			gui.lastHealth = (int)currentArmor;
			gui.displayHealth = (int)currentArmor;
			gui.lastHealthTime = Util.getMillis();
		}

		gui.lastHealth = (int)currentArmor;

		return shouldFlash;
	}
	
	public enum ArmorRenderType {
		ICONS,
		BAR,
		NUMERIC,
		BAR_NUMERIC
	}
}
