package com.voxoid.bubbliminate.core.model;

import java.beans.PropertyChangeListener;
import java.util.List;

import com.voxoid.bubbliminate.core.rules.IMove;


public interface IGameHistory {
	public static class Snapshot {
		private ICirclesGameState state;
		private IMove prevMove;
		
		/**
		 * Constructs a snapshot.
		 * @param state The state at that step.
		 * @param prevMove The previous move causing this step, or null if this is the initial state.
		 */
		public Snapshot(ICirclesGameState state, IMove prevMove) {
			this.state = state;
			this.prevMove = prevMove;
		}
		
		public ICirclesGameState getState() {
			return state;
		}
		
		public IMove getMove() {
			return prevMove;
		}
		
		@Override
		public String toString() {
			if (prevMove != null) {
				return String.format("P%d: %s", (state.getPlayerToMove() + 1),
						prevMove.describe());
			} else {
				return "Beginning of Game";
			}
		}
	}
	
	List<Snapshot> getHistory();
	void rewindTo(Snapshot snapshot);
	void add(ICirclesGameState resultingState, IMove previous);
	void addPropertyChangeListener(PropertyChangeListener listener);
	void removePropertyChangeListener(PropertyChangeListener listener);
	ICirclesGameState undo();
	ICirclesGameState redo();
}
