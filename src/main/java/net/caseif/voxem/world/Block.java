/*
 * Voxem
 * Copyright (c) 2014-2015, Maxim Roncacé <caseif@caseif.net>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package net.caseif.voxem.world;

import net.caseif.voxem.GraphicsHandler;
import net.caseif.voxem.Main;
import net.caseif.voxem.Material;
import net.caseif.voxem.world.generator.Terrain;

import com.google.common.base.Optional;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;

import java.util.HashMap;
import java.util.Set;

public class Block {

    public static int blockHandle; //TODO: figure out what I meant to do with this

    protected Location location;

    protected Material type;

    protected int data;

    protected int light = 0;

    protected final int chunk;

    public static final int maxLight = 16;
    public static final int minLight = 0;
    public static final float horShadow = 2f / maxLight;

    public static final int horAngle = 4;

    /**
     * The block which is currently selected.
     */
    public static Location selected = null;
    //TODO: eliminate the next two variables
    public static int selectedX = 0;
    public static int selectedY = 0;
    public static boolean isSelected = false;

    /**
     * The diameter of a block
     */
    public static final int length = 54;

    /**
     * The factor by which the light level of a block should decrease from its brightest adjacent
     * block
     */
    public static final int lightDistance = 1; // I'm pretty sure this now does literally nothing

    public int lastLightUpdate = -1;

    public final HashMap<String, Object> metadata = new HashMap<>();

    public Block(Material m, int data, Location location) {
        this.type = m;
        this.data = data;
        this.location = location;
        this.chunk = location.getChunk();
        for (int i = (int) location.getY() - 1; i >= 0; i--) {
            Block b = getLevel().getBlock((int) location.getX(), i).orNull();
            if (b != null) {
                light = b.getLightLevel() - lightDistance;
                break;
            }
        }
    }

    public Block(Material m, Location location) {
        this(m, 0, location);
    }

    public void addToWorld() {
        Chunk c = location.getLevel().getChunk(location.getChunk());
        if (c == null)
            c = new Chunk(location.getLevel(), location.getChunk());
        c.setBlock(Chunk.getIndexInChunk((int) getLocation().getX()),
                (int) getLocation().getY(), this);
    }

    public Level getLevel() {
        return location.getLevel();
    }

    public int getX() {
        return (int) location.getX();
    }

    public int getY() {
        return (int) location.getY();
    }

    public Location getLocation() {
        return location.clone();
    }

    public Material getType() {
        return type;
    }

    public int getData() {
        return data;
    }

    public int getLightLevel() {
        return light;
    }

    public void setX(int x) {
        this.location.setX(x);
    }

    public void setY(int y) {
        this.location.setY(y);
    }

    public void setLocation(int x, int y) {
        this.location.setX(x);
        this.location.setY(y);
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public void setType(Material type) {
        this.type = type;
    }

    public void setLightLevel(int light) {
        this.light = light;
    }

    public boolean updateLight() {
        int newLight;
        Block up = null, down = null, left, right;
        if (getY() > 0)
            up = getLevel().getBlock(getX(), getY() - 1).orNull();
        if (getY() < Main.world.getChunkHeight() - 1)
            down = getLevel().getBlock(getX(), getY() + 1).orNull();
        left = getLevel().getBlock(getX() - 1, getY()).orNull();
        right = getLevel().getBlock(getX() + 1, getY()).orNull();
        Block[] adjacent = new Block[]{up, down, left, right};
        if (getY() <= Block.getTop(location))
            newLight = Block.maxLight;
        else {
            float average = 0;
            int total = 0;
            for (Block bl : adjacent) {
                if (bl != null) {
                    average += bl.getLightLevel();
                    total += 1;
                }
            }
            average /= total;
            if ((int) Math.floor(average) - Block.lightDistance >= Block.minLight)
                newLight = (int) Math.floor(average) - Block.lightDistance;
            else
                newLight = Block.minLight;
        }
        boolean changed = newLight != getLightLevel();
        lastLightUpdate = TickManager.getTotalTicks();
        if (changed) {
            setLightLevel(newLight);
            for (Block bl : adjacent)
                if (bl != null && bl.lastLightUpdate < TickManager.getTotalTicks()) {
                    try {
                        bl.updateLight();
                    } catch (StackOverflowError ignored) {} //TODO: Handle this properly
                }
        }
        for (int y = getY() + 1; y < getLevel().getWorld().getChunkHeight(); y++) {
            Block b = getLevel().getBlock(getX(), y).orNull();
            if (b != null) {
                if (b.lastLightUpdate != TickManager.getTicks()) {
                    try {
                        if (!b.updateLight()) { // I don't know wtf this code is for but I might need it later
                            break;
                        } else {
                            break;
                        }
                    } catch (StackOverflowError ignored) {} //TODO: Handle this properly
                }
            }
        }
        return changed;
    }

    public void destroy() {
        setType(Material.AIR);
    }

    public Block clone() {
        return new Block(type, location);
    }

    //TODO: make this faster
    public static int getTop(Location location) {
        for (int yy = 0; yy < Main.world.getChunkHeight(); yy++) {
            try {
                Block b = location.getLevel().getBlock(location.getX(), yy).orNull();
                if (b == null) { // column does not exist
                    return -1;
                }
                if (b.isSolid()) {
                    return yy;
                }
            } catch (NullPointerException ex) {
                int i = 0;
            }
        }
        return -1;
    }

    public static void updateSelectedBlock() {
        double mouseX = (Mouse.getX() - GraphicsHandler.xOffset) / (float) Block.length;
        double mouseY = (Display.getHeight() - Mouse.getY() - GraphicsHandler.yOffset) /
                (float) Block.length;
        double xDiff = mouseX - Main.player.getX();
        double yDiff = mouseY - Main.player.getY();
        double angle = Math.atan2(xDiff, yDiff); // IntelliJ tells me this shouldn't work. I concur.
        boolean found = false;
        for (double d = 0.5; d <= 5; d += 0.5) {
            double xAdd = d * Math.sin(angle);
            double yAdd = d * Math.cos(angle);
            int blockX = (int) Math.floor(Main.player.getX() + xAdd);
            int blockY = (int) Math.floor(Main.player.getY() + yAdd);
            synchronized (Main.lock) {
                if (blockY >= 0 && blockY <= Main.world.getChunkHeight() - 1) {
                    if (!isAir(Main.player.getLevel().getBlock(blockX, blockY))) {
                        //TODO: verfiy that player isn't peeking through blocks
                        Block.selected = new Location(Main.player.getLevel(), blockX, blockY);
                        found = true;
                        break;
                    }
                }
            }
        }
        if (!found)
            Block.selected = null;
    }

    public boolean isSolid() {
        return getType() != Material.AIR && !(hasMetadata("solid") && !(Boolean) getMetadata("solid"));
    }

    public Set<String> getAllMetadata() {
        return metadata.keySet();
    }

    public Object getMetadata(String key) {
        return metadata.get(key);
    }

    public boolean hasMetadata(String key) {
        return metadata.containsKey(key);
    }

    public void setMetadata(String key, Object value) {
        metadata.put(key, value);
    }

    public static boolean isAir(Optional<Block> block) {
        return !block.isPresent() || block.get().getType() == Material.AIR;
    }

}
