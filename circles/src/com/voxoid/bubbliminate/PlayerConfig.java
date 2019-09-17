package com.voxoid.bubbliminate;

import org.apache.commons.lang.Validate;

public class PlayerConfig {
	public enum Type {
		HUMAN,
		CPU
	}
	
	public static final int MIN_CPU_LEVEL = 1;
	public static final int MAX_CPU_LEVEL = 4;
	
	public final Type type;
	public final int cpuLevel;
	

	public static PlayerConfig human() {
		return new PlayerConfig(Type.HUMAN, 1);
	}
	
	public static PlayerConfig cpu(int level) {
		return new PlayerConfig(Type.CPU, level);
	}
	
	public PlayerConfig(Type type, int cpuLevel) {
		this.type = type;
		Validate.isTrue(cpuLevel >= MIN_CPU_LEVEL && cpuLevel <= MAX_CPU_LEVEL);
		this.cpuLevel = cpuLevel;
	}
	
	/** For de-serialization only. */
	public PlayerConfig() {
		type = Type.HUMAN;
		cpuLevel = 1;
	}

	@Override
	public String toString() {
		return "PlayerConfig [" + (type == Type.HUMAN ? "HUMAN" : "CPU level " + cpuLevel) + "]";
	}
	
	
}
