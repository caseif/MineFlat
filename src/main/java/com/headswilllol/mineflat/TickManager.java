package com.headswilllol.mineflat;

public class TickManager {

	/**
	 * The current tick count
	 */
	private static int ticks = 0;

	/**
	 * The last timestamp at which a tick elapsed
	 */
	private static long lastTick = Timing.getTime();

	/**
	 * The number of ticks in a second
	 */
	private static final int TICKS_PER_SECOND = 30;

	/**
	 * The number of ticks per in-game day
	 */
	private static final int TICKS_PER_DAY = 24000;

	/**
	 * Chance a single stagnant entity will begin randomly moving ("wandering") in a given tick
	 */
	public static final int MOVE_CHANCE = 300;

	/**
	 * Maximum blocks an entity may randomly move (wander) at once
	 */
	public static final int MAX_MOVE_DISTANCE = 6;

	/**
	 * Chance that an entity will spawn in a given tick if the world is at half-mob capacity
	 */
	public static final int SPAWN_CHANCE = 600;
	
	/**
	 * Retrieves the current tick count of the game.
	 * @return the current tick count of the game.
	 */
	public static int getTicks(){
		return ticks;
	}
	
	/**
	 * Sets the current tick count of the game.
	 */
	public static void setTicks(int ticks){
		TickManager.ticks = ticks;
	}

	/**
	 * Generates RTEs (Random Tick Events) and basically controls every non-player action that happens in the world
	 */
	public static void handleTick(){
		/*Random r = new Random();
		if (Main.world.getMobCount() < Mob.MOB_CAPACITY){ // check that world isn't full
			int actualChance = (int)((float)Main.world.getMobCount()
					/ (float)Mob.MOB_CAPACITY * 2f * (float)SPAWN_CHANCE) + 1;
			if (r.nextInt(actualChance) == 0){
				System.out.println("spawn");
				EntityType type = Mob.mobTypes.get(r.nextInt(Mob.mobTypes.size()));
				Chunk c = Main.world.chunks.values().toArray(new Chunk[]{})[r.nextInt(Main.world.getChunkCount())];
				double x = Double.POSITIVE_INFINITY;
				double y = Double.POSITIVE_INFINITY;
				while (x == Double.POSITIVE_INFINITY && y == Double.POSITIVE_INFINITY){
					for (int xx = 0; xx < Main.world.getChunkLength(); xx++){
						for (int yy = 0; yy < Main.world.getChunkHeight(); yy++){
							Block b = c.getBlock(xx, yy);
							if (b != null && b.getLightLevel() <= Mob.getMaximumLightLevel(type) &&
									b.getType() == Material.AIR &&
									(yy > 0 && c.getBlock(xx, yy - 1).getType() == Material.AIR) || yy == 0 &&
									(yy < Main.world.getChunkHeight() - 1 &&
											c.getBlock(xx, yy + 1).getType() != Material.AIR) ||
											yy == Main.world.getChunkHeight() - 1){
								if (r.nextInt(200) == 0){
									x = xx;
									y = yy;
									System.out.println(xx + ", " + yy);
								}
							}
							if (y != Double.POSITIVE_INFINITY)
								break;
						}
						if (x != Double.POSITIVE_INFINITY)
							break;
					}
				}
				switch (type){
				case ZOMBIE:
					Main.world.addEntity(new Zombie(Chunk.getWorldXFromChunkIndex(c.getNum(), (int)x), (float)y));
				default:
				}
			}
		}
		for (Entity e : Main.world.getEntities()){
			if (e instanceof Mob){ // make sure it's not just an item drop or something
				Mob m = (Mob)e;
				if (m.getPlannedWalkDistance() == 0){ // check if entity is already moving
					if (r.nextInt(MOVE_CHANCE) == 0){ // decide whether entity should move
						float distance = r.nextInt(MAX_MOVE_DISTANCE) + 1;
						if (r.nextInt(2) == 0) // move left if 1, else move right
							distance *= -1;
						m.setPlannedWalkDistance(distance); // update
						// start movement
						if (distance > 0)
							m.setXVelocity(m.getSpeed());
						else
							m.setXVelocity(-m.getSpeed());
					}
				}
				else {
					if (Math.abs(m.getX() - m.getActualWalkDistance()) >=
							Math.abs(m.getPlannedWalkDistance())){ // check if entity should still be moving
						m.setActualWalkDistance(0); // reset
						m.setXVelocity(0); // stop the entity
					}
					else
						m.setActualWalkDistance(m.getActualWalkDistance() +
								Math.abs(m.getX() - m.getLastX())); // update
				}
			}
		}*/
		if (ticks < TICKS_PER_DAY)
			ticks += 1;
		else
			ticks = 0;
		//System.out.println(ticks);
		lastTick = Timing.getTime();
	}

	/**
	 * Checks whether ticks have elapsed since the last iteration, and if so, handles them
	 * @return The number of ticks which have elapsed since the last check
	 */
	public static int checkForTick(){
		int elapsed = (int)((Timing.getTime() - lastTick) / (Timing.TIME_RESOLUTION / 1000)) /
				(1000 / TICKS_PER_SECOND); // elapsed ticks since last tick
		for (int i = 0; i < elapsed; i++)
			handleTick(); // handle each tick separately
		return elapsed;
	}

}
