package com.voxoid.bubbliminate.core;

import com.voxoid.bubbliminate.core.model.IGame;

public interface IDifferentGameListener {
	/**
	 * Called when a different game has been loaded or started.
	 * @param game
	 */
	void differentGame(IGame game);
}
