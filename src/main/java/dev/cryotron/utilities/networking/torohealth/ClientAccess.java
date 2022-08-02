package dev.cryotron.utilities.networking.torohealth;

import dev.cryotron.utilities.client.particles.BarState;

public class ClientAccess {
    public static boolean reachPlayer(float dammie) {
    	
    	BarState.dammie = (int) dammie;
    	
    	return true;
    }
}
