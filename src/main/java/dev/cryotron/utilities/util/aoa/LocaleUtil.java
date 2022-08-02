package dev.cryotron.utilities.util.aoa;

import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class LocaleUtil {
	@OnlyIn(Dist.CLIENT)
	public static String getLocaleString(String langKey) {
		return getLocaleString(langKey, (ChatFormatting)null);
	}
	
	@OnlyIn(Dist.CLIENT)
	public static String getLocaleString(String langKey, String... args) {
		return getLocaleString(langKey, null, args);
	}
	
	@OnlyIn(Dist.CLIENT)
	public static String getLocaleString(String langKey, @Nullable ChatFormatting colour, String... args) {
		return (colour != null ? colour : "") + I18n.get(langKey, (Object[])args);
	}
}
