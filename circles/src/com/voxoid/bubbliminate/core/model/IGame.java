/**
 * Copyright (c) 1996-2009 by 21st Century Systems Inc. All rights reserved.
 *
 * Data and materials contained herein are proprietary to 21st Century Systems, Inc.
 * and may contain trade secrets or patented technology.
 *
 * Use is subject to the software license agreement contained in or referred to in
 * this plug-ins about.html file. Please refer any questions to:
 *
 *
 * 21st Century Systems, Inc.
 * 2611 Jefferson Davis Highway, Suite 111000,
 * Arlington, VA 22202
 *
 * $Id$
 */
package com.voxoid.bubbliminate.core.model;

import java.beans.PropertyChangeListener;
import java.util.List;

/**
 * @author jbecker
 *
 */
public interface IGame {
	GameConfig getConfig();
	int getNumPlayers();
	List<IPlayer> getPlayers();
	ICirclesGameState getCurrentState();
	void setCurrentState(ICirclesGameState gameState);
	float getEnvironmentRadius();
	float getMinCircleRadius();
	
	/**
	 * Convenience method to get the player that is next to move in the current game state.
	 * @return The {@link IPlayer}
	 */
	IPlayer getCurPlayer();
	
	/**
	 * Convenience method to get the player state of the player that is next to move in the current game state.
	 * @return The {@link IPlayerState}
	 */
	IPlayerState getCurPlayerState();
	
	/**
	 * Returns the player that won the game, or null if no one has won yet.
	 * @return The winning player, or null if none yet.
	 */
	IPlayer getWinner();
	
	IGameHistory getHistory();
	
	void addPropertyChangeListener(PropertyChangeListener listener);
	void removePropertyChangeListener(PropertyChangeListener listener);
}
