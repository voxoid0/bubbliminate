package com.voxoid.bubbliminate;

import java.util.List;

import com.voxoid.bubbliminate.core.model.GameConfig;
import com.voxoid.bubbliminate.core.model.IPlayer;

/**
 * Creates a {@link IPlayer} for a game.
 * @author Joel
 *
 */
public interface IPlayerFactory {

	/**
	 * 
	 * @param Number of players in the game, and number of players to create
	 * @return new {@link IPlayer} instance
	 */
	List<IPlayer> createPlayers(GameConfig gameConfig);
}
