package com.voxoid.bubbliminate.core.ai.minimax;

import java.util.List;

import com.voxoid.bubbliminate.core.model.IPlayer;

/**
 * Creates a set of possible moves for the given game state and player.
 * @author joel.becker
 * 
 * TODO: The circles impl of this should iterate over:
 * 1. Pop combos
 *     1. With grow
 *     2. With split
 *     3. With move
 *     4. Those three popping only myself
 * 2. Significant directions and/or magnitudes
 *     1. With split, each cell with sig directions
 *     2. With grow, each cell with sig magnitudes
 *     3. With move, each cell with sig directions, then sig magnitudes within each direction
 *
 */
public interface IMovesGenerator {
	List<IGameMove> generateMoves(IGameState state, int player, int ply);
	
	/**
	 * Generates all moves for the given moving player's opponents, possibly returning only a few moves
	 * for only one of the opponents, depending on the ply and thoroughness thresholds.
	 * 
	 * @param state Current game state
	 * @param movingPlayerNum The index of the player for which to generate moves
	 * @param ply Which ply of the search algorithm we are on (might determine thoroughness e.g.)
	 * @return
	 */
	List<IGameMove> generateBestReplyMoves(IGameState state, int movingPlayerNum, int ply);
}
