package com.voxoid.bubbliminate.core;


public class Vector2 {
	
	public static final Vector2 ZERO = new Vector2(0f, 0f);
	
	public final float x;
	public final float y;
	
	public Vector2(float x, float y) {
		this.x = x;
		this.y = y;
	}
	
	/** For de-serialization only. */
	public Vector2() {
		x = 0; y = 0;
	}
	
//	public Vector2(Point point) {
//		this((float) point.x, (float) point.y);
//	}
	
	public Vector2 add(Vector2 other) {
		return new Vector2(x + other.x, y + other.y);
	}
	
	public Vector2 subtract(Vector2 other) {
		return new Vector2(x - other.x, y - other.y);
	}
	
	public Vector2 multiply(float scalar) {
		return new Vector2(x * scalar, y * scalar);
	}
	
	public Vector2 divide(float scalar) {
		return new Vector2(x / scalar, y / scalar);
	}
	
	public float dot(Vector2 other) {
		return x * other.x + y * other.y;
	}
	
	public float length() {
		return (float) Math.sqrt(x*x + y*y);
	}
	
	public float lengthSquared() {
		return x*x + y*y;
	}
	
	public Vector2 normalized() {
		float invLen = 1.0f / length();
		return new Vector2(x * invLen, y * invLen);
	}
	
	public Vector2 rotate90() {
		return new Vector2(-y, x);
	}
	
	public Vector2 rotate180() {
		return new Vector2(-x, -y);
	}
	
	public Vector2 rotate270() {
		return new Vector2(y, -x);
	}
	
	public Vector2 neg() {
		return new Vector2(-x, -y);
	}
	
//	public PointF toPoint() {
//		return new PointF(x, y);
//	}
	
	@Override
	public String toString() {
		return String.format("(%.2f,%.2f)", x, y);
	}
	
	public Vector2 clamp(float length) {
		return (lengthSquared() > length*length) ? normalized().multiply(length) : this;
	}
	
	@Override
	public boolean equals(Object object) {
		if (object == null) return false;
		if (!(object instanceof Vector2)) return false;
		Vector2 other = (Vector2) object;
		return other.x == x && other.y == y;
	}
}
