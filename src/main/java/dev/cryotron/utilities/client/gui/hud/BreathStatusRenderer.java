package dev.cryotron.utilities.client.gui.hud;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import dev.cryotron.utilities.CTUtilities;
import dev.cryotron.utilities.common.CTUtilitiesConfig;
import dev.cryotron.utilities.util.aoa.NumberUtil;
import dev.cryotron.utilities.util.aoa.RenderUtil;
import dev.cryotron.utilities.util.aoa.ColourUtil;
import dev.cryotron.utilities.util.aoa.LocaleUtil;

import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffects;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.EventPriority;

public class BreathStatusRenderer {
	private static final ResourceLocation BREATH_BAR = CTUtilities.id("textures/gui/overlay/misc/breath_bar.png");
	private static final ResourceLocation CONDUIT_ICON = CTUtilities.id("textures/gui/overlay/misc/conduit_icon.png");
	private static final ResourceLocation DROWN_BAR = CTUtilities.id("textures/gui/overlay/misc/drowning.png");
	private static float deltaHealth = 0;
	private static int lastHealthTime = 0;
	@SuppressWarnings("unused")
	private static float lastTickHealth = 0;

	public static void init() {
		MinecraftForge.EVENT_BUS.addListener(EventPriority.LOWEST, false, RenderGuiOverlayEvent.Pre.class, BreathStatusRenderer::onBreathRender);
	}

	private static void onBreathRender(RenderGuiOverlayEvent.Pre ev) {
		if (ev.isCanceled() || !ev.getOverlay().id().equals(VanillaGuiOverlay.AIR_LEVEL.id()))
			return;

		BreathRenderType renderType = CTUtilitiesConfig.CLIENT.breathRenderType.get();

		if (renderType == BreathRenderType.ICONS)
			return;

		Minecraft mc = Minecraft.getInstance();

		if (!(mc.gui instanceof ForgeGui gui) || mc.options.hideGui || !gui.shouldDrawSurvivalElements())
			return;

		LocalPlayer player = mc.player;
		PoseStack matrix = ev.getPoseStack();
		
		
		int left = (mc.getWindow().getGuiScaledWidth() / 2) + 10; //-91
		int top = mc.getWindow().getGuiScaledHeight() - gui.rightHeight;
		gui.rightHeight += 11;

		gui.setupOverlayRenderState(true, false);
		ev.setCanceled(true);

		mc.getProfiler().push("breath");
		RenderSystem.enableBlend();

		float currentBreath = player.getAirSupply();
		float maxBreath = 300;
		boolean waterBreathing = mc.player.hasEffect(MobEffects.WATER_BREATHING);
		boolean conduit = mc.player.hasEffect(MobEffects.CONDUIT_POWER);
		
		if ( currentBreath >= maxBreath && !player.isUnderWater() ) {
			return;
		}

		if (renderType == BreathRenderType.NUMERIC) {
			renderNumeric(matrix, mc, gui, left, top, currentBreath, maxBreath, waterBreathing, conduit);
		}
		else {
			renderBar(matrix, mc, gui, left, top, currentBreath, maxBreath, waterBreathing, conduit);

			if (renderType ==  BreathRenderType.BAR_NUMERIC)
				renderNumeric(matrix, mc, gui, left, top, currentBreath, maxBreath, waterBreathing, conduit);
		}

		RenderSystem.disableBlend();
		mc.getProfiler().pop();
		//MinecraftForge.EVENT_BUS.post(new RenderGuiOverlayEvent.Post(matrix, ev, RenderGuiOverlayEvent.ElementType.LAYER));
	}

	private static void renderBar(PoseStack matrix, Minecraft mc, ForgeGui gui, int left, int top, float currentBreath, float maxBreath, boolean waterBreathing, boolean conduit) {
		int uvY = 0;
		if (waterBreathing) {
			uvY = 36; 
		}
		else if (conduit) {
			uvY = 48;
		}
		else if (currentBreath <= 0) {
			uvY = 24;
		}

		if (deltaHealth != 0 && lastHealthTime + 20 < mc.player.tickCount)
			deltaHealth = 0;

		lastTickHealth = currentBreath;
		matrix.pushPose();
		matrix.translate(left, top - 1.9, 0);
		RenderUtil.prepRenderTexture(BREATH_BAR);

		float healthWidth = 81 * (currentBreath / maxBreath); 
		float drownWidth = 81 * ((currentBreath*-1)/20);

		if (currentBreath < maxBreath)
			RenderUtil.renderCustomSizedTexture(matrix, 0, 0, 0, 12, 81, 12, 81, 60);

		if (!mc.player.isAlive()) {
			matrix.popPose();

			return;
		}
		
		
		
		if (currentBreath <= 0) {
			RenderUtil.renderCustomSizedTexture(matrix, 0, 0, 0, uvY, 81, 12, 81, 60);
			
			RenderUtil.prepRenderTexture(DROWN_BAR);		
			RenderUtil.renderCustomSizedTexture(matrix, 0, 8, 0, 8, drownWidth, 12, 81, 12);
			
		} else {
			RenderUtil.renderCustomSizedTexture(matrix, 0, 0, 0, uvY, healthWidth, 12, 81, 60);
		}


		//RenderUtil.drawColouredBox(matrix, 0, 0, 0, 81, 11, 0x44000000);
		matrix.popPose();
	}

	private static void renderNumeric(PoseStack matrix, Minecraft mc, ForgeGui gui, int left, int top, float currentBreath, float maxBreath, boolean waterBreathing, boolean conduit) {
		int healthColour;

		healthColour = ColourUtil.RGB(0, 168, 243);

		matrix.pushPose();

		if (CTUtilitiesConfig.CLIENT.breathRenderType.get() == BreathRenderType.NUMERIC) {

			matrix.translate(left + 15, top + 0.9, 0);
			matrix.scale(0.9f, 0.9f, 1);

			if (currentBreath > 0) {
				renderIcon(matrix, mc, currentBreath, maxBreath, handleBreathState(mc.player, gui, (int)Math.ceil(currentBreath)), conduit);

				RenderUtil.drawCenteredScaledString(matrix, mc.font, NumberUtil.roundToNthDecimalPlace(currentBreath, 1) + "/" + NumberUtil.roundToNthDecimalPlace(maxBreath, 1), 34, 0, 1, healthColour, RenderUtil.StringRenderType.OUTLINED);
			}
			else {
				RenderUtil.drawCenteredScaledString(matrix, mc.font, LocaleUtil.getLocaleString("Drowning!"), (CTUtilitiesConfig.CLIENT.breathRenderType.get() == BreathRenderType.BAR_NUMERIC ? 28.5f : 24), 0, 1, ColourUtil.RGB(150, 0, 0), RenderUtil.StringRenderType.OUTLINED);
			}
		}
		else {
			gui.leftHeight += 2;


			matrix.translate(left + 17, top + 1.2, 0);
			matrix.scale(0.8f, 0.8f, 1);

			if (currentBreath > 0) {
				renderIcon(matrix, mc, currentBreath, maxBreath, handleBreathState(mc.player, gui, (int)Math.ceil(currentBreath)), conduit);

				RenderUtil.drawCenteredScaledString(matrix, mc.font, NumberUtil.roundToNthDecimalPlace(currentBreath, 1) + "/" + NumberUtil.roundToNthDecimalPlace(maxBreath, 1), 34, 0, 1, healthColour, RenderUtil.StringRenderType.OUTLINED);
			}
			else {
				RenderUtil.drawCenteredScaledString(matrix, mc.font, LocaleUtil.getLocaleString("Drowning!"), (CTUtilitiesConfig.CLIENT.breathRenderType.get() == BreathRenderType.BAR_NUMERIC ? 28.5f : 24), 0, 1, ColourUtil.RGB(150, 0, 0), RenderUtil.StringRenderType.OUTLINED);
			}
		}

		matrix.popPose();
	}
	
	private static void renderIcon(PoseStack matrix, Minecraft mc, float currentBreath, float maxBreath, boolean flashing, boolean conduit) {
		int uvX = 0;
		int uvY = 9;
		int y = -1;

		if (conduit) {
			RenderSystem.setShaderTexture(0, CONDUIT_ICON);
			RenderUtil.renderCustomSizedTexture(matrix, 0, y, 0, 0, 9, 9, 9, 9);
		} else {
			RenderSystem.setShaderTexture(0, GuiComponent.GUI_ICONS_LOCATION);
			RenderUtil.renderCustomSizedTexture(matrix, 0, y, uvX + 16, uvY + 9, 9, 9, 256, 256);
		}
		

	}
	
	// Possibly deprecated. -CT
	private static boolean handleBreathState(LocalPlayer player, ForgeGui gui, float currentBreath) {
		boolean shouldFlash = gui.healthBlinkTime > (long)gui.tickCount && (gui.healthBlinkTime - (long)gui.tickCount) / 3L % 2L == 1L;

		if (currentBreath < gui.lastHealth && player.invulnerableTime > 0) {
			gui.lastHealthTime = Util.getMillis();
			gui.healthBlinkTime = gui.tickCount + 20;
		}
		else if (currentBreath > gui.lastHealth && player.invulnerableTime > 0) {
			gui.lastHealthTime = Util.getMillis();
			gui.healthBlinkTime = gui.tickCount + 10;
		}

		if (Util.getMillis() - gui.lastHealthTime > 1000L) {
			gui.lastHealth = (int)currentBreath;
			gui.displayHealth = (int)currentBreath;
			gui.lastHealthTime = Util.getMillis();
		}

		gui.lastHealth = (int)currentBreath;

		return shouldFlash;
	}

	public enum BreathRenderType {
		ICONS,
		BAR,
		NUMERIC,
		BAR_NUMERIC
	}
}
