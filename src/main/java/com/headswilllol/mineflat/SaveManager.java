package com.headswilllol.mineflat;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.headswilllol.mineflat.entity.*;
import com.headswilllol.mineflat.util.FileUtil;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javax.net.ssl.SSLContext;
import java.io.*;

public class SaveManager {

	/**
	 * Parses all loaded chunks in a world to a JSON object.
	 */
	@SuppressWarnings("unchecked")
	public static JSONObject saveWorld(World world){

		System.out.println("Saving chunks...");

		JSONObject save = world.getJSON();
		save.put("name", Main.world.getName());
		save.put("createTime", Main.world.creationTime);
		save.put("modifyTime", System.currentTimeMillis() / 1000L);
		save.put("chunkCount", Main.world.getChunkCount());
		save.put("chunkLength", Main.world.getChunkLength());
		save.put("chunkHeight", Main.world.getChunkHeight());
		save.put("seed", Main.world.getSeed());
		save.put("ticks", TickManager.getTicks());
		save.put("playerLevel", Main.player.getLevel().getIndex());
		save.put("playerChunk", Main.player.getLocation().getChunk());

		JSONObject levels = (JSONObject)save.get("levels");
		if (levels == null) {
			levels = new JSONObject();
			System.out.println("created new levels object");
		}
		for (Level level : world.getLevels()){
			levels.put(Integer.toString(level.getIndex()), saveLevel(level));
		}
		save.put("levels", levels);
		return save;
	}

	public static void saveWorldToMemory(World world){
		world.setJSON(saveWorld(world));
	}

	public static void writeWorldToDisk(World world){

		System.out.println("Writing chunks...");

		saveWorldToMemory(world);
		File saveFolder = new File(FileUtil.getAppDataFolder() + File.separator +
				".mineflat", "saves");
		saveFolder = new File(saveFolder, world.getName());
		if (!saveFolder.exists())
			saveFolder.mkdirs();
		File saveFile = new File(saveFolder, "level.json");
		File zippedFile = new File(saveFolder, "level.gz");
		if (saveFile.exists())
			saveFile.delete();
		if (zippedFile.exists())
			zippedFile.delete();
		try {
			saveFile.createNewFile();
			PrintWriter writer = new PrintWriter(saveFile);
			Gson gson = new GsonBuilder().setPrettyPrinting().create();
			JsonElement je = new JsonParser().parse(world.getJSON().toJSONString());
			writer.write(gson.toJson(je));
			writer.close();
			FileUtil.gzip(saveFile.getAbsolutePath(), zippedFile.getAbsolutePath());
		}
		catch (IOException ex){
			ex.printStackTrace();
			System.err.println("Failed to save world to disk!");
		}
		finally {
			saveFile.delete();
		}
	}

	//TODO: make this method more type-safe
	public static void loadWorld(String world) {
		File saveFolder = new File(FileUtil.getAppDataFolder() + File.separator +
				".mineflat", "saves");
		saveFolder = new File(saveFolder, world);
		if (!saveFolder.exists())
			saveFolder.mkdirs();
		File zippedFile = new File(saveFolder, "level.gz");
		File saveFile = new File(saveFolder, "level.json");
		if (!zippedFile.exists())
			return;
		FileUtil.ungzip(zippedFile.getAbsolutePath(), saveFile.getAbsolutePath());
		try {
			JSONObject save = (JSONObject)new JSONParser().parse(new FileReader(saveFile));
			Main.world = new World((String)save.get("name"), longToInt((Long)save.get("chunkCount")),
					longToInt((Long)save.get("chunkLength")), longToInt((Long)save.get("chunkHeight")));
			Main.world.setJSON(save);
			Main.world.seed = (Long)save.get("seed");
			Main.world.creationTime = (Long)save.get("createTime");
			TickManager.setTicks(longToInt((Long)save.get("ticks")));
			//for (Object lKey : ((JSONObject)save.get("levels")).keySet())
			loadLevel(Main.world, longToInt((Long)save.get("playerLevel")));
		}
		catch (FileNotFoundException ex){
			ex.printStackTrace();
			System.err.println("Failed to load world from disk - save file cannot be found!");
		}
		catch (IOException ex){
			ex.printStackTrace();
			System.err.println("Failed to load world from disk - save file cannot be read!");
		}
		catch (ParseException ex){
			ex.printStackTrace();
			System.err.println("Failed to load world from disk - save file is invalid!");
		}
		catch (ClassCastException ex){
			ex.printStackTrace();
			System.err.println("Failed to load world from disk - save file is invalid!");
		}
		finally {
			saveFile.delete();
		}
	}

	public static Chunk loadChunk(Level level, int chunk){
		JSONObject jChunk = (JSONObject)((JSONObject)((JSONObject)((JSONObject)((JSONObject)level
				.getWorld()
				.getJSON()
				.get("levels"))
				.get(Integer.toString(level.getIndex()))))
				.get("chunks")).get(Integer.toString(chunk));
		if (jChunk != null) {
			Chunk c = new Chunk(level, chunk);
			for (Object blockObj : (JSONArray)jChunk.get("blocks")) {
				JSONObject block = (JSONObject)blockObj;
				Material type = Material.valueOf((String)block.get("type"));
				if (type == null)
					type = Material.AIR;
				Block b = new Block(type, new Location(level, Chunk.getWorldXFromChunkIndex(chunk,
						block.get("x") instanceof Integer ? (Integer)block.get("x") : (Long)block.get("x")),
						block.get("y") instanceof Integer ? (Integer)block.get("y") : (Long)block.get("y")));
				JSONObject meta = (JSONObject)block.get("metadata");
				for (Object key : meta.keySet())
					b.setMetadata((String)key, meta.get(key));
				b.addToWorld();
			}
			System.out.println("about to check entities");
			for (Object entityObj : (JSONArray)jChunk.get("entities")) {
				System.out.println("found entity");
				JSONObject entity = (JSONObject)entityObj;
				EntityType type = EntityType.valueOf((String)entity.get("type"));
				System.out.println("type is " + type.toString());
				float x = Chunk.getWorldXFromChunkIndex(c.getIndex(), Float.valueOf(Double.toString((Double)entity.get("x"))));
				float y = Float.valueOf(Double.toString((Double)entity.get("y")));
				float w = Float.valueOf(Double.toString((Double)entity.get("w")));
				float h = Float.valueOf(Double.toString((Double)entity.get("h")));
				Entity e;
				if (entity.containsKey("living")) {
					if (entity.containsKey("mob")) {
						switch (type) {
							case ZOMBIE:
								e = new Zombie(new Location(level, x, y));
								break;
							default:
								e = new LivingEntity(type, new Location(level, x, y), w, h);
								break;
						}
						((Mob)e).setPlannedWalkDistance((Float)entity.get("pwd"));
						((Mob)e).setActualWalkDistance((Float)entity.get("awd"));
						((Mob)e).setLastX((Float)entity.get("lx"));
					}
					else {
						switch (type) {
							case PLAYER:
								e = new Player(new Location(level, x, y));
								break;
							case HUMAN:
								e = new Human(new Location(level, x, y));
								break;
							default:
								e = new LivingEntity(type, new Location(level, x, y), w, h);
						}
					}
					((LivingEntity)e).setFacingDirection(Direction.valueOf((String)entity.get("fd")));
					((LivingEntity)e).setMovementDirection(Direction.valueOf((String)entity.get("md")));
					((LivingEntity)e).setJumping((Boolean)entity.get("j"));
				}
				else
					e = new Entity(type, new Location(level, x, y), w, h);
				e.setXVelocity(Float.valueOf(Double.toString((Double)entity.get("xv"))));
				e.setYVelocity(Float.valueOf(Double.toString((Double)entity.get("yv"))));
				level.addEntity(e);
				if (type == EntityType.PLAYER)
					Main.player = (Player)e;
			}
			c.updateLight();
			return c;
		}
		return null;
	}

	public static Level loadLevel(World world, int level){
		Main.world.addLevel(level);
		Level l = Main.world.getLevel(level);
		//for (Object cKey : ((JSONObject)jLevel.get("chunks")).keySet())
		if (longToInt((Long)world.getJSON().get("playerLevel")) == level) {
			loadChunk(l, longToInt((Long)world.getJSON().get("playerChunk")));
			Chunk.handleChunkLoading(true);
		}
		for (Chunk c : l.chunks.values())
			c.updateLight();
		return l;
	}

	/**
	 * Saves a chunk to a JSON object. <strong>This method does not save the JSON to memory or to disk.</strong>
	 * @param chunk the chunk to save.
	 * @return the created JSON object.
	 */
	public static JSONObject saveChunk(Chunk chunk){
		JSONObject c = new JSONObject();
		JSONArray blocks = new JSONArray();
		for (int x = 0; x < Main.world.getChunkLength(); x++){
			for (int y = 0; y < Main.world.getChunkHeight(); y++){
				Block block = chunk.getBlock(x, y);
				if (block != null){
					JSONObject b = new JSONObject();
					b.put("x", x);
					b.put("y", y);
					b.put("type", block.getType().toString());
					//TODO: store data values
					blocks.add(b);
					JSONObject meta = new JSONObject();
					for (String key : block.getAllMetadata())
						meta.put(key, block.getMetadata(key));
					b.put("metadata", meta);
				}
			}
		}
		c.put("blocks", blocks);
		JSONArray entities = new JSONArray();
		for (Entity entity : chunk.getEntities()){
			JSONObject e = new JSONObject();
			e.put("type", entity.getType().toString());
			e.put("x", entity.getLocation().getPosInChunk());
			e.put("y", entity.getY());
			e.put("w", entity.width);
			e.put("h", entity.height);
			e.put("xv", entity.getXVelocity());
			e.put("yv", entity.getYVelocity());
			if (entity instanceof LivingEntity){
				e.put("living", true);
				LivingEntity le = (LivingEntity)entity;
				e.put("fd", le.getFacingDirection().toString());
				e.put("md", le.getMovementDirection().toString());
				e.put("j", le.isJumping());
				if (le instanceof Mob){
					e.put("mob", true);
					Mob m = (Mob)le;
					e.put("pwd", m.getPlannedWalkDistance());
					e.put("awd", m.getActualWalkDistance());
					e.put("lx", m.getLastX());
				}
			}
			entities.add(e);
		}
		c.put("entities", entities);
		return c;
	}

	/**
	 * Saves a level to a JSON object. <strong>This method does not save the JSON to memory or to disk.</strong>
	 * @param level the chunk to save.
	 * @return the created JSON object.
	 */
	public static JSONObject saveLevel(Level level){
		JSONObject l = new JSONObject();
		JSONObject chunks = new JSONObject();
		JSONObject levels = (JSONObject)level.getWorld().getJSON().get("levels");
		if (levels != null) {
			l = (JSONObject)levels.get(Integer.toString(level.getIndex()));
			chunks = (JSONObject)l.get("chunks");
		}
		for (Chunk chunk : level.chunks.values()){
			chunks.put(Integer.toString(chunk.getIndex()), saveChunk(chunk));
		}
		l.put("chunks", chunks);
		return l;
	}

	/**
	 * Saves a chunk to its world's {@link JSONObject JSON object}
	 * @param chunk the chunk to save.
	 */
	public static void saveChunkToMemory(Chunk chunk){
		JSONObject world = chunk.getLevel().getWorld().getJSON();
		JSONObject levels = (JSONObject)world.get("levels");
		if (levels == null)
			levels = new JSONObject();
		JSONObject level = (JSONObject)levels.get(Integer.toString(chunk.getLevel().getIndex()));
		if (level == null)
			level = new JSONObject();
		JSONObject chunks = (JSONObject)level.get("chunks");
		if (chunks == null)
			chunks = new JSONObject();
		System.out.println(chunks.size());
		JSONObject jChunk = saveChunk(chunk);
		chunks.put(Integer.toString(chunk.getIndex()), jChunk);
		level.put("chunks", chunks);
		levels.put(Integer.toString(chunk.getLevel().getIndex()), level);
		world.put("levels", levels);
		if (world == chunk.getLevel().getWorld().getJSON())
			System.out.println("somethingsomething");
	}

	/**
	 * Saves a level to its world's {@link JSONObject JSON object}
	 * @param level the chunk to save.
	 */
	public static void saveLevelToMemory(Level level){
		JSONObject world = level.getWorld().getJSON();
		JSONObject levels = (JSONObject)world.get("levels");
		if (levels == null)
			levels = new JSONObject();
		JSONObject jLevel = saveLevel(level);
		levels.put(Integer.toString(level.getIndex()), level);
		world.put("levels", levels);
	}

	// this saves me a bit of casting and makes the code look nicer
	private static int longToInt(long number){
		return (int)number;
	}

}
