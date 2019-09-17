package com.voxoid.bubbliminate.gameplay;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.voxoid.bubbliminate.AllInputAdapter;
import com.voxoid.bubbliminate.CirclesGlobal;
import com.voxoid.bubbliminate.ITimeUpdatable;
import com.voxoid.bubbliminate.Vector2Util;
import com.voxoid.bubbliminate.core.model.CircleUtil;
import com.voxoid.bubbliminate.core.model.ICircle;
import com.voxoid.bubbliminate.core.rules.MoveType;
import com.voxoid.bubbliminate.core.rules.SplitMove;

public class TouchMoveInputProcessing extends AllInputAdapter implements ITimeUpdatable {

	private IGameController gameController;
	private GameViewTouch view;
	private Vector2 splitSwipeStart = null;	// world coords
	private Vector2 prevPoint; // world coords
	private float circleEdgeThresh = 0.75f;
	private float maxPixelsFromEdgeForGrow = CirclesGlobal.platform.inchesToPixels(0.25f);
	private MoveType moveTypeInProgress = null;
	private Vector2 moveOffset;
	private float growRadiusOffset;
	private boolean dragStartedOutsideOfCircle;
	private Vector2 pointWhereDragEnteredCircle;
	private boolean twoFingersInvolved = false;
	
	
	public TouchMoveInputProcessing(IGameController gameController,
			GameViewTouch view) {
		super();
		this.gameController = gameController;
		this.view = view;
	}

	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		// Reset move state with new touch
		moveTypeInProgress = null;
		gameController.setCurrCircle(null);
		pointWhereDragEnteredCircle = null;
		
		if (pointer > 0) {
			Gdx.app.log("2fingers", "TouchDown: pointer=" + pointer + "    button=" + button);
			twoFingersInvolved = true;
			return false;
		} 
		
		System.out.println("Down: pointer=" + pointer + "    button=" + button);
		
		Vector2 world = view.screenToWorld(new Vector2(screenX, screenY));
		splitSwipeStart = world.cpy();
		prevPoint = world;
		float maxWorldUnitsFromEdgeForGrow = maxPixelsFromEdgeForGrow / CirclesGlobal.gameEnvirTransform.getScaleX();
		ICircle circle = CircleUtil.findClosestCircleEdgeWithinRange(
				Vector2Util.toCoreVector2(world),
				maxWorldUnitsFromEdgeForGrow,
				gameController.getGame().getCurrentState().getPlayerState(gameController.getCurrPlayer().getIndex()).getCircles());
		if (circle != null) {
			gameController.setCurrCircle(circle);
			dragStartedOutsideOfCircle = !CircleUtil.isInsideCircle(Vector2Util.toCoreVector2(world), circle);
			
			moveOffset = world.cpy().sub(Vector2Util.fromCoreVector2(circle.getLocation()));
			float touchRadius = moveOffset.len();
			if (touchRadius / circle.getRadius() > circleEdgeThresh) {
				moveTypeInProgress = MoveType.Grow;
				gameController.setMoveType(MoveType.Grow);
				growRadiusOffset = circle.getRadius() - touchRadius;
				//gameController.setControlPoint(world);
				touchDragged(screenX, screenY, pointer);
			} else {
				moveTypeInProgress = MoveType.Move;
				gameController.setMoveType(MoveType.Move);
				//gameController.setControlPoint(Vector2Util.fromCoreVector2(circle.getLocation()));
				touchDragged(screenX, screenY, pointer);
			}
		} else {
			moveTypeInProgress = MoveType.Split;
			splitSwipeStart = world.cpy();
			gameController.setCurrCircle(null);
		}
		return false; // Allow other things like buttons to handle the touch too
	}

	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		boolean handled;
		if (moveTypeInProgress != null && gameController.getCurrCircle() != null && !twoFingersInvolved) {
			if (moveTypeInProgress == MoveType.Split && !SplitMove.canBePerformedOn(gameController.getCurrCircle(), gameController.getGame().getConfig())) {
				gameController.handleCannotSplit();
			} else {
				gameController.confirmMove();
			}
			handled = true;
		} else {
		    handled = false;
		}
		twoFingersInvolved = false;
		return handled;
	}

	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) {
		
		if (pointer > 0) {
			twoFingersInvolved = true;
		}
		if (twoFingersInvolved) return false;

		Vector2 world = view.screenToWorld(new Vector2(screenX, screenY));
		if (moveTypeInProgress == MoveType.Split) {
			if (gameController.getCurrCircle() == null) {
				System.out.println(world.toString());
				ICircle circle = gameController.getGame().getCurrentState().circleAt(Vector2Util.toCoreVector2(world));
	
				// Split swipe has entered one of the player's circles
				if (circle != null && circle.getPlayer() == gameController.getCurrPlayer()) {
					completeSplit(world, circle);
				}
			} else {
				// TODO: wait until touch has penetrated to or past center of circle before splitting
			}
		} else if (gameController.getCurrCircle() != null) {
			Vector2 fromCircle = world.cpy().sub(Vector2Util.fromCoreVector2(gameController.getCurrCircle().getLocation()));
			
			// Move control point for Move or Grow
			if (moveTypeInProgress == MoveType.Move) {
				gameController.setControlPoint(fromCircle.cpy().sub(moveOffset));
			} else { // Grow
				
				// Save point where/when drag entered circle
				if (!CircleUtil.isInsideCircle(
						Vector2Util.toCoreVector2(world),
						gameController.getCurrCircle())) {
					pointWhereDragEnteredCircle = null;
				} else {
					if (pointWhereDragEnteredCircle == null) {
						pointWhereDragEnteredCircle = world.cpy();
					}
				}
				
				// Check if turned into a split
				if (dragStartedOutsideOfCircle && dragWentPastCircleCenter(world) &&
						CircleUtil.isInsideCircle(
							Vector2Util.toCoreVector2(world),
							gameController.getCurrCircle())) {
					
					completeSplit(pointWhereDragEnteredCircle, gameController.getCurrCircle());
					touchUp(screenX, screenY, pointer, 0);
				} else {
					// Continue grow adjustments
					float distFromCenter = fromCircle.len();
					gameController.setControlPoint(new Vector2(0f, distFromCenter + growRadiusOffset));
				}
			}
			prevPoint = world;
		}
		return true;
	}

	private boolean dragWentPastCircleCenter(Vector2 world) {
		float cos = splitSwipeStart.cpy().sub(world).dot(
				Vector2Util.fromCoreVector2(gameController.getCurrCircle().getLocation()).sub(world));
		return cos < 0;
	}

	private void completeSplit(Vector2 world, ICircle circle) {
		gameController.setCurrCircle(circle);
		// TODO: set split initial control point here?
		gameController.setMoveType(MoveType.Split);
		gameController.setControlPoint(splitSwipeStart.cpy().sub(world));
		//gameController.confirmMove();
	}

	@Override
	public void update(float deltaTime) {
		
	}

	
}
