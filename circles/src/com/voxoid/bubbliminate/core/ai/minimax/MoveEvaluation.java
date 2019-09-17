package com.voxoid.bubbliminate.core.ai.minimax;

public class MoveEvaluation {

	public final IGameMove move;
	public final int score;
	
	public MoveEvaluation(IGameMove move, int score) {
		this.move = move;
		this.score = score;
	}
	
	public MoveEvaluation(int score) {
		this.move = null;
		this.score = score;
	}
	
}
