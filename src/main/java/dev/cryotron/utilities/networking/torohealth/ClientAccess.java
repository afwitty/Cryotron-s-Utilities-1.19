package dev.cryotron.utilities.networking.torohealth;

import dev.cryotron.utilities.client.particles.BarState;
import net.minecraft.world.phys.Vec3;

public class ClientAccess {
    public static boolean reachPlayerClientDamage(double x, double y, double z, double offset, boolean isDamaged, boolean isPlayer, float dammie, boolean isCrit) {

    	BarState.entityLocation = new Vec3(x, y, z);
    	BarState.offset = offset;
    	BarState.isDamaged = isDamaged;
    	BarState.isPlayer = isPlayer;
    	BarState.dammie = dammie;
    	BarState.isCrit = isCrit;

    	return true;
    }
}
