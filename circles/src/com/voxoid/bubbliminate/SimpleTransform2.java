package com.voxoid.bubbliminate;

import com.badlogic.gdx.math.Vector2;

public class SimpleTransform2 {

	float scale;
	float tx;
	float ty;
	
	public static final SimpleTransform2 NONE = new SimpleTransform2(1f, 0f, 0f);
	
	public SimpleTransform2(float scale, float tx, float ty) {
		super();
		this.scale = scale;
		this.tx = tx;
		this.ty = ty;
	}
	
	
	
	public float getScaleX() {
		return scale;
	}

	public float getScaleY() {
		return scale;
	}


	public float getTx() {
		return tx;
	}



	public float getTy() {
		return ty;
	}



	public Vector2 transform(Vector2 v) {
		Vector2 v2 = new Vector2(v.x * scale + tx, v.y * scale + ty);
		return v2;
	}
	
	public com.voxoid.bubbliminate.core.Vector2 transform(com.voxoid.bubbliminate.core.Vector2 v) {
		return new com.voxoid.bubbliminate.core.Vector2(v.x * scale + tx, v.y * scale + ty);
	}
	
	public float transformX(float x) {
		return x * scale + tx;
	}
	
	public float transformY(float y) {
		return y * scale + ty;
	}
}
