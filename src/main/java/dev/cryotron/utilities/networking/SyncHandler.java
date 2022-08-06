package dev.cryotron.utilities.networking;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.CriticalHitEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import dev.cryotron.utilities.CTUtilities;
import dev.cryotron.utilities.client.particles.BarState;
import dev.cryotron.utilities.client.particles.BarStates;
import dev.cryotron.utilities.client.particles.BarParticle;
import dev.cryotron.utilities.networking.appleskin.MessageExhaustionSync;
import dev.cryotron.utilities.networking.appleskin.MessageSaturationSync;
import dev.cryotron.utilities.networking.torohealth.MessageDamageDoneSync;

public class SyncHandler
{
	private static final String PROTOCOL_VERSION = Integer.toString(1);
	public final static SimpleChannel CHANNEL = NetworkRegistry.ChannelBuilder
		.named(new ResourceLocation(CTUtilities.ID, "sync"))
		.clientAcceptedVersions(s -> true)
		.serverAcceptedVersions(s -> true)
		.networkProtocolVersion(() -> PROTOCOL_VERSION)
		.simpleChannel();

	public static void init()
	{
		CHANNEL.registerMessage(1, MessageExhaustionSync.class, MessageExhaustionSync::encode, MessageExhaustionSync::decode, MessageExhaustionSync::handle);
		CHANNEL.registerMessage(2, MessageSaturationSync.class, MessageSaturationSync::encode, MessageSaturationSync::decode, MessageSaturationSync::handle);
		CHANNEL.registerMessage(3, MessageDamageDoneSync.class, MessageDamageDoneSync::encode, MessageDamageDoneSync::decode, MessageDamageDoneSync::handle);
		
		MinecraftForge.EVENT_BUS.register(new SyncHandler());
	}

	/*
	 * Sync saturation (vanilla MC only syncs when it hits 0)
	 * Sync exhaustion (vanilla MC does not sync it at all)
	 */
	private static final Map<UUID, Float> lastSaturationLevels = new HashMap<>();
	private static final Map<UUID, Float> lastExhaustionLevels = new HashMap<>();
	
	private static boolean isPlayer = false;
	private static boolean isDamaged = false;
	private static boolean isCrit = false;
	private static float dammie = 0;
	private static int entityID;
	
	@SubscribeEvent
	public void onLivingUpdateEvent(LivingUpdateEvent event)
	{
		if (!(event.getEntity() instanceof ServerPlayer))
			return;

		ServerPlayer player = (ServerPlayer) event.getEntity();
		Float lastSaturationLevel = lastSaturationLevels.get(player.getUUID());
		Float lastExhaustionLevel = lastExhaustionLevels.get(player.getUUID());

		if (lastSaturationLevel == null || lastSaturationLevel != player.getFoodData().getSaturationLevel())
		{
			Object msg = new MessageSaturationSync(player.getFoodData().getSaturationLevel());
			CHANNEL.sendTo(msg, player.connection.connection, NetworkDirection.PLAY_TO_CLIENT);
			lastSaturationLevels.put(player.getUUID(), player.getFoodData().getSaturationLevel());
		}

		float exhaustionLevel = player.getFoodData().getExhaustionLevel();
		if (lastExhaustionLevel == null || Math.abs(lastExhaustionLevel - exhaustionLevel) >= 0.01f)
		{
			Object msg = new MessageExhaustionSync(exhaustionLevel);
			CHANNEL.sendTo(msg, player.connection.connection, NetworkDirection.PLAY_TO_CLIENT);
			lastExhaustionLevels.put(player.getUUID(), exhaustionLevel);
		}
	}

	@SubscribeEvent
	public void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event)
	{
		if (!(event.getPlayer() instanceof ServerPlayer))
			return;

		lastSaturationLevels.remove(event.getPlayer().getUUID());
		lastExhaustionLevels.remove(event.getPlayer().getUUID());
	}
	
	@SubscribeEvent
	public void playerAttackEvent( AttackEntityEvent event ) {
		isPlayer = true;	 
	}
	
	@SubscribeEvent
	public void playerCritEvent( CriticalHitEvent event ) {	
		if ( event.isVanillaCritical() ) {
			isCrit = true;
		}
	}
	
	@SubscribeEvent
	public void playerDamageEvent (LivingHurtEvent event) {
		
		if (event.getSource().getEntity() instanceof ServerPlayer) {
			ServerPlayer sp = (ServerPlayer) event.getSource().getEntity();

			isDamaged = true;
			isPlayer = true;	 
			dammie = event.getAmount();

			Vec3 entityLocation = event.getEntityLiving().position().add(0, event.getEntityLiving().getBbHeight()/2, 0);
			double x = entityLocation.x();
			double y = entityLocation.y();
			double z = entityLocation.z();
			double offset = event.getEntityLiving().getBbWidth();
			
			Object msg = new MessageDamageDoneSync(x,y,z,offset, isDamaged, isPlayer, dammie, isCrit);
			CHANNEL.sendTo(msg, sp.connection.connection, NetworkDirection.PLAY_TO_CLIENT);		
			
			// Reset values after sending packet
			isPlayer = false;
			isDamaged = false;
			isCrit = false;
			dammie = 0;

		}
	}
}