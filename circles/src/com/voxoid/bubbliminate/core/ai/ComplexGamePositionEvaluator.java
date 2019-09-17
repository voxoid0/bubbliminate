package com.voxoid.bubbliminate.core.ai;

import org.apache.commons.lang.NotImplementedException;

import com.badlogic.gdx.math.Circle;
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
 * Outcome of equation should be:
 *   * [A] More total area/size is better than less
 *   * Ability to split better than just more area (to a point)
 *   * [A] More circles is better than more area (to a point)
 *   * [A] More space to grow
 *   * [A] There is a point at which more area is better than more circles, but often/usually more bubbles is better than more area
 *   * [A] More area is better than more space
 *   * [A] Better to have one circle in range of a smaller opponent circle than one in range of a larger opponent circle (better to shrink them than to run away sometimes)
 *   * [A?] (With greater than 2-player game) better to let another opponent sacrifice a circle to kill an opponent's circle(s) than to sacrifice your own (unless you are immediately threatened by being in range of them)
 *   * [B] Having opponent in range while they don't have you in range is better than neither having each other in range,
 *       unless it means sacrificing a bubble under circumstances under which sacrificing is not desirable
 *   * [B] Being in range of more opponent circles (is better than being in range of fewer)
 *   * [B] Better to have fewer circles in opponent's range than more (if you don't have theirs in range)
 *   * Include environment edge when considering room to grow
 * 
 * The items marked with [A] are criteria that should be met by calculating the combined area covered by a player's circle ranges (union of range rings, not simple sum of range ring areas).
 * The items marked with [B] should be taken care of by the optimal move search algorithm
 * 
 * TODO:
 * Can reach them but they can't reach me - but that's the same as power - can only happen through power
 * More grow room = higher score
 * More times splittable = higher score
 * More area covered = higher score
 * @author Joel
 *
 */
public class ComplexGamePositionEvaluator implements IGamePositionEvaluator {

	public static final int MAX_SCORE = 1000;

	private transient CirclesAreaCalc circlesAreaCalc;
	private float valueForNumCircles = 0f;
	public boolean normalize = false;
	
	
	public ComplexGamePositionEvaluator(CirclesAreaCalc circlesAreaCalc) {
		this(circlesAreaCalc, 0f);
	}
	/**
	 * 
	 * @param circlesAreaCalc {@link CirclesAreaCalc}; recommended resolution (cellSize) is the minimum circle radius for the game.
	 */
	public ComplexGamePositionEvaluator(CirclesAreaCalc circlesAreaCalc, float valueForNumCircles) {
		this.circlesAreaCalc = circlesAreaCalc;
		this.valueForNumCircles = valueForNumCircles;
	}
	
	/** For de-serialization only. */
	public ComplexGamePositionEvaluator() {}

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
		
		return normalize ? 
				(int) ((playerStrength[player] - oppositionStrength) / gameStrength * MAX_SCORE) :
				(int) (playerStrength[player] - oppositionStrength);
	}

	public float calculatePlayerStrength(ICirclesGameState gs, int playerNum) {
		float strength = 0f;
		
		IPlayerState playerState = gs.getPlayerState(playerNum);
		circlesAreaCalc.setCircles(CircleUtil.rangesToMathCircles(playerState));
		circlesAreaCalc.clipToCircle(new Circle(0f, 0f, gs.getGame().getEnvironmentRadius()));
		float areaStrength = circlesAreaCalc.calculateArea();

		strength += areaStrength + (areaStrength * valueForNumCircles * playerState.getNumCircles());
		
		return strength;
	}
	
	@Override
	public int minimum() {
		throw new NotImplementedException();
	}

	@Override
	public int maximum() {
		throw new NotImplementedException();
	}

}
