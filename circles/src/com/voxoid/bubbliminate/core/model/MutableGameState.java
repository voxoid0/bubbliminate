package com.voxoid.bubbliminate.core.model;

import java.util.List;

public class MutableGameState extends GameState {

	public MutableGameState(ICirclesGameState dub, boolean incrementPlayerToMove) {
		super(dub, incrementPlayerToMove);
	}
	
	public MutableGameState(IGame game) {
		super(game);
	}

	public MutableGameState(IGame game, List<IPlayerState> playerStates) {
		super(game);
		setPlayerStates(playerStates);
	}
	
	public void setPlayerStates(List<IPlayerState> playerStates) {
		this.playerStates = playerStates;
	}

	public void setIncrementPlayerToMove(boolean incrementPlayerToMove) {
		this.incrementPlayerToMove = incrementPlayerToMove;
	}
	
	public void setPlayerToMove(int playerToMove) {
		this.playerToMove = playerToMove;
	}
}
