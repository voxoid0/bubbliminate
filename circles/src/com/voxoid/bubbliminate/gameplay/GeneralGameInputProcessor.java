package com.voxoid.bubbliminate.gameplay;

import org.apache.commons.lang.Validate;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.mappings.Ouya;
import com.voxoid.bubbliminate.AllInputAdapter;

public class GeneralGameInputProcessor extends AllInputAdapter {

	private IGameController gameController;
	
	public GeneralGameInputProcessor(IGameController gameController) {
		Validate.notNull(gameController);
		this.gameController = gameController;
	}
	
	@Override
	public boolean buttonDown(Controller controller, int buttonCode) {
		if (buttonCode == Ouya.BUTTON_MENU) {
			gameController.exit();
			return true;
		} else if (buttonCode == Ouya.BUTTON_R1) {	// R1/R2 are swapping in libgdx
			gameController.showInstructions();
			return true;
		} else if (buttonCode == Ouya.BUTTON_L1) { // L1/L2 are swapped in libgdx
			gameController.toggleCircleLabels();
			return true;
		} else {
			return false;
		}
	}

	@Override
	public boolean keyDown(int keycode) {
		if (keycode == Input.Keys.ESCAPE || keycode == Input.Keys.BACK) {
			gameController.exit();
			return true;
		} else if (keycode == Input.Keys.F1) {
			gameController.showInstructions();
			return true;
		} else if (keycode == Input.Keys.F2) {
			gameController.toggleCircleLabels();
			return true;
		} else {
			return false;
		}
	}

}
