package dev.cryotron.utilities.client.particles;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public class BarState {

public final LivingEntity entity;

  public float health;
  public float previousHealth;
  public float previousHealthDisplay;
  public float previousHealthDelay;
  public float lastDmg;
  public float lastDmgCumulative;
  public float lastHealth;
  public float lastDmgDelay;
  @SuppressWarnings("unused")
private float animationSpeed = 0;
  
  public static float dammie;
  public static boolean isDamaged;
  public static boolean isPlayer;
  public static boolean isCrit;
  public static int entityID;
  
  public static Vec3 entityLocation;
  public static double offset;

  private static final float HEALTH_INDICATOR_DELAY = 10;

  public BarState(LivingEntity entity) {
    this.entity = entity;
  }

  public void tick() {
    health = Math.min(entity.getHealth(), entity.getMaxHealth());
    incrementTimers();
    
    if ((lastHealth != health) && dammie > 0.00f && isPlayer && isDamaged) {
    	handleHealthChange();  
    } else    	
    	
    if ((dammie == 0.00f) && isDamaged && isPlayer) {		
    	handleHealthChange();
    }
  }

  // Possibly deprecated.
  @SuppressWarnings("unused")
private void reset() {
    lastHealth = health;
    lastDmg = 0;
    lastDmgCumulative = 0;
  }

  private void incrementTimers() {
    if (this.lastDmgDelay > 0) {
      this.lastDmgDelay--;
    }
    if (this.previousHealthDelay > 0) {
      this.previousHealthDelay--;
    }
  }

  // VERY possibly deprecated. -CT
  private void handleHealthChange() {

	lastDmg = Mth.ceil(lastHealth) - Mth.ceil(health);
    lastDmgCumulative += lastDmg;
    
    lastDmgDelay = HEALTH_INDICATOR_DELAY * 2;
    lastHealth = health;
    
    BarStates.PARTICLES.add(new BarParticle(entityLocation, offset, dammie, isCrit));  	   

    isDamaged = false;


  }

//  	// Possibly deprecated.
//  private void updateAnimations() {
//    if (previousHealthDelay > 0) {
//      float diff = previousHealthDisplay - health;
//
//      if (diff > 0) {
//        animationSpeed = diff / 10f;
//      }
//      
//    } else if (previousHealthDelay < 1 && previousHealthDisplay > health) {
//      previousHealthDisplay -= animationSpeed;
//    } else {
//      previousHealthDisplay = health;
//      previousHealth = health;
//      previousHealthDelay = HEALTH_INDICATOR_DELAY;
//    }
//  }

}