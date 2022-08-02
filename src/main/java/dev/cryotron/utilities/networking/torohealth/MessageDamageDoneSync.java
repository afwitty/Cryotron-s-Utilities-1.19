package dev.cryotron.utilities.networking.torohealth;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;

public class MessageDamageDoneSync {
	
	public float dammie;
	
	public MessageDamageDoneSync(float f) {
		this.dammie = f;
	}
	
	public static void encode(MessageDamageDoneSync pkt, FriendlyByteBuf buf) {
		buf.writeFloat(pkt.dammie);
	}
	
	public static MessageDamageDoneSync decode(FriendlyByteBuf buf) {
		return new MessageDamageDoneSync(buf.readFloat());
	}
	
	public static boolean handle(final MessageDamageDoneSync message, Supplier<NetworkEvent.Context> ctx)
	{
		final var success = new AtomicBoolean(false);
        ctx.get().enqueueWork(() -> {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT,   () -> () -> success.set(ClientAccess.reachPlayer(message.dammie)
            		));
        });

        ctx.get().setPacketHandled(true);
        return success.get();
	}
	
}

