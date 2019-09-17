package com.voxoid.bubbliminate.core.ai;

import com.voxoid.bubbliminate.core.Vec2i;
import com.voxoid.bubbliminate.core.Vector2;
import com.voxoid.bubbliminate.core.model.ICircle;
import com.voxoid.bubbliminate.core.model.ICirclesGameState;
import com.voxoid.bubbliminate.core.model.IPlayer;
import com.voxoid.bubbliminate.core.model.IPlayerState;

/**
 * 
 * @author Joel
 *
 */
public class CirclesGrid extends ItemGrid<ICircle> {
	private Vector2 offset;
	private float cellSize;
	
	/**
	 * 
	 * @param cellSize
	 * @param width
	 * @param height
	 * @param offset Offset in grid units of world origin (0,0).
	 */
	public CirclesGrid(float cellSize, int width, int height, Vector2 offset) {
		super(width, height);
		this.offset = offset;
		this.cellSize = cellSize;
	}
	
	public void add(ICircle circle) {
		Vec2i gridLoc = getGridLocFor(circle.getLocation());
		if (gridLoc.x >= 0 && gridLoc.y >= 0 && gridLoc.x < getWidth() && gridLoc.y < getHeight()) {
			add(gridLoc.x, gridLoc.y, circle);
		}
	}
	
	/**
	 * Adds all circles in the given game state, except for the given exceptCircle or those belonging
	 * to the given exceptPlayer.
	 * 
	 * @param state
	 * @param exceptCircle
	 * @param exceptPlayer
	 */
	public void addAll(ICirclesGameState state, ICircle exceptCircle, IPlayer exceptPlayer) {
		for (IPlayerState ps : state.getPlayerStates()) {
			if (ps.getPlayer() != exceptPlayer) {
				for (ICircle c : ps.getCircles()) {
					if (c != exceptCircle) {
						add(c);
					}
				}
			}
		}
	}

	public void addAllPlayer(ICirclesGameState state, ICircle exceptCircle, IPlayer player) {
		IPlayerState ps = state.getPlayerState(player.getIndex());
		for (ICircle c : ps.getCircles()) {
			if (c != exceptCircle) {
				add(c);
			}
		}
	}
	
	public Vec2i getGridLocFor(Vector2 worldLoc) {
		int x = (int) (worldLoc.x / cellSize + offset.x);
		int y = (int) (worldLoc.y / cellSize + offset.y);
		return new Vec2i(x, y);
	}
	
	public Vector2 getWorldLocForGridLoc(Vec2i gridLoc) {
		return new Vector2((gridLoc.x - offset.x) * cellSize, (gridLoc.y - offset.y) * cellSize);
	}
	
	
}
