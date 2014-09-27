package com.headswilllol.mineflat.entity;

import com.headswilllol.mineflat.event.Event;
import com.headswilllol.mineflat.event.human.HumanMoveEvent;
import com.headswilllol.mineflat.world.Location;

/**
 * Represents a human in the world.
 */
public class Human extends LivingEntity {

	public Human(Location location){
		super(EntityType.HUMAN, location, 0.5f, 2f);
	}

	public Location getLocation(){
		return location;
	}

	@Override
	public void setX(float x){
		Location old = getLocation();
		super.setX(x);
		Event.fireEvent(new HumanMoveEvent(this, getLocation(), old));
	}

	@Override
	public void setY(float y){
		Location old = getLocation();
		super.setY(y);
		Event.fireEvent(new HumanMoveEvent(this, getLocation(), old));
	}

}
