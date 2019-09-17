package com.voxoid.bubbliminate;

import java.util.Map;

/**
 * Interface to cross-platform game data, which also allows
 * the data to be stored in the OUYA game data on that platform.
 * @author joel.becker
 *
 */
public interface IGameData {

	Map<String, String> getAllGameData();
	String getGameData(String key);
	void putGameData(String key, String value);
}
