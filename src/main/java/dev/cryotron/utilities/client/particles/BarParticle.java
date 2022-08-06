package dev.cryotron.utilities.client.particles;

import java.util.Random;

import dev.cryotron.utilities.CTUtilities;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public class BarParticle {
	  public float damage;
	  public static Random RAND = new Random();

	  public double x = 0;
	  public double y = 0;
	  public double z = 0;
	  public double xPrev = 0;
	  public double yPrev = 0;
	  public double zPrev = 0;

	  public int age = 0;

	  public double ax = 0.00;
	  public double ay = 0.0005; // from -0.02, 0.01, 0.005
	  public double az = 0.00;

	  public double vx = 0;
	  public double vy = 0;
	  public double vz = 0;
	  
	  public boolean isCrit = false;

	  public BarParticle(Vec3 entityLocation, double offsetBy, float damage, boolean crit) {
	    Minecraft client = Minecraft.getInstance();
	    Vec3 cameraLocation = client.gameRenderer.getMainCamera().getPosition();
	    Vec3 camOffset = cameraLocation.subtract(entityLocation).normalize().scale(offsetBy);
	    Vec3 pos = entityLocation.add(camOffset);

	    age = 0;
	    this.damage = damage;
	    
	    vx = RAND.nextGaussian() * 0.002; // from * 0.04
	    vy = -0.005 + RAND.nextGaussian() * 0.01; //0.10 + (RAND.nextGaussian() * 0.05);
	    vz = RAND.nextGaussian() * 0.002; // from * 0.04

	    x = pos.x;
	    y = pos.y+0.75; 
	    z = pos.z;

	    xPrev = x;
	    yPrev = y;
	    zPrev = z;
	    
	    isCrit = crit;
	  }

	  public void tick() {
	    xPrev = x;
	    yPrev = y;
	    zPrev = z;
	    age++;
	    x += vx;
	    y += vy;
	    z += vz;
	    vx += ax;
	    vy += ay;
	    vz += az;
	  }
}
