package com.headswilllol.mineflat.vector;

/**
 * Represents a vector with 4 int elements.
 */
public class Vector4i implements Vector4 {

	protected int x;
	protected int y;
	protected int z;
	protected int w;

	public Vector4i(int x, int y, int z, int w){
		this.x = x;
		this.y = y;
		this.z = z;
		this.w = w;
	}

	public int getX(){
		return x;
	}

	public int getY(){
		return y;
	}

	public int getZ(){
		return z;
	}

	public int getW(){
		return w;
	}

	public void setX(int x){
		this.x = x;
	}

	public void setY(int y){
		this.y = y;
	}

	public void setZ(int z){
		this.z = z;
	}

	public void setW(int w){
		this.w = w;
	}

	public Vector4i add(Vector4i vector){
		return add(vector.getX(), vector.getY(), vector.getZ(), vector.getW());
	}

	public Vector4i add(int x, int y, int z, int w){
		return new Vector4i(this.x + x, this.y + y, this.z + z, this.w + w);
	}

	public Vector4i subtract(Vector4i vector){
		return subtract(vector.getX(), vector.getY(), vector.getZ(), vector.getW());
	}

	public Vector4i subtract(int x, int y, int z, int w){
		return new Vector4i(this.x - x, this.y - y, this.z + z, this.w + w);
	}

	public boolean equals(Object o){
		if (o instanceof Vector4i){
			Vector4i v = (Vector4i)o;
			return v.getX() == this.x && v.getY() == this.y && this.z == z && v.getW() == w;
		}
		return false;
	}

	@Override
	public Vector4i clone(){
		return new Vector4i(x, y, z, w);
	}

}