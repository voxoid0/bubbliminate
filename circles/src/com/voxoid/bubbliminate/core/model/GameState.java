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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.thoughtworks.xstream.XStream;
import com.voxoid.bubbliminate.CirclesGlobal;
import com.voxoid.bubbliminate.core.Vector2;

/**
 * @author jbecker
 *
 */
public class GameState implements ICirclesGameState {

	private IGame game;
	protected int playerToMove;
	protected List<IPlayerState> playerStates;
	protected boolean incrementPlayerToMove;
	protected IPlayer winner;
	protected boolean draw;


	/** For de-serialization only. */
	public GameState() {
	}
	
	/**
	 * Constructs a game with player states but no circles for the players.
	 * @param game
	 */
	public GameState(IGame game) {
		this.game = game;
		playerToMove = 0;
		playerStates = new ArrayList<IPlayerState>(game.getNumPlayers());
		for (IPlayer player : game.getPlayers()) {
			playerStates.add(new PlayerState(player));
		}
	}

	/**
	 * Constructs a duplicate of the given game state.
	 * @param dub Game state of which to be a copy.
	 */
	public GameState(ICirclesGameState dub, boolean incrementPlayerToMove) {
		int nPlayers = dub.getGame().getNumPlayers();
		game = dub.getGame();
		
		playerStates = new ArrayList<IPlayerState>(nPlayers);
		for (int i = 0; i < nPlayers; i++) {
			playerStates.add(new PlayerState(dub.getPlayerState(i)));
		}
		
		this.incrementPlayerToMove = incrementPlayerToMove;
		playerToMove = dub.getPlayerToMove();
	}

	/** {@inheritDoc} */
	@Override
	public int getPlayerToMove() {
		int player = playerToMove;
		if (incrementPlayerToMove) {
			
			//// Find which player to move next
			do {
				player = (player + 1) % game.getNumPlayers();
			} while (!getPlayerState(player).isAlive());
		}
		return player;
	}
	
	@Override
	public void setPlayerToMove(int player) {
		playerToMove = player;
		incrementPlayerToMove = false;
	}
	
	/** {@inheritDoc} */
	@Override
	public IGame getGame() {
		return game;
	}

	/** {@inheritDoc} */
	@Override
	public IPlayerState getPlayerState(int player) {
		return playerStates.get(player);
	}
	
	/** {@inheritDoc} */
	@Override
	public Collection<IPlayerState> getPlayerStates() {
		return Collections.unmodifiableCollection(playerStates);
	}
	
	public int getNumCircles() {
		int count = 0;
		for (IPlayerState ps : getPlayerStates()) {
			count += ps.getNumCircles();
		}
		return count;
	}
	
	@Override
	public ICircle circleAt(Vector2 loc) {
		for (IPlayerState ps : getPlayerStates()) {
			for (ICircle circle : ps.getCircles()) {
				if (CircleUtil.isInside(circle, 0f, loc)) {
					return circle;
				}
			}
		}
		return null;
	}

	public boolean gameIsOver() {
		int numCircles = getNumCircles();
		for (IPlayerState ps : playerStates) {
			if (ps.getNumCircles() == numCircles) {
				winner = ps.getPlayer();
				return true;
			}
		}
		return false;
	}
	
	@Override
	public boolean gameIsOverFor(int playerNum) {
		return playerStates.get(playerNum).getNumCircles() == 0;
	}

	public void setIsDraw(boolean isDraw) {
		this.draw = isDraw;
	}
	
	/**
	 * Two GameStates are equal if they have the same set of circles for each player
	 * at the same locations with the same radii, and the player to move is the same.
	 */
	@Override
	public boolean equals(Object object) {
		if (object == null) return false;
		if (object == this) return true;
		if (!(object instanceof GameState)) return false;
		
		ICirclesGameState other = (ICirclesGameState) object;
		if (other.getPlayerToMove() != getPlayerToMove()) return false;
		if (other.getPlayerStates().size() != getPlayerStates().size()) return false;
		Iterator<IPlayerState> psIter = getPlayerStates().iterator();
		
		float epsilon = getGame().getMinCircleRadius() * 0.1f;
		for (IPlayerState ops : other.getPlayerStates()) {
			IPlayerState ps = psIter.next();
			Collection<ICircle> myCircles = ps.getCircles();
			if (ops.getNumCircles() != ps.getNumCircles()) return false;
			
			for (ICircle opc : ops.getCircles()) {
				if (CircleUtil.findMatchingCircle(opc, myCircles, epsilon) == null) return false;
			}
		}
		return true;
	}
	
	public void save(File file) throws IOException {
		XStream xstream = CirclesGlobal.systemUtil.createXStream();
		OutputStream out = new FileOutputStream(file);
		try {
			xstream.toXML(this, out);
		} finally {
			out.close();
		}
	}
	
	public static GameState load(File file) {
		XStream xstream = CirclesGlobal.systemUtil.createXStream();
		return (GameState) xstream.fromXML(file);
	}
}
