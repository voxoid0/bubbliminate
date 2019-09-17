package com.voxoid.bubbliminate;

public class MathUtil {

	public static final double LN2 = Math.log(2.0);
	
	public static int getNextPot(int n) {
		return (int) Math.pow(2.0, Math.ceil(log2(n)));
	}
	
	public static double log2(double n) {
		return Math.log(n) / LN2;
	}
}
