package com.voxoid.bubbliminate;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.Controllers;
import com.badlogic.gdx.controllers.mappings.Ouya;
import com.badlogic.gdx.math.Vector2;

public class ControllerHelper {

	private float axisAwakeThreshold = 0.5f;

	public Vector2 applyDeadZone(int xAxisCode, int yAxisCode, Controller controller) {
		float x = controller.getAxis(xAxisCode);
		float y = controller.getAxis(yAxisCode);
		return new Vector2(
				Math.abs(x) > axisAwakeThreshold ? x : 0f,
				Math.abs(y) > axisAwakeThreshold ? y : 0f);
	}
	
	
	/**
	 * Finds the first controller/double-axis in the lists that have a direction not in the dead zone,
	 * returning that direction, or {@link Vector2.Zero} if none of the axes/controllers are awake.
	 * 
	 * @param doubleAxes
	 * @param ctls
	 * @return
	 */
	public Vector2 findFirstAwakeDirection(int[] doubleAxes, Iterable<Controller> ctls) {
		for (Controller ctl : ctls) {
			for (int i = 0; i + 1 < doubleAxes.length; i++) {
				Vector2 dir = applyDeadZone(doubleAxes[i], doubleAxes[i+1], ctl);
				if (dir.x + dir.y != 0f) {
					return dir;
				}
			}
		}
		return Vector2.Zero;
	}
	
	/**
	 * Returns true if the given button is down on any controller.
	 * 
	 * @param buttonCode
	 * @return
	 */
	public boolean isDownForAnyController(int buttonCode) {
		for (Controller ctl : Controllers.getControllers()) {
			if (ctl.getButton(buttonCode)) {
				return true;
			}
		}
		return false;
	}
	
	public Vector2 getDpadDirection(Controller ctl) {
		float x = ctl.getButton(Ouya.BUTTON_DPAD_LEFT) ? -1f :
			(ctl.getButton(Ouya.BUTTON_DPAD_RIGHT) ? 1f : 0f);
		float y = ctl.getButton(Ouya.BUTTON_DPAD_DOWN) ? 1f :
			(ctl.getButton(Ouya.BUTTON_DPAD_UP) ? -1f : 0f);
		return new Vector2(x, y);
	}
	
	public Vector2 findFirstDpadDir() {
		for (Controller ctl : Controllers.getControllers()) {
			Vector2 dir = getDpadDirection(ctl);
			if (dir != Vector2.Zero) {
				return dir;
			}
		}
		return Vector2.Zero;
	}
	
	public Vector2 getArrowKeysDir() {
		float x = Gdx.input.isKeyPressed(Input.Keys.LEFT) ? -1f :
			(Gdx.input.isKeyPressed(Input.Keys.RIGHT) ? 1f : 0f);
		float y = Gdx.input.isKeyPressed(Input.Keys.DOWN) ? 1f :
			(Gdx.input.isKeyPressed(Input.Keys.UP) ? -1f : 0f);
		return new Vector2(x, y);		
	}
	
	public Vector2 getDpadAndKeyboardArrowsDir() {
		Vector2 dpadDir = findFirstDpadDir();
		return dpadDir.equals(Vector2.Zero) ? getArrowKeysDir() : dpadDir;
	}
}
