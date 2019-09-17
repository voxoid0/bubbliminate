package com.voxoid.bubbliminate.core.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.collections15.CollectionUtils;
import org.apache.commons.collections15.Transformer;
import org.apache.commons.collections15.collection.CompositeCollection;
import org.apache.commons.lang.mutable.MutableBoolean;

import com.voxoid.bubbliminate.core.Vector2;
import com.voxoid.bubbliminate.core.rules.GrowMove;
import com.voxoid.bubbliminate.core.rules.MoveMove;
import com.voxoid.bubbliminate.core.rules.SplitMove;

public class GameStateUtil {

	public static List<Vector2> getOpponentCircleLocations(ICirclesGameState gameState, IPlayer player) {
		return getCircleLocations(getOpponentCircles(gameState, player));
	}
	
	public static List<Vector2> getCircleLocations(Collection<ICircle> circles) {
		return (List<Vector2>) CollectionUtils.collect(circles,
				new Transformer<ICircle, Vector2>() {

					@Override
					public Vector2 transform(ICircle input) {
						return input.getLocation();
					}
			});
	}
	
	/**
	 * 
	 * @param gameState
	 * @param player
	 * @return An unmodifiable collection of opponent circles.
	 */
	public static Collection<ICircle> getOpponentCircles(ICirclesGameState gameState, IPlayer player) {
		CompositeCollection<ICircle> circles = new CompositeCollection<ICircle>();
		for (IPlayerState ps : gameState.getPlayerStates()) {
			if (ps.getPlayer() != player) {
				circles.addComposited(ps.getCircles());
			}
		}
		return circles;
	}
	
	public static Collection<ICircle> getAllCircles(ICirclesGameState gameState) {
		return getOpponentCircles(gameState, null);
	}
	
	/**
	 * 
	 * @param circle
	 * @param gameConfig
	 * @param playerState
	 * @return True if the given circle can be grown big enough to split, without colliding with the player's own circles (or the environment boundary)
	 */
	public static boolean circleCanGrowToSplit(ICircle circle, GameConfig gameConfig,
			IPlayerState playerState) {
		
		ICircle closestOwn = CircleUtil.findClosestCircleEdge(circle, playerState.getCircles());
		// TODO: move "can grow/move as much as the number of circles you have" rule into a single location!
		float maxAllowedRadius = GrowMove.calcMaxRadius(gameConfig, playerState, circle);
		float maxBeforeShrinkOthers = closestOwn == null ? maxAllowedRadius :
			closestOwn.getLocation().subtract(circle.getLocation()).length() - closestOwn.getRadius();
		float maxRadius = Math.min(maxBeforeShrinkOthers, maxAllowedRadius);
		ICircle hypotheticalCircle = new Circle(circle.getPlayer(), circle.getLocation(), maxRadius);
		return SplitMove.canBePerformedOn(hypotheticalCircle, gameConfig);
	}
	
	/**
	 * Finds the maximum circle of space in the game state that does not collide
	 * with the given player circles nor the move range of the given opponent
	 * circles. If there are no other circles then the function returns the
	 * maximum radius from the circle's location up to the environment boundary.
	 * 
	 * @param circle
	 * @param playerCircles
	 * @param oppCircles
	 * @param player
	 * @param gameState
	 * @param gameConfig
	 * @return
	 */
	public static float findMaxRadiusAmongOwnCirclesAndOpponentRanges(ICircle circle, Collection<ICircle> playerCircles, Collection<ICircle> oppCircles, IPlayer player, ICirclesGameState gameState, GameConfig gameConfig) {
		
		List<ICircle> constrainingCircles = new ArrayList<ICircle>();
		
		// Add own circles, to avoid shrinking/popping self
		constrainingCircles.addAll(playerCircles);
		
		// Add opponent move ranges (as circles)
		for (ICircle oppCirc : oppCircles) {
			IPlayerState oppState = gameState.getPlayerState(oppCirc.getPlayer().getIndex());
			constrainingCircles.add(new Circle(oppCirc.getPlayer(), oppCirc.getLocation(),
					MoveMove.getMoveRange(oppCirc, oppState)));
		}
		
		ICircle closest = CircleUtil.findClosestCircleEdge(circle, constrainingCircles);
		float maxRadius = closest != null ?
				closest.getLocation().subtract(circle.getLocation()).length() - closest.getRadius() :
				gameConfig.getEnvironmentRadius() - circle.getLocation().length() - circle.getRadius();
		return maxRadius;
	}
	
	
	/**
	 * Tries to find a safe location within the given bounding circle, and
	 * within the environment and the player's moving range, where the given
	 * circle cannot be popped or even shrunk by the given opponent circles, nor
	 * squish the player's own circles. If the circle cannot exist within the
	 * bound without being popped, null is returned.
	 *
	 * TODO: Also consider shrinking own circles without popping them; and even popping them?
	 * 
	 * @param circle
	 *            Circle for which to find a safe location
	 * @param oppCircles
	 *            Opponent circles to watch out for
	 * @param gameState
	 *            {@link ICirclesGameState}
	 * @param gameConfig
	 *            {@link GameConfig}
	 * @param bound
	 *            Bounding circle of allowed locations; the entire circle does
	 *            not have to reside in this; only its location.
	 * @param maxAttempts
	 *            Maximum attempts to find a safe location
	 * @return
	 */
	public static Vector2 findSafeLocWithinBound(ICircle circle, Collection<ICircle> oppCircles, ICirclesGameState gameState, GameConfig gameConfig, ICircle bound, int maxAttempts) {
		Vector2 safeLoc = null;
		float safeRadius = 0f;
		
		ICircle circleBound = new Circle(bound.getPlayer(), bound.getLocation(), bound.getRadius() + circle.getRadius());
		if (circle.getRadius() <= circleBound.getRadius()) {
			IPlayer player = circle.getPlayer();
			IPlayerState playerState = gameState.getPlayerState(player.getIndex());
			float popRadius = gameConfig.getMinCircleRadius();
			
			Collection<ICircle> playerOtherCircles = new ArrayList<ICircle>(playerState.getCircles());
			playerOtherCircles.remove(circle);
			
			
			// First check if the entire bound is unsafe (bound - popRadius is completely encompassed by an opponent circle's range)
			for (ICircle oppCir : oppCircles) {
				IPlayerState oppState = gameState.getPlayerState(oppCir.getPlayer().getIndex());
				if (CircleUtil.isInside(circleBound, MoveMove.getMoveRange(oppCir, oppState) + popRadius*2f, oppCir.getLocation())) {
					return null;
				}
			}
			
			float maxMoveDist = MoveMove.getMaxMoveDist(circle, playerState);
			for (int i = 0; i < maxAttempts; i++) {
				
				// Choose random location within the bound
				Vector2 locWithinBound = CircleUtil.randomLocWithinCircle(bound);
				
				// Limit it to where the circle can be moved by its player
				Vector2 newLoc = locWithinBound.subtract(circle.getLocation()).clamp(maxMoveDist).add(circle.getLocation());
				
				// Limit it to the environment boundary
				newLoc = newLoc.clamp(gameConfig.getEnvironmentRadius() - circle.getRadius());
				
				ICircle newCircle = new Circle(player, newLoc, circle.getRadius());
				
				float maxRadius = GameStateUtil.findMaxRadiusAmongOwnCirclesAndOpponentRanges(
						newCircle, playerOtherCircles, oppCircles, player, gameState, gameConfig);
				if (maxRadius > safeRadius) {
					safeRadius = maxRadius;
					safeLoc = locWithinBound;
					if (maxRadius >= circle.getRadius()) {
						break;
					}
				}
			}
		}
		
		return safeLoc;
	}
	
	/**
	 * Returns true if the given circle is in shrinking or popping range of one or more of its opponents, and
	 * it may be possible for it to escape (no opponent range completely encompasses the circle's move range)
	 * @param circle
	 * @param gameState
	 * @param gameConfig
	 * @return
	 */
	public static boolean inOpponentRangeButCanMaybeEscape(ICircle circle, ICirclesGameState gameState, GameConfig gameConfig) {
		MutableBoolean oppCanShrink = new MutableBoolean();
		MutableBoolean cannotEscape = new MutableBoolean();
		checkOpponentRanges(circle, gameState, gameConfig, oppCanShrink, null, cannotEscape);
		return oppCanShrink.isTrue() && cannotEscape.isFalse();
	}
	
	/**
	 * Flags the given circle in the given game state for whether it can be shrunk and/or popped by one or more
	 * opponents, and whether it can even escape the popping range of all the opponents (by moving).
	 * 
	 * @param circle
	 * @param gameState
	 * @param gameConfig
	 * @param oppCanShrink
	 * @param oppCanPop
	 * @param cannotEscape
	 */
	public static void checkOpponentRanges(ICircle circle, ICirclesGameState gameState, GameConfig gameConfig,
			MutableBoolean oppCanShrink, MutableBoolean oppCanPop, MutableBoolean cannotEscape) {
				
		if (oppCanShrink != null) oppCanShrink.setValue(false);
		if (oppCanPop != null) oppCanPop.setValue(false);
		if (cannotEscape != null) cannotEscape.setValue(false);
		
		IPlayerState playerState = gameState.getPlayerState(circle.getPlayer().getIndex());
		float playerMaxMoveDist = MoveMove.getMaxMoveDist(circle, playerState);
		float poppingRadius = gameConfig.getMinCircleRadius();
		
		for (ICircle oppCirc : getOpponentCircles(gameState, circle.getPlayer())) {
			IPlayerState oppState = gameState.getPlayerState(oppCirc.getPlayer().getIndex());
			float oppDist = MoveMove.getMaxMoveDist(oppCirc, oppState);
			
			if (CircleUtil.edgesAreWithin(oppDist, circle, oppCirc)) {
				if (oppCanShrink != null) oppCanShrink.setValue(true);
				
				float distBetw = circle.getLocation().subtract(oppCirc.getLocation()).length();
				float oppMoveRange = MoveMove.getMoveRange(oppCirc, oppState);
				
				if (distBetw <= oppMoveRange + poppingRadius) {
					if (oppCanPop != null) oppCanPop.setValue(true);
					
					if (distBetw <= oppMoveRange + poppingRadius - playerMaxMoveDist) {
						if (cannotEscape != null) cannotEscape.setValue(true);
					}
				}
			}
		}
	}
	
	/**
	 * Returns true if the given circle can shrink (or pop) any of the given other circles.
	 * 
	 * @param circle
	 * @param others
	 * @return
	 */
	public static boolean canShrinkAny(ICircle circle, Collection<ICircle> others, ICirclesGameState gameState) {
		IPlayerState playerState = gameState.getPlayerState(circle.getPlayer().getIndex());
		float playerDist = MoveMove.getMaxMoveDist(circle, playerState);
		
		for (ICircle other : others) {
			if (CircleUtil.edgesAreWithin(playerDist, circle, other)) {
				return true;
			}
		}
		return false;
	}
		
	
	public static boolean playerCanGrowToSplit(GameConfig gameConfig, IPlayerState playerState) {
		boolean can = false;
		for (ICircle circle : playerState.getCircles()) {
			if (circleCanGrowToSplit(circle, gameConfig, playerState)) {
				can = true;
				break;
			}
		}
		return can;
	}
	
	/**
	 * Gets the circle boundary in which the given popper circle would pop the given "popped" circle, 
	 * given the given game state givenly given.
	 *  
	 * @param popper
	 * @param popped
	 * @param gameState
	 * @return
	 */
	public static ICircle getPopRange(ICircle popper, ICircle popped, ICirclesGameState gameState) {
		ICircle popRange;
		float popRadius = gameState.getGame().getConfig().getMinCircleRadius();
		float maxMoveDist = MoveMove.getMaxMoveDist(popper, gameState.getPlayerState(popper.getPlayer().getIndex()));
		if (!CircleUtil.edgeIsWithin(popper, popRadius + maxMoveDist, popped.getLocation())) {
			popRange = null;
		} else {
			popRange = new Circle(popped.getPlayer(), popped.getLocation(), popRadius + popper.getRadius());
		}
		return popRange;
	}
}
