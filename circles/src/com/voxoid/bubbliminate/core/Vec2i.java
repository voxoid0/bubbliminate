package com.voxoid.bubbliminate.core;

public class Vec2i {
	public final int x;
	public final int y;
	
	
	public Vec2i(int x, int y) {
		super();
		this.x = x;
		this.y = y;
	}
	
	@Override
	public boolean equals(Object other) {
		if (other == null || !(other instanceof Vec2i)) {
			return false;
		}
		Vec2i o = (Vec2i) other;
		return o.x == x && o.y == y;
	}
	
	@Override
	public String toString() {
		return String.format("(%d,%d)", x, y);
	}
	
}
