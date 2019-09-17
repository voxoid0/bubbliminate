package com.voxoid.bubbliminate.core.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.Validate;

import com.voxoid.bubbliminate.PlayerConfig;
import com.voxoid.bubbliminate.PlayerConfig.Type;


/**
 * Configuration of the game.
 * 
 * @author Joel
 *
 */
public class GameConfig {

	private static final float MIN_CIRCLE_RADIUS = 0.5f;
	private static final float SPACE_BETW_PLAYER_CENTERS = 5f;	// Odd number is better! (makes it more fair in 2-player game)
	private static final float SPACE_BETW_2_PLAYERS = 7f;

	private List<PlayerConfig> playerConfigs;
	private float envRadius;
	private float ringRadius;

	public GameConfig(int nHumanPlayers) {
		this(createHumanPlayerConfigs(nHumanPlayers));
	}
	
	/** For de-serialization only. */
	public GameConfig() {}
	
	public GameConfig(int nHumanPlayers, float envRadius, float ringRadius) {
		this(createHumanPlayerConfigs(nHumanPlayers), envRadius, ringRadius);
	}
	public static List<PlayerConfig> createHumanPlayerConfigs(int num) {
		List<PlayerConfig> configs = new ArrayList<PlayerConfig>(num);
		for (int i = 0; i < num; i++) {
			configs.add(PlayerConfig.human());
		}
		return configs;
	}
	
	public GameConfig(List<PlayerConfig> playerConfigs) {
		this(playerConfigs, 
				getDefaultEnvRadius(playerConfigs.size(), getDefaultRingRadius(playerConfigs.size())),
				getDefaultRingRadius(playerConfigs.size()));
	}
	
	public GameConfig(List<PlayerConfig> playerConfigs, float envRadius, float ringRadius) {
		Validate.noNullElements(playerConfigs);
		this.playerConfigs = playerConfigs;
		this.envRadius = envRadius;
		this.ringRadius = ringRadius;
	}

	public int getNumPlayers() {
		return playerConfigs.size();
	}
	
	public float getEnvironmentRadius() {
		return envRadius;
	}
	
	public float getRingRadius() {
		return ringRadius;
	}

	public float getMinCircleRadius() {
		return MIN_CIRCLE_RADIUS;
	}
	
	public List<PlayerConfig> getPlayerConfigs() {
		return Collections.unmodifiableList(playerConfigs);
	}
	
	public static float getDefaultRingRadius(int nPlayers) {
		/*
		 * O = half of space between player centers
		 * H = ?
		 * theta = 180 / number of players
		 * 
		 * sin(theta) = O/H -->
		 * H = O / sin(theta)
		 */
		double theta = Math.PI / nPlayers;
		float spaceBetw = nPlayers == 2 ? SPACE_BETW_2_PLAYERS : SPACE_BETW_PLAYER_CENTERS;
		float hyp = (spaceBetw / 2.0f) / (float) Math.sin(theta);
		return hyp;
	}
	
	public static float getDefaultEnvRadius(int nPlayers, float ringRadius) {
		return ringRadius * 3f;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("GameConfig [envRadius=").append(envRadius);
		sb.append(", ringRadius=").append(ringRadius);
		sb.append(", playerConfigs=");
		for (PlayerConfig pc : playerConfigs) {
			sb.append(pc.toString()).append(", \n");
		}
		return sb.toString();
	}
}
