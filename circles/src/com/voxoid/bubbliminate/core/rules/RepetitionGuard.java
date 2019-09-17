package com.voxoid.bubbliminate.core.rules;

import java.util.List;

import com.voxoid.bubbliminate.core.model.ICirclesGameState;
import com.voxoid.bubbliminate.core.model.IGameHistory.Snapshot;


/**
 * Guards against the maximum move repetitions allowed for a game. Requires the client to add each
 * new game state to the game's history. Successive identical game states are not counted until a
 * whole round of player moves 
 * 
 * @author joel.becker
 *
 */
public class RepetitionGuard {

	private int maxOccurences;
	
	
	public RepetitionGuard(int maxOccurences) {
		this.maxOccurences = maxOccurences;
	}

	/**
	 * 
	 * @param gameState
	 * @return True if the game state has not been repeated the maximum times allowed; false otherwise.
	 */
	public boolean isOk(ICirclesGameState gameState) {
		return countOccurances(gameState) < maxOccurences;
	}

	/**
	 * Counts the number of repetitions of the given game state found for 
	 * @param gameState
	 * @return
	 */
	public int countOccurances(ICirclesGameState gameState) {
		int occurances = 0;
		List<Snapshot> snapshots = gameState.getGame().getHistory().getHistory();
		
		for (Snapshot snapshot : snapshots) {
			if (snapshot.getState().equals(gameState)) {
				occurances++;
			}
		}
		
		return occurances;
	}	
}
