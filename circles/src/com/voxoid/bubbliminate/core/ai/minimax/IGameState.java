package com.voxoid.bubbliminate.core.ai.minimax;

public interface IGameState {

	boolean gameIsOver();
	boolean gameIsOverFor(int playerNum);
}
