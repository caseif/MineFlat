package com.headswilllol.mineflat.entity;

import static org.lwjgl.opengl.GL11.*;

import java.util.HashMap;

import com.headswilllol.mineflat.Block;
import com.headswilllol.mineflat.Direction;
import com.headswilllol.mineflat.GraphicsHandler;
import com.headswilllol.mineflat.Main;
import com.headswilllol.mineflat.Timing;
import com.headswilllol.mineflat.util.ImageUtil;

public class Entity {

	/**
	 * The speed at which entities will fall
	 */
	public static final float gravity = .6f;

	/**
	 * The terminal downwards velocity of entities
	 */
	public static final float terminalVelocity = 1f;

	/**
	 * The width of the entity relative to a block's width
	 */
	public float width;

	/**
	 * The height of the entity relative to a block's height
	 */
	public float height;

	/**
	 * The current velocity on the x axis (e.g. from moving, throwing)
	 */
	public static float xVelocity = 0;

	/**
	 * The current velocity on the y axis (e.g. from falling, jumping)
	 */
	public static float yVelocity = 0;

	/**
	 * The vertical offset in pixels of entities in relation to the block they are standing on.
	 */
	public static final int vertOffset = Block.length / Block.horAngle / 2;

	public static HashMap<EntityType, Integer> sprites = new HashMap<EntityType, Integer>();

	protected float x;
	protected float y;
	protected EntityType type;

	public Entity(EntityType type, float x, float y, float width, float height){
		this.type = type;
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
	}

	public float getX(){
		return x;
	}

	public float getY(){
		return y;
	}

	public EntityType getType(){
		return type;
	}

	public void setX(float x){
		this.x = x;
	}

	public void setY(float y){
		this.y = y;
	}

	public void setType(EntityType type){
		this.type = type;
	}

	public float getXVelocity(){
		return xVelocity;
	}

	public void setXVelocity(float v){
		xVelocity = v;
	}

	public float getYVelocity(){
		return yVelocity;
	}

	public void setYVelocity(float v){
		yVelocity = v;
	}

	public void manageMovement(){

		if (!isOnGround()){
			if (getYVelocity() < terminalVelocity){
				float newFallSpeed = getYVelocity() +
						(gravity * Timing.delta / Timing.timeResolution);
				if (newFallSpeed > terminalVelocity)
					newFallSpeed = terminalVelocity;
				setYVelocity(newFallSpeed);
			}
		}

		if (!isXMovementBlocked())
			setX(x + xVelocity * (Timing.delta / Timing.timeResolution));
		else {
			setXVelocity(0);
			if (this instanceof Mob){
				((Mob)this).setPlannedWalkDistance(0);
				((Mob)this).setActualWalkDistance(0);
			}
		}

		float newY = getY() + getYVelocity();

		if (newY >= 0 && newY <= Main.world.getChunkHeight() - 1){
			float pX = getX() >= 0 ? getX() :
				getX() - 1;
			if (Block.isSolid(pX, (float)Math.floor(newY)))
				setYVelocity(gravity);
		}

		setY(getY() + getYVelocity());
		if (Math.floor(getY() + height) < Main.world.getChunkHeight()){
			float x = (Math.abs(getX()) % 1 >= 0.5 && getX() > 0) ||
					(Math.abs(getX()) % 1 <= 0.5 && getX() < 0) ?
							getX() - 4f / Block.length : getX() + 4f / Block.length;
							if (x < 0) x -= 1;
							Block below = null;
							if (getY() >= -height)
								below = Block.getBlock((float)x, (float)Math.floor(getY() + height));
							if (Block.isSolid(below)){
								if((float)below.getY() - getY() < height){
									setY(below.getY() - height);
								}  	   
							}
		}
	}

	public boolean isOnGround(){
		if (Math.floor(getY() + height) < Main.world.getChunkHeight()){
			float x = (Math.abs(getX()) % 1 >= width / 2 && getX() > 0) || (Math.abs(getX()) % 1 <= width / 2 &&
					getX() < 0) ? getX() - width / 4 : getX() + width / 4;
					if (x < 0)
						x -= 1;
					Block below = null;
					if (getY() >= -height)
						below = Block.getBlock((float)x, (float)Math.floor(getY() + height));
					if (Block.isSolid(below))
						return true;
					else
						return false;
		}

		else return true;
	}

	public boolean isXMovementBlocked(){
		float newX = x >= 0 ? getX() + (xVelocity * (Timing.delta / Timing.timeResolution)) :
			getX() - 1 + (xVelocity * (Timing.delta / Timing.timeResolution));
		int minY = (int)Math.floor(y);
		int maxY = (int)Math.floor(y + height - 1);
		for (int y = minY; y <= maxY; y++)
			if (Block.isSolid(newX, y))
				return true;
		return false;
	}

	public void draw(){
		glPushMatrix();
		glEnable(GL_BLEND);
		glBindTexture(GL_TEXTURE_2D, sprites.get(type));
		glColor3f(1f, 1f, 1f);
		glTranslatef(getX() * Block.length + GraphicsHandler.xOffset - (width / 2) * Block.length,
				getY() * Block.length + GraphicsHandler.yOffset, 0);
		if (this instanceof LivingEntity && ((LivingEntity)this).getFacing() == Direction.RIGHT){
			glTranslatef(Block.length * width, 0f, 0f);
			glScalef(-1f, 1f, 1f);
		}
		glBegin(GL_QUADS);
		int hWidth = (int)(Block.length * width);
		int hHeight = (int)(Block.length * height);
		glTexCoord2f(0f, 0f);
		glVertex2f(0f, vertOffset);
		glTexCoord2f(1f, 0f);
		glVertex2f(hWidth, vertOffset);
		glTexCoord2f(1f, 1f);
		glVertex2f(hWidth, hHeight - vertOffset);
		glTexCoord2f(0f, 1f);
		glVertex2f(0f, hHeight - vertOffset);
		glEnd();
		glDisable(GL_BLEND);
		glBindTexture(GL_TEXTURE_2D, 0);
		glPopMatrix();
	}

	public static void initialize(){
		for (EntityType et : EntityType.values()){
			if (et != EntityType.ITEM_DROP){
				try {
					Entity.sprites.put(et, ImageUtil.createTextureFromStream(
							//(InputStream)ImageIO.createImageInputStream(
							//		ImageUtil.scaleImage(
							//				ImageIO.read(
													LivingEntity.class.getClassLoader().getResourceAsStream(
															"textures/" + et.toString().toLowerCase() + ".png"
															)
							//						), 64, 64
							//				)
							//		)
							));
				}
				catch (Exception ex){
					System.err.println("Exception occurred while preparing texture for player sprite");
					ex.printStackTrace();
				}
			}
		}
	}

}
