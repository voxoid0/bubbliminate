package com.voxoid.bubbliminate.core.ai;

import com.voxoid.bubbliminate.core.ai.minimax.IGamePositionEvaluator;
import com.voxoid.bubbliminate.core.ai.minimax.IGameState;
import com.voxoid.bubbliminate.core.model.CircleUtil;
import com.voxoid.bubbliminate.core.model.GameConfig;
import com.voxoid.bubbliminate.core.model.GameStateUtil;
import com.voxoid.bubbliminate.core.model.ICircle;
import com.voxoid.bubbliminate.core.model.ICirclesGameState;
import com.voxoid.bubbliminate.core.model.IPlayerState;
import com.voxoid.bubbliminate.core.rules.SplitMove;

/**
 * circle that cannot be split and has no room to grow enough among own circles = 0.8 points
 * circle that cannot be split but has room among own circles to grow = 0.9 points 
 * circle that can be split = 1.0 points
 * 
 * TODO:
 * Can reach them but they can't reach me - but that's the same as power - can only happen through power
 * More grow room = higher score
 * More times splittable = higher score
 * More area covered = higher score
 * @author Joel
 *
 */
public class GamePositionEvaluator implements IGamePositionEvaluator {

	public static final int MAX_SCORE = 1000;
	
	private static final float CIRCLE_CAN_SPLIT_SCORE = 1f;
	private static final float CIRCLE_CAN_GROW_SCORE = 0.9f;
	private static final float CIRCLE_CAINT_DO_NOTHIN_SCORE = 0.8f;
	
	@Override
	public int eval(IGameState s, int player) {
		ICirclesGameState state = (ICirclesGameState) s;
		
		GameConfig gameConfig = state.getGame().getConfig();
		
		float playerStrength[] = new float[gameConfig.getNumPlayers()];
		float gameStrength = 0f;
//		float strongestOpposition = 0;
		float oppositionStrength = 0f;
		for (int i = 0; i < playerStrength.length; i++) {
			playerStrength[i] = calculatePlayerStrength(state, i);
			gameStrength += playerStrength[i];
			if (i != player) {
//				strongestOpposition = (float) Math.max(strongestOpposition, playerStrength[i]);
				oppositionStrength += playerStrength[i];
			}
		}
		
		return (int) ((playerStrength[player] - oppositionStrength) / gameStrength * MAX_SCORE);
	}

	public float calculatePlayerStrength(ICirclesGameState gs, int playerNum) {
		float strength = 0f;
		IPlayerState ps = gs.getPlayerState(playerNum);
		for (ICircle c : ps.getCircles()) {
			strength += calculateCircleStrength(c, gs);
		}
		return strength;
	}
	
	private float calculateCircleStrength(ICircle circle, ICirclesGameState gs) {
		float strength;
		GameConfig gameConfig = gs.getGame().getConfig();
		if (SplitMove.canBePerformedOn(circle, gameConfig)) {
			strength = CIRCLE_CAN_SPLIT_SCORE;
		} else {
			IPlayerState playerState = gs.getPlayerState(circle.getPlayer().getIndex());
			if (GameStateUtil.circleCanGrowToSplit(circle, gameConfig, playerState)) {
				strength = CIRCLE_CAN_GROW_SCORE;
			} else {
				strength = CIRCLE_CAINT_DO_NOTHIN_SCORE;
			}
		}
		return strength;
	}
	
	@Override
	public int minimum() {
		return -MAX_SCORE;
	}

	@Override
	public int maximum() {
		return MAX_SCORE;
	}

}
