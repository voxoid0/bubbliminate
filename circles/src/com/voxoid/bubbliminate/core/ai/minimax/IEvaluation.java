package com.voxoid.bubbliminate.core.ai.minimax;


public interface IEvaluation {
	IGameMove bestMove(IGameState s, int playerNum);
	
	/**
	 * Finishes up the move calculation right away. This method is called from another thread.
	 */
	void hurryUp();
	
	void cancel();

}
