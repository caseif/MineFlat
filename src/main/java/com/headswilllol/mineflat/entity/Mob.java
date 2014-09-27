package com.headswilllol.mineflat.entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.headswilllol.mineflat.world.Location;

public abstract class Mob extends LivingEntity {

	protected float walkDistance;
	protected float distance;
	protected float lastX;
	public static final int MOB_CAPACITY = 100;
	public static final List<EntityType> mobTypes = new ArrayList<EntityType>();
	public static final HashMap<EntityType, Integer> light = new HashMap<EntityType, Integer>();
	
	public Mob(EntityType type, Location location, float width, float height){
		super(type, location, width, height);
	}
	
	public static void initialize(){
		mobTypes.add(EntityType.ZOMBIE);
		light.put(EntityType.ZOMBIE, 8);
	}
	
	public float getPlannedWalkDistance(){
		return walkDistance;
	}
	
	public float getActualWalkDistance(){
		return distance;
	}
	
	public float getLastX(){
		return lastX;
	}
	
	public void setPlannedWalkDistance(float distance){
		this.walkDistance = distance;
	}
	
	public void setActualWalkDistance(float distance){
		this.distance = distance;
	}
	
	public void setLastX(float x){
		this.lastX = x;
	}
	
	public int getMaximumLightLevel(){
		return light.get(type);
	}
	
	public static int getMaximumLightLevel(EntityType type){
		return light.get(type);
	}

}
