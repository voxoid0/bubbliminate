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
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.Validate;

/**
 * @author jbecker
 *
 */
public class Game implements IGame {

	private GameConfig config;
	private ICirclesGameState state;
	private List<IPlayer> players;
	private transient IGameHistory history;
	private transient PropertyChangeSupport propSupport;
	
	
	/** For de-serialization only. */
	public Game() {
	}
	
	/**
	 * Constructs a game; the IGameState will be null until setCurrentState() is called.
	 * @param players The players
	 * @param environmentRadius Environment radius
	 */
	public Game(Collection<IPlayer> players, GameConfig gameConfig) {
		Validate.notEmpty(players);
		this.players = new ArrayList<IPlayer>(players);
		Validate.notNull(gameConfig);
		this.config = gameConfig;
		readResolve();
	}
	
	/** {@inheritDoc} */
	@Override
	public ICirclesGameState getCurrentState() {
		return state;
	}

	/** {@inheritDoc} */
	@Override
	public int getNumPlayers() {
		return players.size();
	}

	/** {@inheritDoc} */
	@Override
	public List<IPlayer> getPlayers() {
		return Collections.unmodifiableList(players);
	}

	/** {@inheritDoc} */
	@Override
	public void setCurrentState(ICirclesGameState gameState) {
		ICirclesGameState old = state;
		state = gameState;
		propSupport.firePropertyChange("currentState", old, gameState);
	}
	
	/** {@inheritDoc} */
	@Override
	public float getEnvironmentRadius() {
		return config.getEnvironmentRadius();
	}

	/** {@inheritDoc} */
	@Override
	public float getMinCircleRadius() {
		return config.getMinCircleRadius();
	}
	
	public GameConfig getConfig() {
		return config;
	}

	@Override
	public IPlayer getCurPlayer() {
		return players.get(getCurrentState().getPlayerToMove());
	}

	@Override
	public IPlayerState getCurPlayerState() {
		return state.getPlayerState(getCurrentState().getPlayerToMove());
	}
	
	@Override
	public IPlayer getWinner() {
		int nPlayersAlive = 0;
		int winner = 0;
		int player = 0;
		for (IPlayerState pstate : getCurrentState().getPlayerStates()) {
			if (pstate.isAlive()) {
				nPlayersAlive++;
				winner = player;
			}
			player++;
		}
		
		if (nPlayersAlive == 1) {
			return players.get(winner);
		} else {
			return null;
		}
	}
	
	@Override
	public IGameHistory getHistory() {
		return history;
	}

	@Override
	public void addPropertyChangeListener(PropertyChangeListener listener) {
		propSupport.addPropertyChangeListener(listener);
	}

	@Override
	public void removePropertyChangeListener(PropertyChangeListener listener) {
		propSupport.removePropertyChangeListener(listener);
	}
	
	private Object readResolve() {
		history = new GameHistory();
		propSupport = new PropertyChangeSupport(this);
		return this;
	}	
}
