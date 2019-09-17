package com.voxoid.bubbliminate.gameplay;

import org.apache.commons.lang.Validate;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.Controllers;
import com.badlogic.gdx.controllers.mappings.Ouya;
import com.badlogic.gdx.math.Vector2;
import com.voxoid.bubbliminate.AllInputAdapter;
import com.voxoid.bubbliminate.ControllerHelper;
import com.voxoid.bubbliminate.ITimeUpdatable;
import com.voxoid.bubbliminate.Vector2Util;
import com.voxoid.bubbliminate.core.PolarCoords;
import com.voxoid.bubbliminate.core.rules.MoveType;

public class AdjustInputProcessor extends AllInputAdapter implements ITimeUpdatable {

	private static final int[] ADJUSTER_AXES = {
		Ouya.AXIS_LEFT_X, Ouya.AXIS_LEFT_Y,
	};
	private static final int[] TWEAK_AXES = {
		Ouya.AXIS_RIGHT_X, Ouya.AXIS_RIGHT_Y,
	};
	
	private static final float MICRO_ADJUST_FACTOR = 0.05f;
	private static final float MAX_GAME_UNITS_PER_SECOND = 5f;
	private static final float MAX_DEGREES_PER_SECOND = 180f;
	
	
	private IGameController gameController;
	private float axisAwakeThreshold = 0.5f;
	private ControllerHelper helper = new ControllerHelper();
	
	
	public AdjustInputProcessor(IGameController gameController) {
		Validate.notNull(gameController);
		this.gameController = gameController;
	}
	
	@Override
	public void update(float delta) {
		Vector2 dirAdjust = helper.findFirstAwakeDirection(ADJUSTER_AXES, Controllers.getControllers());
		Vector2 dirTweak = helper.findFirstAwakeDirection(TWEAK_AXES, Controllers.getControllers());
		if (dirAdjust == Vector2.Zero && dirTweak == Vector2.Zero) {
			dirAdjust = helper.getArrowKeysDir();
			
			// Tweak with shift key
			if (Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) || Gdx.input.isKeyPressed(Input.Keys.SHIFT_RIGHT)) {
				dirAdjust = dirAdjust.scl(MICRO_ADJUST_FACTOR);
			}
		}
		
		if (!dirAdjust.equals(Vector2.Zero)) {
			handleStick(dirAdjust, delta);
		} else if (!dirTweak.equals(Vector2.Zero)) {
			handleStick(dirTweak.scl(MICRO_ADJUST_FACTOR), delta);
		}
	}
	
	private void handleStick(Vector2 dir, float delta) {
		dir = new Vector2(dir.x, -dir.y);
		switch (gameController.getCurrMoveType()) {
		case Move:
			gameController.moveControlPoint(dir.scl(delta * MAX_GAME_UNITS_PER_SECOND));
			break;
		case Grow:
			float r = gameController.getControlPoint().len();
			float dirFromStick = (Math.abs(dir.x) > Math.abs(dir.y) ? dir.x : dir.y);
			float newRadius = r + dirFromStick * delta * MAX_GAME_UNITS_PER_SECOND;
			gameController.setControlPoint(dir.nor().scl(newRadius));
			break;
		case Split:
			Vector2 ctlPnt = gameController.getControlPoint();
			float oldAngle = new PolarCoords(Vector2Util.toCoreVector2(ctlPnt)).getAngle().getDegrees();
			float newAngle = oldAngle - dir.x * delta * MAX_DEGREES_PER_SECOND;
			Vector2 newCtlPnt = Vector2Util.fromDegrees(newAngle);
			gameController.setControlPoint( newCtlPnt);
			break;
		}
	}
	
	@Override
	public boolean buttonDown(Controller controller, int buttonCode) {
		if (buttonCode == Ouya.BUTTON_O) {
			gameController.completeMove();
		} else if (buttonCode == Ouya.BUTTON_U) {
			gameController.setMoveType(MoveType.Move);
		} else if (buttonCode == Ouya.BUTTON_Y) {
			gameController.setMoveType(MoveType.Grow);
		} else if (buttonCode == Ouya.BUTTON_A) {
			gameController.setMoveType(MoveType.Split);
		} else if (buttonCode == Ouya.BUTTON_L2) {	// BUTTON_L1 / L2 are swapped in this version of libgdx
			gameController.selectDifferentCircle();
		} else if (buttonCode == Ouya.BUTTON_DPAD_UP) {
			gameController.selectDifferentCircle();
			gameController.cycleSelectionUp();
		} else if (buttonCode == Ouya.BUTTON_DPAD_DOWN) {
			gameController.selectDifferentCircle();
			gameController.cycleSelectionDown();
		} else if (buttonCode == Ouya.BUTTON_DPAD_LEFT) {
			gameController.selectDifferentCircle();
			gameController.cycleSelectionLeft();
		} else if (buttonCode == Ouya.BUTTON_DPAD_RIGHT) {
			gameController.selectDifferentCircle();
			gameController.cycleSelectionRight();
		} else {
			return false;
		}
		return true;
	}

	@Override
	public boolean keyDown(int keycode) {
		if (keycode == Input.Keys.ENTER) {
			gameController.completeMove();
		} else if (keycode == Input.Keys.M) {
			gameController.setMoveType(MoveType.Move);
		} else if (keycode == Input.Keys.G) {
			gameController.setMoveType(MoveType.Grow);
		} else if (keycode == Input.Keys.S) {
			gameController.setMoveType(MoveType.Split);
		} else if (keycode == Input.Keys.BACKSPACE) {
			gameController.selectDifferentCircle();
		} else if (keycode == Input.Keys.W) {
			gameController.selectDifferentCircle();
			gameController.cycleSelectionUp();
		} else if (keycode == Input.Keys.S) {
			gameController.selectDifferentCircle();
			gameController.cycleSelectionDown();
		} else if (keycode == Input.Keys.A) {
			gameController.selectDifferentCircle();
			gameController.cycleSelectionLeft();
		} else if (keycode == Input.Keys.D) {
			gameController.selectDifferentCircle();
			gameController.cycleSelectionRight();
		} else {
			return false;
		}
		return true;
	}
}
