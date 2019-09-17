package com.voxoid.bubbliminate.core.rules;

import java.util.Collections;

import com.voxoid.bubbliminate.core.model.Circle;
import com.voxoid.bubbliminate.core.model.GameConfig;
import com.voxoid.bubbliminate.core.model.GameState;
import com.voxoid.bubbliminate.core.model.ICircle;
import com.voxoid.bubbliminate.core.model.ICirclesGameState;
import com.voxoid.bubbliminate.core.model.IPlayer;
import com.voxoid.bubbliminate.core.model.IPlayerState;
import com.voxoid.bubbliminate.core.model.MutableGameStateDiff;

public class GrowMove extends CirclesMove {

	private ICircle circle;
	private float newRadius;
	
	public GrowMove(GameConfig gameConfig, ICirclesGameState state, IPlayer player, ICircle circle, float newRadius) {
		super(player);
		this.circle = circle;
		this.newRadius = newRadius;
		
		//// Validate move
		IPlayerState playerState = state.getPlayerState(player.getIndex());
		this.newRadius = Math.min(									// Can't exceed max radius
				Math.max(newRadius, circle.getRadius()),			// Can't shrink yourself either
				calcMaxRadius(gameConfig, playerState, circle));
//		if (this.newRadius < gameConfig.getMinCircleRadius()) {
//			this.newRadius = gameConfig.getMinCircleRadius();
//		}
	}

	/**
	 * Calculates the maximum radius to which the given circle is allowed to grow, given the player's state.
	 * The radius is also bounded to the environment boundary.
	 * 
	 * @param gameConfig
	 * @param playerState
	 * @param circle
	 * @return
	 */
	public static float calcMaxRadius(GameConfig gameConfig, IPlayerState playerState, ICircle circle) {
		float newRadius = circle.getRadius() + playerState.getNumCircles();
		float maxLocRadius = gameConfig.getEnvironmentRadius() - newRadius;
		if (circle.getLocation().lengthSquared() >= maxLocRadius*maxLocRadius ||
				newRadius > gameConfig.getEnvironmentRadius()) {
			
			newRadius = gameConfig.getEnvironmentRadius() - circle.getLocation().length();
		}
		return newRadius;
	}
	
	/** {@inheritDoc} */
	@Override
	public String describe() {
		return String.format("Grow by %.1f", newRadius - circle.getRadius());
	}

	/** {@inheritDoc} */
	@Override
	public ICirclesGameState make(ICirclesGameState old, MutableGameStateDiff diffOut) {
		ICirclesGameState newState = new GameState(old, true);
		IPlayerState playerState = newState.getPlayerState(getPlayerNum());
		playerState.removeCircle(circle);
		ICircle newCircle = new Circle(getPlayer(), circle.getLocation(), newRadius);
		playerState.addCircle(newCircle);
		CircleColliderUtil.assertCircle(newState, newCircle, null, diffOut);
		
		diffOut.setMovedOld(Collections.singleton(circle));
		diffOut.setMovedNew(Collections.singleton(newCircle));
		
		return newState;
	}

	/** {@inheritDoc} */
	@Override
	public ICircle getCircle() {
		return circle;
	}
	
	public float getNewRadius() {
		return newRadius;
	}

	@Override
	public String toString() {
		return String.format("Player %d Grows %s to radius %.2f", getPlayerNum(), circle, newRadius);
	}

	@Override
	public MoveType getMoveType() {
		return MoveType.Grow;
	}
	
	@Override
	public boolean equals(IMove other, float distEpsilon, float radianEpsilon) {
		if (other == null) {
			return false;
		}
		if (!(other instanceof GrowMove)) {
			return false;
		}
		if (Math.abs(((GrowMove) other).getNewRadius() - getNewRadius()) > distEpsilon) {
			return false;
		}
		return true;
	}
}
