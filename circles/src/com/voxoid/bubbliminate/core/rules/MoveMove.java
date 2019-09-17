/**
 * Copyright (c) 1996-2009 by 21st Century Systems Inc. All rights reserved.
 *
 * Data and materials contained herein are proprietary to 21st Century Systems, Inc.
 * and may contain trade secrets or patented technology.
 *
 * Use is subject to the software license agreement contained in or referred to in
 * this plug-ins about.html file. Please refer any questions to:
 *
 *
 * 21st Century Systems, Inc.
 * 2611 Jefferson Davis Highway, Suite 111000,
 * Arlington, VA 22202
 *
 * $Id$
 */
package com.voxoid.bubbliminate.core.rules;

import java.util.Collections;

import com.voxoid.bubbliminate.core.Angle;
import com.voxoid.bubbliminate.core.Vector2;
import com.voxoid.bubbliminate.core.model.Circle;
import com.voxoid.bubbliminate.core.model.GameConfig;
import com.voxoid.bubbliminate.core.model.GameState;
import com.voxoid.bubbliminate.core.model.ICircle;
import com.voxoid.bubbliminate.core.model.ICirclesGameState;
import com.voxoid.bubbliminate.core.model.IPlayer;
import com.voxoid.bubbliminate.core.model.IPlayerState;
import com.voxoid.bubbliminate.core.model.MutableGameStateDiff;

/**
 * @author jbecker
 *
 */
public class MoveMove extends CirclesMove {

	private ICircle circle;
	private Vector2 newLocation;
	
	public MoveMove(GameConfig gameConfig, ICirclesGameState state, IPlayer player, ICircle circle, Vector2 newLocation) {
		super(player);
		this.circle = circle;
		this.newLocation = newLocation;
		
		validateMove(gameConfig, state, player);
	}
	
	public static float getMaxMoveDist(ICircle circle, IPlayerState playerState) {
		return playerState.getNumCircles();
	}
	public static float getMoveRange(ICircle circle, IPlayerState playerState) {
		return circle.getRadius() + getMaxMoveDist(circle, playerState);
	}

	private void validateMove(GameConfig gameConfig, ICirclesGameState state, IPlayer player) {
		// Keep within allowed range
		Vector2 changeInLocation = newLocation.subtract(circle.getLocation());
		float distSquared = changeInLocation.lengthSquared();
		float maxDist = getMaxMoveDist(circle, state.getPlayerState(player.getIndex()));
		if (distSquared > maxDist*maxDist) {
			newLocation = changeInLocation.normalized().multiply(maxDist).add(circle.getLocation());
		}

		// Keep in environment bounds
		float maxDistFromEnvCenter = gameConfig.getEnvironmentRadius() - circle.getRadius();
		if (newLocation.lengthSquared() > maxDistFromEnvCenter * maxDistFromEnvCenter) {
			float distFromCenter = maxDistFromEnvCenter;
			newLocation = newLocation.normalized().multiply(distFromCenter);
		}
	}
	
	/** {@inheritDoc} */
	@Override
	public String describe() {
		return String.format("Move %.1f at %s", getDistance(), getAngle().formatDegrees(0));
	}

	/** {@inheritDoc} */
	@Override
	public ICirclesGameState make(ICirclesGameState old, MutableGameStateDiff diffOut) {
		ICirclesGameState newState = new GameState(old, true);
		IPlayerState playerState = newState.getPlayerState(getPlayerNum());
		playerState.removeCircle(circle);
		ICircle newCircle = new Circle(getPlayer(), newLocation, circle.getRadius());
		playerState.addCircle(newCircle);
		CircleColliderUtil.assertCircle(newState, newCircle, null, diffOut);
		
		diffOut.setMovedOld(Collections.singleton(circle));
		diffOut.setMovedNew(Collections.singleton(newCircle));
		
		return newState;
	}

	public Vector2 getNewLocation() {
		return newLocation;
	}
	
	public float getDistance() {
		Vector2 deltaLoc = new Vector2(newLocation.x - circle.getLocation().x,
				newLocation.y - circle.getLocation().y);
		return (float) Math.sqrt(deltaLoc.x * deltaLoc.x + deltaLoc.y * deltaLoc.y);
	}
	
	public Angle getAngle() {
		Vector2 deltaLoc = new Vector2(newLocation.x - circle.getLocation().x,
				newLocation.y - circle.getLocation().y);
		return Angle.fromRadians((float) Math.atan2(deltaLoc.y, deltaLoc.x));
	}

	/** {@inheritDoc} */
	@Override
	public ICircle getCircle() {
		return circle;
	}

	@Override
	public String toString() {
		return String.format("Player %d Moves %s to %s", getPlayerNum(), circle, newLocation);
	}

	@Override
	public MoveType getMoveType() {
		return MoveType.Move;
	}
	
	/** Always false. */
	@Override
	public boolean equals(IMove other, float distEpsilon, float radianEpsilon) {
		return false;
	}
	
}
