package com.voxoid.bubbliminate.core.rules;

import com.voxoid.bubbliminate.core.Vector2;
import com.voxoid.bubbliminate.core.model.ICircle;
import com.voxoid.bubbliminate.core.model.ICirclesGameState;
import com.voxoid.bubbliminate.core.model.IPlayerState;

public class CirclePointQuery {
	
	private ICircle circle;
	private int player;
	
	public CirclePointQuery(Vector2 point, ICirclesGameState gameState) {
		for (int i = 0; i < gameState.getGame().getNumPlayers(); i++) {
			IPlayerState playerState = gameState.getPlayerState(i);
			for (ICircle circle : playerState.getCircles()) {
				if (circle.getLocation().subtract(point).length() < circle.getRadius()) {
					this.circle = circle;
					this.player = i;
					break;
				}
			}
		}
	}
	
	public ICircle getCircle() {
		return circle;
	}
	
	public int getPlayer() {
		return player;
	}
}
