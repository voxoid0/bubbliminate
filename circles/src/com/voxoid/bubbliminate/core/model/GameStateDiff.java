package com.voxoid.bubbliminate.core.model;

import java.util.Collection;
import java.util.Collections;

/**
 * Difference between one game state and another after a move was made, expressed in circles
 * directly changed by the move (move, grow, or split), circles shrunk, and circles popped.
 * @author Joel
 *
 */
public class GameStateDiff {
//	protected IGame game;
	protected Collection<ICircle> movedOld;
	protected Collection<ICircle> movedNew;
	protected Collection<ICircle> popped;
	protected Collection<ICircle> shrunk;
	
	public static final GameStateDiff EMPTY = new GameStateDiff();
	
	public GameStateDiff() {
		movedOld = Collections.emptyList();
		movedNew = Collections.emptyList();
		popped = Collections.emptyList();
		shrunk = Collections.emptyList();
	}
	
	public GameStateDiff(Collection<ICircle> movedOld,
			Collection<ICircle> movedNew, Collection<ICircle> popped) {
//		this.game = game;
		this.movedOld = movedOld;
		this.movedNew = movedNew;
		this.popped = popped;
		this.shrunk = Collections.emptyList();	// TODO: add to ctor args
	}

//	public IGame getGame() {
//		return game;
//	}

	public Collection<ICircle> getMovedOld() {
		return Collections.unmodifiableCollection(movedOld);
	}

	public Collection<ICircle> getMovedNew() {
		return Collections.unmodifiableCollection(movedNew);
	}

	public Collection<ICircle> getDestroyed() {
		return Collections.unmodifiableCollection(popped);
	}
	
	public Collection<ICircle> getShrunk() {
		return Collections.unmodifiableCollection(shrunk);
	}
}
