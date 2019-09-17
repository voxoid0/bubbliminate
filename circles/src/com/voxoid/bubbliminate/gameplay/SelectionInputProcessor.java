package com.voxoid.bubbliminate.gameplay;

import java.util.Collection;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.Controllers;
import com.badlogic.gdx.controllers.mappings.Ouya;
import com.voxoid.bubbliminate.AllInputAdapter;
import com.voxoid.bubbliminate.ControllerHelper;
import com.voxoid.bubbliminate.ITimeUpdatable;
import com.voxoid.bubbliminate.core.Vector2;
import com.voxoid.bubbliminate.core.model.CircleUtil;
import com.voxoid.bubbliminate.core.model.ICircle;
import com.voxoid.bubbliminate.core.rules.MoveType;


public class SelectionInputProcessor extends AllInputAdapter implements ITimeUpdatable {
	
	private static final float COS60 = (float) Math.cos(Math.PI / 6.0);
	
	private IGameController gameController;
	private ControllerHelper helper = new ControllerHelper();
	
	
	/** True when an axis entered the live zone. */
	private boolean axisAwoke[] = new boolean[10];
	
	private float axisAwakeThreshold = 0.5f;
	
	public SelectionInputProcessor(IGameController gameController) {
		this.gameController = gameController;
	}
	
	@Override
	public void update(float delta) {
		for (Controller ctl : Controllers.getControllers()) {
			for (int axis = 0; axis < 6; axis++) {
				if (Math.abs(ctl.getAxis(axis)) > axisAwakeThreshold && !axisAwoke[axis]) {
					axisAwoke[axis] = true;
					if (axis == Ouya.AXIS_LEFT_X || axis == Ouya.AXIS_LEFT_Y) {
						handleStick(ctl.getAxis(Ouya.AXIS_LEFT_X), ctl.getAxis(Ouya.AXIS_LEFT_Y));
					} else if (axis == Ouya.AXIS_RIGHT_X || axis == Ouya.AXIS_RIGHT_Y) {
						handleStick(ctl.getAxis(Ouya.AXIS_RIGHT_X), ctl.getAxis(Ouya.AXIS_RIGHT_Y));
					}
				} else {
					axisAwoke[axis] = false;
				}
			}
		}
	}
	
	private void handleStick(float x, float y) {
		ICircle currCircle = gameController.getCurrCircle();
		if (currCircle != null) {
			Collection<ICircle> playerCircles = gameController.getGame().getCurPlayerState().getCircles();
			ICircle bestCircle;
			bestCircle = findBestCircle(x, y, currCircle, playerCircles);
			gameController.setCurrCircle(bestCircle);
		}
	}

	/**
	 * Finds the circle that best fits going in the given x/y direction from the given circle's location.
	 * @param x
	 * @param y
	 * @param currCircle
	 * @param playerCircles
	 * @return
	 */
	private ICircle findBestCircle(float x, float y, ICircle currCircle,
			Collection<ICircle> playerCircles) {
		ICircle bestCircle;
		
//		Iterator<ICircle> iter = playerCircles.iterator();
//		ICircle found = null;
//		while (iter.hasNext() && (found = iter.next()) != currCircle);
//		bestCircle = iter.hasNext() ? iter.next() : playerCircles.iterator().next();
		
		if (playerCircles.size() > 1) {
			
			// Order circles by distance
			SortedMap<Float, ICircle> circleByDistance = new TreeMap<Float, ICircle>();
			for (ICircle other : playerCircles) {
				circleByDistance.put(other.getLocation().subtract(currCircle.getLocation()).length(), other);
			}
			
			// Find closest circle within 45 degrees of stick direction
			Vector2 stickDir = new Vector2(x, -y).normalized();
			bestCircle = currCircle;
			for (ICircle other : circleByDistance.values()) {
				if (other != currCircle) {
					Vector2 dirTowardOther = other.getLocation().subtract(currCircle.getLocation()).normalized();
					float cosAngle = stickDir.dot(dirTowardOther);
					if (cosAngle > COS60) {
						bestCircle = other;
						break;
					}
				}
			}
		} else {
			bestCircle = currCircle; // select the circle already selected
		}
		return bestCircle;
	}
	
	private void cycleSelection(int dir) {
		ICircle circle = gameController.getCurrCircle();
		List<ICircle> playerCircles = gameController.getGame().getCurrentState().getPlayerState(circle.getPlayer().getIndex()).getCircles();
		int i = 0;
		while (i < playerCircles.size() && playerCircles.get(i) != circle) i++;
		if (i < playerCircles.size()) {
			i = (i + dir + playerCircles.size()) % playerCircles.size();
			gameController.setCurrCircle(playerCircles.get(i));
		}
	}

	@Override
	public boolean keyDown(int keycode) {
		if (keycode == Input.Keys.S) {
			gameController.cycleSelectionDown();
		} else if (keycode == Input.Keys.A) {
			gameController.cycleSelectionLeft();
		} else if (keycode == Input.Keys.D) {
			gameController.cycleSelectionRight();
		} else if (keycode == Input.Keys.W) {
			gameController.cycleSelectionUp();
		} else if (keycode == Input.Keys.M) {
			gameController.setMoveType(MoveType.Move);
		} else if (keycode == Input.Keys.G) {
			gameController.setMoveType(MoveType.Grow);
		} else if (keycode == Input.Keys.X) {
			gameController.setMoveType(MoveType.Split);
		} else {
			return false;
		}
		return true;
	}

	@Override
	public boolean buttonDown(Controller controller, int buttonCode) {
		if (buttonCode == Ouya.BUTTON_DPAD_DOWN) {
			gameController.cycleSelectionDown();
		} else if (buttonCode == Ouya.BUTTON_DPAD_LEFT) {
			gameController.cycleSelectionLeft();
		} else if (buttonCode == Ouya.BUTTON_DPAD_RIGHT) {
			gameController.cycleSelectionRight();
		} else if (buttonCode == Ouya.BUTTON_DPAD_UP) {
			gameController.cycleSelectionUp();
		} else if (buttonCode == Ouya.BUTTON_U){
			gameController.selectCircle(gameController.getCurrCircle());
			gameController.setMoveType(MoveType.Move);
		} else if (buttonCode == Ouya.BUTTON_Y){
			gameController.selectCircle(gameController.getCurrCircle());
			gameController.setMoveType(MoveType.Grow);
		} else if (buttonCode == Ouya.BUTTON_A){
			gameController.selectCircle(gameController.getCurrCircle());
			gameController.setMoveType(MoveType.Split);
		} else {
			return false;
		}
		return true;
	}
}
