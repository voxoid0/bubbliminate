package com.voxoid.bubbliminate.core.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.commons.collections15.MultiMap;
import org.apache.commons.collections15.multimap.MultiHashMap;
import org.apache.commons.lang.Validate;

import com.voxoid.bubbliminate.core.Vector2;
import com.voxoid.bubbliminate.core.util.Pair;

public class CircleUtil {

	public static boolean edgesAreWithin(float units, ICircle c1, ICircle c2) {
		float combinedRadius = c1.getRadius() + c2.getRadius() + units;
		float distSqBetween = distSqBetweenCenters(c1, c2);
		return distSqBetween < combinedRadius*combinedRadius;
	}
	
	/**
	 * 
	 * @param circle
	 * @param units
	 * @param loc
	 * @return True if the edge of the given circle is within the given units of the given location.
	 */
	public static boolean edgeIsWithin(ICircle circle, float units, Vector2 loc) {
		
		float rangeRadius = circle.getRadius() + units;
		float distSqBetween = circle.getLocation().subtract(loc).lengthSquared();
		return distSqBetween < rangeRadius*rangeRadius;
	}
	
	public static float distBetweenEdges(ICircle c1, ICircle c2) {
		return c1.getLocation().subtract(c2.getLocation()).length() - c1.getRadius() - c2.getRadius(); 
	}
	
	public static float distSqBetweenCenters(ICircle c1, ICircle c2) {
		return c1.getLocation().subtract(c2.getLocation()).lengthSquared();
	}

	/**
	 * Determines if the given circle is inside the given radius of the given location; i.e. its edge does
	 * not go past the radius of the location.
	 * 
	 * @param circle
	 * @param radius
	 * @param location
	 * @return
	 */
	public static boolean isInside(ICircle circle, float radius, Vector2 location) {
		float locBoundRadius = radius - circle.getRadius();
		return circle.getLocation().subtract(location).lengthSquared() < locBoundRadius*locBoundRadius; 
	}
	
	public static boolean isInsideCircle(Vector2 point, ICircle circle) {
		return point.subtract(circle.getLocation()).lengthSquared() < circle.getRadius()*circle.getRadius();
	}
	
	/**
	 * Finds a circle with the same location, radius, and player number.
	 * @param circle
	 * @param circles
	 * @param epsilon
	 * @return
	 */
	public static ICircle findMatchingCircle(ICircle circle, Collection<ICircle> circles, float epsilon) {
		Validate.notNull(circle);
		Validate.noNullElements(circles);
		for (ICircle other : circles) {
			if (Math.abs(other.getLocation().x - circle.getLocation().x) <= epsilon &&
					Math.abs(other.getLocation().y - circle.getLocation().y) <= epsilon &&
					Math.abs(other.getRadius() - circle.getRadius()) <= epsilon &&
					other.getPlayer().getIndex() == circle.getPlayer().getIndex()) {
				return other;
			}
		}
		return null;
	}

	/**
	 * 
	 * @param circles
	 * @param maxTravelDist
	 * @param otherLocs
	 * @param othersRadius
	 * @param epsilon
	 * @return {@link SortedMap} of the number of circles eliminated to all
	 *         locations that eliminate that many circles, in descending order,
	 *         but the numbers are negative so that the natural integer ordering
	 *         makes it descending order.
	 */
	public static SortedMap<Integer, Collection<Pair<ICircle, Vector2>>> findMaxCollidingCircles(Collection<ICircle> circles, float maxTravelDist,
			Collection<Vector2> otherLocs, float othersRadius, float epsilon) {
		
		MultiMap<Integer, Pair<ICircle, Vector2>> accum = new MultiHashMap<Integer, Pair<ICircle, Vector2>>();
		for (ICircle circle : circles) {
			MultiMap<Integer, Vector2> locs = findMaxCollidingCirclesMultimap(
					circle, maxTravelDist, otherLocs, othersRadius, epsilon);
			for (Map.Entry<Integer, Collection<Vector2>> e : locs.entrySet()) {
				for (Vector2 loc : e.getValue()) {
					accum.put(e.getKey(), new Pair(circle, loc));
				}
			}
		}
		return new TreeMap<Integer, Collection<Pair<ICircle, Vector2>>>(accum.map());
	}
	
	public static SortedMap<Integer, Collection<Vector2>> findMaxCollidingCircles(ICircle circle, float maxTravelDist,
			Collection<Vector2> otherLocs, float othersRadius, float epsilon) {
		
		return new TreeMap<Integer, Collection<Vector2>>(
				findMaxCollidingCirclesMultimap(circle, maxTravelDist, otherLocs, othersRadius, epsilon)
				.map());
	}
	
	private static MultiMap<Integer, Vector2> findMaxCollidingCirclesMultimap(ICircle circle, float maxTravelDist,
			Collection<Vector2> otherLocs, float othersRadius, float epsilon) {
	
		// Map maps the negative number of circles eliminated (to be able to use ascending order)
		// to the location that accomplishes the elimination
		MultiMap<Integer, Vector2> newLocBestFirst = new MultiHashMap<Integer, Vector2>();
		
		// epsilon-ize the circle's radius to ensure touching
		float touchRadius = circle.getRadius() - epsilon;
		
		// Circle which encompasses the area that the circle can reach via maxTravelDist
		Circle rangeCircle = new Circle(Player.NONE, circle.getLocation(), circle.getRadius() + maxTravelDist);
		
		/*
		 * For each pair of opponent circles that can be simultaneously touched by the main circle,
		 * within the given max travel distance from the circle's current location,
		 * calculate how many circles in addition to the touch are touched when the circle is touched
		 * against the two circles both ways (a circle can touch two other circles in two ways).
		 */
		Vector2[] otherLocsArray = new ArrayList<Vector2>(otherLocs).toArray(new Vector2[0]);
		for (int i = 0; i < otherLocsArray.length; i++) {
			Vector2 other1 = otherLocsArray[i];
			Circle other1Circle = new Circle(Player.NONE, other1, othersRadius);
			
			if (edgesAreWithin(-epsilon, rangeCircle, other1Circle)) {
				for (int j = i+1; j < otherLocsArray.length; j++) {
					Vector2 other2 = otherLocsArray[j];
					Circle other2Circle = new Circle(Player.NONE, other2, othersRadius);
					if (edgesAreWithin(-epsilon, rangeCircle, other2Circle)) {
					
						Vector2[] touchLocs = findTouchingLocations(touchRadius,
								other1Circle,
								other2Circle);
						for (Vector2 touchLoc : touchLocs) {
							Circle touchCircle = new Circle(Player.NONE, touchLoc, touchRadius);
							
							if (isInside(touchCircle, rangeCircle.getRadius(), rangeCircle.getLocation())) {
								// Find how many other circles are popped with these two
								int touchCount = 2;
								for (Vector2 another : otherLocsArray) {
									if (another != other1 && another != other2) {
										if (edgesAreWithin(-epsilon,
												touchCircle,
												new Circle(Player.NONE, another, othersRadius))) {
											touchCount++;
										}
									}
								}
							
								newLocBestFirst.put(-touchCount, touchLoc);
							}
						}
					}
				}
			}
		}
		
		return newLocBestFirst;
	}
	
	/**
	 * Finds zero or two locations for a circle of the given radius whose edge touches the edges
	 * of the other two given circles. Zero if the two circles are too far apart for a circle
	 * of the given radius to touch them both at the same time.
	 * 
	 * @param radius
	 * @param other1
	 * @param other2
	 * @return
	 */
	public static Vector2[] findTouchingLocations(float radius, ICircle other1, ICircle other2) {
//	}
//	public static Vector2[] findTouchingLocations(float radius, Vector2 other1Loc, float other1Radius,
//			Vector2 other2Loc, float other2Radius) {
	
		Vector2[] answer;
		if (!edgesAreWithin(radius * 2f, other1, other2)) {
			answer = new Vector2[0];
		} else {
			answer = new Vector2[2];
			
			/*
			 * Let C be the touching location, A and B be other1 and other2.
			 * Let a, b, and c be the length of each side of the triangle opposite of
			 * circle/location/angle A, B, and C
			 */
			// Step 1: Find angle C (using law of cosines)
			Vector2 A = other1.getLocation();
			Vector2 B = other2.getLocation();
			float a = radius + other2.getRadius();
			float b = radius + other1.getRadius();
			float c = A.subtract(B).length();
//			float cSquared = distSqBetweenCenters(other1, other2);
			float cosC = (a*a + b*b - c*c) / (2f * a * b);
			float angleC = (float) Math.acos(cosC);
			
			// Step 2: Find angle A (using law of sines)
//			float c = (float) Math.sqrt(cSquared);
			float angleA = (float) Math.asin(a/c * (float) Math.sin(angleC));
			
			// Step 3: Find location of C using vector math and trigonometry
			Vector2 dirFromAToB = B.subtract(A).normalized();
			Vector2 D = A.add(dirFromAToB.multiply(b * (float) Math.cos(angleA)));
			Vector2 C = D.add(dirFromAToB.rotate90().multiply(b * (float) Math.sin(angleA)));
			answer[0] = C;
			answer[1] = D.subtract(C.subtract(D));
		}
		return answer;
	}
	
	/**
	 * 
	 * @param circle
	 * @param others
	 * @return Closest other circle, or null if others collection is empty
	 */
	public static ICircle findClosestCircleEdge(ICircle circle, Collection<ICircle> others) {
		ICircle closest = null;
		float closestDistBetwEdges = Float.MAX_VALUE;
		
		for (ICircle other : others) {
			if (other != circle) {
				float dist = circle.getLocation().subtract(other.getLocation()).length();
				float distBetwEdges = dist - circle.getRadius() - other.getRadius();
				if (distBetwEdges < closestDistBetwEdges) {
					closestDistBetwEdges = distBetwEdges;
					closest = other;
				}
			}
		}
		return closest;
	}

	/**
	 * 
	 * @param circle
	 * @param others
	 * @return Closest other circle, or null if others collection is empty
	 */
	public static ICircle findClosestCircleEdge(Vector2 point, Collection<ICircle> circles) {
		ICircle closest = null;
		float closestDistFromEdge = Float.MAX_VALUE;
		
		for (ICircle circle : circles) {
			float dist = point.subtract(circle.getLocation()).length() - circle.getRadius();
			if (dist < closestDistFromEdge) {
				closestDistFromEdge = dist;
				closest = circle;
			}
		}
		return closest;
	}

	public static ICircle findClosestCircleEdgeWithinRange(Vector2 point, float distFromEdge, Collection<ICircle> circles) {
		ICircle circle = findClosestCircleEdge(point, circles);
		return circle.getLocation().subtract(point).length() - circle.getRadius() <= distFromEdge ?
				circle : null;
	}
	
	public static Vector2 randomLocWithinCircle(ICircle bound) {
		Vector2 locWithinBound = new Vector2(
				(float) (Math.random() * bound.getRadius() * 2.0 - bound.getRadius()),
				(float) (Math.random() * bound.getRadius() * 2.0 - bound.getRadius()));
		locWithinBound = locWithinBound.clamp(bound.getRadius()).add(bound.getLocation());
		return locWithinBound;
	}
	
	public static ICircle findLeftmost(Collection<ICircle> circles) {
		ICircle ret = null;
		float extremePos = Float.MAX_VALUE;
		for (ICircle circle : circles) {
			if (circle.getLocation().x < extremePos) {
				extremePos = circle.getLocation().x;
				ret = circle;
			}
		}
		return ret;
	}

	public static ICircle findBottommost(Collection<ICircle> circles) {
		ICircle ret = null;
		float extremePos = Float.MAX_VALUE;
		for (ICircle circle : circles) {
			if (circle.getLocation().y < extremePos) {
				extremePos = circle.getLocation().y;
				ret = circle;
			}
		}
		return ret;
	}
	
	public static ICircle findRightmost(Collection<ICircle> circles) {
		ICircle ret = null;
		float extremePos = Float.NEGATIVE_INFINITY;
		for (ICircle circle : circles) {
			if (circle.getLocation().x > extremePos) {
				extremePos = circle.getLocation().x;
				ret = circle;
			}
		}
		return ret;
	}
	
	public static ICircle findTopmost(Collection<ICircle> circles) {
		ICircle ret = null;
		float extremePos = Float.NEGATIVE_INFINITY;
		for (ICircle circle : circles) {
			if (circle.getLocation().y > extremePos) {
				extremePos = circle.getLocation().y;
				ret = circle;
			}
		}
		return ret;
	}
	
	public static ICircle findNextLeft(ICircle currCircle, Collection<ICircle> circles) {
		ICircle ret = null;
		if (currCircle == null) {
			if (!circles.isEmpty()) {
				ret = circles.iterator().next();
			}
		} else {
			float shortestDist = Float.MAX_VALUE;
			for (ICircle circle : circles) {
				float dist = Math.abs(currCircle.getLocation().x - circle.getLocation().x);
				if (circle != currCircle && circle.getLocation().x < currCircle.getLocation().x &&
						dist < shortestDist) {
					shortestDist = dist;
					ret = circle;
				}
			}
			if (ret == null) { 
				ret = findRightmost(circles);
			}
		}
		return ret;
	}
	
	
	public static ICircle findNextBelow(ICircle currCircle, Collection<ICircle> circles) {
		ICircle ret = null;
		float shortestDist = Float.MAX_VALUE;
		if (currCircle == null) {
			if (!circles.isEmpty()) {
				ret = circles.iterator().next();
			}
		} else {
			for (ICircle circle : circles) {
				float dist = Math.abs(currCircle.getLocation().y - circle.getLocation().y);
				if (circle != currCircle && circle.getLocation().y < currCircle.getLocation().y &&
						dist < shortestDist) {
					shortestDist = dist;
					ret = circle;
				}
			}
			if (ret == null) {
				ret = findTopmost(circles);
			}
		}
		return ret;
	}

	public static ICircle findNextRight(ICircle currCircle, Collection<ICircle> circles) {
		ICircle ret = null;
		if (currCircle == null) {
			if (!circles.isEmpty()) {
				ret = circles.iterator().next();
			}
		} else {
			float shortestDist = Float.MAX_VALUE;
			for (ICircle circle : circles) {
				float dist = Math.abs(circle.getLocation().x - currCircle.getLocation().x);
				if (circle != currCircle && circle.getLocation().x > currCircle.getLocation().x &&
						dist < shortestDist) {
					shortestDist = dist;
					ret = circle;
				}
			}
			if (ret == null) {
				ret = findLeftmost(circles);
			}
		}
		return ret;
	}

	public static ICircle findNextAbove(ICircle currCircle, Collection<ICircle> circles) {
		ICircle ret = null;
		if (currCircle == null) {
			if (!circles.isEmpty()) {
				ret = circles.iterator().next();
			}
		} else {
			float shortestDist = Float.MAX_VALUE;
			for (ICircle circle : circles) {
				float dist = Math.abs(circle.getLocation().y - currCircle.getLocation().y);
				if (circle != currCircle && circle.getLocation().y > currCircle.getLocation().y &&
						dist < shortestDist) {
					shortestDist = dist;
					ret = circle;
				}
			}
			if (ret == null) {
				ret = findBottommost(circles);
			}
		}
		return ret;
	}
	
	public static List<com.badlogic.gdx.math.Circle> toMathCircles(List<ICircle> icircles) {
		List<com.badlogic.gdx.math.Circle> circles = new ArrayList<com.badlogic.gdx.math.Circle>();
		for (ICircle c : icircles) {
			circles.add(new com.badlogic.gdx.math.Circle(c.getLocation().x, c.getLocation().y, c.getRadius()));
		}
		return circles;
	}
	
	public static List<com.badlogic.gdx.math.Circle> rangesToMathCircles(IPlayerState playerState) {
		List<com.badlogic.gdx.math.Circle> circles = new ArrayList<com.badlogic.gdx.math.Circle>();
		for (ICircle c : playerState.getCircles()) {
			float rangeRadius = c.getRadius() + playerState.getNumCircles();
			circles.add(new com.badlogic.gdx.math.Circle(c.getLocation().x, c.getLocation().y, rangeRadius));
		}
		return circles;
	}
}
