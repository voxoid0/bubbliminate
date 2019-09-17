package com.voxoid.bubbliminate.core.ai.minimax;


/**
 * Used with best reply search, this must be a two-sided comparison, as if for a two player game.
 * E.g. compare the player to the strongest opponent.
 * 
 * @author Joel
 *
 */
public interface IGamePositionEvaluator {
	int eval(IGameState state, int player);
	int minimum();
	int maximum();
}
