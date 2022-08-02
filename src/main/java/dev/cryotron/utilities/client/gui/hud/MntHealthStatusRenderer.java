package dev.cryotron.utilities.client.gui.hud;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import dev.cryotron.utilities.CTUtilities;
import dev.cryotron.utilities.common.CTUtilitiesConfig;
import dev.cryotron.utilities.util.aoa.NumberUtil;
import dev.cryotron.utilities.util.aoa.RandomUtil;
import dev.cryotron.utilities.util.aoa.RenderUtil;
import dev.cryotron.utilities.util.aoa.ColourUtil;
import dev.cryotron.utilities.util.aoa.LocaleUtil;


import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.gui.ForgeIngameGui;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.EventPriority;

public class MntHealthStatusRenderer {
	private static final ResourceLocation MOUNT_HEALTH_BAR = CTUtilities.id("textures/gui/overlay/misc/mnthealth_bar.png");
	private static float deltaHealth = 0;
	private static int lastHealthTime = 0;
	private static float lastTickHealth = 0;

	public static void init() {
		MinecraftForge.EVENT_BUS.addListener(EventPriority.LOWEST, false, RenderGameOverlayEvent.PreLayer.class, MntHealthStatusRenderer::onMntHealthRender);
	}

	private static void onMntHealthRender(RenderGameOverlayEvent.PreLayer ev) {
		if (ev.isCanceled() || ev.getOverlay() != ForgeIngameGui.MOUNT_HEALTH_ELEMENT)
			return;

		MntHealthRenderType renderType = CTUtilitiesConfig.CLIENT.mntHealthRenderType.get();

		if (renderType == MntHealthRenderType.ICONS)
			return;

		Minecraft mc = Minecraft.getInstance();
		

		if (!(mc.gui instanceof ForgeIngameGui gui) || mc.options.hideGui || !gui.shouldDrawSurvivalElements())
			return;
		
		LocalPlayer player = mc.player;
		PoseStack matrix = ev.getPoseStack();
		LivingEntity le = (LivingEntity) player.getVehicle();

		if ( !(player.getVehicle() instanceof LivingEntity) ) {
			return;
		}
		
		int left = (mc.getWindow().getGuiScaledWidth() / 2) + 10; //-91
		int top = mc.getWindow().getGuiScaledHeight() - gui.right_height;
		gui.right_height += 11;

		gui.setupOverlayRenderState(true, false);
		ev.setCanceled(true);

		mc.getProfiler().push("breath");
		RenderSystem.enableBlend();

		float currentHealth = le.getHealth();
		float maxHealth = le.getMaxHealth();
		float mntArmor = le.getArmorValue();
		boolean poisoned = le.hasEffect(MobEffects.POISON);
		boolean withered = le.hasEffect(MobEffects.WITHER);
		boolean frozen = le.isFullyFrozen();
		float absorption = mc.player.getAbsorptionAmount();
		
		if (renderType == MntHealthRenderType.NUMERIC) {
			renderNumeric(matrix, mc, le, gui, left, top, currentHealth, maxHealth, poisoned, withered, frozen, absorption, mntArmor);
		}
		else {
			renderBar(matrix, mc, le, gui, left, top, currentHealth, maxHealth, poisoned, withered, frozen, absorption);

			if (renderType ==  MntHealthRenderType.BAR_NUMERIC)
				renderNumeric(matrix, mc, le, gui, left, top, currentHealth, maxHealth, poisoned, withered, frozen, absorption, mntArmor);
		}

		RenderSystem.disableBlend();
		mc.getProfiler().pop();
		MinecraftForge.EVENT_BUS.post(new RenderGameOverlayEvent.Post(matrix, ev, RenderGameOverlayEvent.ElementType.LAYER));
	}

	private static void renderBar(PoseStack matrix, Minecraft mc, LivingEntity le, ForgeIngameGui gui, int left, int top, float currentHealth, float maxHealth, boolean poisoned, boolean withered, boolean frozen, float absorption) {
		int uvY = 0;
		
		if (!NumberUtil.roundToNthDecimalPlace(lastTickHealth, 1).equals(NumberUtil.roundToNthDecimalPlace(currentHealth, 1))) {
			if (lastTickHealth < currentHealth) {
				if (lastHealthTime == 0) {
					lastTickHealth = currentHealth;
					lastHealthTime = mc.player.tickCount;

					return;
				}

				if (deltaHealth < 0)
					deltaHealth = 0;

				deltaHealth += currentHealth - lastTickHealth;
			}
			else {
				if (deltaHealth > 0)
					deltaHealth = 0;

				deltaHealth -= lastTickHealth - currentHealth;
			}

			lastHealthTime = mc.player.tickCount + 12;
		}

		if (deltaHealth != 0 && lastHealthTime + 20 < mc.player.tickCount)
			deltaHealth = 0;

		lastTickHealth = currentHealth;
		matrix.pushPose();
		matrix.translate(left, top - 1.9, 0);
		RenderUtil.prepRenderTexture(MOUNT_HEALTH_BAR);

		float healthWidth = 81 * (currentHealth / maxHealth); 

		if (currentHealth < maxHealth)
			RenderUtil.renderCustomSizedTexture(matrix, 0, 0, 0, 12, 81, 12, 81, 120);

		if (!mc.player.isAlive()) {
			matrix.popPose();

			return;
		}

		RenderUtil.renderCustomSizedTexture(matrix, 0, 0, 0, uvY, healthWidth, 12, 81, 120);

		if (deltaHealth != 0) {
			int deltaUvY = deltaHealth < 0 ? 24 : 36;
			float deltaWidth = mc.player.tickCount - lastHealthTime < 8 ? 1 : ((12 - (mc.player.tickCount - 8 - lastHealthTime)) / (float)12);
			float width = Math.min(Math.abs(deltaHealth), maxHealth) / maxHealth * 81 * deltaWidth;
			float x = deltaHealth < 0 ? healthWidth : healthWidth - width;

			if (deltaHealth < 0 && x + width > 81)
				width = 81 - x;

			RenderUtil.renderScaledCustomSizedTexture(matrix, x, 0, x, deltaUvY, width, 12, width, 12, 81, 120);
		}

		RenderUtil.drawColouredBox(matrix, 0, 0, 0, 81, 11, 0x44000000);
		matrix.popPose();
	}

	private static void renderNumeric(PoseStack matrix, Minecraft mc, LivingEntity le, ForgeIngameGui gui, int left, int top, float currentHealth, float maxHealth, boolean poisoned, boolean withered, boolean frozen, float absorption, float mntArmor) {
		int healthColour;
		
		healthColour = ColourUtil.RGB(255, 174, 200);

		matrix.pushPose();

		if (CTUtilitiesConfig.CLIENT.mntHealthRenderType.get() == MntHealthRenderType.NUMERIC) {
			if (mntArmor > 0)
				left -= 15;

			matrix.translate(left + 15, top + 0.9, 0);
			matrix.scale(0.9f, 0.9f, 1);

			if (currentHealth > 0) {
				renderIcon(matrix, mc, currentHealth, maxHealth, handleHealthState(mc.player, gui, (int)Math.ceil(currentHealth)));

				RenderUtil.drawCenteredScaledString(matrix, mc.font, NumberUtil.roundToNthDecimalPlace(currentHealth, 1) + "/" + NumberUtil.roundToNthDecimalPlace(maxHealth, 1), 34, 0, 1, healthColour, RenderUtil.StringRenderType.OUTLINED);
			
				if (mntArmor > 0) {
					RenderUtil.drawCenteredScaledString(matrix, mc.font, "+", 67, 0, 1, ColourUtil.RGB(255, 255, 255), RenderUtil.StringRenderType.OUTLINED);
					RenderUtil.drawCenteredScaledString(matrix, mc.font, NumberUtil.roundToNthDecimalPlace(mntArmor, 1), 83, 0, 1, ColourUtil.RGB(255, 255, 255), RenderUtil.StringRenderType.OUTLINED);
				}
			
			}
			else {
				RenderUtil.drawCenteredScaledString(matrix, mc.font, LocaleUtil.getLocaleString("Dead Mount :("), (CTUtilitiesConfig.CLIENT.mntHealthRenderType.get() == MntHealthRenderType.BAR_NUMERIC ? 28.5f : 24), 0, 1, ColourUtil.RGB(150, 0, 0), RenderUtil.StringRenderType.OUTLINED);
			}
		}
		else {
			gui.left_height += 2;

			if (mntArmor > 0)
				left -= 8;

			matrix.translate(left + 17, top + 1.2, 0);
			matrix.scale(0.8f, 0.8f, 1);

			if (currentHealth > 0) {
				renderIcon(matrix, mc, currentHealth, maxHealth, handleHealthState(mc.player, gui, (int)Math.ceil(currentHealth)));

				RenderUtil.drawCenteredScaledString(matrix, mc.font, NumberUtil.roundToNthDecimalPlace(currentHealth, 1) + "/" + NumberUtil.roundToNthDecimalPlace(maxHealth, 1), 34, 0, 1, healthColour, RenderUtil.StringRenderType.OUTLINED);
			
				if (mntArmor > 0) {
					RenderUtil.drawCenteredScaledString(matrix, mc.font, "+", 62, 0, 1, ColourUtil.RGB(255, 255, 255), RenderUtil.StringRenderType.OUTLINED);
					RenderUtil.drawCenteredScaledString(matrix, mc.font, NumberUtil.roundToNthDecimalPlace(mntArmor, 1), 70, 0, 1, ColourUtil.RGB(255, 255, 255), RenderUtil.StringRenderType.OUTLINED);
				}
			}
			else {
				RenderUtil.drawCenteredScaledString(matrix, mc.font, LocaleUtil.getLocaleString("Dead Mount :("), (CTUtilitiesConfig.CLIENT.mntHealthRenderType.get() == MntHealthRenderType.BAR_NUMERIC ? 28.5f : 24), 0, 1, ColourUtil.RGB(150, 0, 0), RenderUtil.StringRenderType.OUTLINED);
			}
		}

		matrix.popPose();
	}
	
	private static void renderIcon(PoseStack matrix, Minecraft mc, float currentHealth, float maxHealth, boolean flashing) {
		int uvX = 0;
		int uvY = 9;
		int y = -1;
		LivingEntity le;
		if (mc.player.getVehicle() instanceof LivingEntity) {
			le = (LivingEntity) mc.player.getVehicle();
		}
		else {
			return;
		}

		if (currentHealth <= maxHealth * 0.2f && RandomUtil.fiftyFifty())
			y += 1;

		if (mc.gui.tickCount % 25 == 0 && le.hasEffect(MobEffects.REGENERATION))
			y -= 2;

		RenderSystem.setShaderTexture(0, GuiComponent.GUI_ICONS_LOCATION);
		RenderUtil.renderCustomSizedTexture(matrix, 0, y, Gui.HeartType.CONTAINER.getX(false, flashing), 0, 9, 9, 256, 256);

		if (flashing) {
			RenderUtil.renderCustomSizedTexture(matrix, 0, y, uvX + 70, 0, 9, 9, 256, 256); //61,9
		}
		else {
			RenderUtil.renderCustomSizedTexture(matrix, 0, y, uvX + 52, uvY, 9, 9, 256, 256);
		}
		if (currentHealth == maxHealth) {
			RenderUtil.renderCustomSizedTexture(matrix, 0, y, uvX + 88, uvY, 9, 9, 256, 256);
		}
		else {
			RenderUtil.renderCustomSizedTexture(matrix, 0, y, uvX + 97, uvY, 9, 9, 256, 256);
		}		

	}

	private static boolean handleHealthState(LocalPlayer player, ForgeIngameGui gui, float currentHealth) {
		boolean shouldFlash = gui.healthBlinkTime > (long)gui.tickCount && (gui.healthBlinkTime - (long)gui.tickCount) / 3L % 2L == 1L;

		if (currentHealth < gui.lastHealth && player.invulnerableTime > 0) {
			gui.lastHealthTime = Util.getMillis();
			gui.healthBlinkTime = gui.tickCount + 20;
		}
		else if (currentHealth > gui.lastHealth && player.invulnerableTime > 0) {
			gui.lastHealthTime = Util.getMillis();
			gui.healthBlinkTime = gui.tickCount + 10;
		}

		if (Util.getMillis() - gui.lastHealthTime > 1000L) {
			gui.lastHealth = (int)currentHealth;
			gui.displayHealth = (int)currentHealth;
			gui.lastHealthTime = Util.getMillis();
		}

		gui.lastHealth = (int)currentHealth;

		return shouldFlash;
	}

	public enum MntHealthRenderType {
		ICONS,
		BAR,
		NUMERIC,
		BAR_NUMERIC
	}
}
