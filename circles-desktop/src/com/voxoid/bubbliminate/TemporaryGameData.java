package com.voxoid.bubbliminate;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Game data that lives only in RAM and is not persisted.
 * 
 * @author joel.becker
 *
 */
public class TemporaryGameData implements IGameData {

	private Map<String, String> data = new HashMap<String, String>();
	
	@Override
	public Map<String, String> getAllGameData() {
		return Collections.unmodifiableMap(data);
	}

	@Override
	public String getGameData(String key) {
		return data.get(key);
	}

	@Override
	public void putGameData(String key, String value) {
		data.put(key, value);
	}

}
