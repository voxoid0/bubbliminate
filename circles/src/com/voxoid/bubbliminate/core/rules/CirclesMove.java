package com.voxoid.bubbliminate.core.rules;

import org.apache.commons.lang.Validate;

import com.voxoid.bubbliminate.core.ai.minimax.IGameMove;
import com.voxoid.bubbliminate.core.ai.minimax.IGameState;
import com.voxoid.bubbliminate.core.model.ICirclesGameState;
import com.voxoid.bubbliminate.core.model.IPlayer;
import com.voxoid.bubbliminate.core.model.MutableGameStateDiff;

public abstract class CirclesMove implements IGameMove, IMove {
	private IPlayer player;

	public CirclesMove(IPlayer player) {
		Validate.notNull(player);
		this.player = player;
	}
	
	@Override
	public int getPlayerNum() {
		return player.getIndex();
	}
	
	public IPlayer getPlayer() {
		return player;
	}

	@Override
	public IGameState make(IGameState state) {
		return make((ICirclesGameState) state, new MutableGameStateDiff());
	}

	public abstract ICirclesGameState make(ICirclesGameState state, MutableGameStateDiff diff);
}
