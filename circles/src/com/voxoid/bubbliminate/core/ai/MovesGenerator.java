package com.voxoid.bubbliminate.core.ai;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.commons.lang.Validate;

import com.voxoid.bubbliminate.core.Angle;
import com.voxoid.bubbliminate.core.Vec2i;
import com.voxoid.bubbliminate.core.Vector2;
import com.voxoid.bubbliminate.core.ai.minimax.IEvaluation;
import com.voxoid.bubbliminate.core.ai.minimax.IGameMove;
import com.voxoid.bubbliminate.core.ai.minimax.IGamePositionEvaluator;
import com.voxoid.bubbliminate.core.ai.minimax.IGameState;
import com.voxoid.bubbliminate.core.ai.minimax.IMovesGenerator;
import com.voxoid.bubbliminate.core.model.Circle;
import com.voxoid.bubbliminate.core.model.CircleUtil;
import com.voxoid.bubbliminate.core.model.GameConfig;
import com.voxoid.bubbliminate.core.model.GameStateUtil;
import com.voxoid.bubbliminate.core.model.ICircle;
import com.voxoid.bubbliminate.core.model.ICirclesGameState;
import com.voxoid.bubbliminate.core.model.IGame;
import com.voxoid.bubbliminate.core.model.IPlayer;
import com.voxoid.bubbliminate.core.model.IPlayerState;
import com.voxoid.bubbliminate.core.rules.GrowMove;
import com.voxoid.bubbliminate.core.rules.MoveMove;
import com.voxoid.bubbliminate.core.rules.SplitMove;
import com.voxoid.bubbliminate.core.util.Pair;

public class MovesGenerator implements IMovesGenerator {

	private static final float SQRT_PI = (float) Math.sqrt(Math.PI);

	private static final int MOVE_IN_ORDER_TO_GROW_ATTEMPTS = 100;

	private static final int MOVE_TO_ESCAPE_ATTEMPTS = 0;
	
	private GameConfig gameConfig;
	private float gameUnitEspilon;
	private float minGrowth;
	private float minMoveDist;
	private int thoroughness1MaxPly = 3;
	private int thoroughness2MaxPly = 4;
	private int penultimatePly;

	private int maxEliminationMoveMoves = 1000;
	private int maxEliminationGrowMoves = 1000;
	private int numSplitAngleSteps = 16;

	
	public MovesGenerator(GameConfig gameConfig, int penultimatePly) {
		this(gameConfig, penultimatePly, 2, 4);
	}	
	
	public MovesGenerator(GameConfig gameConfig, int penultimatePly, int thoroughness1MaxPly, int thoroughness2MaxPly) {
		Validate.notNull(gameConfig);
		this.gameConfig = gameConfig;
		this.gameUnitEspilon = gameConfig.getMinCircleRadius() / 100f;
		this.minGrowth = gameConfig.getMinCircleRadius() / 4f;
		this.minMoveDist = gameConfig.getMinCircleRadius() / 2f;
		this.penultimatePly = penultimatePly;
		this.thoroughness1MaxPly = thoroughness1MaxPly;
		this.thoroughness2MaxPly = thoroughness2MaxPly;
	}
	
	/** For de-serialization only. */
	public MovesGenerator() {}
	
	/**
	 * TODO: base thoroughness on ply
	 */
	@Override
	public List<IGameMove> generateMoves(IGameState state, int playerNum, int ply) {
		List<IGameMove> moves = new ArrayList<IGameMove>();
		
		ICirclesGameState cs = (ICirclesGameState) state;
		IPlayer player = cs.getPlayerState(playerNum).getPlayer();
		
		addEliminationMoves(moves, cs, player, ply, null);
		addPowerIncreaseMoves(moves, cs, player, ply);
		addDefensiveMoves(moves, cs, player, ply);
		if (moves.isEmpty()) pass(moves, cs, player, ply);
		return moves;
	}
	
	
	
	public int getMaxEliminationMoveMoves() {
		return maxEliminationMoveMoves;
	}

	public void setMaxEliminationMoveMoves(int maxEliminationMoveMoves) {
		this.maxEliminationMoveMoves = maxEliminationMoveMoves;
	}

	public int getMaxEliminationGrowMoves() {
		return maxEliminationGrowMoves;
	}

	public void setMaxEliminationGrowMoves(int maxEliminationGrowMoves) {
		this.maxEliminationGrowMoves = maxEliminationGrowMoves;
	}

	public int getNumSplitAngleSteps() {
		return numSplitAngleSteps;
	}

	public void setNumSplitAngleSteps(int numSplitAngleSteps) {
		this.numSplitAngleSteps = numSplitAngleSteps;
	}

	/**
	 * 
	 * @param state
	 * @param player
	 * @return
	 * @throws IllegalStateException If no opponent has any circles
	 */
	public IPlayer findAnOpponentWithCircles(ICirclesGameState state, IPlayer player) {
		for (IPlayerState ps : state.getPlayerStates()) {
			if (ps.getPlayer() != player) {
				if (ps.getNumCircles() > 0) {
					return ps.getPlayer();
				}
			}
		}
		throw new IllegalStateException("No opponent with circles!");
	}
	
	public void pass(List<IGameMove> out, ICirclesGameState state, IPlayer player, int ply) {
		List<ICircle> circles = state.getPlayerState(player.getIndex()).getCircles();
		if (circles.isEmpty()) {
			throw new IllegalStateException("Cannot generate moves for a player with no circles");
		}
		out.add(new GrowMove(gameConfig, state, player, circles.get(0), circles.get(0).getRadius()));
	}
	
	public void addPowerIncreaseMoves(List<IGameMove> out, ICirclesGameState state, IPlayer player, int ply) {
		if (!addSplitMoves(out, state, player, ply)) {
			IPlayerState playerState = state.getPlayerState(player.getIndex());
			
			/*
			 * Since we already (presumably!) added all grow moves up to my
			 * circles' edges, we just check if we have a circle that can be
			 * split without hurting ourselves. If not, we might need to move in
			 * order to have room to grow in order to split
			 */
			if (!GameStateUtil.playerCanGrowToSplit(gameConfig, playerState)) {
				addMoveInOrderToGrowMoves(out, state, player, ply);
			}
		}
	}
	
	public void addEliminationMoves(List<IGameMove> out, ICirclesGameState state, IPlayer player, int ply, IPlayer targetPlayer) {
		addBestEliminationMoveMoves(out, state, player, ply, maxEliminationMoveMoves, targetPlayer);
		addBestEliminationGrowMoves(out, state, player, ply, maxEliminationGrowMoves, targetPlayer);
		
		// TODO: make player-chasing more intelligent: shrink more than one simultaneously?
		for (ICircle circle : state.getPlayerState(player.getIndex()).getCircles()) {
			addShrinkOpponentCircles(out, state, player, circle);
		}
	}
	
	/**
	 * The only defensive, non-aggressive moves are move moves. This method adds moves for each circle that
	 * run away from opponent ranges, if the circle is within range of opponents.
	 * 
	 * @param out
	 * @param state
	 * @param player
	 * @param ply
	 */
	public void addDefensiveMoves(List<IGameMove> out, ICirclesGameState state, IPlayer player, int ply) {
		addEvadeOpponentsInRange(out, state, player);
	}

	private void addEvadeOpponentsInRange(List<IGameMove> out,
			ICirclesGameState state, IPlayer player) {
		//		Collection<ICircle> oppCircs = GameStateUtil.getOpponentCircles(state, player);
//		MutableBoolean oppCanShrink = new MutableBoolean();
//		MutableBoolean oppCanPop = new MutableBoolean();
//		MutableBoolean cannotEscape = new MutableBoolean();
//		IPlayerState playerState = state.getPlayerState(player.getIndex());
		for (ICircle circle : state.getPlayerState(player.getIndex()).getCircles()) {
			
			if (GameStateUtil.inOpponentRangeButCanMaybeEscape(circle, state, gameConfig)) {
				Vector2 loc = findBestMoveToGrowLocation(circle, player, state);
				if (!loc.equals(circle.getLocation())) {
					MoveMove move = new MoveMove(gameConfig, state, player, circle, loc);
					out.add(move);
				}
//				int i = 0;
//				do {
//					Vector2 move = new Vector2(
//							(float) (Math.random() * maxMoveDist * 2.0 - maxMoveDist),
//							(float) (Math.random() * maxMoveDist * 2.0 - maxMoveDist)).clamp(maxMoveDist);
//					ICircle movedCircle = new Circle(circle.getPlayer(), circle.getLocation().add(move), circle.getRadius());
//					GameStateUtil.checkOpponentRanges(movedCircle, state, gameConfig, oppCanShrink, oppCanPop, cannotEscape);
//				} while (i < MOVE_TO_ESCAPE_ATTEMPTS);
			}
		}
	}

	/**
	 * This will add the MoveMoves that eliminate opponent circles, from the most number of opponent circles
	 * to the least. How many of the player's own circles are eliminated is disregarded (that judgment is 
	 * left to the {@link IGamePositionEvaluator} and {@link IEvaluation}).
	 * 
	 * @param out
	 * @param state
	 * @param player
	 * @param ply
	 * @param max
	 */
	public void addBestEliminationMoveMoves(List<IGameMove> out, ICirclesGameState state, IPlayer player, int ply, int max, IPlayer targetPlayer) {
		addBestMultiEliminationMoveMoves(out, state, player, max, targetPlayer);
		addSingleEliminationMoveMoves(out, state, player, true, targetPlayer);
	}

	private void addBestMultiEliminationMoveMoves(List<IGameMove> out,
			ICirclesGameState state, IPlayer player, int max, IPlayer targetPlayer) {
		
		List<ICircle> circles = state.getPlayerState(player.getIndex()).getCircles();
		int nCircles = circles.size();
		float poppingRadius = gameConfig.getMinCircleRadius();
		
		List<Vector2> opponentCircleLocs;
		if (targetPlayer == null) {
			opponentCircleLocs = GameStateUtil.getOpponentCircleLocations(state, player);
		} else {
			opponentCircleLocs = GameStateUtil.getCircleLocations(
					state.getPlayerState(targetPlayer.getIndex()).getCircles());
		}
		
		SortedMap<Integer, Collection<Pair<ICircle, Vector2>>> locByEliminated = CircleUtil.findMaxCollidingCircles(
				circles, nCircles, opponentCircleLocs, poppingRadius, gameUnitEspilon);
		
		// Return the best moves, up to the max number asked for
		int count = 0;
		for (Collection<Pair<ICircle, Vector2>> pairs : locByEliminated.values()) {
			for (Pair<ICircle, Vector2> pair : pairs) {
				MoveMove move = new MoveMove(gameConfig, state, player, pair.left, pair.right);
				out.add(move);
				count++;
				if (count >= max) return;
			}
		}
		// TODO: Add eliminations of one circle
		// TODO: Adjust location to get out of post-range of opponents (post = after circles are eliminated)
	}
	
	/**
	 * Adds MoveMoves that pop a single opponent circle without being in popping range of any opponent circles,
	 * and trying not to be within shrinking range either.
	 * TODO: {@link GamePositionEvaluator} should one way or another score higher based on player's circle sizes, 
	 * so that a move that eliminates an opponent circle without being able to be shrunk is scored higher than
	 * if he can be shrunk.
	 * 
	 * @param out
	 * @param gameState
	 * @param player
	 */
	public void addSingleEliminationMoveMoves(List<IGameMove> out,
			ICirclesGameState gameState, IPlayer player, boolean includeUnsafe, IPlayer targetPlayer) {
		
		List<ICircle> circles = gameState.getPlayerState(player.getIndex()).getCircles();
		
		Collection<ICircle> oppCircles;
		if (targetPlayer == null) {
			oppCircles = GameStateUtil.getOpponentCircles(gameState, player);
		} else {
			oppCircles = gameState.getPlayerState(targetPlayer.getIndex()).getCircles();
		}
		
		List<ICircle> oppCirclesExceptPopped = new ArrayList<ICircle>(oppCircles); // mutable list of opponent circles, for ignoring opponent circle to be popped
		
		for (ICircle circle : circles) {
			for (ICircle oppCirc : oppCircles) {
				ICircle popRange = GameStateUtil.getPopRange(circle, oppCirc, gameState);
				if (popRange != null) {
					oppCirclesExceptPopped.remove(oppCirc);	// (Will be restored) 
					final Vector2 safePopLoc = GameStateUtil.findSafeLocWithinBound(
							circle, oppCirclesExceptPopped, gameState, gameConfig, popRange, 100);
					oppCirclesExceptPopped.add(oppCirc);	// Restore
					
					Vector2 newLoc = safePopLoc;
					if (safePopLoc == null && includeUnsafe) {
						newLoc = oppCirc.getLocation();
					}
					if (newLoc != null) {
						MoveMove move = new MoveMove(gameConfig, gameState, player, circle, newLoc);
						out.add(move);
					}
				}
			}
		}
	}
	
	public void addBestEliminationGrowMoves(List<IGameMove> out, ICirclesGameState state, IPlayer player, int ply, int max, IPlayer targetPlayer) {
		addGrowMoves(out, state, player, ply, targetPlayer);
	}
	
	/**
	 * For each circle, find spaces where we can move in order to grow. The ideal space
	 * is where the circle can be grown to maximum radius without being in range of opponents
	 * or pushing into own circles.
	 * 
	 * @param out
	 * @param state
	 * @param player
	 * @param ply
	 */
	public void addMoveInOrderToGrowMoves(List<IGameMove> out, ICirclesGameState state, IPlayer player, int ply) {
		IGame game = state.getGame();
		
		// For now we'll try random locations within moving range, and keep the best one
		for (ICircle circle : state.getPlayerState(player.getIndex()).getCircles()) {
			Vector2 newLoc = findBestMoveToGrowLocation(circle, player, state);
			if (!newLoc.equals(circle.getLocation()) &&
					enoughRoomFromEdgeToGrowSome(game, circle)) {
				
				MoveMove move = new MoveMove(gameConfig, state, player, circle, newLoc);
				out.add(move);
			}
		}
	}

	private boolean enoughRoomFromEdgeToGrowSome(IGame game, ICircle circle) {
		return CircleUtil.isInside(circle,
				circle.getRadius() > SplitMove.getMinSplitRadius(game.getConfig()) ?
				game.getEnvironmentRadius() - game.getMinCircleRadius()/2f : // At least enough room from edge to grow a noticeable amount
				game.getEnvironmentRadius() - (SplitMove.getMinSplitRadius(game.getConfig()) - circle.getRadius() + gameUnitEspilon), // At least enough room from edge to grow to splittable size 
				Vector2.ZERO);
	}
	

	/**
	 * Finds the location in moving range for the given circle that has the most room for growing without
	 * growing into own circles or into the moving range of opponent circles.
	 * 
	 * @param circle
	 * @param player
	 * @param gameState
	 * @return
	 */
	public Vector2 findBestMoveToGrowLocation(ICircle circle, IPlayer player, ICirclesGameState gameState) {
		float bestRadius = 0f;
		Vector2 bestLoc = circle.getLocation();
		
		IPlayerState playerState = gameState.getPlayerState(player.getIndex());
		float maxMoveDist = MoveMove.getMaxMoveDist(circle, playerState);
		Circle moveBound = new Circle(player, circle.getLocation(), maxMoveDist);
		
		Collection<ICircle> playerOtherCircles = new ArrayList<ICircle>(playerState.getCircles());
		playerOtherCircles.remove(circle);
		
		Collection<ICircle> oppCircles = GameStateUtil.getOpponentCircles(gameState, player);
		
		for (int i = 0; i < MOVE_IN_ORDER_TO_GROW_ATTEMPTS; i++) {
			Vector2 newLoc = CircleUtil.randomLocWithinCircle(moveBound).clamp(
					gameConfig.getEnvironmentRadius() - circle.getRadius());
			ICircle newCircle = new Circle(player, newLoc, circle.getRadius());
			
			float maxRadius = GameStateUtil.findMaxRadiusAmongOwnCirclesAndOpponentRanges(
					newCircle, playerOtherCircles, oppCircles, player, gameState, gameConfig);
			if (maxRadius > bestRadius) {
				bestRadius = maxRadius;
				bestLoc = newLoc;
			}
		}
		
		return bestLoc;
	}
	
	/**
	 * Generates only the moves to find the best reply among all opponents of the movingPlayerNum player.
	 */
	@Override
	public List<IGameMove> generateBestReplyMoves(IGameState state, int movingPlayerNum, int ply) {
		List<IGameMove> moves = new ArrayList<IGameMove>();
		ICirclesGameState cgs = (ICirclesGameState) state;
		IPlayer targetPlayer = cgs.getGame().getPlayers().get(movingPlayerNum);
		if (ply <= thoroughness2MaxPly) {
			moves = new ArrayList<IGameMove>();
			int numPlayers = cgs.getGame().getNumPlayers();
			for (int o = 0; o < numPlayers; o++) {
				if (o != movingPlayerNum && cgs.getPlayerState(o).getNumCircles() > 0) {
					addEliminationMoves(moves, cgs,
							cgs.getGame().getPlayers().get(o),
							ply, targetPlayer);
//					moves.addAll(generateMoves(state, o, ply));
				}
			}
		} else {
			//// Generate only best reply moves (closest circles etc)
			Vector2 home = getAverateLocation(cgs.getPlayerState(movingPlayerNum).getCircles());
//			Collection<ICircle> circles = getOpponentCirclesOrderedByDistance(home, cgs, movingPlayerNum);
			ICircle circle = getClosestOpponentCircle(home, cgs, movingPlayerNum);
			if (circle != null) {
				IPlayer player = circle.getPlayer();
				IPlayerState playerState = cgs.getPlayerState(player.getIndex());
				
				addGrowToMaxRadius(moves, cgs, player, playerState, circle);
				addMoveTowardConcentratedAreas(3, moves, cgs, player, circle);	// TODO: use faster version?
				moves.add(new MoveMove(gameConfig, cgs, player, circle, home));
				
				addSplitMove(moves, cgs, playerState);
			}
		}
		if (moves.isEmpty()) {
			pass(moves, cgs, findAnOpponentWithCircles(cgs, targetPlayer), ply);
		}
		return moves;
	}

	/** Performs a split move on the first circle of the player that is splittable. 
	 * 
	 */
	private void addSplitMove(List<IGameMove> moves, ICirclesGameState cgs,
			IPlayerState playerState) {
		
		Iterator<ICircle> iter = playerState.getCircles().iterator();
		while (iter.hasNext()) {
			ICircle circle = iter.next();
			if (SplitMove.canBePerformedOn(circle, gameConfig)) {
				Angle splitAngle = Angle.fromRadians((float) (Math.random() * Math.PI));
				moves.add(new SplitMove(gameConfig, cgs, circle.getPlayer(), circle, splitAngle));
				break;
			}
		}
	}
	
	public Vector2 getAverateLocation(Collection<ICircle> circles) {
		Vector2 sum = Vector2.ZERO;
		for (ICircle c : circles) {
			sum = sum.add(c.getLocation());
		}
		return sum.divide(circles.size());
	}
	
	public void addGrowMoves(List<IGameMove> out, ICirclesGameState state, IPlayer player, int ply, IPlayer targetPlayer) {
		
		// Maximum radius of each circle
		addGrowToMaxRadii(out, state, player, targetPlayer);
		
		if (ply <= thoroughness1MaxPly) {
			// Maximum radius of each circle up to the edge of the player's other circles - TODO: of closes only?
			addGrowMaxUpToMyCircleEdges(out, state, player, targetPlayer);
			
			addGrowMaxUpToMyCirclePopping(out, state, player, targetPlayer);
		}
	}

	/**
	 * Adds a grow move for each combination of one of the player's circles growing up to the edge
	 * of one of his other circles. Moves are not added for circles whose max radius only reaches
	 * another one of his circle's edge.
	 * 
	 * @param out
	 * @param state
	 * @param player
	 */
	public void addGrowMaxUpToMyCircleEdges(List<IGameMove> out,
			ICirclesGameState state, IPlayer player, IPlayer targetPlayer) {
		
		IPlayerState playerState = state.getPlayerState(player.getIndex());
		
		Collection<ICircle> targetPlayerCircles = targetPlayer == null ? null :
			state.getPlayerState(targetPlayer.getIndex()).getCircles();

		for (ICircle circle : playerState.getCircles()) {
			
			if (targetPlayerCircles == null || GameStateUtil.canShrinkAny(circle, targetPlayerCircles, state)) {
				
				float maxRadius = GrowMove.calcMaxRadius(state.getGame().getConfig(), playerState, circle);
				
				for (ICircle other : playerState.getCircles()) {
					
					if (other != circle) {
						
						if (CircleUtil.edgeIsWithin(other, maxRadius, circle.getLocation())) {
							
							float newRadius = other.getLocation().subtract(circle.getLocation()).length() - other.getRadius();
							
							if (newRadius < maxRadius - gameUnitEspilon && newRadius - circle.getRadius() >= minGrowth) {
								out.add(new GrowMove(gameConfig, state, player, circle, newRadius));
							}
						}
					}
				}
			}
		}
	}

	public void addGrowMaxUpToMyCirclePopping(List<IGameMove> out,
			ICirclesGameState state, IPlayer player, IPlayer targetPlayer) {
		
		IPlayerState playerState = state.getPlayerState(player.getIndex());
		
		Collection<ICircle> targetPlayerCircles = targetPlayer == null ? null :
			state.getPlayerState(targetPlayer.getIndex()).getCircles();
		
		for (ICircle circle : playerState.getCircles()) {
			
			if (targetPlayerCircles == null || GameStateUtil.canShrinkAny(circle, targetPlayerCircles, state)) {
			
				for (ICircle other : playerState.getCircles()) {
					if (other != circle) {
						float newRadius = other.getLocation().subtract(circle.getLocation()).length() - gameConfig.getMinCircleRadius() - gameUnitEspilon;
						float maxRadius = GrowMove.calcMaxRadius(state.getGame().getConfig(),
								playerState, circle);
						
						// Only grow if the new radius is other than the max allowed (which is covered elsewhere),
						// and if it is actually growing
						if (newRadius < maxRadius - gameUnitEspilon &&
								maxRadius > circle.getRadius() + gameUnitEspilon &&
								newRadius - circle.getRadius() >= minGrowth) {
							
							out.add(new GrowMove(gameConfig, state, player, circle, newRadius));
						}
					}
				}
			}
		}
	}
	
	public void addGrowToMaxRadii(List<IGameMove> out, ICirclesGameState state,
			IPlayer player, IPlayer targetPlayer) {

		IPlayerState playerState = state.getPlayerState(player.getIndex());
		
		Collection<ICircle> targetPlayerCircles = targetPlayer == null ? null :
			state.getPlayerState(targetPlayer.getIndex()).getCircles();
		
		for (ICircle circle : playerState.getCircles()) {
			if (targetPlayerCircles == null || GameStateUtil.canShrinkAny(circle, targetPlayerCircles, state)) {
				addGrowToMaxRadius(out, state, player, playerState, circle);
			}
		}
	}

	private void addGrowToMaxRadius(List<IGameMove> out,
			ICirclesGameState state, IPlayer player, IPlayerState playerState,
			ICircle circle) {
		float maxRadius = GrowMove.calcMaxRadius(state.getGame().getConfig(), playerState, circle);
		if (maxRadius > circle.getRadius() + gameUnitEspilon &&
				 maxRadius - circle.getRadius() >= minGrowth) {
			out.add(new GrowMove(gameConfig, state, player, circle,
					maxRadius));
		}
	}
	
	/**
	 * <li>Toward most concentrated area(s)
	 * <li>Toward each other circle
	 * <li>Away from each other circle
	 * 
	 * <li>Being careful not to pop self against boundary.
	 * @param out
	 * @param state
	 * @param player
	 */
	public void addMoveMoves(List<IGameMove> out, ICirclesGameState state, IPlayer player, int ply) {
		if (ply <= thoroughness2MaxPly) {
			for (ICircle circle : state.getPlayerState(player.getIndex()).getCircles()) {
				addMoveTowardConcentratedAreas(5, out, state, player, circle);
			}
			
			if (ply <= thoroughness1MaxPly) {
				for (ICircle circle : state.getPlayerState(player.getIndex()).getCircles()) {
					addChaseOpponentCircles(out, state, player, circle);
				}
				for (ICircle circle : state.getPlayerState(player.getIndex()).getCircles()) {
					addEvadeOpponentCircles(out, state, player, circle);
				}
				for (ICircle circle : state.getPlayerState(player.getIndex()).getCircles()) {
					addAvoidOwnCircles(out, state, player, circle);
				}
			}
		}
	}
	
	public void addMoveTowardConcentratedAreas(int maxMoves, List<IGameMove> out, ICirclesGameState state, IPlayer player, ICircle circle) {
		IPlayerState ps = state.getPlayerState(player.getIndex());
		float cellSize = circle.getRadius() * SQRT_PI;
		float encompassingRadius = circle.getRadius() + ps.getNumCircles();
		int width = (int) Math.ceil(encompassingRadius / cellSize + 0.5f) * 2 + 1;
		int height = width;
		// TODO: clip width/height to environment
		Vector2 offset = new Vector2(width / 2f - circle.getLocation().x * cellSize,
				height / 2f - circle.getLocation().y * cellSize);
		
		CirclesGrid grid = new CirclesGrid(cellSize, width, height, offset);
		grid.addAll(state, circle, player);
		
		// TODO: subtract # player's circles from # opponent's circles
		CirclesGrid playerGrid = new CirclesGrid(cellSize, width, height, offset);
		playerGrid.addAllPlayer(state, circle, player);
		
		Collection<Vec2i> cellByConcentration = getCellsByConcentration(grid, playerGrid);
		
		// Add moves that move to the most concentrated areas.
		// TODO: if a cell is much less concentrated than the one before, skip it and the rest
		Vector2 cellCenter = new Vector2(cellSize / 2f, cellSize / 2f);
		int count = 0;
		for (Vec2i cellLoc : cellByConcentration) {
			count++;
			if (count > maxMoves) break;
			Vector2 loc = grid.getWorldLocForGridLoc(cellLoc);
			out.add(new MoveMove(gameConfig, state, player, circle, loc.add(cellCenter)));
		}
	}
	
	public void addChaseOpponentCircles(List<IGameMove> out, ICirclesGameState state, IPlayer player, ICircle circle) {
		for (IPlayerState ps : state.getPlayerStates()) {
			if (ps.getPlayer() != player) {
				for (ICircle oppCircle : ps.getCircles()) {
					// MoveMove will cap the distance to the max distance the player is allowed to move
					out.add(new MoveMove(gameConfig, state, player, circle, oppCircle.getLocation()));
				}
			}
		}
	}

	public void addShrinkOpponentCircles(List<IGameMove> out, ICirclesGameState gameState, IPlayer player, ICircle circle) {
		IPlayerState playerState = gameState.getPlayerState(player.getIndex());
		float poppingRadius = gameConfig.getMinCircleRadius();
		float maxMoveDist = MoveMove.getMaxMoveDist(circle, playerState);
		for (ICircle oppCircle : GameStateUtil.getOpponentCircles(gameState, player)) {
			float maxReachIntoOppCirc = circle.getLocation().subtract(oppCircle.getLocation()).length() - circle.getRadius() - oppCircle.getRadius();
			
			// If we can shrink him but can't pop him
			if (maxReachIntoOppCirc < oppCircle.getRadius() && maxReachIntoOppCirc > poppingRadius) {
				// MoveMove will cap the distance to the max distance the player is allowed to move
				out.add(new MoveMove(gameConfig, gameState, player, circle, oppCircle.getLocation()));
			}
		}
	}
	
	public void addEvadeOpponentCircles(List<IGameMove> out, ICirclesGameState state, IPlayer player, ICircle circle) {
		for (IPlayerState ps : state.getPlayerStates()) {
			if (ps.getPlayer() != player) {
				for (ICircle oppCircle : ps.getCircles()) {
					// MoveMove will cap the distance to the max distance the player is allowed to move
					MoveMove move = new MoveMove(gameConfig, state, player, circle,
							oppCircle.getLocation().neg());
					if (move.getDistance() >= minMoveDist) {
						out.add(move);
					}
				}
			}
		}
	}
	
	/**
	 * @param out
	 * @param state
	 * @param player
	 * @param circle
	 */
	public void addAvoidOwnCircles(List<IGameMove> out, ICirclesGameState state, IPlayer player, ICircle circle) {
		for (ICircle c : state.getPlayerState(player.getIndex()).getCircles()) {
			if (c != circle) {
				// MoveMove will cap the distance to the max distance the player is allowed to move
				// and will prevent moving outside of boundary
				MoveMove move = new MoveMove(gameConfig, state, player, circle, c.getLocation().neg());
				if (move.getDistance() >= minMoveDist) {
					out.add(move);
				}
			}
		}
	}
	
	/**
	 * Returns the cells in a collection whose iterator iterates ordered by opponent concentration
	 * minus player concentration.
	 * 
	 * TODO: score by nPlayerCirclesPrime / nTotalCircles 
	 * 
	 * @param max
	 * @param width
	 * @param height
	 * @param grid
	 * @param playerGrid
	 * @return
	 */
	public Collection<Vec2i> getCellsByConcentration(CirclesGrid grid, CirclesGrid playerGrid) {
		
		SortedMap<Integer, Vec2i> cellByConcentration = new TreeMap<Integer, Vec2i>(new Comparator<Integer>() {
			@Override
			public int compare(Integer o1, Integer o2) {
				return o1 < o2 ? 1 : (o1 > o2 ? -1 : 0);
			}
		});
		int count = 0;
		for (int y = 0; y < grid.height; y++) {
			for (int x = 0; x < grid.width; x++) {
				List<ICircle> opponentsCell = grid.getItemsIn(x, y);
				List<ICircle> playerCell = playerGrid.getItemsIn(x, y);
				if (opponentsCell.size() > 0) {
					cellByConcentration.put(opponentsCell.size() - playerCell.size(), new Vec2i(x, y));
				}
			}
		}
		return cellByConcentration.values();
	}
	
	/**
	 * Returns the collection of the given player's circles whose iterator iterates in order from closest to furthest from the given home location.
	 * 
	 * @param home
	 * @param state
	 * @param player
	 * @return
	 */
	private Collection<ICircle> getPlayerCirclesOrderedByDistance(Vector2 home, ICirclesGameState state, int player) {
		SortedMap<Float, ICircle> map = new TreeMap<Float, ICircle>();
		for (ICircle circle : state.getPlayerState(player).getCircles()) {
			map.put(circle.getLocation().subtract(home).lengthSquared(), circle);
		}
		return map.values();
	}

	/**
	 * 
	 * @param home
	 * @param state
	 * @param player
	 * @return
	 */
	private Collection<ICircle> getOpponentCirclesOrderedByDistance(Vector2 home, ICirclesGameState state, int playerNum) {
		SortedMap<Float, ICircle> map = new TreeMap<Float, ICircle>();
		IPlayerState playerState = state.getPlayerState(playerNum);
		for (IPlayerState ps : state.getPlayerStates()) {
			if (ps != playerState) {
				for (ICircle circle : ps.getCircles()) {
					map.put(circle.getLocation().subtract(home).lengthSquared(), circle);
				}
			}
		}
		return map.values();
	}
	
	private ICircle getClosestOpponentCircle(Vector2 home, ICirclesGameState state, int playerNum) {
		ICircle closestCircle = null;
		float shortestDist = Float.MAX_VALUE;
		IPlayerState playerState = state.getPlayerState(playerNum);
		for (IPlayerState ps : state.getPlayerStates()) {
			if (ps != playerState) {
				for (ICircle circle : ps.getCircles()) {
					float range = circle.getRadius() + ps.getNumCircles();
					float dist = circle.getLocation().subtract(home).length() - range;
					if (dist < shortestDist) {
						shortestDist = dist;
						closestCircle = circle;
					}
				}
			}
		}
		return closestCircle;
	}

	public boolean addSplitMoves(List<IGameMove> out, ICirclesGameState state, IPlayer player, int ply) {
		boolean added = false;
		float angleStep = (float) Math.PI / (float) numSplitAngleSteps;
		// Don't consider splits in the last ply, because anyone could split and increase their score. What's important in the last ply is who can be eliminated.
//		if (ply < penultimatePly) {
			// TODO: chose smart    -- parallel and perpendicular to closest opponent and closest own
			for (ICircle circle : state.getPlayerState(player.getIndex()).getCircles()) {
				if (SplitMove.canBePerformedOn(circle, gameConfig)) {
					float startAngle = (float) (Math.random() * Math.PI);
					for (float angle = 0f; angle < (float) Math.PI - 0.01; angle += angleStep) {
						Angle splitAngle = Angle.fromRadians(angle + startAngle);
						out.add(new SplitMove(gameConfig, state, player, circle, splitAngle));
					}
					added = true;
				}
			}
//		}
		return added;
	}
}
