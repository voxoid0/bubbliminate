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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
public class SplitMove extends CirclesMove {
	
	private static final float SQRT2 = (float) Math.sqrt(2);
	
	/** Amound by which a circle's radius is divided when split. */
	public static final float NEW_RADIUS_DIVISOR = 2f;
	
	private GameConfig gameConfig;
	private ICircle circle;
	private Angle splitAngle;
	
	public SplitMove(GameConfig gameConfig, ICirclesGameState state, IPlayer player, ICircle circle, Angle splitAngle) {
		super(player);
		this.gameConfig = gameConfig;
		this.circle = circle;
		this.splitAngle = splitAngle;
	}
	
	public static boolean canBePerformedOn(ICircle circle, GameConfig gameConfig) {
		return circle.getRadius() / NEW_RADIUS_DIVISOR >= (gameConfig.getMinCircleRadius() - 0.01f);
	}
	
	public static float getMinSplitRadius(GameConfig gameConfig) {
		return gameConfig.getMinCircleRadius() * NEW_RADIUS_DIVISOR;
	}
	
	/** {@inheritDoc} */
	@Override
	public String describe() {
		return String.format("Split at %s", splitAngle.formatDegrees(1));
	}

	/** {@inheritDoc} */
	@Override
	public ICirclesGameState make(ICirclesGameState old, MutableGameStateDiff diff) {
//		diff.setGame(game);
		diff.setMovedOld(Collections.singleton(circle));
		
		ICirclesGameState newState = new GameState(old, true);
		IPlayerState playerState = newState.getPlayerState(getPlayerNum());
		Vector2 splitLoc = circle.getLocation();
		Angle perpSplit = Angle.fromRadians((float) (splitAngle.getRadians() + (Math.PI / 2.0)));
		float newRadius = circle.getRadius() / NEW_RADIUS_DIVISOR;
		if (newRadius < (gameConfig.getMinCircleRadius() - 0.01f)) {
			throw new IllegalArgumentException("SplitMove cannot be created for circle which would split into circles smaller than minimum radius.");
		}
		
		Vector2 moveVec[] = new Vector2[2];
		Vector2 dirVec[] = new Vector2[2];
		dirVec[0] = perpSplit.toVector2();
		dirVec[1] = dirVec[0].rotate180();
		moveVec[0] = dirVec[0].multiply(newRadius);
		moveVec[1] = dirVec[1].multiply(newRadius);
		
		playerState.removeCircle(circle);
		
		//// Create the two circles, constraining to environment bounds
		List<ICircle> shrunk = new ArrayList<ICircle>();
		List<ICircle> destroyed = new ArrayList<ICircle>();
		List<ICircle> newCircles = new ArrayList<ICircle>();
		
		ICircle alsoIgnore = null;
		for (int i = 0; i < 2; i++) {
			ICircle newCircle = null;
			
			Vector2 newLoc = splitLoc.add(moveVec[i]);
			
			// If the new location pushes against the environment boundary, adjust the circle's radius
			// TODO: If the radius divisor is <= 2 then we can skip this check, if we need to squeeze more performance out of this method
			float maxDistFromEnvCenter = gameConfig.getEnvironmentRadius() - newRadius;
			if (newLoc.lengthSquared() > maxDistFromEnvCenter*maxDistFromEnvCenter) {
				float adjustedRadius = calcRadiusOfCircleBetweenEnvironmentAndA(splitLoc, dirVec[i]);
				Vector2 adjustedLoc = splitLoc.add(dirVec[i].multiply(adjustedRadius));
				if (adjustedRadius >= gameConfig.getMinCircleRadius() - 0.01) {
					newCircle = new Circle(getPlayer(), adjustedLoc, adjustedRadius);
				} else {
					newCircle = null;
				}
			} else {
				newCircle = new Circle(getPlayer(), newLoc, newRadius);
			}
			
			if (newCircle != null) {
				newCircles.add(newCircle);
				playerState.addCircle(newCircle);
				
				MutableGameStateDiff circleDiff = new MutableGameStateDiff();
				
				CircleColliderUtil.assertCircle(newState, newCircle, alsoIgnore, circleDiff);
				alsoIgnore = newCircle;
				
				shrunk.addAll(circleDiff.getShrunk());
				destroyed.addAll(circleDiff.getDestroyed());
			}
		}
		
		diff.setDestroyed(destroyed);
		diff.setShrunk(shrunk);
		diff.setMovedNew(newCircles);
		
		return newState;
	}
	
	private float calcRadiusOfCircleBetweenEnvironmentAndA(Vector2 A, Vector2 B) {
		float R = gameConfig.getEnvironmentRadius();
		float r = -(A.lengthSquared() - R*R) / (2f * (A.dot(B) + R));
		return r;
	}

	/** {@inheritDoc} */
	@Override
	public ICircle getCircle() {
		return circle;
	}

	public Angle getSplitAngle() {
		return splitAngle;
	}

	@Override
	public String toString() {
		return String.format("Player %d Splits %s at %s", getPlayerNum(), circle, splitAngle);
	}

	@Override
	public MoveType getMoveType() {
		return MoveType.Split;
	}

	/** Always false. */
	@Override
	public boolean equals(IMove other, float distEpsilon, float radianEpsilon) {
		return false;
	}
}
