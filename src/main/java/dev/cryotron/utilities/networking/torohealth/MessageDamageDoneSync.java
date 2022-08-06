package dev.cryotron.utilities.networking.torohealth;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;

public class MessageDamageDoneSync {
	
	public double x;
	public double y;
	public double z;
	public double offset;
	public boolean isDamaged;
	public boolean isPlayer;
	public float dammie;
	public boolean isCrit;
	
	public MessageDamageDoneSync(double x, double y, double z, double offset, boolean isDamaged, boolean isPlayer, float dammie, boolean isCrit) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.offset = offset;
		this.isDamaged = isDamaged;
		this.isPlayer = isPlayer;
		this.dammie = dammie;
		this.isCrit = isCrit;
	}
	
	public static void encode(MessageDamageDoneSync pkt, FriendlyByteBuf buf) {
		buf.writeDouble(pkt.x);
		buf.writeDouble(pkt.y);
		buf.writeDouble(pkt.z);
		buf.writeDouble(pkt.offset);
		buf.writeBoolean(pkt.isDamaged);
		buf.writeBoolean(pkt.isPlayer);
		buf.writeFloat(pkt.dammie);	
		buf.writeBoolean(pkt.isCrit);
	}
	
	public static MessageDamageDoneSync decode(FriendlyByteBuf buf) {
		return new MessageDamageDoneSync(buf.readDouble(), buf.readDouble(), buf.readDouble(), buf.readDouble(), buf.readBoolean(), buf.readBoolean(), buf.readFloat(), buf.readBoolean());
	}
	
	public static boolean handle(final MessageDamageDoneSync message, Supplier<NetworkEvent.Context> ctx)
	{
		final var success = new AtomicBoolean(false);
        ctx.get().enqueueWork(() -> {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT,   () -> () -> success.set(ClientAccess.reachPlayerClientDamage(message.x, message.y, message.z, message.offset, message.isDamaged, message.isPlayer, message.dammie, message.isCrit)
            		));
        });

        ctx.get().setPacketHandled(true);
        return success.get();
	}
	
}

