package com.voxoid.bubbliminate.core.ai;

import java.util.concurrent.Future;

import com.voxoid.bubbliminate.core.model.ICirclesGameState;
import com.voxoid.bubbliminate.core.rules.IMove;

public interface ICpuPlayer {
	Future<IMove> chooseMove(ICirclesGameState gameState);
}
