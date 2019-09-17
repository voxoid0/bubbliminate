package com.voxoid.bubbliminate.core.rules;

import java.util.LinkedList;
import java.util.List;

import com.voxoid.bubbliminate.core.model.Circle;
import com.voxoid.bubbliminate.core.model.ICircle;
import com.voxoid.bubbliminate.core.model.ICirclesGameState;
import com.voxoid.bubbliminate.core.model.IPlayerState;
import com.voxoid.bubbliminate.core.model.MutableGameStateDiff;


public class CircleColliderUtil {
	
	public static float COLLISION_EPSILON = 0.001f;
	
	private CircleColliderUtil() {
		// nothing
	}
	
	/**
	 * Asserts the given circles (new) location/size, destroying and shrinking others as necessary,
	 * according to how they collide with it.
	 * 
	 * @param gameState
	 * @param circle
	 * @param diffOut Stores the circles that were shrunk and destroyed, as well as the game. The other diff fields (moved new/old circles) are left untouched.
	 */
	public static void assertCircle(ICirclesGameState gameState, ICircle circle, ICircle alsoIgnore, MutableGameStateDiff diffOut) {
		List<ICircle> destroyed = new LinkedList<ICircle>();
		List<ICircle> shrunk = new LinkedList<ICircle>();
		
		List<ICircle> toAdd = new LinkedList<ICircle>();
		List<ICircle> toRemove = new LinkedList<ICircle>();
		
		for (IPlayerState playerState : gameState.getPlayerStates()) {
			for (ICircle other : playerState.getCircles()) {
				if (other != circle && other != alsoIgnore) {
					float distSqBetw = circle.getLocation().subtract(other.getLocation()).lengthSquared();
					float combinedRadius = circle.getRadius() + other.getRadius();
					if (distSqBetw < combinedRadius*combinedRadius) {
						float distBetw = (float) Math.sqrt(distSqBetw);
						float newRadius = distBetw - circle.getRadius();
						if (newRadius < gameState.getGame().getMinCircleRadius() - COLLISION_EPSILON) {
							
							//// Circle destroyed
//							playerState.removeCircle(other);
							toRemove.add(other);
							
							destroyed.add(other);
						} else {
							
							//// Circle shrunk
//							playerState.removeCircle(other);
							toRemove.add(other);
							toAdd.add(new Circle(playerState.getPlayer(), other.getLocation(), newRadius));
							
							shrunk.add(other);
						}
					}
				}
			}
			
			//// Remove and add circles (now that we're not iterating over them);
			for (ICircle c : toRemove) {
				playerState.removeCircle(c);
			}
			for (ICircle c : toAdd) {
				playerState.addCircle(c);
			}
			
			diffOut.setDestroyed(destroyed); int i = 0;
			diffOut.setShrunk(shrunk);
			
			toRemove.clear();
			toAdd.clear();
		}
	}
}
