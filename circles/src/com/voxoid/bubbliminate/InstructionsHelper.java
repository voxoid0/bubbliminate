package com.voxoid.bubbliminate;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;

import com.badlogic.gdx.scenes.scene2d.Stage;
import com.voxoid.bubbliminate.ui.MenuUtils;

public class InstructionsHelper {

	private static final String INSTRUCTIONS_WATCHED = "instructionsWatched";
	private static final String FIRST_TIME_INSTRUCTIONS = 
			"Use Two fingers to Pan and Zoom.\n\nMove a bubble by dragging its center.\n\nGrow by dragging its edge.\n\nSplit by swiping through a bubble." +
			" The more bubbles you have from splitting, the farther you can Move or Grow.\n\nPop all other players to Win!";
	
	private static final String[] INSTRUCTIONS = {
        "Welcome to Bubbliminate!\n\nThe goal of the game is to eliminate the other players' bubbles, until you are the last survivor!",
        "Each player has their own color and starts with one bubble. The players take turns Moving, Growing, or Splitting one of their bubbles. You cannot move or grow past the circular game boundary.\n\n" +
        		"Create more bubbles by splitting one of your bubbles into two. The more bubbles you have, the further you can move or grow.\n\n" +
        		"Shrink and pop bubbles by pushing into them when you move or grow. A bubble's center is fixed, so pushing on the side of the bubble shrinks it towards its center. Even a small bubble can push into a big bubble to shrink or pop it.",
        "Use Two fingers to Pan and Zoom.\n\nMove a bubble by dragging its center.\n\nGrow by dragging its edge.\n\nSplit by swiping through a bubble at the desired angle.",
		"Note: Any controller can be used by any player. This makes setup and game play easy, but do be careful to leave the other controllers alone when it's not your turn!"
	};
	
	private IGameData gameData;
	
	public InstructionsHelper(IGameData gameData) {
		Validate.notNull(gameData);
		this.gameData = gameData;
	}
	
	public void showInstructionsIfFirstTime(final Stage stage, final Runnable after) {
		if (StringUtils.isEmpty(gameData.getGameData(INSTRUCTIONS_WATCHED))) {
			showFirstTimeInstructions(stage, after);
			gameData.putGameData(INSTRUCTIONS_WATCHED, "1");
		} else {
			if (after != null) after.run();
		}
	}
	
	public void showFirstTimeInstructions(final Stage stage, final Runnable after) {
		MenuUtils.messageDialog(stage, FIRST_TIME_INSTRUCTIONS, "OK", new Runnable() {
			public void run() {
				if (after != null) after.run();
			}
		});
	}
	
	public void showInstructions(final Stage stage, final Runnable after) {
		// TODO: THIS is why I need a UI thread!!!
		MenuUtils.messageDialog(stage, INSTRUCTIONS[0], "Next", new Runnable() {
			public void run() {
				MenuUtils.messageDialog(stage, INSTRUCTIONS[1], "Next", new Runnable() {
					public void run() {
					    if (INSTRUCTIONS.length > (CirclesGlobal.isTouchDevice ? 3 : 2)) {
    						MenuUtils.messageDialog(stage, INSTRUCTIONS[2], "Done", new Runnable() {
    							public void run() {
    							    if (INSTRUCTIONS.length > (CirclesGlobal.isTouchDevice ? 4 : 3)) {
            							MenuUtils.messageDialog(stage, INSTRUCTIONS[3], "OK", new Runnable() {
            								public void run() {
            									if (!CirclesGlobal.isTouchDevice) MenuUtils.messageDialog(stage, INSTRUCTIONS[4], "OK", after);
            										}
            							});
    							    } else {
    							    	if (after != null) after.run();
    							    }
    							}
    						});
					    }
						
					}
				});
				
			}
		});
	}
}
