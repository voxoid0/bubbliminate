package com.voxoid.bubbliminate;

import com.badlogic.gdx.math.Matrix3;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;

public class Vector2Util {

//	public static final Vector2 UP = new Vector2(0f, -1f);
//	public static final Vector2 DOWN = new Vector2(0f, 1f);
//	public static final Vector2 LEFT = new Vector2(-1f, 0f);
//	public static final Vector2 RIGHT = new Vector2(1f, 0f);
	public static Vector2 up() { return new Vector2(0f, 1f); }
	public static Vector2 down() { return new Vector2(0f, -1f); }
	public static Vector2 left() { return new Vector2(-1f, 0f); }
	public static Vector2 right() { return new Vector2(1f, 0f); }
	public static Vector2 fromDegrees(float angle) {
		angle = (float) Math.toRadians(angle);
		return new Vector2((float) Math.cos(angle), (float) Math.sin(angle));
	}
	
	public static Vector2 fromCoreVector2(com.voxoid.bubbliminate.core.Vector2 coreVec) {
		return new Vector2(coreVec.x, coreVec.y);
	}
	
	public static com.voxoid.bubbliminate.core.Vector2 toCoreVector2(Vector2 vec) {
		return new com.voxoid.bubbliminate.core.Vector2(vec.x, vec.y);
	}
	
	public static Matrix4 toMat4(Matrix3 mat3) {
		Matrix4 mat4 = new Matrix4();
		mat4.val[Matrix4.M00] = mat3.val[Matrix3.M00];
		mat4.val[Matrix4.M01] = mat3.val[Matrix3.M01];
		mat4.val[Matrix4.M02] = mat3.val[Matrix3.M02];
		mat4.val[Matrix4.M10] = mat3.val[Matrix3.M10];
		mat4.val[Matrix4.M11] = mat3.val[Matrix3.M11];
		mat4.val[Matrix4.M12] = mat3.val[Matrix3.M12];
		mat4.val[Matrix4.M20] = mat3.val[Matrix3.M20];
		mat4.val[Matrix4.M21] = mat3.val[Matrix3.M21];
		mat4.val[Matrix4.M22] = mat3.val[Matrix3.M22];
		return mat4;
	}
}
