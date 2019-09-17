package com.voxoid.bubbliminate.core.ai.minimax;

public interface IGameMove {
	int getPlayerNum();
	
	IGameState make(IGameState state);
}
